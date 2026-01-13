package com.sanbait.shadowgrid.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

public class GridSavedData extends SavedData {
    private static final String DATA_NAME = "shadow_grid_data";

    // Stores unlocked sectors as "X:Z" strings
    private final Set<String> unlockedSectors = new HashSet<>();

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
            // System.out.println("[ShadowGrid] Unlocked sector " + x + ":" + z + ", syncing to all clients. Total unlocked: " + unlockedSectors);
        }
    }

    public Set<String> getUnlockedSectors() {
        return new HashSet<>(unlockedSectors);
    }

    public static GridSavedData load(CompoundTag tag) {
        GridSavedData data = new GridSavedData();
        data.unlockedSectors.clear();

        ListTag list = tag.getList("UnlockedSectors", Tag.TAG_STRING);
        for (Tag t : list) {
            data.unlockedSectors.add(t.getAsString());
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (String s : unlockedSectors) {
            list.add(StringTag.valueOf(s));
        }
        tag.put("UnlockedSectors", list);
        return tag;
    }
}
