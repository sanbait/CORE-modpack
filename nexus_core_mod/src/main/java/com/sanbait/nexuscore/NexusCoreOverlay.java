package com.sanbait.nexuscore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class NexusCoreOverlay implements IGuiOverlay {
    public static final NexusCoreOverlay INSTANCE = new NexusCoreOverlay();

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit
                && hit.getEntity() instanceof NexusCoreEntity core) {

            int x = screenWidth / 2;
            int y = screenHeight / 2 - 30; // Above crosshair slightly

            // Level Text
            guiGraphics.drawCenteredString(mc.font,
                    Component.translatable("overlay.nexuscore.level", core.getCurrentLevel()), x, y, 0xFF55FF);

            // HP Text
            float hp = core.getHealth();
            float max = core.getMaxHealth();
            // Format numbers to avoid excessively long decimals if any, though translation
            // handles string insertion primarily.
            // Assuming simple integer-like display is desired or letting toString handle
            // it.
            // For closer parity with %.0f, we can format before passing or just pass
            // integers if appropriate.
            // Using translatable with args implies we pass values.
            guiGraphics.drawCenteredString(mc.font, Component.translatable("overlay.nexuscore.hp",
                    String.format("%.0f", hp), String.format("%.0f", max)), x, y + 10, 0xFFFFFF);

            // Radius Info
            double radius = NexusCoreConfig.BASE_RADIUS.get()
                    + (core.getCurrentLevel() * NexusCoreConfig.RADIUS_PER_LEVEL.get());
            guiGraphics.drawCenteredString(mc.font,
                    Component.translatable("overlay.nexuscore.radius", String.format("%.1f", radius)), x, y + 20,
                    0xFFFFFF);

            // Next Upgrade / Repair Item
            String costCost = NexusCoreEntity.getNextUpgradeCost(core.getCurrentLevel());
            if (core.getHealth() < core.getMaxHealth()) {
                guiGraphics.drawCenteredString(mc.font,
                        Component.translatable("overlay.nexuscore.repair_cost", costCost), x, y + 30,
                        0xFFAA00);
            } else {
                guiGraphics.drawCenteredString(mc.font,
                        Component.translatable("overlay.nexuscore.upgrade_cost", costCost), x, y + 30,
                        0xFFFF55);
            }
        }
    }
}
