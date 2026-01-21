package com.sanbait.shadowgrid.client;

import java.util.HashSet;
import java.util.Set;

public class ClientGridData {
    private static final Set<String> unlockedSectors = new HashSet<>();

    public static void setUnlockedSectors(Set<String> sectors) {
        unlockedSectors.clear();
        unlockedSectors.addAll(sectors);
        // FIX FPS: Убран дебаг-принт который спамил в консоль
        // System.out.println("[ShadowGrid] Updated unlocked sectors: " +
        // unlockedSectors);
    }

    public static boolean isSectorUnlocked(int x, int z) {
        // FIX FPS: Убрана лишняя проверка и любые возможные дебаг-принты
        // Прямая проверка без лишних операций
        return unlockedSectors.contains(x + ":" + z);
    }

    public static Set<String> getUnlockedSectors() {
        return new HashSet<>(unlockedSectors);
    }

    public static int getUnlockedCount() {
        return unlockedSectors.size();
    }

    // Calculate cost for unlocking next sector (same logic as GatewayHandler)
    public static int calculateCost(int totalUnlocked) {
        int n = totalUnlocked - 1;
        if (n < 0)
            return 0;
        if (n == 0)
            return 10;
        if (n == 1)
            return 50;
        if (n == 2)
            return 100;
        return 200 * (int) Math.pow(2, n - 3);
    }
}
