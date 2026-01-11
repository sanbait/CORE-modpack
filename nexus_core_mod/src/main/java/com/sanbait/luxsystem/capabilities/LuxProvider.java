package com.sanbait.luxsystem.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuxProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<ILuxStorage> LUX_CAP = CapabilityManager.get(new CapabilityToken<>() {
    });

    private LuxCapability backend = null;
    private final LazyOptional<ILuxStorage> optional = LazyOptional.of(this::createBackend);

    private LuxCapability createBackend() {
        if (this.backend == null) {
            this.backend = new LuxCapability();
        }
        return this.backend;
    }

    // Allow setting custom backend (e.g. for different capacities)
    public void setBackend(LuxCapability backend) {
        this.backend = backend;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == LUX_CAP) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createBackend().saveNBT(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createBackend().loadNBT(nbt);
    }
}
