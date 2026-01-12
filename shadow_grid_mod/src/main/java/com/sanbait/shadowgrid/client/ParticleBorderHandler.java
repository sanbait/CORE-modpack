package com.sanbait.shadowgrid.client;

import com.sanbait.shadowgrid.ShadowGridMod;
import com.sanbait.shadowgrid.world.GridSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

import com.sanbait.shadowgrid.client.ClientGridData;

@Mod.EventBusSubscriber(modid = ShadowGridMod.MODID, value = Dist.CLIENT)
public class ParticleBorderHandler {

    private static final int SECTOR_SIZE = com.sanbait.shadowgrid.world.BiomeGridConfig.SECTOR_SIZE;
    private static final int HALF_SIZE = SECTOR_SIZE / 2;
    private static final Random random = new Random();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused())
            return;

        Player player = mc.player;
        Level level = mc.level;

        // "Cheap" check: Only run every 2 ticks to save FPS
        if (player.tickCount % 2 != 0)
            return;

        BlockPos pos = player.blockPosition();

        // 1. Calculate Current Sector (using new centered logic)
        int sectorX = Math.floorDiv(pos.getX() + HALF_SIZE, SECTOR_SIZE);
        int sectorZ = Math.floorDiv(pos.getZ() + HALF_SIZE, SECTOR_SIZE);

        // 2. Check Boundaries
        // We want to verify if Adjacent Sectors are locked.
        // Note: Client doesn't have the full WorldSavedData easily accessible unless
        // synced.
        // We relied on ClientGridData earlier. Let's use it.

        // Wait, ClientGridData isn't fully implemented locally to read from here easily
        // without a singleton or similar.
        // Assuming ClientGridData has static methods or instance we can access.
        // For this cheap implementation, let's assume 'default' safe behavior if data
        // unknown,
        // but we need to know if neighboring entries are BLOCKED.

        // Let's rely on simple math relative to the 'Box'.

        int minX = (sectorX * SECTOR_SIZE) - HALF_SIZE;
        int maxX = (sectorX * SECTOR_SIZE) + HALF_SIZE;
        int minZ = (sectorZ * SECTOR_SIZE) - HALF_SIZE;
        int maxZ = (sectorZ * SECTOR_SIZE) + HALF_SIZE;

        // 3. Draw particles along the walls IF the player is relatively close to them
        // (within 64 blocks)
        // AND if the adjacent sector is LOCKED.
        int dist = 64;

        if (Math.abs(pos.getX() - minX) < dist) {
            // West Wall (Neighbor is X-1)
            if (!ClientGridData.isSectorUnlocked(sectorX - 1, sectorZ)) {
                drawWall(level, minX, pos.getZ(), true, pos);
            }
        }
        if (Math.abs(pos.getX() - maxX) < dist) {
            // East Wall (Neighbor is X+1)
            if (!ClientGridData.isSectorUnlocked(sectorX + 1, sectorZ)) {
                drawWall(level, maxX, pos.getZ(), true, pos);
            }
        }
        if (Math.abs(pos.getZ() - minZ) < dist) {
            // North Wall (Neighbor is Z-1)
            if (!ClientGridData.isSectorUnlocked(sectorX, sectorZ - 1)) {
                drawWall(level, pos.getX(), minZ, false, pos);
            }
        }
        if (Math.abs(pos.getZ() - maxZ) < dist) {
            // South Wall (Neighbor is Z+1)
            if (!ClientGridData.isSectorUnlocked(sectorX, sectorZ + 1)) {
                drawWall(level, pos.getX(), maxZ, false, pos);
            }
        }
    }

    private static void drawWall(Level level, int x, int z, boolean isXFixed, BlockPos playerPos) {
        // Draw a long wall extending +/- 60 blocks from player
        double centerCoord = isXFixed ? playerPos.getZ() : playerPos.getX();

        for (double offset = -60; offset <= 60; offset += 1.5) { // Denser: every 1.5 blocks
            if (random.nextFloat() > 0.3f)
                continue; // Randomize slightly to avoid rigid grid look, but keep it dense

            double axisPos = centerCoord + offset;

            double px = isXFixed ? x + 0.5 : axisPos + (random.nextDouble() - 0.5);
            double pz = !isXFixed ? z + 0.5 : axisPos + (random.nextDouble() - 0.5);

            // Get height at this specific wall point
            int heightX = (int) px;
            int heightZ = (int) pz;
            double py = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, heightX,
                    heightZ) + 1 + random.nextDouble() * 3;

            level.addParticle(ParticleTypes.WITCH, px, py, pz, 0, 0, 0);
            level.addParticle(ParticleTypes.SMOKE, px, py, pz, 0, 0, 0);
        }
    }
}
