package com.sanbait.shadowgrid.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GridSavedData extends SavedData {
    private static final String DATA_NAME = "shadow_grid_data";

    // Stores unlocked sectors as "X:Z" strings
    private final Set<String> unlockedSectors = new HashSet<>();

    // Stores biome assignments as "X:Z" -> ResourceLocation
    private final Map<String, ResourceLocation> sectorBiomes = new HashMap<>();

    public GridSavedData() {
        // Unlock the central sector by default
        unlockedSectors.add("0:0");
    }

    public static GridSavedData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(GridSavedData::load, GridSavedData::new, DATA_NAME);
        }
        return new GridSavedData(); // Fallback for client (client doesn't save)
    }

    public boolean isSectorUnlocked(int x, int z) {
        return unlockedSectors.contains(x + ":" + z);
    }

    public void unlockSector(int x, int z) {
        String sectorKey = x + ":" + z;
        if (unlockedSectors.add(sectorKey)) {
            setDirty();
            // Sync to all players immediately - CRITICAL for client to update!
            com.sanbait.shadowgrid.network.ShadowNetwork
                    .sendToAll(new com.sanbait.shadowgrid.network.PacketSyncGrid(getUnlockedSectors()));
            // FIX FPS: Убран дебаг-принт
            // System.out.println("[ShadowGrid] Unlocked sector " + x + ":" + z + ", syncing
            // to all clients. Total unlocked: " + unlockedSectors);
        }
    }

    public Set<String> getUnlockedSectors() {
        return new HashSet<>(unlockedSectors);
    }

    /**
     * Gets the biome for a sector. If not assigned, generates a random one.
     * 
     * @param level The server level (needed for world seed)
     */
    public ResourceKey<Biome> getBiomeForSector(int x, int z, ServerLevel level) {
        String sectorKey = x + ":" + z;

        // Check if biome is already assigned
        if (sectorBiomes.containsKey(sectorKey)) {
            ResourceLocation loc = sectorBiomes.get(sectorKey);
            return ResourceKey.create(Registries.BIOME, loc);
        }

        // Generate new random biome
        long worldSeed = level.getSeed();
        ResourceKey<Biome> biome = BiomeGridConfig.generateRandomBiome(x, z, worldSeed);

        // Save it
        sectorBiomes.put(sectorKey, BiomeGridConfig.getBiomeLocation(biome));
        setDirty();

        return biome;
    }

    /**
     * Sets a specific biome for a sector (used during world generation)
     */
    public void setSectorBiome(int x, int z, ResourceLocation biome) {
        String sectorKey = x + ":" + z;
        sectorBiomes.put(sectorKey, biome);
        setDirty();
    }

    public static GridSavedData load(CompoundTag tag) {
        GridSavedData data = new GridSavedData();
        data.unlockedSectors.clear();

        // Load unlocked sectors
        ListTag list = tag.getList("UnlockedSectors", Tag.TAG_STRING);
        for (Tag t : list) {
            data.unlockedSectors.add(t.getAsString());
        }

        // Load biome assignments
        if (tag.contains("SectorBiomes", Tag.TAG_COMPOUND)) {
            CompoundTag biomesTag = tag.getCompound("SectorBiomes");
            for (String key : biomesTag.getAllKeys()) {
                String biomeStr = biomesTag.getString(key);
                data.sectorBiomes.put(key, new ResourceLocation(biomeStr));
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        // Save unlocked sectors
        ListTag list = new ListTag();
        for (String s : unlockedSectors) {
            list.add(StringTag.valueOf(s));
        }
        tag.put("UnlockedSectors", list);

        // Save biome assignments
        CompoundTag biomesTag = new CompoundTag();
        for (Map.Entry<String, ResourceLocation> entry : sectorBiomes.entrySet()) {
            biomesTag.putString(entry.getKey(), entry.getValue().toString());
        }
        tag.put("SectorBiomes", biomesTag);

        return tag;
    }
}
