package com.sanbait.luxsystem.items;

import com.sanbait.luxsystem.capabilities.ILuxStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LuxPickaxeItem extends PickaxeItem implements ILuxStorage {
    private final int capacity;

    public LuxPickaxeItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
        this.capacity = 1000;
    }

    // NBT wrapper for ILuxStorage logic
    // We assume the Itemstack passed in context calls these, but strictly speaking
    // Item class is a singleton. The Storage should be on the ItemStack capability.
    // For simplicity in this implementation, we use static helpers or manual NBT
    // handling
    // because implementing the interface on the Item class itself doesn't
    // automatically
    // make the ItemStack have the capability without a Provider.

    // Helper to get lux from stack
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
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        // Lux consumption is now handled by NexusCore.onBlockBreak event to respect
        // Config
        // We do NOT consume here to avoid double-charging.
        return super.mineBlock(stack, level, state, pos, entity);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float base = super.getDestroySpeed(stack, state);
        if (getLux(stack) > 0) {
            return base * 1.5f; // 50% Speed boost if charged
        }
        return base;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (com.sanbait.nexuscore.util.ClientHooks.isShiftDown()) {
            tooltip.add(Component.translatable("tooltip.luxsystem.lux_pickaxe_stats")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal(" "));
            tooltip.add(Component.translatable("tooltip.luxsystem.lux_pickaxe_passive")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.luxsystem.lux_pickaxe_active")
                    .withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.literal(" "));

            // Lux Charge
            int currentLux = getLux(stack);
            int maxLux = getMaxLuxStored(stack); // Use stack-aware method
            tooltip.add(Component.literal("Lux: " + currentLux + " / " + maxLux)
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.nexuscore.hold_shift")
                    .withStyle(ChatFormatting.GRAY));
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }

    // OPTIMIZATION: Tick logic removed. Charging is now handled centrally by
    // NexusCoreEntity.
    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId,
            boolean isSelected) {
        // No-op
    }

    // Helper for max lux from stack capability
    public int getMaxLuxStored(ItemStack stack) {
        return stack.getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP)
                .map(com.sanbait.luxsystem.capabilities.ILuxStorage::getMaxLuxStored)
                .orElse(capacity);
    }

    // ILuxStorage implementation
    @Override
    public int getLuxStored() {
        return 0; // Context-less call, ideally shouldn't be used
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
        return 0xFFFF00; // Yellow/Gold color for Lux
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (!slotChanged && oldStack.getItem() == newStack.getItem()) {
            return false;
        }
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }
}
