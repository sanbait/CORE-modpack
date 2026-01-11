package com.sanbait.nexuscore;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NexusCoreRenderer extends GeoEntityRenderer<NexusCoreEntity> {
    public NexusCoreRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new NexusCoreModel());
    }

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
            poseStack.mulPose(this.entityRenderDispatcher.cameraRotation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);

            com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();

            net.minecraft.client.gui.Font font = this.getFontRenderer();
            float xOffset = -font.width(luxText) / 2.0F;
            font.drawInBatch(luxText, xOffset, 0, 0xFFFF00, false, poseStack.last().pose(), bufferSource,
                    net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, packedLight);

            float xOffsetGen = -font.width(genText) / 2.0F;
            font.drawInBatch(genText, xOffsetGen, 10, 0x00FF00, false, poseStack.last().pose(), bufferSource,
                    net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, packedLight);

            com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();

            poseStack.popPose();
        }
    }
}
