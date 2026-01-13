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

            // Visual Update: Replace with Soul Campfire to show "Activated"
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE.defaultBlockState(), 3);

            // Also play sound
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.END_PORTAL_SPAWN,
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
        } else {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§eЭтот сектор уже открыт."),
                    true);
            // Ensure visual matches
            if (level.getBlockState(pos).getBlock() != net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE) {
                level.setBlock(pos, net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE.defaultBlockState(), 3);
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
