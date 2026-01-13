package com.sanbait.shadowgrid.client;

import com.sanbait.shadowgrid.ShadowGridMod;
import com.sanbait.shadowgrid.world.BiomeGridConfig;
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

@Mod.EventBusSubscriber(modid = ShadowGridMod.MODID, value = Dist.CLIENT)
public class ParticleBorderHandler {

    private static final int SECTOR_SIZE = BiomeGridConfig.SECTOR_SIZE;
    private static final int HALF_SIZE = SECTOR_SIZE / 2;
    private static final Random random = new Random();
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused())
            return;

        // OPTIMIZED: Spawn particles very rarely (every 40 ticks = 2 seconds) and only close to walls
        tickCounter++;
        if (tickCounter % 40 != 0)
            return;

        Player player = mc.player;
        Level level = mc.level;
        BlockPos pos = player.blockPosition();

        // Calculate current sector
        int sectorX = Math.floorDiv(pos.getX() + HALF_SIZE, SECTOR_SIZE);
        int sectorZ = Math.floorDiv(pos.getZ() + HALF_SIZE, SECTOR_SIZE);

        // Sector boundaries
        int minX = (sectorX * SECTOR_SIZE) - HALF_SIZE;
        int maxX = (sectorX * SECTOR_SIZE) + HALF_SIZE;
        int minZ = (sectorZ * SECTOR_SIZE) - HALF_SIZE;
        int maxZ = (sectorZ * SECTOR_SIZE) + HALF_SIZE;

        // Only spawn particles if player is very close to wall (within 16 blocks)
        int closeDist = 16;
        
        // Spawn sinister void particles (SOUL) near walls
        if (Math.abs(pos.getX() - minX) < closeDist && !ClientGridData.isSectorUnlocked(sectorX - 1, sectorZ)) {
            spawnVoidParticle(level, minX, pos.getY() + random.nextDouble() * 2, pos.getZ() + (random.nextDouble() - 0.5) * 10);
        }
        if (Math.abs(pos.getX() - maxX) < closeDist && !ClientGridData.isSectorUnlocked(sectorX + 1, sectorZ)) {
            spawnVoidParticle(level, maxX, pos.getY() + random.nextDouble() * 2, pos.getZ() + (random.nextDouble() - 0.5) * 10);
        }
        if (Math.abs(pos.getZ() - minZ) < closeDist && !ClientGridData.isSectorUnlocked(sectorX, sectorZ - 1)) {
            spawnVoidParticle(level, pos.getX() + (random.nextDouble() - 0.5) * 10, pos.getY() + random.nextDouble() * 2, minZ);
        }
        if (Math.abs(pos.getZ() - maxZ) < closeDist && !ClientGridData.isSectorUnlocked(sectorX, sectorZ + 1)) {
            spawnVoidParticle(level, pos.getX() + (random.nextDouble() - 0.5) * 10, pos.getY() + random.nextDouble() * 2, maxZ);
        }
    }

    private static void spawnVoidParticle(Level level, double x, double y, double z) {
        // Spawn SOUL particle for sinister void effect (very lightweight)
        level.addParticle(ParticleTypes.SOUL, x, y, z, 
                (random.nextDouble() - 0.5) * 0.1, 
                random.nextDouble() * 0.05, 
                (random.nextDouble() - 0.5) * 0.1);
    }
}
