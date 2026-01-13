package com.sanbait.luxsystem.capabilities;

import net.minecraft.nbt.CompoundTag;

public class LuxCapability implements ILuxStorage {
    private int lux;
    private int capacity;

    public LuxCapability() {
        this.lux = 0;
        this.capacity = 1000; // Default capacity suitable for basic items
    }

    public LuxCapability(int capacity) {
        this.lux = 0;
        this.capacity = capacity;
    }

    @Override
    public int getLuxStored() {
        return this.lux;
    }

    @Override
    public int getMaxLuxStored() {
        return this.capacity;
    }

    @Override
    public int receiveLux(int maxReceive, boolean simulate) {
        int energyReceived = Math.min(this.capacity - this.lux, maxReceive);
        if (!simulate) {
            this.lux += energyReceived;
        }
        return energyReceived;
    }

    @Override
    public int extractLux(int maxExtract, boolean simulate) {
        int energyExtracted = Math.min(this.lux, maxExtract);
        if (!simulate) {
            this.lux -= energyExtracted;
        }
        return energyExtracted;
    }

    public void setLux(int lux) {
        this.lux = lux;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void saveNBT(CompoundTag nbt) {
        nbt.putInt("Lux", this.lux);
        nbt.putInt("Capacity", this.capacity);
    }

    public void loadNBT(CompoundTag nbt) {
        this.lux = nbt.getInt("Lux");
        if (nbt.contains("Capacity")) {
            this.capacity = nbt.getInt("Capacity");
        }
    }
}
