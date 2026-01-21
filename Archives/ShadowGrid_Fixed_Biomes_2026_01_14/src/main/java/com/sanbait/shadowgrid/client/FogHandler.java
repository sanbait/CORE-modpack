package com.sanbait.shadowgrid.client;

import com.sanbait.shadowgrid.ShadowGridMod;
import com.sanbait.shadowgrid.world.BiomeGridConfig;
import com.sanbait.shadowgrid.world.GridSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShadowGridMod.MODID, value = Dist.CLIENT)
public class FogHandler {

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (!isLocked(event.getCamera().getEntity().blockPosition())) {
            return;
        }

        // В закрытом секторе - густой туман
        event.setNearPlaneDistance(0.0f);
        event.setFarPlaneDistance(15.0f);
        event.setCanceled(true); // Отменяем ванильный туман
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        if (!isLocked(event.getCamera().getEntity().blockPosition())) {
            return;
        }

        // Черный цвет
        event.setRed(0.0f);
        event.setGreen(0.0f);
        event.setBlue(0.0f);
    }

    private static boolean isLocked(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return false;

        int SECTOR_SIZE = BiomeGridConfig.SECTOR_SIZE;
        int HALF_SIZE = SECTOR_SIZE / 2;

        int sectorX = Math.floorDiv(pos.getX() + HALF_SIZE, SECTOR_SIZE);
        int sectorZ = Math.floorDiv(pos.getZ() + HALF_SIZE, SECTOR_SIZE);

        return !ClientGridData.isSectorUnlocked(sectorX, sectorZ);
    }
}
