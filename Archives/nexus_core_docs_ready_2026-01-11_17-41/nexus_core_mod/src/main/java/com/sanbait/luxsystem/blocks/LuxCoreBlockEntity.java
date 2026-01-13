package com.sanbait.luxsystem.blocks;

import com.sanbait.luxsystem.ModBlockEntities;
import com.sanbait.nexuscore.NexusCoreConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class LuxCoreBlockEntity extends BlockEntity {
    private final FluidTank luxTank = new FluidTank(100000);
    private int tier = 1;
    private int luxGeneration = 25;

    public LuxCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LUX_CORE_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LuxCoreBlockEntity entity) {
        if (level.isClientSide)
            return;

        // Every second (20 ticks)
        if (level.getGameTime() % 20 == 0) {
            // 1. Generate Lux from fluid
            int fluidAmount = entity.luxTank.getFluidAmount();
            if (fluidAmount >= entity.luxGeneration) {
                entity.luxTank.drain(entity.luxGeneration,
                        net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);

                // 2. Charge nearby players' items
                entity.chargeNearbyPlayers(level, pos);
            }
        }

        // Debug visualization: Show radius every 5 seconds
        if (level.isClientSide && level.getGameTime() % 100 == 0) {
            double radius = entity.getRadius();
            for (int i = 0; i < 32; i++) {
                double angle = (Math.PI * 2 * i) / 32;
                double x = pos.getX() + 0.5 + Math.cos(angle) * radius;
                double z = pos.getZ() + 0.5 + Math.sin(angle) * radius;
                level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,
                        x, pos.getY() + 1, z, 0, 0.05, 0);
            }
        }
    }

    private void chargeNearbyPlayers(Level level, BlockPos pos) {
        double radius = getRadius();
        java.util.List<net.minecraft.world.entity.player.Player> players = level.getEntitiesOfClass(
                net.minecraft.world.entity.player.Player.class,
                new net.minecraft.world.phys.AABB(pos).inflate(radius));

        for (net.minecraft.world.entity.player.Player player : players) {
            if (player.blockPosition().distSqr(pos) > radius * radius)
                continue;

            // Priority: Armor > Weapons > Tools
            chargeLuxItems(player.getInventory().armor, luxGeneration);
            chargeLuxItems(java.util.Arrays.asList(player.getMainHandItem(), player.getOffhandItem()), luxGeneration);
            chargeLuxItems(player.getInventory().items, luxGeneration);
        }
    }

    private void chargeLuxItems(java.util.List<net.minecraft.world.item.ItemStack> items, int amount) {
        for (net.minecraft.world.item.ItemStack stack : items) {
            if (stack.isEmpty())
                continue;
            if (stack.getItem() instanceof com.sanbait.luxsystem.items.LuxPickaxeItem) {
                int current = com.sanbait.luxsystem.items.LuxPickaxeItem.getLux(stack);
                int max = 1000;
                if (current < max) {
                    int toAdd = Math.min(amount, max - current);
                    com.sanbait.luxsystem.items.LuxPickaxeItem.setLux(stack, current + toAdd);
                    return; // Charge one item per tick
                }
            }
        }
    }

    public double getRadius() {
        return switch (tier) {
            case 1 -> 16.0;
            case 2 -> 24.0;
            case 3 -> 32.0;
            case 4 -> 48.0;
            case 5 -> 64.0;
            default -> 16.0;
        };
    }

    public boolean isPositionInRadius(BlockPos checkPos) {
        double radius = getRadius();
        return this.worldPosition.distSqr(checkPos) < (radius * radius);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tier = tag.getInt("lux_tier");
        if (tier < 1)
            tier = 1;
        luxTank.readFromNBT(tag.getCompound("LuxTank"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("lux_tier", tier);
        tag.put("LuxTank", luxTank.writeToNBT(new CompoundTag()));
    }

    public FluidTank getLuxTank() {
        return luxTank;
    }
}
