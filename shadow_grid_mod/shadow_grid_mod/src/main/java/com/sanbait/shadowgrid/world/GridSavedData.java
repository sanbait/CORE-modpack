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
        // Unlock the 4 central sectors by default (since spawn is at 0,0 intersection)
        unlockedSectors.add("0:0");
        unlockedSectors.add("0:-1");
        unlockedSectors.add("-1:0");
        unlockedSectors.add("-1:-1");
    }

    public static GridSavedData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            // ALWAYS use the Overworld storage for global syncing
            ServerLevel overworld = serverLevel.getServer().getLevel(Level.OVERWORLD);
            if (overworld != null) {
                return overworld.getDataStorage().computeIfAbsent(GridSavedData::load, GridSavedData::new, DATA_NAME);
            }
            return serverLevel.getDataStorage().computeIfAbsent(GridSavedData::load, GridSavedData::new, DATA_NAME);
        }
        return new GridSavedData(); // Fallback for client (client doesn't save)
    }

    public boolean isSectorUnlocked(int x, int z) {
        return unlockedSectors.contains(x + ":" + z);
    }

    public void unlockSector(int x, int z, Level level) {
        String sectorKey = x + ":" + z;
        System.out.println("[ShadowGrid] unlockSector called for: " + sectorKey);
        if (unlockedSectors.add(sectorKey)) {
            setDirty();
            System.out.println(
                    "[ShadowGrid] Sector " + sectorKey + " unlocked! Total unlocked: " + unlockedSectors.size());

            // 1. Sync unlocked state to all clients
            com.sanbait.shadowgrid.network.ShadowNetwork
                    .sendToAll(new com.sanbait.shadowgrid.network.PacketSyncGrid(getUnlockedSectors()));

            // 2. FORCE RESEND CHUNKS in the sector to players
            // The client previously ignored these chunks, so we must resend them.
            if (level instanceof ServerLevel serverLevel) {
                int SECTOR_SIZE = BiomeGridConfig.SECTOR_SIZE;
                int centerX = x * SECTOR_SIZE;
                int centerZ = z * SECTOR_SIZE;

                // Radius of chunks to update (sector is 16x16 chunks)
                // We iterate slightly larger area just to be safe, or exact sector.
                int minChunkX = (centerX - SECTOR_SIZE / 2) >> 4;
                int maxChunkX = (centerX + SECTOR_SIZE / 2) >> 4;
                int minChunkZ = (centerZ - SECTOR_SIZE / 2) >> 4;
                int maxChunkZ = (centerZ + SECTOR_SIZE / 2) >> 4;

                System.out.println("[ShadowGrid] Resending chunks in range: " + minChunkX + "," + minChunkZ + " to "
                        + maxChunkX + "," + maxChunkZ);

                for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                    for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                        net.minecraft.world.level.chunk.LevelChunk chunk = serverLevel.getChunkSource().getChunk(cx, cz,
                                false);
                        if (chunk != null) {
                            // Create packet
                            net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket packet = new net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket(
                                    chunk, serverLevel.getLightEngine(), null, null);

                            // Send to all players tracking this chunk
                            serverLevel.getChunkSource().chunkMap.getPlayers(chunk.getPos(), false).forEach(player -> {
                                player.connection.send(packet);
                            });
                        }
                    }
                }
            }
        } else {
            System.out.println("[ShadowGrid] Sector " + sectorKey + " was already unlocked!");
        }
    }

    // Перегрузка для обратной совместимости
    public void unlockSector(int x, int z) {
        unlockSector(x, z, null);
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
