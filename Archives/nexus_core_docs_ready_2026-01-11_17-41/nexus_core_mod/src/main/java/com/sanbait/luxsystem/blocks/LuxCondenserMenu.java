package com.sanbait.luxsystem.blocks;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class LuxCondenserMenu extends AbstractContainerMenu {
    private final LuxCondenserBlockEntity blockEntity;

    public LuxCondenserMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        this(id, playerInv,
                (LuxCondenserBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public LuxCondenserMenu(int id, Inventory playerInv, LuxCondenserBlockEntity entity) {
        super(ModMenuTypes.LUX_CONDENSER_MENU.get(), id);
        this.blockEntity = entity;

        // Slot 0: Input Bucket (Left-Middle)
        this.addSlot(new SlotItemHandler(entity.output, 0, 62, 35));
        // Slot 1: Output Crystal (Right-Middle)
        this.addSlot(new SlotItemHandler(entity.output, 1, 98, 35));

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

    public LuxCondenserBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
