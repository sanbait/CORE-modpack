package com.sanbait.luxsystem.capabilities;

public interface ILuxStorage {
    int getLuxStored();

    int getMaxLuxStored();

    int receiveLux(int maxReceive, boolean simulate);

    int extractLux(int maxExtract, boolean simulate);
}
