package com.sanbait.nexuscore.client;

import com.sanbait.luxsystem.capabilities.LuxProvider;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;
import net.minecraftforge.server.ServerLifecycleHooks;

public class LuxItemDecorator implements IItemDecorator {
    public static final LuxItemDecorator INSTANCE = new LuxItemDecorator();

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        // OPTIMIZATION: Check NBT first to avoid expensive Capability lookups every
        // frame.
        // We sync "LuxStored" and "LuxMax" from Server.
        if (stack.hasTag()) {
            net.minecraft.nbt.CompoundTag tag = stack.getTag();
            if (tag.contains("LuxStored")) {
                int current = tag.getInt("LuxStored");
                int max = 0;

                if (tag.contains("LuxMax")) {
                    max = tag.getInt("LuxMax");
                } else {
                    // Fallback: This happens if item hasn't been synced heavily yet.
                    // We can try to guess or do ONE slow lookup?
                    // Safe default to avoid crash/zero division:
                    // Or try capability just ONCE?
                    // Let's rely on standard capability check if NBT is partial.
                    // But typically LuxMax should be synced.
                    // Let's default to a standard value if missing to avoid Lag Spikes?
                    // Or check capability efficiently.
                    var capOpt = stack.getCapability(LuxProvider.LUX_CAP);
                    if (capOpt.isPresent()) {
                        max = capOpt.resolve().get().getMaxLuxStored();
                    } else {
                        max = 1000; // Absolute fallback
                    }
                }

                if (max > 0 && current > 0) {
                    renderBar(guiGraphics, xOffset, yOffset, current, max);
                    return true;
                }
                // If 0 lux, don't render.
                return true;
            }
        }

        // Secondary check: If NBT is missing completely (fresh item on client?), try
        // Cap
        // But only if we suspect it's a Lux item.
        // Doing getCapability on EVERY item in existence (dirt, stone) is what causes
        // lag.
        // So we must FILTER first.

        // Fast Check: Is item instance of ILuxStorage?
        if (stack.getItem() instanceof com.sanbait.luxsystem.capabilities.ILuxStorage) {
            var capOpt = stack.getCapability(LuxProvider.LUX_CAP);
            if (capOpt.isPresent()) {
                var cap = capOpt.resolve().get();
                int max = cap.getMaxLuxStored();
                if (max > 0 && cap.getLuxStored() > 0) {
                    renderBar(guiGraphics, xOffset, yOffset, cap.getLuxStored(), max);
                    return true;
                }
            }
        }

        return false;
    }

    private void renderBar(GuiGraphics gui, int x, int y, int current, int max) {
        float pct = (float) current / max;
        int width = Math.round(13.0F * pct);
        int color = 0xFFFFFF00; // Yellow/Gold

        // Render background (black line)
        // x + 2, y + 13 is standard durability bar pos.
        // We want it ABOVE durability?
        // Durability is at bottom 2 pixels.
        // Let's put Lux at top? Or just above durability?
        // y + 11?

        int barX = x + 2;
        int barY = y + 11; // 2 pixels above durability (y+13)

        gui.fill(barX, barY, barX + 13, barY + 2, 0xFF000000); // Black bg
        gui.fill(barX, barY, barX + width, barY + 1, color); // Color bar
    }
}
