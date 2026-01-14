package com.sanbait.shadowgrid.world;

import net.minecraft.server.level.ServerLevel;

/**
 * Thread-local context holder for passing ServerLevel to biome source mixins.
 * This is necessary because BiomeSource doesn't have direct access to the
 * Level.
 */
public class BiomeLevelContext {
    private static final ThreadLocal<ServerLevel> CURRENT_LEVEL = new ThreadLocal<>();

    public static void setCurrentLevel(ServerLevel level) {
        CURRENT_LEVEL.set(level);
    }

    public static ServerLevel getCurrentLevel() {
        return CURRENT_LEVEL.get();
    }

    public static void clearCurrentLevel() {
        CURRENT_LEVEL.remove();
    }
}
