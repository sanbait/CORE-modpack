package com.sanbait.shadowgrid.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.sanbait.shadowgrid.ShadowGridMod;
import com.sanbait.shadowgrid.world.BiomeGridConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = ShadowGridMod.MODID, value = Dist.CLIENT)
public class VoidWallRenderer {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null)
            return;

        // Calculate player's sector
        int halfSize = BiomeGridConfig.SECTOR_SIZE / 2;
        int playerSectorX = Math.floorDiv(mc.player.getBlockX() + halfSize, BiomeGridConfig.SECTOR_SIZE);
        int playerSectorZ = Math.floorDiv(mc.player.getBlockZ() + halfSize, BiomeGridConfig.SECTOR_SIZE);

        // Render walls for ALL unlocked sectors in visible range (3 sectors radius)
        // This ensures walls are always visible, not just for current sector
        int renderRadius = 3; // Рендерим стены для секторов в радиусе 3 от игрока
        
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        // Translate to camera position so we can draw in world coords relative to
        // camera
        Vec3 camPos = event.getCamera().getPosition();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        RenderSystem.enableBlend(); // Allow opacity to work if we want? Actually, for opaque, disable blend is
                                    // faster.
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true); // Write depth so it occludes skybox/clouds behind it
        RenderSystem.disableCull(); // Draw both sides

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f mat = poseStack.last().pose();

        // Render walls for all unlocked sectors in range
        for (int offsetX = -renderRadius; offsetX <= renderRadius; offsetX++) {
            for (int offsetZ = -renderRadius; offsetZ <= renderRadius; offsetZ++) {
                int sectorX = playerSectorX + offsetX;
                int sectorZ = playerSectorZ + offsetZ;
                
                // Only render walls for UNLOCKED sectors (walls are on boundaries of unlocked sectors)
                if (ClientGridData.isSectorUnlocked(sectorX, sectorZ)) {
                    // Check all 4 neighbors for this unlocked sector
                    checkAndDrawWall(buffer, mat, sectorX, sectorZ, 1, 0); // East
                    checkAndDrawWall(buffer, mat, sectorX, sectorZ, -1, 0); // West
                    checkAndDrawWall(buffer, mat, sectorX, sectorZ, 0, 1); // South
                    checkAndDrawWall(buffer, mat, sectorX, sectorZ, 0, -1); // North
                }
            }
        }

        tesselator.end();

        // Render text on walls (price for unlocking) - use separate buffer source
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        renderWallText(poseStack, bufferSource, camPos, playerSectorX, playerSectorZ, renderRadius);
        bufferSource.endBatch(); // Flush text rendering

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    private static void renderWallText(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 camPos,
            int playerSectorX, int playerSectorZ, int renderRadius) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.font == null)
            return;

        int unlockedCount = ClientGridData.getUnlockedCount();
        int cost = ClientGridData.calculateCost(unlockedCount + 1); // Cost for next unlock

        // Render text only on walls that are close to player (within 1 sector)
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                int sectorX = playerSectorX + offsetX;
                int sectorZ = playerSectorZ + offsetZ;

                if (ClientGridData.isSectorUnlocked(sectorX, sectorZ)) {
                    // Check all 4 neighbors
                    renderTextOnWall(poseStack, bufferSource, camPos, mc.font, sectorX, sectorZ, 1, 0, cost); // East
                    renderTextOnWall(poseStack, bufferSource, camPos, mc.font, sectorX, sectorZ, -1, 0, cost); // West
                    renderTextOnWall(poseStack, bufferSource, camPos, mc.font, sectorX, sectorZ, 0, 1, cost); // South
                    renderTextOnWall(poseStack, bufferSource, camPos, mc.font, sectorX, sectorZ, 0, -1, cost); // North
                }
            }
        }
    }

    private static void renderTextOnWall(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 camPos,
            Font font, int secX, int secZ, int dx, int dz, int cost) {
        int targetX = secX + dx;
        int targetZ = secZ + dz;

        // Only render text if target sector is LOCKED
        if (ClientGridData.isSectorUnlocked(targetX, targetZ))
            return;

        int sectorSize = BiomeGridConfig.SECTOR_SIZE;
        int halfSize = sectorSize / 2;

        int centerX = secX * sectorSize;
        int centerZ = secZ * sectorSize;

        // Calculate wall position
        float wallX, wallZ;
        if (dx != 0) {
            wallX = centerX + (dx * halfSize);
            wallZ = centerZ; // Center of wall
        } else {
            wallX = centerX; // Center of wall
            wallZ = centerZ + (dz * halfSize);
        }

        // Text position: center of wall, at player eye height
        float textY = (float) camPos.y;
        float textX = wallX;
        float textZ = wallZ;

        // Calculate distance from camera
        float distX = (float) (textX - camPos.x);
        float distZ = (float) (textZ - camPos.z);
        float distY = (float) (textY - camPos.y);
        float distance = (float) Math.sqrt(distX * distX + distZ * distZ + distY * distY);

        // Only render if close enough (within 64 blocks)
        if (distance > 64.0f)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        poseStack.pushPose();
        poseStack.translate(textX - camPos.x, textY - camPos.y, textZ - camPos.z);

        // Face camera
        poseStack.mulPose(Axis.YP.rotationDegrees(-mc.player.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(mc.player.getXRot()));

        // Scale based on distance - БОЛЬШОЙ текст (как в примере)
        float scale = 0.05f * (1.0f + (64.0f - distance) / 64.0f); // Увеличен размер в 2.5 раза
        poseStack.scale(-scale, -scale, scale);

        // Disable depth test for text visibility (как в NexusCoreRenderer)
        RenderSystem.disableDepthTest();
        
        Component text = Component.literal("Цена: " + cost + " Lux");
        float textWidth = font.width(text) / 2.0f;
        
        // ЯРКИЙ цвет - золотой/желтый с подсветкой (как в примере)
        int textColor = 0xFFFF00; // Яркий желтый/золотой
        int glowColor = 0xFFAA00; // Оранжевое свечение
        
        // Рендерим фон (полупрозрачный темный прямоугольник) - ПЕРЕД текстом
        float padding = 4.0f;
        float bgAlpha = 0.8f; // Более непрозрачный фон для лучшей читаемости
        
        // Рендерим фон через отдельный buffer
        Tesselator bgTesselator = Tesselator.getInstance();
        BufferBuilder bgBuffer = bgTesselator.getBuilder();
        Matrix4f bgMat = poseStack.last().pose();
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bgBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int bgAlphaInt = (int)(bgAlpha * 255);
        bgBuffer.vertex(bgMat, -textWidth - padding, -padding, 0).color(0, 0, 0, bgAlphaInt).endVertex();
        bgBuffer.vertex(bgMat, textWidth + padding, -padding, 0).color(0, 0, 0, bgAlphaInt).endVertex();
        bgBuffer.vertex(bgMat, textWidth + padding, font.lineHeight + padding, 0).color(0, 0, 0, bgAlphaInt).endVertex();
        bgBuffer.vertex(bgMat, -textWidth - padding, font.lineHeight + padding, 0).color(0, 0, 0, bgAlphaInt).endVertex();
        bgTesselator.end();
        
        // Рендерим текст с glow эффектом (несколько слоев для свечения)
        // Glow layer 1 (самый большой, самый прозрачный)
        font.drawInBatch(text, -textWidth, 0, (0x40 << 24) | glowColor, false,
                poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        font.drawInBatch(text, -textWidth + 1, 0, (0x40 << 24) | glowColor, false,
                poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        font.drawInBatch(text, -textWidth - 1, 0, (0x40 << 24) | glowColor, false,
                poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        font.drawInBatch(text, -textWidth, 1, (0x40 << 24) | glowColor, false,
                poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        font.drawInBatch(text, -textWidth, -1, (0x40 << 24) | glowColor, false,
                poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        
        // Glow layer 2 (средний)
        font.drawInBatch(text, -textWidth + 0.5f, 0, (0x80 << 24) | glowColor, false,
                poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        font.drawInBatch(text, -textWidth - 0.5f, 0, (0x80 << 24) | glowColor, false,
                poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        
        // Main text (яркий желтый)
        font.drawInBatch(text, -textWidth, 0, textColor, false,
                poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        
        RenderSystem.enableDepthTest();
        poseStack.popPose();
    }

    private static void checkAndDrawWall(BufferBuilder buffer, Matrix4f mat, int currentX, int currentZ, int dx,
            int dz) {
        int targetX = currentX + dx;
        int targetZ = currentZ + dz;

        // If target sector is LOCKED, draw a wall between us
        if (!ClientGridData.isSectorUnlocked(targetX, targetZ)) {
            drawWall(buffer, mat, currentX, currentZ, dx, dz);
        }
    }

    private static void drawWall(BufferBuilder buffer, Matrix4f mat, int secX, int secZ, int dx, int dz) {
        // Calculate boundary coordinate
        int sectorSize = BiomeGridConfig.SECTOR_SIZE;
        int halfSize = sectorSize / 2;

        // Center of CURRENT sector
        int centerX = secX * sectorSize; // e.g. 0, 1024, -1024
        int centerZ = secZ * sectorSize;

        // Wall position depends on direction
        float x1, z1, x2, z2;

        if (dx != 0) { // East/West Wall
            float wallX = centerX + (dx * (float) halfSize); // 0 + 512 = 512
            x1 = wallX;
            x2 = wallX;
            z1 = centerZ - halfSize;
            z2 = centerZ + halfSize;
        } else { // North/South Wall
            float wallZ = centerZ + (dz * (float) halfSize);
            z1 = wallZ;
            z2 = wallZ;
            x1 = centerX - halfSize;
            x2 = centerX + halfSize;
        }

        float yBottom = -64.0f;
        float yTop = 320.0f;

        // **AESTHETICS**: Dark with pulsing effect (sinister void)
        Minecraft mc = Minecraft.getInstance();
        long gameTime = mc.level != null ? mc.level.getGameTime() : 0;
        
        // Pulsing darkness effect (subtle)
        float pulse = 0.85f + 0.15f * (float) Math.sin(gameTime * 0.05f);
        float r = 0.05f * pulse;
        float g = 0.0f;
        float b = 0.1f * pulse;
        float a = 0.95f;

        // Main wall quad
        buffer.vertex(mat, x1, yBottom, z1).color(r, g, b, a).endVertex();
        buffer.vertex(mat, x2, yBottom, z2).color(r, g, b, a).endVertex();
        buffer.vertex(mat, x2, yTop, z2).color(r, g, b, a).endVertex();
        buffer.vertex(mat, x1, yTop, z1).color(r, g, b, a).endVertex();

        // Add "void tendrils" pattern - dark wavy lines for sinister effect
        // Render vertical dark stripes for texture (very subtle, performance-friendly)
        float stripeWidth = 8.0f; // Width of each stripe
        float stripeSpacing = 16.0f; // Space between stripes
        
        // Calculate how many stripes fit on this wall
        float wallLength = (dx != 0) ? (z2 - z1) : (x2 - x1);
        int numStripes = (int) (wallLength / stripeSpacing);
        
        // Render subtle dark stripes (only a few, not too many for FPS)
        for (int i = 0; i < Math.min(numStripes, 8); i++) { // Max 8 stripes per wall
            float stripePos = i * stripeSpacing;
            float stripeX1, stripeZ1, stripeX2, stripeZ2;
            
            if (dx != 0) {
                // Vertical stripe on East/West wall
                stripeX1 = x1;
                stripeX2 = x2;
                stripeZ1 = z1 + stripePos;
                stripeZ2 = z1 + stripePos + stripeWidth;
            } else {
                // Vertical stripe on North/South wall
                stripeX1 = x1 + stripePos;
                stripeX2 = x1 + stripePos + stripeWidth;
                stripeZ1 = z1;
                stripeZ2 = z2;
            }
            
            // Darker stripe color (almost black)
            float stripeR = 0.0f;
            float stripeG = 0.0f;
            float stripeB = 0.0f;
            float stripeA = 0.3f * pulse; // Subtle pulsing
            
            // Only render if stripe is within wall bounds
            if ((dx != 0 && stripeZ2 <= z2) || (dx == 0 && stripeX2 <= x2)) {
                buffer.vertex(mat, stripeX1, yBottom, stripeZ1).color(stripeR, stripeG, stripeB, stripeA).endVertex();
                buffer.vertex(mat, stripeX2, yBottom, stripeZ2).color(stripeR, stripeG, stripeB, stripeA).endVertex();
                buffer.vertex(mat, stripeX2, yTop, stripeZ2).color(stripeR, stripeG, stripeB, stripeA).endVertex();
                buffer.vertex(mat, stripeX1, yTop, stripeZ1).color(stripeR, stripeG, stripeB, stripeA).endVertex();
            }
        }
    }
}
