package com.sanbait.shadowgrid.world;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;

public class BiomeGridConfig {

    public static final int SECTOR_SIZE = 256;

    // Weighted biome pool: biome -> weight
    private static final Map<ResourceLocation, Integer> BIOME_WEIGHTS = new LinkedHashMap<>();
    private static final List<ResourceLocation> WEIGHTED_POOL = new ArrayList<>();
    private static int totalWeight = 0;

    private static boolean configLoaded = false;

    // Global world seed for generation (set on server start)
    public static long currentWorldSeed = 0L;

    // Flag to track if mixin has been called (for logging)
    public static boolean mixinCalled = false;

    // Biome cache: ResourceKey -> Holder (filled on first access)
    private static final Map<ResourceKey<Biome>, Holder<Biome>> BIOME_CACHE = new HashMap<>();
    private static Registry<Biome> cachedRegistry = null;

    /**
     * Loads biome weights from config file
     */
    // Data structure for biome config
    private static class BiomeEntry {
        ResourceLocation id;
        int weight;
        int minDistance; // In sectors

        BiomeEntry(ResourceLocation id, int weight, int minDistance) {
            this.id = id;
            this.weight = weight;
            this.minDistance = minDistance;
        }
    }

    private static final List<BiomeEntry> BIOME_ENTRIES = new ArrayList<>();

    /**
     * Loads biome weights from config file
     */
    public static void loadConfig() {
        if (configLoaded)
            return;

        Path configPath = FMLPaths.CONFIGDIR.get().resolve("shadowgrid_biomes.json");

        try {
            Files.createDirectories(configPath.getParent());
        } catch (Exception e) {
            System.err.println("[ShadowGrid] Failed to create config directory: " + e.getMessage());
        }

        if (!Files.exists(configPath)) {
            copyDefaultConfig(configPath);
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            JsonObject weights = json.getAsJsonObject("biome_weights");

            BIOME_ENTRIES.clear();
            totalWeight = 0;

            for (String key : weights.keySet()) {
                try {
                    ResourceLocation loc = new ResourceLocation(key);
                    if (weights.get(key).isJsonObject()) {
                        // New format: { "weight": 10, "min_distance": 5 }
                        JsonObject entryObj = weights.getAsJsonObject(key);
                        int w = entryObj.has("weight") ? entryObj.get("weight").getAsInt() : 10;
                        int d = entryObj.has("min_distance") ? entryObj.get("min_distance").getAsInt() : 0;
                        if (w > 0) {
                            BIOME_ENTRIES.add(new BiomeEntry(loc, w, d));
                        }
                    } else {
                        // Old format: int weight
                        int w = weights.get(key).getAsInt();
                        if (w > 0) {
                            BIOME_ENTRIES.add(new BiomeEntry(loc, w, 0));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[ShadowGrid] Invalid config entry for " + key + ": " + e.getMessage());
                }
            }

            configLoaded = true;
            System.out.println("[ShadowGrid] Loaded " + BIOME_ENTRIES.size() + " biomes from config.");

        } catch (Exception e) {
            System.err.println("[ShadowGrid] Failed to load biome config, using defaults: " + e.getMessage());
            loadDefaultBiomes();
        }
    }

    // Removed buildWeightedPool() as it is now dynamic based on distance
    private static void buildWeightedPool() {
        // No-op for compatibility if called elsewhere, but logic moved to generate
    }

    /**
     * Copies default config from resources
     */
    private static void copyDefaultConfig(Path target) {
        try {
            // Create default config
            JsonObject root = new JsonObject();
            root.addProperty("_comment", "Shadow Grid - Biome Weight Configuration");
            root.addProperty("_description", "Configure biome spawn weights. Higher = more common. 0 = disabled.");

            JsonObject weights = new JsonObject();

            // Format: "biome_id": { "weight": 10, "min_distance": 0 }
            addBiomeEntry(weights, "minecraft:plains", 10, 0);
            addBiomeEntry(weights, "minecraft:forest", 10, 0);
            addBiomeEntry(weights, "minecraft:birch_forest", 8, 0);
            addBiomeEntry(weights, "minecraft:dark_forest", 6, 2); // A bit further
            addBiomeEntry(weights, "minecraft:taiga", 8, 0);
            addBiomeEntry(weights, "minecraft:desert", 10, 0);
            addBiomeEntry(weights, "minecraft:savanna", 8, 0);
            addBiomeEntry(weights, "minecraft:jungle", 8, 4); // Rare/Far
            addBiomeEntry(weights, "minecraft:badlands", 6, 5); // Far
            addBiomeEntry(weights, "minecraft:swamp", 7, 0);
            addBiomeEntry(weights, "minecraft:meadow", 7, 1);
            addBiomeEntry(weights, "minecraft:grove", 5, 2);
            addBiomeEntry(weights, "minecraft:snowy_plains", 6, 3);
            addBiomeEntry(weights, "minecraft:ice_spikes", 3, 5); // Rare/Far
            addBiomeEntry(weights, "minecraft:snowy_taiga", 6, 3);
            addBiomeEntry(weights, "minecraft:sunflower_plains", 6, 1);
            addBiomeEntry(weights, "minecraft:windswept_hills", 6, 2);
            addBiomeEntry(weights, "minecraft:cherry_grove", 5, 3);
            addBiomeEntry(weights, "minecraft:mangrove_swamp", 6, 4);
            addBiomeEntry(weights, "minecraft:mushroom_fields", 2, 8); // Very Rare/Far
            addBiomeEntry(weights, "minecraft:ocean", 5, 0);
            addBiomeEntry(weights, "minecraft:deep_ocean", 3, 2);

            root.add("biome_weights", weights);

            // Write to file
            try (Writer writer = Files.newBufferedWriter(target)) {
                new Gson().toJson(root, writer);
            }

            System.out.println("[ShadowGrid] Created default biome config at: " + target);

        } catch (Exception e) {
            System.err.println("[ShadowGrid] Failed to create default config: " + e.getMessage());
        }
    }

    private static void addDefaultWeight(JsonObject obj, String biome, int weight) {
        // obj.addProperty(biome, weight); // OLD
        addBiomeEntry(obj, biome, weight, 0); // Default distance 0
    }

    private static void addBiomeEntry(JsonObject obj, String biome, int weight, int minDist) {
        JsonObject entry = new JsonObject();
        entry.addProperty("weight", weight);
        entry.addProperty("min_distance", minDist);
        obj.add(biome, entry);
    }

    private static void loadDefaultBiomes() {
        BIOME_ENTRIES.clear();
        addBiome(Biomes.PLAINS.location(), 10, 0);
        addBiome(Biomes.FOREST.location(), 10, 0);
        addBiome(Biomes.DESERT.location(), 10, 0);
        configLoaded = true;
    }

    private static void addBiome(ResourceLocation biome, int weight, int dist) {
        BIOME_ENTRIES.add(new BiomeEntry(biome, weight, dist));
    }

    /**
     * Generates a deterministic random biome for a sector based on world seed and
     * sector coordinates
     */
    public static ResourceKey<Biome> generateRandomBiome(int sectorX, int sectorZ, long worldSeed) {
        if (!configLoaded) {
            loadConfig();
        }

        if (BIOME_ENTRIES.isEmpty()) {
            return Biomes.PLAINS;
        }

        // Calculate distance from spawn (0,0) in sectors (Manhattan or Chebyshev?
        // usually Max(|x|,|z|) for square grids/rings)
        int dist = Math.max(Math.abs(sectorX), Math.abs(sectorZ));

        // Filter valid biomes based on distance
        List<BiomeEntry> validBiomes = new ArrayList<>();
        int currentTotalWeight = 0;

        for (BiomeEntry entry : BIOME_ENTRIES) {
            if (dist >= entry.minDistance) {
                validBiomes.add(entry);
                currentTotalWeight += entry.weight;
            }
        }

        if (validBiomes.isEmpty()) {
            return Biomes.PLAINS; // Fallback if nothing matches (shouldn't happen if baseline biomes are dist 0)
        }

        // Deterministic RNG
        long sectorSeed = worldSeed;
        sectorSeed = sectorSeed * 31L + sectorX;
        sectorSeed = sectorSeed * 31L + sectorZ;
        Random random = new Random(sectorSeed);

        // Weighted Selection
        if (currentTotalWeight <= 0) {
            return Biomes.PLAINS;
        }
        int pick = random.nextInt(currentTotalWeight);
        int current = 0;

        for (BiomeEntry entry : validBiomes) {
            current += entry.weight;
            if (pick < current) {
                return ResourceKey.create(Registries.BIOME, entry.id);
            }
        }

        return ResourceKey.create(Registries.BIOME, validBiomes.get(0).id);
    }

    /**
     * Gets the biome ResourceLocation for saving to NBT
     */
    public static ResourceLocation getBiomeLocation(ResourceKey<Biome> biomeKey) {
        return biomeKey.location();
    }

    /**
     * Reloads config from disk (useful for runtime changes)
     */
    public static void reloadConfig() {
        configLoaded = false;
        loadConfig();
    }

    /**
     * Caches a biome holder for later use
     */
    public static void cacheBiome(ResourceKey<Biome> biomeKey, Holder<Biome> holder) {
        BIOME_CACHE.put(biomeKey, holder);
    }

    /**
     * Gets cached biome holder, or null if not cached
     */
    public static Holder<Biome> getCachedBiome(ResourceKey<Biome> biomeKey) {
        return BIOME_CACHE.get(biomeKey);
    }

    /**
     * Sets the cached registry (called when registry becomes available)
     */
    public static void setCachedRegistry(Registry<Biome> registry) {
        cachedRegistry = registry;
    }

    /**
     * Gets the cached registry
     */
    public static Registry<Biome> getCachedRegistry() {
        return cachedRegistry;
    }

    /**
     * Gets biome holder from cached registry
     */
    public static Holder<Biome> getBiomeFromCache(ResourceKey<Biome> biomeKey) {
        // First try cached holder
        Holder<Biome> cached = BIOME_CACHE.get(biomeKey);
        if (cached != null) {
            return cached;
        }

        // Then try cached registry
        if (cachedRegistry != null) {
            try {
                Holder<Biome> holder = cachedRegistry.getHolderOrThrow(biomeKey);
                BIOME_CACHE.put(biomeKey, holder);
                return holder;
            } catch (Exception e) {
                // Biome not in registry
            }
        }

        return null;
    }
}
