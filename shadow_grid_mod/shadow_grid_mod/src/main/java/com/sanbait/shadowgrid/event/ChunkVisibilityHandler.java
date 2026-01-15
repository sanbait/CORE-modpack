package com.sanbait.shadowgrid.event;

import com.sanbait.shadowgrid.ShadowGridMod;
import com.sanbait.shadowgrid.world.BiomeGridConfig;
import com.sanbait.shadowgrid.world.GridSavedData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShadowGridMod.MODID)
public class ChunkVisibilityHandler {

    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Watch event) {
        try {
            if (!(event.getLevel() instanceof net.minecraft.server.level.ServerLevel)) {
                return;
            }
            net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) event.getLevel();

            if (level.isClientSide) {
                return;
            }

            // Only enforce in Overworld
            if (level.dimension() != Level.OVERWORLD) {
                return;
            }

            ServerPlayer player = event.getPlayer();
            ChunkPos chunkPos = event.getPos();

            // Check if sector is unlocked
            GridSavedData data = GridSavedData.get(level);
            if (data == null)
                return; // Safety check

            final int SECTOR_SIZE = BiomeGridConfig.SECTOR_SIZE;
            final int HALF_SIZE = SECTOR_SIZE / 2;

            // Calculate generic block pos for chunk center to determine sector
            int blockX = chunkPos.getMinBlockX() + 8;
            int blockZ = chunkPos.getMinBlockZ() + 8;

            int sectorX = Math.floorDiv(blockX + HALF_SIZE, SECTOR_SIZE);
            int sectorZ = Math.floorDiv(blockZ + HALF_SIZE, SECTOR_SIZE);

            boolean unlocked = data.isSectorUnlocked(sectorX, sectorZ);

            // DEBUG LOGGING
            if (itemLog < 50) { // Limit logs
                System.out.println("[ShadowGrid-DEBUG] Watch Chunk: " + chunkPos + " -> Sector: " + sectorX + ","
                        + sectorZ + " | Unlocked: " + unlocked);
                itemLog++;
            }

            if (!unlocked) {
                event.setCanceled(true);
            }
        } catch (Exception e) {
            // Log error but do NOT crash connection
            if (itemLog < 5) {
                System.err.println("[ShadowGrid] Error in ChunkVisible: " + e.getMessage());
                e.printStackTrace();
                itemLog++;
            }
        }
    }

    private static int itemLog = 0;
}
