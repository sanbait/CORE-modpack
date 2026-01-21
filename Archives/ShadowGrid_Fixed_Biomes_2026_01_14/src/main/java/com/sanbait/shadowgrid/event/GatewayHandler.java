package com.sanbait.shadowgrid.event;

import com.sanbait.shadowgrid.ShadowGridMod;
import com.sanbait.shadowgrid.registry.ModBlocks;
import com.sanbait.shadowgrid.world.GridSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShadowGridMod.MODID)
public class GatewayHandler {

    // private static final int SECTOR_SIZE = 512; (Use BiomeGridConfig)

    @SubscribeEvent
    public static void onBlockBreak(net.minecraftforge.event.level.BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof net.minecraft.world.level.Level level) || level.isClientSide)
            return;

        BlockPos pos = event.getPos();

        // Проверяем сам Gateway блок
        if (event.getState().is(ModBlocks.GATEWAY.get())) {
            event.setCanceled(true);
            if (event.getPlayer() != null) {
                event.getPlayer().sendSystemMessage(
                        Component.literal("Gateway blocks cannot be destroyed!")
                                .withStyle(ChatFormatting.RED));
            }
            return;
        }

        // Проверяем блоки структуры Gateway (в радиусе 5x5x5 блоков от Gateway)
        // Ищем ближайший Gateway блок в радиусе
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
                for (int dz = -5; dz <= 5; dz++) {
                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    if (level.getBlockState(checkPos).is(ModBlocks.GATEWAY.get())) {
                        // Найден Gateway блок рядом - защищаем все блоки структуры
                        event.setCanceled(true);
                        if (event.getPlayer() != null) {
                            event.getPlayer().sendSystemMessage(
                                    Component.literal("Gateway structure cannot be destroyed!")
                                            .withStyle(ChatFormatting.RED));
                        }
                        return;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof net.minecraft.world.level.Level level) || level.isClientSide)
            return;

        BlockPos pos = event.getPos();

        com.sanbait.shadowgrid.world.GridSavedData data = com.sanbait.shadowgrid.world.GridSavedData.get(level);
        final int SECTOR_SIZE = com.sanbait.shadowgrid.world.BiomeGridConfig.SECTOR_SIZE;
        final int HALF_SIZE = SECTOR_SIZE / 2;

        int sectorX = Math.floorDiv(pos.getX() + HALF_SIZE, SECTOR_SIZE);
        int sectorZ = Math.floorDiv(pos.getZ() + HALF_SIZE, SECTOR_SIZE);

        if (!data.isSectorUnlocked(sectorX, sectorZ)) {
            event.setCanceled(true);
            if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
                player.sendSystemMessage(
                        Component.literal("Cannot place blocks in locked sector!")
                                .withStyle(ChatFormatting.RED));
            }
        }
    }
}
