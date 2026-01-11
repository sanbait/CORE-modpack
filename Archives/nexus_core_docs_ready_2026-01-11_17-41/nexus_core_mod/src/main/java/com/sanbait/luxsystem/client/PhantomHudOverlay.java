package com.sanbait.luxsystem.client;

import com.sanbait.luxsystem.CoreRadiusManager;
import com.sanbait.luxsystem.PhantomBlockHandler;
import com.sanbait.nexuscore.NexusCore;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NexusCore.MODID, value = Dist.CLIENT)
public class PhantomHudOverlay {

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        // Only render on CROSSHAIR layer to avoid drawing multiple times
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type())
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.hitResult == null)
            return;

        if (mc.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hit = (BlockHitResult) mc.hitResult;
            BlockPos pos = hit.getBlockPos();

            // Check conditions
            if (mc.level.getBlockState(pos).is(PhantomBlockHandler.REQUIRES_LUX) &&
                    !CoreRadiusManager.isInRadius(mc.level, pos)) {

                int width = event.getWindow().getGuiScaledWidth();
                int height = event.getWindow().getGuiScaledHeight();

                // Draw Warning Text
                String text = "⚠ UNSTABLE MATTER ⚠";
                int color = 0xFF5555; // Red
                // Pulsing
                float time = mc.level.getGameTime() + event.getPartialTick();
                int alpha = (int) (128 + 127 * Math.sin(time * 0.2f));
                int fullColor = (alpha << 24) | 0xFF00FF; // Purple glowing

                int x = (width - mc.font.width(text)) / 2;
                int y = (height / 2) + 15; // Below crosshair

                event.getGuiGraphics().drawString(mc.font, text, x, y, fullColor);

                String subText = "Requires Nexus Light";
                int subX = (width - mc.font.width(subText)) / 2;
                event.getGuiGraphics().drawString(mc.font, subText, subX, y + 10, 0xAAAAAA);
            }
        }
    }
}
