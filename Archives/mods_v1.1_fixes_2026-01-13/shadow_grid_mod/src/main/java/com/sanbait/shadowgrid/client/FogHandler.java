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
        // Fog prevented
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        // Fog prevented
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
