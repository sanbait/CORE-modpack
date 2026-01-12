package com.sanbait.luxsystem.items;

import com.sanbait.luxsystem.capabilities.ILuxStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LuxArmorItem extends ArmorItem implements ILuxStorage {
    private final int capacity;

    public LuxArmorItem(ArmorMaterial material, Type type, Properties properties, int capacity) {
        super(material, type, properties);
        this.capacity = capacity;
    }

    public static int getLux(ItemStack stack) {
        return stack.getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP)
                .map(com.sanbait.luxsystem.capabilities.ILuxStorage::getLuxStored)
                .orElse(0);
    }

    public static void setLux(ItemStack stack, int amount) {
        stack.getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP).ifPresent(cap -> {
            if (cap instanceof com.sanbait.luxsystem.capabilities.LuxCapability impl) {
                impl.setLux(amount);
            }
        });
    }

    @Override
    @SuppressWarnings("removal")
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide) {
            // OPTIMIZATION: Charging is now handled centrally by NexusCoreEntity
            // if (level.getGameTime() % 100 == 0) { ... }

            // Effects
            stack.getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP).ifPresent(cap -> {
                int lux = cap.getLuxStored();
                int max = cap.getMaxLuxStored(); // Use dynamic max from capability (config-aware)
                if (lux > 0) {
                    // Consume lux slowly (every 100 ticks = 5 seconds)
                    if (level.getGameTime() % 100 == 0) {
                        cap.extractLux(1, false);
                        // Sync handled by capability or external tracker?
                        // We should probably sync if we change it.
                        stack.getOrCreateTag().putInt("LuxStored", cap.getLuxStored());
                    }

                    switch (this.type) {
                        case HELMET:
                            // Night Vision if > 50% charge
                            if (lux > max / 2) {
                                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, true, false));
                            }
                            break;
                        case CHESTPLATE:
                            // Passive Resistance?
                            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 0, true, false));
                            break;
                        case LEGGINGS:
                            // Speed
                            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 0, true, false));
                            break;
                        case BOOTS:
                            // Jump Boost II
                            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 20, 1, true, false));
                            player.resetFallDistance(); // Simple fall damage negation tick
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (com.sanbait.nexuscore.util.ClientHooks.isShiftDown()) {
            tooltip.add(Component.translatable("tooltip.luxsystem.lux_armor_set_bonus")
                    .withStyle(ChatFormatting.GOLD));
            // Lux Charge handled globally
        } else {
            tooltip.add(Component.translatable("tooltip.nexuscore.hold_shift")
                    .withStyle(ChatFormatting.GRAY));
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }

    // Stub implementations
    @Override
    public int getLuxStored() {
        return 0;
    }

    @Override
    public int getMaxLuxStored() {
        return capacity;
    }

    @Override
    public int receiveLux(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractLux(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getLux(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        // Armor might need capability lookup as 'capacity' field is final
        // But we have getMaxLuxStored stub now? Actually LuxArmorItem has
        // getMaxLuxStored overridden to return `capacity`
        // But better to use capability if possible for consistency
        int max = 1000;
        var cap = stack.getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP);
        if (cap.isPresent()) {
            max = cap.resolve().get().getMaxLuxStored();
        } else {
            max = capacity;
        }

        if (max <= 0)
            return 0;
        return Math.round(13.0F * (float) getLux(stack) / max);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFFFF00; // Yellow/Gold
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (!slotChanged && oldStack.getItem() == newStack.getItem()) {
            return false;
        }
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }
}
