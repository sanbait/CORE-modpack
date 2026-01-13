package com.sanbait.luxsystem.blocks;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class LuxExtractorMenu extends AbstractContainerMenu {
    private final LuxExtractorBlockEntity blockEntity;

    public LuxExtractorMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        this(id, playerInv,
                (LuxExtractorBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public LuxExtractorMenu(int id, Inventory playerInv, LuxExtractorBlockEntity entity) {
        super(ModMenuTypes.LUX_EXTRACTOR_MENU.get(), id);
        this.blockEntity = entity;

        // Input slot (Centered in Dispenser Grid)
        this.addSlot(new SlotItemHandler(entity.inventory, 0, 80, 35));

        // Player inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInv, k, 8 + k * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public LuxExtractorBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
