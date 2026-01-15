package com.sanbait.shadowgrid.event;

import com.sanbait.shadowgrid.ShadowGridMod;
import com.sanbait.shadowgrid.world.BiomeGridConfig;
import com.sanbait.shadowgrid.world.GridSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShadowGridMod.MODID)
public class BorderUnlockHandler {

    private static final int BORDER_DISTANCE = 8;
    private static final String PENDING_X = "pendingUnlockX";
    private static final String PENDING_Z = "pendingUnlockZ";
    private static final String PENDING_DIR = "pendingUnlockDir";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()
                || !(event.player instanceof ServerPlayer player))
            return;
        if (player.tickCount % 10 != 0)
            return;

        checkProximity(player);
    }

    private static void checkProximity(ServerPlayer player) {
        GridSavedData data = GridSavedData.get(player.level());
        int SECTOR_CHUNKS = 16;
        int HALF_CHUNKS = 8;

        // Use Strict Chunk Math (Same as Client)
        int chunkX = player.chunkPosition().x;
        int chunkZ = player.chunkPosition().z;
        int playerX = player.getBlockX();
        int playerZ = player.getBlockZ();

        int sectorX = Math.floorDiv(chunkX + HALF_CHUNKS, SECTOR_CHUNKS);
        int sectorZ = Math.floorDiv(chunkZ + HALF_CHUNKS, SECTOR_CHUNKS);

        // Calculate distances to current sector borders (in blocks)
        // Sector 0: Chunks -8 to 7.
        // Bounds:
        // East Border (Max X): (sectorX * 16) + 7 (chunk) -> Block ends at (chunk+1)*16
        // - 1.
        // Actually, let's use the center-based logic which is robust.

        // Center of the sector in Chunks:
        // Sector 0 -> Center is effectively "0" (but range is -8 to 7).
        // Let's calculate borders based on Sector Index.

        // MinChunk = sectorX * 16 - 8;
        // MaxChunk = sectorX * 16 + 7;

        int minChunkX = sectorX * 16 - 8;
        int maxChunkX = sectorX * 16 + 7;
        int minChunkZ = sectorZ * 16 - 8;
        int maxChunkZ = sectorZ * 16 + 7;

        // Convert to Block Coordinates for distance check
        // MaxX is the END of maxChunkX (x * 16 + 15)
        // MinX is the START of minChunkX (x * 16)

        int boundEast = (maxChunkX * 16) + 15;
        int boundWest = (minChunkX * 16);
        int boundSouth = (maxChunkZ * 16) + 15;
        int boundNorth = (minChunkZ * 16);

        int distToEast = Math.abs(boundEast - playerX);
        int distToWest = Math.abs(playerX - boundWest);
        int distToSouth = Math.abs(boundSouth - playerZ);
        int distToNorth = Math.abs(playerZ - boundNorth);

        String direction = null;
        int targetX = sectorX;
        int targetZ = sectorZ;

        if (distToEast <= BORDER_DISTANCE && !data.isSectorUnlocked(sectorX + 1, sectorZ)) {
            direction = "EAST";
            targetX++;
        } else if (distToWest <= BORDER_DISTANCE && !data.isSectorUnlocked(sectorX - 1, sectorZ)) {
            direction = "WEST";
            targetX--;
        } else if (distToSouth <= BORDER_DISTANCE && !data.isSectorUnlocked(sectorX, sectorZ + 1)) {
            direction = "SOUTH";
            targetZ++;
        } else if (distToNorth <= BORDER_DISTANCE && !data.isSectorUnlocked(sectorX, sectorZ - 1)) {
            direction = "NORTH";
            targetZ--;
        }

        if (direction != null) {
            promptUnlock(player, direction, targetX, targetZ, data);
        } else {
            clearPending(player);
        }
    }

    private static void promptUnlock(ServerPlayer player, String direction, int tx, int tz, GridSavedData data) {
        int cost = calculateCost(data.getUnlockedSectors().size());
        player.displayClientMessage(
                Component.literal("[Sneak + Right Click] Unlock " + direction + " for " + cost + " Lux Crystals")
                        .withStyle(ChatFormatting.YELLOW),
                true);

        player.getPersistentData().putInt(PENDING_X, tx);
        player.getPersistentData().putInt(PENDING_Z, tz);
        player.getPersistentData().putString(PENDING_DIR, direction);
    }

    private static void clearPending(Player player) {
        if (player.getPersistentData().contains(PENDING_X)) {
            player.getPersistentData().remove(PENDING_X);
            player.getPersistentData().remove(PENDING_Z);
            player.getPersistentData().remove(PENDING_DIR);
        }
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        tryUnlock(event.getEntity(), event.getLevel());
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        tryUnlock(event.getEntity(), event.getLevel());
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        tryUnlock(event.getEntity(), event.getLevel());
    }

    private static void tryUnlock(net.minecraft.world.entity.LivingEntity entity, Level level) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player) || !player.isShiftKeyDown())
            return;
        if (!player.getPersistentData().contains(PENDING_X))
            return;

        int tx = player.getPersistentData().getInt(PENDING_X);
        int tz = player.getPersistentData().getInt(PENDING_Z);
        String dir = player.getPersistentData().getString(PENDING_DIR);
        GridSavedData data = GridSavedData.get(level);

        if (data.isSectorUnlocked(tx, tz)) {
            player.sendSystemMessage(Component.literal("Region already unlocked!").withStyle(ChatFormatting.YELLOW));
            return;
        }

        int cost = calculateCost(data.getUnlockedSectors().size());
        if (deductCrystals(player, cost)) {
            data.unlockSector(tx, tz, level);
            clearPending(player);
            player.sendSystemMessage(
                    Component.literal("UNLOCKED " + dir + " (" + tx + ":" + tz + ")!").withStyle(ChatFormatting.GREEN));
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    private static boolean deductCrystals(ServerPlayer player, int cost) {
        net.minecraft.world.item.Item currency = net.minecraftforge.registries.ForgeRegistries.ITEMS
                .getValue(new net.minecraft.resources.ResourceLocation("luxsystem", "lux_crystal"));

        if (currency == null) {
            player.sendSystemMessage(
                    Component.literal("Error: 'luxsystem:lux_crystal' not found!").withStyle(ChatFormatting.RED));
            return false;
        }

        int count = player.getInventory().countItem(currency);
        if (count < cost) {
            player.sendSystemMessage(Component.literal("Need " + cost + " Lux Crystals! (Have " + count + ")")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        int remaining = cost;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == currency) {
                int take = Math.min(stack.getCount(), remaining);
                stack.shrink(take);
                remaining -= take;
            }
        }
        return true;
    }

    private static int calculateCost(int total) {
        if (total <= 1)
            return 10;
        if (total == 2)
            return 50;
        if (total == 3)
            return 100;
        return 200 * (int) Math.pow(2, total - 4);
    }
}
