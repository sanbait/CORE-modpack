package com.sanbait.shadowgrid.network;

import com.sanbait.shadowgrid.ShadowGridMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class ShadowNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ShadowGridMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void register() {
        int id = 0;
        // Register on BOTH sides - client needs to receive packets!
        // PLAY_TO_CLIENT means server sends to client, but registration happens on both sides
        CHANNEL.registerMessage(id++, PacketSyncGrid.class, PacketSyncGrid::toBytes, PacketSyncGrid::new,
                PacketSyncGrid::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendToPlayer(PacketSyncGrid packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToAll(PacketSyncGrid packet) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }
}
