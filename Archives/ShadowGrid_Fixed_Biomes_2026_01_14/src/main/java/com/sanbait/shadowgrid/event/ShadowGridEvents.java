package com.sanbait.shadowgrid.event;

import com.sanbait.shadowgrid.ShadowGridMod;
import com.sanbait.shadowgrid.network.PacketSyncGrid;
import com.sanbait.shadowgrid.network.ShadowNetwork;
import com.sanbait.shadowgrid.world.GridSavedData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShadowGridMod.MODID)
public class ShadowGridEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            net.minecraft.server.level.ServerLevel level = player.serverLevel();
            GridSavedData data = GridSavedData.get(level);

            // Sync Grid Data
            ShadowNetwork.sendToPlayer(new PacketSyncGrid(data.getUnlockedSectors()), player);
        }
    }

    // Removing onServerStarted to prevent double execution or loading issues
    /*
     * @SubscribeEvent
     * public static void
     * onServerStarted(net.minecraftforge.event.server.ServerStartedEvent event) {
     * net.minecraft.server.MinecraftServer server = event.getServer();
     * net.minecraft.server.level.ServerLevel overworld =
     * server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
     * 
     * if (overworld == null)
     * return;
     * 
     * GridSavedData data = GridSavedData.get(overworld);
     * if (!data.areLayerPortalsGenerated()) {
     * com.sanbait.shadowgrid.ShadowGridMod.LOGGER.
     * info("Generating Shadow Grid Layer Portals...");
     * 
     * // Generate Portals
     * generateLayerPortals(server);
     * 
     * data.setLayerPortalsGenerated(true);
     * data.save(new net.minecraft.nbt.CompoundTag()); // Ensure save
     * com.sanbait.shadowgrid.ShadowGridMod.LOGGER.
     * info("Layer Portals Generated Successfully!");
     * }
     * }
     */
}
