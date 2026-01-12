package com.sanbait.luxsystem.blocks;

import com.sanbait.luxsystem.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class LuxChargerMenu extends AbstractContainerMenu {
    private final LuxChargerBlockEntity blockEntity;
    
    public LuxChargerMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        this(id, playerInv,
                (LuxChargerBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
    }
    
    public LuxChargerMenu(int id, Inventory playerInv, LuxChargerBlockEntity entity) {
        super(com.sanbait.luxsystem.blocks.ModMenuTypes.LUX_CHARGER_MENU.get(), id);
        this.blockEntity = entity;
        
        // Слот для топлива (кристаллы/тара с Lux) - слева внизу (как в печке)
        this.addSlot(new SlotItemHandler(entity.fuelSlot, 0, 56, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() == ModItems.LUX_CRYSTAL.get() || 
                       stack.getItem() == com.sanbait.luxsystem.ModItems.LIQUID_LUX_BUCKET.get();
            }
        });
        
        // Слот для предметов для зарядки (вход) - слева сверху (как в печке)
        this.addSlot(new SlotItemHandler(entity.inputSlot, 0, 56, 17));
        
        // Слот для заряженных предметов (выход) - справа в центре (как в печке)
        this.addSlot(new SlotItemHandler(entity.outputSlot, 0, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // Нельзя класть вручную
            }
        });
        
        // Инвентарь игрока
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        
        // Хотбар игрока
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInv, k, 8 + k * 18, 142));
        }
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();
            
            // Слот для топлива (0)
            if (index == 0) {
                // Из слота топлива в инвентарь игрока
                if (!this.moveItemStackTo(slotStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Слот для предметов (вход) (1)
            else if (index == 1) {
                // Из входного слота в инвентарь игрока
                if (!this.moveItemStackTo(slotStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Слот для заряженных предметов (выход) (2)
            else if (index == 2) {
                // Из выходного слота в инвентарь игрока
                if (!this.moveItemStackTo(slotStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Инвентарь игрока (3-38)
            else {
                // Если это топливо (кристалл или ведро) - в слот топлива
                if (slotStack.getItem() == ModItems.LUX_CRYSTAL.get() || 
                    slotStack.getItem() == com.sanbait.luxsystem.ModItems.LIQUID_LUX_BUCKET.get()) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // Иначе - в слот для предметов (вход)
                else {
                    if (!this.moveItemStackTo(slotStack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
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
        return true;
    }
    
    public LuxChargerBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
