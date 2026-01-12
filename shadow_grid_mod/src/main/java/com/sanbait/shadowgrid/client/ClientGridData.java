package com.sanbait.shadowgrid.client;

import java.util.HashSet;
import java.util.Set;

public class ClientGridData {
    private static final Set<String> unlockedSectors = new HashSet<>();

    public static void setUnlockedSectors(Set<String> sectors) {
        unlockedSectors.clear();
        unlockedSectors.addAll(sectors);
    }

    public static boolean isSectorUnlocked(int x, int z) {
        return unlockedSectors.contains(x + ":" + z);
    }
}
