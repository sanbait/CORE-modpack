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
            // Use DistExecutor to safely run client-side code
            net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                    () -> () -> {
                        ClientGridData.setUnlockedSectors(this.unlockedSectors);
                        // Force re-render of chunks to ensure visibility updates (fixes Bobby/Distant
                        // Horizons issues)
                        if (net.minecraft.client.Minecraft.getInstance().levelRenderer != null) {
                            net.minecraft.client.Minecraft.getInstance().levelRenderer.allChanged();
                        }
                    });
        });
        return true;
    }
}
