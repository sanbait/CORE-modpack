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
            GridSavedData data = GridSavedData.get(player.level());
            ShadowNetwork.sendToPlayer(new PacketSyncGrid(data.getUnlockedSectors()), player);
        }
    }
}
