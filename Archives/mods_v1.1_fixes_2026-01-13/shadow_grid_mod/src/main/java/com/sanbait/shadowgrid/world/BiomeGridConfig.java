package com.sanbait.shadowgrid.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.HashMap;
import java.util.Map;

public class BiomeGridConfig {

    public static final int SECTOR_SIZE = 512;
    private static final Map<String, ResourceKey<Biome>> GRID_BIOMES = new HashMap<>();

    static {
        // Default Configuration (Can be expanded or loaded from file later)
        // Sector 0:0 is usually safe/neutral.
        // Examples:
        // GRID_BIOMES.put("0:1", Biomes.DESERT);
        // GRID_BIOMES.put("1:0", Biomes.OCEAN);

        // Setup initial test grid
        // North
        GRID_BIOMES.put("0:-1", Biomes.ICE_SPIKES);
        // South
        GRID_BIOMES.put("0:1", Biomes.DESERT);
        // East
        GRID_BIOMES.put("1:0", Biomes.JUNGLE);
        // West
        GRID_BIOMES.put("-1:0", Biomes.BADLANDS);
    }

    public static ResourceKey<Biome> getBiomeForSector(int x, int z) {
        return GRID_BIOMES.get(x + ":" + z);
    }
}
