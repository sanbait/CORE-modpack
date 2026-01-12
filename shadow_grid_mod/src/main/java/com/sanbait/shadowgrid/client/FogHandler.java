package com.sanbait.shadowgrid.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sanbait.shadowgrid.ShadowGridMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.sanbait.shadowgrid.world.BiomeGridConfig;

@Mod.EventBusSubscriber(modid = ShadowGridMod.MODID, value = Dist.CLIENT)
public class FogHandler {

    private static final int HALF_SIZE = BiomeGridConfig.SECTOR_SIZE / 2;

    @SubscribeEvent
    public static void onRenderFogColor(ViewportEvent.ComputeFogColor event) {
        if (!shouldApplyFog())
            return;

        // Set Fog to Pitch Black
        event.setRed(0.0F);
        event.setGreen(0.0F);
        event.setBlue(0.0F);
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (!shouldApplyFog())
            return;

        // Make it thick!
        // Start almost immediately
        if (event.getMode() == FogRenderer.FogMode.FOG_TERRAIN) {
            event.setNearPlaneDistance(0.0F);
            event.setFarPlaneDistance(16.0F); // Very short view distance
            event.setCanceled(true); // Override vanilla
        }
    }

    private static boolean shouldApplyFog() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return false;

        // Calculate Sector
        int sectorX = Math.floorDiv(mc.player.getBlockX() + HALF_SIZE, BiomeGridConfig.SECTOR_SIZE);
        int sectorZ = Math.floorDiv(mc.player.getBlockZ() + HALF_SIZE, BiomeGridConfig.SECTOR_SIZE);

        // If sector is LOCKED -> Apply Fog
        return !ClientGridData.isSectorUnlocked(sectorX, sectorZ);
    }
}
