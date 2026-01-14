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

    public static final int SECTOR_SIZE = 512;
    
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
    public static void loadConfig() {
        if (configLoaded) return;
        
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("shadowgrid_biomes.json");
        
        // Ensure config directory exists
        try {
            Files.createDirectories(configPath.getParent());
        } catch (Exception e) {
            System.err.println("[ShadowGrid] Failed to create config directory: " + e.getMessage());
        }
        
        // Copy default config if it doesn't exist
        if (!Files.exists(configPath)) {
            copyDefaultConfig(configPath);
        }
        
        // Load config
        try (Reader reader = Files.newBufferedReader(configPath)) {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            JsonObject weights = json.getAsJsonObject("biome_weights");
            
            BIOME_WEIGHTS.clear();
            totalWeight = 0;
            
            for (String key : weights.keySet()) {
                int weight = weights.get(key).getAsInt();
                if (weight > 0) {
                    ResourceLocation biomeLoc = new ResourceLocation(key);
                    BIOME_WEIGHTS.put(biomeLoc, weight);
                    totalWeight += weight;
                }
            }
            
            buildWeightedPool();
            configLoaded = true;
            
            System.out.println("[ShadowGrid] Loaded " + BIOME_WEIGHTS.size() + " biomes from config (total weight: " + totalWeight + ")");
            
        } catch (Exception e) {
            System.err.println("[ShadowGrid] Failed to load biome config, using defaults: " + e.getMessage());
            loadDefaultBiomes();
        }
    }
    
    /**
     * Builds weighted pool for O(1) random selection
     */
    private static void buildWeightedPool() {
        WEIGHTED_POOL.clear();
        for (Map.Entry<ResourceLocation, Integer> entry : BIOME_WEIGHTS.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                WEIGHTED_POOL.add(entry.getKey());
            }
        }
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
            // Add vanilla biomes with default weights
            addDefaultWeight(weights, "minecraft:plains", 10);
            addDefaultWeight(weights, "minecraft:forest", 10);
            addDefaultWeight(weights, "minecraft:birch_forest", 8);
            addDefaultWeight(weights, "minecraft:dark_forest", 6);
            addDefaultWeight(weights, "minecraft:taiga", 8);
            addDefaultWeight(weights, "minecraft:desert", 10);
            addDefaultWeight(weights, "minecraft:savanna", 8);
            addDefaultWeight(weights, "minecraft:jungle", 8);
            addDefaultWeight(weights, "minecraft:badlands", 6);
            addDefaultWeight(weights, "minecraft:swamp", 7);
            addDefaultWeight(weights, "minecraft:meadow", 7);
            addDefaultWeight(weights, "minecraft:grove", 5);
            addDefaultWeight(weights, "minecraft:snowy_plains", 6);
            addDefaultWeight(weights, "minecraft:ice_spikes", 3);
            addDefaultWeight(weights, "minecraft:snowy_taiga", 6);
            addDefaultWeight(weights, "minecraft:sunflower_plains", 6);
            addDefaultWeight(weights, "minecraft:windswept_hills", 6);
            addDefaultWeight(weights, "minecraft:cherry_grove", 5);
            addDefaultWeight(weights, "minecraft:mangrove_swamp", 6);
            addDefaultWeight(weights, "minecraft:mushroom_fields", 2);
            addDefaultWeight(weights, "minecraft:ocean", 5);
            addDefaultWeight(weights, "minecraft:deep_ocean", 3);
            
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
        obj.addProperty(biome, weight);
    }
    
    /**
     * Fallback: load hardcoded defaults if config fails
     */
    private static void loadDefaultBiomes() {
        BIOME_WEIGHTS.clear();
        totalWeight = 0;
        
        addBiome(Biomes.PLAINS.location(), 10);
        addBiome(Biomes.FOREST.location(), 10);
        addBiome(Biomes.DESERT.location(), 10);
        addBiome(Biomes.TAIGA.location(), 8);
        addBiome(Biomes.JUNGLE.location(), 8);
        addBiome(Biomes.SAVANNA.location(), 8);
        addBiome(Biomes.BADLANDS.location(), 6);
        addBiome(Biomes.SWAMP.location(), 7);
        addBiome(Biomes.ICE_SPIKES.location(), 3);
        addBiome(Biomes.MUSHROOM_FIELDS.location(), 2);
        addBiome(Biomes.OCEAN.location(), 5);
        
        buildWeightedPool();
        configLoaded = true;
    }
    
    private static void addBiome(ResourceLocation biome, int weight) {
        BIOME_WEIGHTS.put(biome, weight);
        totalWeight += weight;
    }

    /**
     * Generates a deterministic random biome for a sector based on world seed and sector coordinates
     */
    public static ResourceKey<Biome> generateRandomBiome(int sectorX, int sectorZ, long worldSeed) {
        if (!configLoaded) {
            loadConfig();
        }
        
        if (WEIGHTED_POOL.isEmpty()) {
            return Biomes.PLAINS; // Ultimate fallback
        }
        
        // Create deterministic seed from world seed and sector coordinates
        long sectorSeed = worldSeed;
        sectorSeed = sectorSeed * 31L + sectorX;
        sectorSeed = sectorSeed * 31L + sectorZ;
        
        Random random = new Random(sectorSeed);
        int index = random.nextInt(WEIGHTED_POOL.size());
        ResourceLocation biomeLoc = WEIGHTED_POOL.get(index);
        
        return ResourceKey.create(Registries.BIOME, biomeLoc);
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
