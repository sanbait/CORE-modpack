package com.sanbait.luxsystem.blocks;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import com.sanbait.luxsystem.ModBlockEntities;
import com.sanbait.luxsystem.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class LuxExtractorBlockEntity extends BlockEntity implements net.minecraft.world.MenuProvider {
    public final FluidTank outputTank = new FluidTank(4000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> outputTank);
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> inventory);

    private int processTime = 0;

    public LuxExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LUX_EXTRACTOR_BE.get(), pos, state);
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.literal("Lux Extractor");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new LuxExtractorMenu(id, playerInv, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LuxExtractorBlockEntity entity) {
        if (level.isClientSide)
            return;

        ItemStack input = entity.inventory.getStackInSlot(0);
        if (input.isEmpty()) {
            entity.processTime = 0;
            return;
        }

        int mbPerItem = entity.getExtractionAmount(input);
        if (mbPerItem > 0) {
            // Check if output tank has space
            if (entity.outputTank.getFluidAmount() + mbPerItem <= entity.outputTank.getCapacity()) {
                entity.processTime++;
                if (entity.processTime >= 60) { // 3 seconds per item operation

                    // Fill tank
                    int filled = entity.outputTank.fill(new FluidStack(ModFluids.LIQUID_LUX_SOURCE.get(), mbPerItem),
                            IFluidHandler.FluidAction.EXECUTE);

                    if (filled > 0) {
                        input.shrink(1); // Consume item
                        entity.processTime = 0;
                    }
                }
            }
        } else {
            entity.processTime = 0;
        }
    }

    private int getExtractionAmount(ItemStack item) {
        if (item.getItem() == com.sanbait.luxsystem.ModItems.ANCIENT_LUX_VASE.get())
            return 10000; // 10 Buckets
        if (item.getItem() == com.sanbait.luxsystem.ModItems.ANCIENT_LUX_ORB.get())
            return 5000; // 5 Buckets
        if (item.getItem() == com.sanbait.luxsystem.ModItems.FOSSILIZED_LUX_AMBER.get())
            return 2000; // 2 Buckets
        if (item.getItem() == com.sanbait.luxsystem.ModItems.LUX_CRYSTAL_FRAGMENT.get())
            return 250; // 1/4 Bucket
        return 0;
    }

    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return fluidHandler.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER)
            return itemHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        outputTank.readFromNBT(tag.getCompound("OutputTank"));
        inventory.deserializeNBT(tag.getCompound("Inventory"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("OutputTank", outputTank.writeToNBT(new CompoundTag()));
        tag.put("Inventory", inventory.serializeNBT());
    }
}
