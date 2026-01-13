package com.sanbait.nexuscore.gui;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class NexusCoreMenu extends AbstractContainerMenu {
    private final com.sanbait.nexuscore.NexusCoreEntity coreEntity;
    private static final int UPGRADE_SLOT_INDEX = 0;

    public NexusCoreMenu(int containerId, Inventory playerInventory, com.sanbait.nexuscore.NexusCoreEntity entity) {
        super(com.sanbait.nexuscore.NexusCore.CORE_MENU.get(), containerId);
        this.coreEntity = entity;

        // Upgrade slot
        // Center X: (176 / 2) - (16 / 2) = 80
        // Y: 20 (Center slot of Hopper)
        this.addSlot(new SlotItemHandler(entity.getUpgradeInventory(), UPGRADE_SLOT_INDEX, 80, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                // Strict check: Only accept the specific upgrade item for current level
                net.minecraft.world.item.Item required = entity.getUpgradeCostItem(entity.getCurrentLevel());
                return stack.is(required);
            }
        });

        // Player inventory
        // Aligned for Hopper GUI (starts at Y=51)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index == UPGRADE_SLOT_INDEX) {
                // Moving from upgrade slot to player inventory
                if (!this.moveItemStackTo(slotStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to upgrade slot
                boolean isDiamond = slotStack.is(Items.DIAMOND);
                boolean isLuxCrystal = slotStack.is(com.sanbait.luxsystem.ModItems.LUX_CRYSTAL.get());

                // Also check dynamic upgrade cost
                net.minecraft.world.item.Item costItem = coreEntity.getUpgradeCostItem(coreEntity.getCurrentLevel());
                boolean isUpgradeItem = slotStack.is(costItem);

                if (isDiamond || isLuxCrystal || isUpgradeItem) {
                    if (!this.moveItemStackTo(slotStack, UPGRADE_SLOT_INDEX, UPGRADE_SLOT_INDEX + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.coreEntity.isAlive() && this.coreEntity.distanceTo(player) < 8.0D;
    }

    public com.sanbait.nexuscore.NexusCoreEntity getCoreEntity() {
        return this.coreEntity;
    }
}
