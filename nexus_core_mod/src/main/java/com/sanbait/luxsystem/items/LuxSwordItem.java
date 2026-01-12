package com.sanbait.luxsystem.items;

import com.sanbait.luxsystem.capabilities.ILuxStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LuxSwordItem extends SwordItem implements ILuxStorage {
    private final int capacity = 500;

    public LuxSwordItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    // Helper to get lux from stack
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
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Lux consumption is now handled by NexusCore.onLivingHurt event to respect
        // Config
        // We just check if we have enough Lux to apply the effect
        stack.getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP).ifPresent(cap -> {
            // Apply effect if we have Lux (or had it before the event consumed it?)
            // Since Event runs before this, if we have > 0 now, we definitely had enough.
            // If we have 0 now, we might have just used the last bit.
            // For simplicity, let's just checking if the capability exists.
            // Actually, let's just apply blindness with a chance or if Lux > 0.
            if (cap.getLuxStored() > 0) {
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 1));
            }
        });
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId,
            boolean isSelected) {
        // Charging handled centrally by NexusCoreEntity
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (com.sanbait.nexuscore.util.ClientHooks.isShiftDown()) {
            // Detailed stats
            tooltip.add(Component.translatable("tooltip.luxsystem.lux_sword_stats")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal(" "));
            tooltip.add(Component.translatable("tooltip.luxsystem.lux_sword_passive")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.luxsystem.lux_sword_active")
                    .withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.literal(" "));

            // Lux Charge
            int currentLux = getLux(stack);
            int maxLux = getMaxLuxStored(stack); // Use stack-aware method
            tooltip.add(Component.literal("Lux: " + currentLux + " / " + maxLux)
                    .withStyle(ChatFormatting.AQUA));
        } else {
            // Generic 'Hold Shift' message
            tooltip.add(Component.translatable("tooltip.nexuscore.hold_shift")
                    .withStyle(ChatFormatting.GRAY));
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }

    // Helper for max lux from stack capability
    public int getMaxLuxStored(ItemStack stack) {
        return stack.getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP)
                .map(com.sanbait.luxsystem.capabilities.ILuxStorage::getMaxLuxStored)
                .orElse(capacity);
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
        int max = getMaxLuxStored(stack);
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
        // Prevent re-equip animation if only Lux NBT changed
        if (!slotChanged && oldStack.getItem() == newStack.getItem()) {
            return false;
        }
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }
}
