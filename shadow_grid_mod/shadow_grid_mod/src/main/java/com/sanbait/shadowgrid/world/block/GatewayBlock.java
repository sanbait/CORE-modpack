package com.sanbait.shadowgrid.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class GatewayBlock extends Block {

    public GatewayBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide()) {
            player.displayClientMessage(
                    Component.literal("§7[Shadow Grid] Approach the zone border to unlock new sectors."), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Visual effects for the gateway
        double xc = pos.getX() + 0.5;
        double zc = pos.getZ() + 0.5;
        double y = pos.getY();

        // 1. Central particle stream
        for (int i = 0; i < 20; i += 2) {
            if (random.nextFloat() < 0.5f) {
                level.addParticle(ParticleTypes.END_ROD, xc, y + i + random.nextDouble(), zc, 0, 0.05, 0);
            }
        }

        // 2. Base flashes
        if (random.nextFloat() < 0.1f) {
            level.addParticle(ParticleTypes.FLASH, xc, y + 1, zc, 0, 0, 0);
        }

        // 3. Portal aura
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
