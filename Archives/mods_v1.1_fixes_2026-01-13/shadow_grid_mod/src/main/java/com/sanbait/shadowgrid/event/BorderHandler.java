package com.sanbait.shadowgrid.event;

import com.sanbait.shadowgrid.ShadowGridMod;
import com.sanbait.shadowgrid.world.GridSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ShadowGridMod.MODID)
public class BorderHandler {

    // Track last sector to avoid spamming title
    private static final Map<ServerPlayer, String> lastSectorMap = new HashMap<>();

    @SubscribeEvent
    public static void onChunkChange(net.minecraftforge.event.entity.EntityEvent.EnteringSection event) {
        if (event.didChunkChange() && event.getEntity() instanceof ServerPlayer player) {
            checkPlayerSector(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        checkPlayerSector(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            lastSectorMap.remove(player);
        }
    }

    private static void checkPlayerSector(net.minecraft.world.entity.player.Player playerEntity) {
        if (playerEntity.level().isClientSide || !(playerEntity instanceof ServerPlayer player))
            return;

        GridSavedData data = GridSavedData.get(player.level());
        BlockPos pos = player.blockPosition();

        // Logic Update: Center Sector (0,0) on World (0,0)
        // Range of Sector 0 becomes [-256, 256] instead of [0, 512]
        int sectorX = Math.floorDiv(pos.getX() + (com.sanbait.shadowgrid.world.BiomeGridConfig.SECTOR_SIZE / 2),
                com.sanbait.shadowgrid.world.BiomeGridConfig.SECTOR_SIZE);
        int sectorZ = Math.floorDiv(pos.getZ() + (com.sanbait.shadowgrid.world.BiomeGridConfig.SECTOR_SIZE / 2),
                com.sanbait.shadowgrid.world.BiomeGridConfig.SECTOR_SIZE);

        String currentSector = sectorX + ":" + sectorZ;
        String lastSector = lastSectorMap.get(player);
        boolean sectorChanged = !currentSector.equals(lastSector);
        lastSectorMap.put(player, currentSector);

        if (!data.isSectorUnlocked(sectorX, sectorZ)) {
            // Apply INFINITE effects (until removed)
            applyPenalty(player);

            // Show title only when entering closed sector (not every check)
            if (sectorChanged) {
                // Send Title Warning (Center of Screen)
                net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket titlePacket = new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                        Component.translatable("shadowgrid.warning.title").withStyle(ChatFormatting.DARK_RED,
                                ChatFormatting.BOLD));
                net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket subtitlePacket = new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                        Component.translatable("shadowgrid.warning.subtitle").withStyle(ChatFormatting.RED));

                player.connection.send(titlePacket);
                player.connection.send(subtitlePacket);
                player.sendSystemMessage(Component.translatable("shadowgrid.warning.chat").withStyle(ChatFormatting.RED));
            }
        } else {
            // Remove effects if entering safe zone
            removePenalty(player);
        }
    }

    private static void applyPenalty(ServerPlayer player) {
        // Use DARKNESS instead of BLINDNESS for pulsing effect (easier to see
        // surroundings occasionally)
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 72000, 0, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.WITHER, 72000, 1, false, false, true)); // Damage
        // Reduced slowness so they can run away
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 72000, 0, false, false, false));
    }

    private static void removePenalty(ServerPlayer player) {
        player.removeEffect(MobEffects.DARKNESS); // Changed from BLINDNESS
        player.removeEffect(MobEffects.WITHER);
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }
}
