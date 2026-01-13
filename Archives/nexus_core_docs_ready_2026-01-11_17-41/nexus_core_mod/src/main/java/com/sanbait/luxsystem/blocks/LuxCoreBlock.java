package com.sanbait.luxsystem.blocks;

import com.sanbait.luxsystem.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LuxCoreBlock extends BaseEntityBlock {
    public LuxCoreBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LuxCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.LUX_CORE_BE.get(), LuxCoreBlockEntity::tick);
    }

    @Override
    public net.minecraft.world.InteractionResult use(BlockState state, Level level, BlockPos pos,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand,
            net.minecraft.world.phys.BlockHitResult hit) {
        if (level.isClientSide)
            return net.minecraft.world.InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LuxCoreBlockEntity core) {
            net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);

            // 1. Fluid Interaction (Bucket)
            if (stack.getItem() instanceof net.minecraft.world.item.BucketItem) {
                // Simplified bucket logic (using FluidUtil/Capabilities is better but complex
                // for boilerplate)
                // For MVP, handle Liquid Lux Bucket specific
                if (stack.getItem() == com.sanbait.luxsystem.ModItems.LIQUID_LUX_BUCKET.get()) {
                    if (core.getLuxTank()
                            .fill(new net.minecraftforge.fluids.FluidStack(
                                    com.sanbait.luxsystem.ModFluids.LIQUID_LUX_SOURCE.get(), 1000),
                                    net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE) > 0) {
                        player.setItemInHand(hand,
                                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BUCKET));
                        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY,
                                net.minecraft.sounds.SoundSource.BLOCKS, 1f, 1f);
                        return net.minecraft.world.InteractionResult.CONSUME;
                    }
                }
            }

            // 2. Tool Charging (LuxPickaxe)
            if (stack.getItem() instanceof com.sanbait.luxsystem.items.LuxPickaxeItem) {
                int current = com.sanbait.luxsystem.items.LuxPickaxeItem.getLux(stack);
                int max = 1000;
                int needed = max - current;

                if (needed > 0) {
                    net.minecraftforge.fluids.FluidStack drained = core.getLuxTank().drain(needed,
                            net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                    if (drained != null && drained.getAmount() > 0) {
                        com.sanbait.luxsystem.items.LuxPickaxeItem.setLux(stack, current + drained.getAmount());
                        player.displayClientMessage(net.minecraft.network.chat.Component
                                .literal("§bCharged: " + drained.getAmount() + " Lux"), true);
                        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                                net.minecraft.sounds.SoundSource.BLOCKS, 1f, 1f);
                        return net.minecraft.world.InteractionResult.CONSUME;
                    }
                }
            }
        }
        return net.minecraft.world.InteractionResult.PASS;
    }
}
