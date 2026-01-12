package com.sanbait.nexuscore;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NexusCoreRenderer extends GeoEntityRenderer<NexusCoreEntity> {
    public NexusCoreRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new NexusCoreModel());
    }

    // shouldRender removed. Relying on Entity.getRenderBoundingBox().inflate(64)
    // This allows standard frustum culling to work correctly with the large model.

    @Override
    public void render(NexusCoreEntity entity, float entityYaw, float partialTick,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            net.minecraft.client.renderer.MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        // Render Stats Text
        if (entity.distanceToSqr(this.entityRenderDispatcher.camera.getPosition()) < 256) {
            String luxText = "Lux: " + entity.getCurrentLux() + " / " + (entity.getMaxLuxStored());
            // Calculate Gen: (Level * Config) * 20 (ticks/sec)
            int genRate = entity.getCurrentLevel() * NexusCoreConfig.CORE_LUX_GENERATION_PER_LEVEL.get() * 20;
            String genText = "Gen: +" + genRate + "/sec";

            poseStack.pushPose();
            poseStack.translate(0.0D, entity.getBoundingBox().getYsize() + 0.5D, 0.0D);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);

            com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();

            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            float xOffset = -font.width(luxText) / 2.0F;
            font.drawInBatch(luxText, xOffset, 0, 0xFFFF00, false, poseStack.last().pose(), bufferSource,
                    net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, packedLight);

            float xOffsetGen = -font.width(genText) / 2.0F;
            font.drawInBatch(genText, xOffsetGen, 10, 0x00FF00, false, poseStack.last().pose(), bufferSource,
                    net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, packedLight);

            com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();

            poseStack.popPose();
        }

        // Render Beacon Beam
        long gameTime = entity.level().getGameTime(); // Fix beam offset

        poseStack.pushPose();
        // BeaconRenderer draws centered at (0,0,0) but the Entity Render context might
        // be slightly off due to GeckoLib model offsets.
        // It looks shifted. Try to align with entity center.
        // Usually Entity rendering is at [X, Y, Z] interpolated.
        // If the beam is "weird", we might need to adjust.
        // Based on screenshots, it looks shifted. Let's ensure no weird rotation from
        // model is applied.
        // Wait, 'super.render' applies model transforms? No, GeoEntityRenderer does.
        // We are OUTSIDE the model transforms if we popped (which we did).
        // But if the beam is offset, maybe we need to be at (0, 0, 0)?
        // Let's try to not translate blindly.
        // If the beam is too far "left/right", check vanilla beacon.

        // Actually, renderBeaconBeam draws x, y, z relative to current stack.
        // The beam is a square.
        // Let's translate UP slightly to start from the "tip" of the core model (looks
        // like 1 block high at lvl 1?)
        // And ensure it is centered.
        // If it was "crooked" (tilted), that's bad.
        // If it was just "shifted", we translate.

        // Fix: Reset rotation to camera for the beam to always face camera (billboard)?
        // No, renderBeaconBeam handles billboarding internally.

        // Fix offset: based on screenshots, it was off-center.
        // Let's try to center it explicitly.
        // poseStack.translate(0, 0, 0) is the entity pivot (feet).

        float[] color = new float[] { 1.0F, 1.0F, 1.0F }; // White beam

        poseStack.pushPose();
        // Fix offset: BeaconBeam renders from corner (0,0), Entity renders from center
        // (0.5,0.5).
        // Translate back by 0.5 to center it.
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        // Also move up if needed (BeaconBeam usually starts at y=0)
        // If the model has height, we might want to start inside it.
        // poseStack.translate(0.0D, 1.0D, 0.0D); // Optional vertical adjustment

        net.minecraft.client.renderer.blockentity.BeaconRenderer.renderBeaconBeam(poseStack, bufferSource,
                net.minecraft.client.renderer.blockentity.BeaconRenderer.BEAM_LOCATION,
                partialTick, 1.0F, gameTime, 0, 1024,
                color, 0.2F, 0.25F);

        poseStack.popPose();

        poseStack.popPose();
    }
}
