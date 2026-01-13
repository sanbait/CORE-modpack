package com.sanbait.shadowgrid.network;

import com.sanbait.shadowgrid.client.ClientGridData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class PacketSyncGrid {
    private final Set<String> unlockedSectors;

    public PacketSyncGrid(Set<String> unlockedSectors) {
        this.unlockedSectors = unlockedSectors;
    }

    public PacketSyncGrid(FriendlyByteBuf buf) {
        int count = buf.readInt();
        this.unlockedSectors = new HashSet<>();
        for (int i = 0; i < count; i++) {
            this.unlockedSectors.add(buf.readUtf());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(unlockedSectors.size());
        for (String s : unlockedSectors) {
            buf.writeUtf(s);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // FIX FPS: Убран дебаг-принт
            // System.out.println("[ShadowGrid] Received sync packet with " + this.unlockedSectors.size() + " unlocked sectors: " + this.unlockedSectors);
            ClientGridData.setUnlockedSectors(this.unlockedSectors);
        });
        return true;
    }
}
