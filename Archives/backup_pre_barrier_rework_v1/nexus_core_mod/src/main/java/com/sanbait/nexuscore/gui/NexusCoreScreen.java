package com.sanbait.nexuscore.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class NexusCoreScreen extends AbstractContainerScreen<NexusCoreMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/hopper.png");

    public NexusCoreScreen(NexusCoreMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 133;
        this.imageWidth = 176;
        this.inventoryLabelY = this.imageHeight - 94; // Adjust label position if needed
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Render Core stats as Overlay text (optional, or just keep it simple)
        // Let's keep the text but position it nicely above the slot
        com.sanbait.nexuscore.NexusCoreEntity core = this.menu.getCoreEntity();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Clean Stats Display - Removed to prevent overlap with title
        // The title "Nexus Core" is enough.
        // String level = "Lvl " + core.getCurrentLevel();
        // String lux = "Lux " + core.getCurrentLux();

        // Draw centered above the slot area
        // guiGraphics.drawCenteredString(this.font, level, x + 88, y + 6, 0x404040);
        // guiGraphics.drawCenteredString(this.font, lux, x + 88, y + 16, 0x404040);
    }
}
