package com.sanbait.luxsystem.blocks;

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
import com.sanbait.luxsystem.ModItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuxCondenserBlockEntity extends BlockEntity implements net.minecraft.world.MenuProvider {
    public final FluidTank inputTank = new FluidTank(4000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    public final ItemStackHandler output = new ItemStackHandler(2) { // Slot 0: Input (Bucket), Slot 1: Output
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0)
                return stack.getItem() == ModItems.LIQUID_LUX_BUCKET.get();
            return false;
        }
    };

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> inputTank);
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> output);

    private int processTime = 0;

    public LuxCondenserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LUX_CONDENSER_BE.get(), pos, state);
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.literal("Lux Condenser");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new LuxCondenserMenu(id, playerInv, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LuxCondenserBlockEntity entity) {
        if (level.isClientSide)
            return;

        // 1. Handle Bucket Input (Slot 0)
        ItemStack bucketInput = entity.output.getStackInSlot(0);
        if (!bucketInput.isEmpty() && bucketInput.getItem() == ModItems.LIQUID_LUX_BUCKET.get()) {
            if (entity.inputTank.getFluidAmount() + 1000 <= entity.inputTank.getCapacity()) {
                entity.inputTank.fill(new FluidStack(ModFluids.LIQUID_LUX_SOURCE.get(), 1000),
                        IFluidHandler.FluidAction.EXECUTE);
                entity.output.setStackInSlot(0, new ItemStack(net.minecraft.world.item.Items.BUCKET));
            }
        }

        // 2. Process Fluid to Crystal (Slot 1)
        if (entity.inputTank.getFluidAmount() >= 1000) {
            ItemStack result = new ItemStack(ModItems.LUX_CRYSTAL.get());

            // Check if can insert into output slot (Slot 1)
            if (entity.output.insertItem(1, result, true).isEmpty()) {
                entity.processTime++;
                if (entity.processTime >= 200) { // 10 seconds
                    entity.inputTank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    entity.output.insertItem(1, result, false);
                    entity.processTime = 0;
                }
            }
        } else {
            entity.processTime = 0;
        }
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
        inputTank.readFromNBT(tag.getCompound("InputTank"));

        CompoundTag outputTag = tag.getCompound("Output");
        // Fix for legacy NBT data having wrong size (e.g. 1 instead of 2)
        if (outputTag.contains("Size", net.minecraft.nbt.Tag.TAG_INT)) {
            outputTag.putInt("Size", 2);
        }
        output.deserializeNBT(outputTag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("InputTank", inputTank.writeToNBT(new CompoundTag()));
        tag.put("Output", output.serializeNBT());
    }
}
