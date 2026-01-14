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
        // НЕ рендерим модель GeckoLib - используем реальные блоки в мире
        // super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

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
        long gameTime = entity.level().getGameTime();

        poseStack.pushPose();

        // Желтое свечение
        float[] beamColor = getColorForLevel(entity.getCurrentLevel());

        poseStack.pushPose();
        // Fix offset: BeaconBeam renders from corner (0,0), Entity renders from center (0.5,0.5)
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        // Рендерим луч маяка
        net.minecraft.client.renderer.blockentity.BeaconRenderer.renderBeaconBeam(poseStack, bufferSource,
                net.minecraft.client.renderer.blockentity.BeaconRenderer.BEAM_LOCATION,
                partialTick, 1.0F, gameTime, 0, 1024,
                beamColor, 0.3F, 0.35F);

        poseStack.popPose();

        poseStack.popPose();
    }

    // Получаем цвет для луча в зависимости от уровня
    private float[] getColorForLevel(int level) {
        // Цветовая схема в зависимости от уровня:
        // Уровень 1-2: Желтый/золотой (1.0, 0.9, 0.3)
        // Уровень 3-4: Более яркий желтый (1.0, 0.95, 0.5)
        // Уровень 5-6: Оранжево-желтый (1.0, 0.85, 0.4)
        // Уровень 7-8: Яркий золотой (1.0, 1.0, 0.6)
        // Уровень 9-10: Почти белый/яркий (1.0, 1.0, 0.9)
        
        if (level <= 2) {
            return new float[] { 1.0f, 0.9f, 0.3f }; // Желтый/золотой
        } else if (level <= 4) {
            return new float[] { 1.0f, 0.95f, 0.5f }; // Более яркий желтый
        } else if (level <= 6) {
            return new float[] { 1.0f, 0.85f, 0.4f }; // Оранжево-желтый
        } else if (level <= 8) {
            return new float[] { 1.0f, 1.0f, 0.6f }; // Яркий золотой
        } else {
            return new float[] { 1.0f, 1.0f, 0.9f }; // Почти белый/яркий
        }
    }
}
