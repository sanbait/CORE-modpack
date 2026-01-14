package com.sanbait.shadowgrid.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GatewayBlock extends Block {
    public GatewayBlock(Properties properties) {
        super(properties);
    }

    @Override
    public net.minecraft.world.InteractionResult use(BlockState state, Level level, BlockPos pos,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand,
            net.minecraft.world.phys.BlockHitResult hit) {
        if (level.isClientSide) {
            return net.minecraft.world.InteractionResult.SUCCESS;
        }

        // Logic to Unlock Sector
        // Assuming we have a GridManager or similar.
        // For now, let's call the server-side command logic or simulate it.
        // Use GridSavedData to unlock manually here? Yes.

        com.sanbait.shadowgrid.world.GridSavedData data = com.sanbait.shadowgrid.world.GridSavedData.get(level);

        // Determine nearby border
        int SECTOR_SIZE = 512;
        int HALF_SIZE = 256;

        int sectorX = Math.floorDiv(pos.getX() + HALF_SIZE, SECTOR_SIZE);
        int sectorZ = Math.floorDiv(pos.getZ() + HALF_SIZE, SECTOR_SIZE);

        int centerX = sectorX * SECTOR_SIZE;
        int centerZ = sectorZ * SECTOR_SIZE;

        int distEast = Math.abs(pos.getX() - (centerX + HALF_SIZE));
        int distWest = Math.abs(pos.getX() - (centerX - HALF_SIZE));
        int distSouth = Math.abs(pos.getZ() - (centerZ + HALF_SIZE));
        int distNorth = Math.abs(pos.getZ() - (centerZ - HALF_SIZE));

        // Determine target sector
        int targetX = sectorX;
        int targetZ = sectorZ;

        // Find closest border
        int minX = Math.min(distEast, distWest);
        int minZ = Math.min(distSouth, distNorth);

        if (minX <= minZ) {
            // Wall is along X-axis border (East or West)
            if (distEast < distWest)
                targetX++;
            else
                targetX--;
        } else {
            // Wall is along Z-axis border (South or North)
            if (distSouth < distNorth)
                targetZ++;
            else
                targetZ--;
        }

        // UNLOCK
        if (!data.isSectorUnlocked(targetX, targetZ)) {
            data.unlockSector(targetX, targetZ, level);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§aСектор " + targetX + ":" + targetZ + " открыт!"),
                    true);

            // Visual Update: Place Soul Campfire ABOVE the gateway (on top of frame)
            BlockPos signalPos = pos.above();
            level.setBlock(signalPos, net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE.defaultBlockState(), 3);

            // Also play sound
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.END_PORTAL_SPAWN,
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);

            // ACTIVATE PARTNER GATEWAY (Dual Activation)
            // Determine our position in the grid to find the neighbor
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            int modChunkX = Math.floorMod(chunkX, 32);
            int modChunkZ = Math.floorMod(chunkZ, 32);

            BlockPos partnerPos = null;

            // X-Axis Wall Logic
            if (modChunkX == 15) {
                // We are at Wall (Offset 14). Partner is at Next Chunk Offset 1.
                // Distance = +3
                partnerPos = pos.east(3);
            } else if (modChunkX == 16) {
                // We are at External (Offset 1). Partner is at Prev Chunk Offset 14.
                // Distance = -3
                partnerPos = pos.west(3);
            }
            // Z-Axis Wall Logic
            else if (modChunkZ == 15) {
                // Wall. Partner South (+3)
                partnerPos = pos.south(3);
            } else if (modChunkZ == 16) {
                // External. Partner North (-3)
                partnerPos = pos.north(3);
            }

            if (partnerPos != null) {
                // Activate partner visual (Raised Campfire)
                BlockPos partnerSignal = partnerPos.above();
                // Ensure chunk is loaded or just set (setBlock handles loaded check internally
                // usually, but safe to check)
                if (level.isLoaded(partnerSignal)) {
                    level.setBlock(partnerSignal,
                            net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE.defaultBlockState(), 3);
                }
            }

        } else {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§eЭтот сектор уже открыт."),
                    true);
            // Ensure visual matches
            BlockPos signalPos = pos.above();
            if (level.getBlockState(signalPos).getBlock() != net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE) {
                level.setBlock(signalPos, net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE.defaultBlockState(), 3);
            }
        }

        return net.minecraft.world.InteractionResult.CONSUME;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // ЛУЧ В НЕБО (Визуальное дополнение к реальному маяку)
        double xc = pos.getX() + 0.5;
        double zc = pos.getZ() + 0.5;
        double y = pos.getY();

        // 1. Центральный поток частиц (густой)
        for (int i = 0; i < 20; i += 2) {
            if (random.nextFloat() < 0.5f) { // 50% шанс на каждый шаг
                level.addParticle(ParticleTypes.END_ROD, xc, y + i + random.nextDouble(), zc, 0, 0.05, 0);
            }
        }

        // 2. Яркие вспышки у основания
        if (random.nextFloat() < 0.1f) {
            level.addParticle(ParticleTypes.FLASH, xc, y + 1, zc, 0, 0, 0);
        }

        // 3. Магическая аура (портал)
        for (int k = 0; k < 2; k++) {
            level.addParticle(ParticleTypes.PORTAL,
                    xc + (random.nextDouble() - 0.5),
                    y + random.nextDouble() * 2,
                    zc + (random.nextDouble() - 0.5),
                    (random.nextDouble() - 0.5) * 0.5,
                    (random.nextDouble() - 0.5) * 0.5,
                    (random.nextDouble() - 0.5) * 0.5);
        }
    }
}
