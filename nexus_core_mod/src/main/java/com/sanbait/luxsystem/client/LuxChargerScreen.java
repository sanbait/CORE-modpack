package com.sanbait.luxsystem.client;

import com.sanbait.luxsystem.blocks.LuxChargerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LuxChargerScreen extends AbstractContainerScreen<LuxChargerMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft",
            "textures/gui/container/furnace.png");

    public LuxChargerScreen(LuxChargerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        
        // Рисуем прогресс-бар зарядки (стрелка вверх от входа к выходу, как в печке)
        var blockEntity = menu.getBlockEntity();
        if (blockEntity != null) {
            int progress = blockEntity.getChargeProgress(); // 0-100
            if (progress > 0) {
                int progressHeight = (int) ((progress / 100.0f) * 14); // 14 пикселей высота стрелки
                guiGraphics.blit(TEXTURE, x + 79, y + 34, 176, 14, 24, progressHeight);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
