package com.sanbait.luxsystem.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sanbait.luxsystem.CoreRadiusManager;
import com.sanbait.luxsystem.PhantomBlockHandler;
import com.sanbait.nexuscore.NexusCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NexusCore.MODID, value = Dist.CLIENT)
public class PhantomHighlightHandler {

    @SubscribeEvent
    public static void onRenderHighlight(RenderHighlightEvent.Block event) {
        if (event.getTarget().getType() != HitResult.Type.BLOCK)
            return;

        BlockPos pos = event.getTarget().getBlockPos();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        // Check if block requires Lux and is outside radius
        if (mc.level.getBlockState(pos).is(PhantomBlockHandler.REQUIRES_LUX) &&
                !CoreRadiusManager.isInRadius(mc.level, pos)) {

            // CANCEL vanilla black outline
            event.setCanceled(true);

            // Render CUSTOM Purple Outline
            PoseStack poseStack = event.getPoseStack();
            VertexConsumer vertexConsumer = event.getMultiBufferSource().getBuffer(RenderType.lines());
            double camX = event.getCamera().getPosition().x;
            double camY = event.getCamera().getPosition().y;
            double camZ = event.getCamera().getPosition().z;

            // Pulsing effect
            float time = mc.level.getGameTime() + event.getPartialTick();
            float alpha = 0.5f + 0.5f * (float) Math.sin(time * 0.2f);

            LevelRenderer.renderLineBox(
                    poseStack,
                    vertexConsumer,
                    event.getTarget().getBlockPos().getX() - camX,
                    event.getTarget().getBlockPos().getY() - camY,
                    event.getTarget().getBlockPos().getZ() - camZ,
                    event.getTarget().getBlockPos().getX() + 1 - camX,
                    event.getTarget().getBlockPos().getY() + 1 - camY,
                    event.getTarget().getBlockPos().getZ() + 1 - camZ,
                    0.8f, 0.0f, 1.0f, alpha // Purple color (R, G, B, A)
            );

            // Render TEXT Overlay (HUD)
            // We can't easily render text *in world* here without depth issues,
            // but we can render it near the cursor or using Debug rendering techniques.
            // For now, let's stick to the outline as it's the core visual request.
            // Text is better handled in a RenderGameOverlayEvent or HUD event.
        }
    }
}
