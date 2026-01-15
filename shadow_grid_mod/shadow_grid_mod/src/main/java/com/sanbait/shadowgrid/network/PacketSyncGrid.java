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
                        System.out.println("[ShadowGrid] PacketSyncGrid received with " + this.unlockedSectors.size()
                                + " unlocked sectors");
                        ClientGridData.setUnlockedSectors(this.unlockedSectors);

                        // Force RELOAD of chunks (not just re-render)
                        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                        if (mc.level != null && mc.levelRenderer != null) {
                            System.out.println("[ShadowGrid] Forcing chunk reload...");
                            // Reload all chunks to force server to resend them
                            mc.levelRenderer.allChanged();
                            mc.levelRenderer.needsUpdate();
                        }
                    });
        });
        return true;
    }
}
