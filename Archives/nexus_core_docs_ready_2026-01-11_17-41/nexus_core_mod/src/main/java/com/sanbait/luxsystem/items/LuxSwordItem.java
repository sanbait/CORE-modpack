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

    public static int getLux(ItemStack stack) {
        return stack.getOrCreateTag().getInt("LuxStored");
    }

    public static void setLux(ItemStack stack, int amount) {
        stack.getOrCreateTag().putInt("LuxStored", amount);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        int lux = getLux(stack);

        if (lux > 0) {
            // Always consume Lux, even if less than 5
            int consumed = Math.min(lux, 5);
            setLux(stack, lux - consumed);

            // Only apply effect if we had enough (5+)
            if (consumed >= 5) {
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 1));
            }
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId,
            boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player) {
            if (level.getGameTime() % 20 == 0) {
                if (com.sanbait.luxsystem.CoreRadiusManager.isInRadius(level, player.blockPosition())) {
                    int current = getLux(stack);
                    if (current < capacity) {
                        setLux(stack, current + 1);
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
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
            tooltip.add(Component.literal("Lux: " + currentLux + " / " + capacity)
                    .withStyle(ChatFormatting.AQUA));
        } else {
            // Generic 'Hold Shift' message
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
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        // Prevent re-equip animation if only Lux NBT changed
        if (!slotChanged && oldStack.getItem() == newStack.getItem()) {
            return false;
        }
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }
}
