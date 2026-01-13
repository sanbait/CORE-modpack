package com.sanbait.nexuscore.util;

import com.sanbait.nexuscore.NexusCoreConfig;
import com.sanbait.nexuscore.NexusCoreEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerLightManager {
    // Track light positions to clean them up when entities move
    private static final Map<UUID, BlockPos> playerLights = new HashMap<>(); // Tracks where light block is
    private static final Map<Integer, BlockPos> coreLights = new HashMap<>();

    // CACHE: Tracks players holding light items to avoid checking inventory every
    // tick.
    // Updated via LivingEquipmentChangeEvent
    private static final java.util.Set<UUID> playersHoldingValidItem = new java.util.HashSet<>();

    public static void onEquipmentChange(net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            updatePlayerCache(player);
        }
    }

    private static void updatePlayerCache(ServerPlayer player) {
        if (isHoldingLightItem(player)) {
            playersHoldingValidItem.add(player.getUUID());
        } else {
            playersHoldingValidItem.remove(player.getUUID());
            // Immediate cleanup if they swapped away
            if (playerLights.containsKey(player.getUUID())) {
                removeLight(player.level(), playerLights.remove(player.getUUID()));
            }
        }
    }

    public static void tickPlayer(ServerPlayer player) {
        if (!NexusCoreConfig.ENABLE_PLAYER_LIGHTS.get())
            return;

        // Check throttle (every 10 ticks = 0.5s)
        if (player.tickCount % 10 != 0)
            return;

        // FAST CHECK: Use cached set from Event
        if (!playersHoldingValidItem.contains(player.getUUID())) {
            return;
        }

        BlockPos currentPos = player.blockPosition().above(); // Head level
        BlockPos oldPos = playerLights.get(player.getUUID());

        // If moved or just started holding
        if (oldPos == null || !oldPos.equals(currentPos)) {
            // Remove old
            if (oldPos != null)
                removeLight(player.level(), oldPos);

            // Place new - LEVEL 8 (Dim light for players)
            if (placeLight(player.level(), currentPos, 8)) {
                playerLights.put(player.getUUID(), currentPos);
            }
        }
    }

    public static void forceCoreLight(NexusCoreEntity core) {
        // State-Based: Just place the light at current position.
        // No tick checks, no movement checks (Core is immobile).
        BlockPos currentPos = core.blockPosition();

        // Ensure we track it for removal later
        if (!coreLights.containsKey(core.getId())) {
            coreLights.put(core.getId(), currentPos);
        }

        // Core is FULL BRIGHTNESS (15)
        placeLight(core.level(), currentPos, 15);
    }

    public static void onCoreRemoved(NexusCoreEntity core) {
        if (coreLights.containsKey(core.getId())) {
            removeLight(core.level(), coreLights.remove(core.getId()));
        }
    }

    public static void onPlayerLoggedOut(ServerPlayer player) {
        if (playerLights.containsKey(player.getUUID())) {
            removeLight(player.level(), playerLights.remove(player.getUUID()));
        }
    }

    private static boolean placeLight(Level level, BlockPos pos, int lightLevel) {
        if (level.isEmptyBlock(pos) || level.getBlockState(pos).isAir()) {
            // OPTIMIZATION: Check if location is already bright enough (e.g. from torch or
            // sun)
            // If current light >= desired level, don't place block to save updates.
            // Exception: We really want to force Level 15 for Core to stop spawns
            // explicitly.
            if (lightLevel < 15 && level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos) >= lightLevel) {
                return false;
            }

            level.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, lightLevel), 3);
            return true;
        }
        return false;
    }

    private static void removeLight(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() == Blocks.LIGHT) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static boolean isHoldingLightItem(ServerPlayer player) {
        String mainHand = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(player.getMainHandItem().getItem())
                .toString();
        String offHand = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(player.getOffhandItem().getItem())
                .toString();

        java.util.List<? extends String> validItems = NexusCoreConfig.PLAYER_LIGHT_ITEMS.get();
        return validItems.contains(mainHand) || validItems.contains(offHand);
    }
}
