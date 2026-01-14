package com.sanbait.luxsystem.blocks;

import com.sanbait.luxsystem.ModBlockEntities;
import com.sanbait.luxsystem.ModItems;
import com.sanbait.luxsystem.LuxSystemConfig;
import com.sanbait.luxsystem.capabilities.ILuxStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuxChargerBlockEntity extends BlockEntity implements MenuProvider {
    
    // Sync Integers
    protected final net.minecraft.world.inventory.ContainerData dataAccess = new net.minecraft.world.inventory.ContainerData() {
        public int get(int index) {
            switch(index) {
                case 0: return LuxChargerBlockEntity.this.luxCharged;
                case 1: return LuxChargerBlockEntity.this.luxToChargeTotal;
                default: return 0;
            }
        }
        public void set(int index, int value) {
            switch(index) {
                case 0: LuxChargerBlockEntity.this.luxCharged = value; break;
                case 1: LuxChargerBlockEntity.this.luxToChargeTotal = value; break;
            }
        }
        public int getCount() {
            return 2;
        }
    };

    
    // Слот для кристаллов/тары с Lux (топливо)
    public final ItemStackHandler fuelSlot = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    
    // Слот для предметов для зарядки (вход)
    public final ItemStackHandler inputSlot = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    
    // Слот для заряженных предметов (выход)
    public final ItemStackHandler outputSlot = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    
    private final LazyOptional<IItemHandler> fuelHandler = LazyOptional.of(() -> fuelSlot);
    private final LazyOptional<IItemHandler> inputHandler = LazyOptional.of(() -> inputSlot);
    private final LazyOptional<IItemHandler> outputHandler = LazyOptional.of(() -> outputSlot);
    
    // Количество Lux в одном кристалле
    private static final int LUX_PER_CRYSTAL = 1000;
    // Количество Lux в одном ведре жидкого Lux (1000 mB = 1000 Lux)
    private static final int LUX_PER_BUCKET = 1000;
    
    // Текущий предмет, который заряжается (копия из inputSlot)
    private ItemStack chargingItem = ItemStack.EMPTY;
    // Сколько Lux уже добавлено в текущий предмет
    private int luxCharged = 0;
    // Сколько Lux нужно добавить всего (из топлива)
    private int luxToChargeTotal = 0;
    
    public int getChargeProgress() {
        if (luxToChargeTotal <= 0) {
            return 0;
        }
        // Возвращаем прогресс от 0 до 100 (для отображения в GUI)
        return this.dataAccess.get(1) == 0 ? 0 : Math.min(100, (this.dataAccess.get(0) * 100) / this.dataAccess.get(1));
    }
    
    public int getMaxChargeProgress() {
        return 100; // Максимум для прогресс-бара
    }
    
    public LuxChargerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LUX_CHARGER_BE.get(), pos, state);
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.luxsystem.lux_charger");
    }
    
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new LuxChargerMenu(id, playerInv, this, this.dataAccess);
    }
    
    /**
     * Тик обработки - заряжает предметы из кристаллов постепенно
     */
    public static void tick(Level level, BlockPos pos, BlockState state, LuxChargerBlockEntity entity) {
        if (level.isClientSide) {
            return;
        }
        
        // Получаем скорость зарядки из конфига (Lux за тик)
        int chargeSpeed = LuxSystemConfig.CHARGER_SPEED.get();
        
        // Проверяем, есть ли топливо (кристаллы или тара с Lux)
        ItemStack fuelStack = entity.fuelSlot.getStackInSlot(0);
        if (fuelStack.isEmpty() || !isValidFuel(fuelStack)) {
            // Нет топлива - сбрасываем зарядку
            entity.chargingItem = ItemStack.EMPTY;
            entity.luxCharged = 0;
            entity.luxToChargeTotal = 0;
            return;
        }
        
        // Проверяем, есть ли предмет для зарядки
        ItemStack inputStack = entity.inputSlot.getStackInSlot(0);
        if (inputStack.isEmpty() || !canChargeItem(inputStack)) {
            // Если нет предмета или он уже полный, сбрасываем
            entity.chargingItem = ItemStack.EMPTY;
            entity.luxCharged = 0;
            entity.luxToChargeTotal = 0;
            entity.setChanged();
            return;
        }

        // Если ChargingItem пуст или отличается от Input (игрок поменял предмет), инициализируем
        if (entity.chargingItem.isEmpty() || !ItemStack.isSameItemSameTags(entity.chargingItem, inputStack)) {
            // New Item inserted
            entity.chargingItem = inputStack.copy();
            entity.chargingItem.setCount(1);
            entity.luxCharged = 0;
            // Recalculate needed lux for the CURRENT fuel cycle
            initNewFuelCycle(entity, chargeSpeed);
        }

        
        // Проверяем, есть ли место в выходном слоте
        ItemStack outputStack = entity.outputSlot.getStackInSlot(0);
        if (!outputStack.isEmpty()) {
            // Если выходной слот не пуст, проверяем можно ли добавить
            if (outputStack.getCount() >= outputStack.getMaxStackSize() || 
                !ItemStack.isSameItem(entity.chargingItem, outputStack) ||
                !ItemStack.isSameItemSameTags(outputStack, entity.chargingItem)) {
                // Нет места - останавливаем зарядку
                entity.chargingItem = ItemStack.EMPTY;
                entity.luxCharged = 0;
                entity.luxToChargeTotal = 0;
                return;
            }
        }
        
        // Постепенно заряжаем предмет
        if (entity.luxCharged < entity.luxToChargeTotal) {
             int luxAvailable = Math.min(chargeSpeed, entity.luxToChargeTotal - entity.luxCharged);
             entity.luxCharged += luxAvailable;
             
             // КРИТИЧНО: Применяем заряд к РЕАЛЬНОМУ предмету в inputSlot, а не к копии!
             // Применяем постепенно каждый тик, чтобы предмет обновлялся в реальном времени
             chargeItem(inputStack, luxAvailable);
             
             entity.setChanged();
        }

        // Если цикл (один кристалл) завершен
        if (entity.luxCharged >= entity.luxToChargeTotal && entity.luxToChargeTotal > 0) {
            // 1. Уничтожаем использованное топливо (ЭТО БЫЛ ОДИН ЦИКЛ)
            consumeFuel(entity);

            // 2. Проверяем, зарядился ли предмет ПОЛНОСТЬЮ? (проверяем РЕАЛЬНЫЙ предмет из inputSlot)
            int currentLux = getLuxFromItem(inputStack);
            int maxLux = getMaxLuxFromItem(inputStack);
            
            if (currentLux >= maxLux) {
                // ПОЛНОСТЬЮ ЗАРЯЖЕН -> ВЫХОД
                // Обновляем копию перед перемещением
                entity.chargingItem = inputStack.copy();
                entity.chargingItem.setCount(1);
                
                 ItemStack finalOutputStack = entity.outputSlot.getStackInSlot(0);
                 if (finalOutputStack.isEmpty()) {
                     entity.outputSlot.setStackInSlot(0, entity.chargingItem);
                     inputStack.shrink(1); // Убираем из входа только когда полностью готов и перемещен
                     if (inputStack.isEmpty()) entity.inputSlot.setStackInSlot(0, ItemStack.EMPTY);
                 } else if (ItemStack.isSameItemSameTags(finalOutputStack, entity.chargingItem) && finalOutputStack.getCount() < finalOutputStack.getMaxStackSize()) {
                     finalOutputStack.grow(1);
                     inputStack.shrink(1);
                     if (inputStack.isEmpty()) entity.inputSlot.setStackInSlot(0, ItemStack.EMPTY);
                 }
                 // Reset
                 entity.chargingItem = ItemStack.EMPTY;
                 entity.luxCharged = 0;
                 entity.luxToChargeTotal = 0;
            } else {
                // НЕ ПОЛНОСТЬЮ -> ПРОДОЛЖАЕМ (Нужен новый цикл топлива)
                // Обновляем копию для следующего цикла
                entity.chargingItem = inputStack.copy();
                entity.chargingItem.setCount(1);
                
                // Сбрасываем прогресс текущего цикла
                entity.luxCharged = 0;
                entity.luxToChargeTotal = 0;
                // Пытаемся начать новый цикл сразу же
                initNewFuelCycle(entity, chargeSpeed);
            }
            entity.setChanged();
        }
    }

    private static void initNewFuelCycle(LuxChargerBlockEntity entity, int chargeSpeed) {
         ItemStack fuelStack = entity.fuelSlot.getStackInSlot(0);
         if (fuelStack.isEmpty() || !isValidFuel(fuelStack)) {
             entity.luxToChargeTotal = 0; // No fuel, stop. WAITING_FOR_FUEL state effectively.
             return;
         }

         int luxFromFuel = 0;
         if (fuelStack.getItem() == ModItems.LUX_CRYSTAL.get()) {
             luxFromFuel = LUX_PER_CRYSTAL;
         } else if (fuelStack.getItem() == com.sanbait.luxsystem.ModItems.LIQUID_LUX_BUCKET.get()) {
             luxFromFuel = LUX_PER_BUCKET;
         }

         int currentLux = getLuxFromItem(entity.chargingItem);
         int maxLux = getMaxLuxFromItem(entity.chargingItem);
         
         // Сколько еще нужно предмету?
         int needed = maxLux - currentLux;
         if (needed <= 0) return;

         // Настраиваем цикл на 1 единицу топлива
         entity.luxToChargeTotal = Math.min(luxFromFuel, needed);
    }

    private static void consumeFuel(LuxChargerBlockEntity entity) {
        ItemStack fuelStack = entity.fuelSlot.getStackInSlot(0);
        if (fuelStack.getItem() == com.sanbait.luxsystem.ModItems.LIQUID_LUX_BUCKET.get()) {
             entity.fuelSlot.setStackInSlot(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BUCKET));
        } else {
             fuelStack.shrink(1);
             if (fuelStack.isEmpty()) {
                 entity.fuelSlot.setStackInSlot(0, ItemStack.EMPTY);
             }
        }
    }
    
    /**
     * Проверяет, валидно ли топливо (кристалл или тара с Lux)
     */
    private static boolean isValidFuel(ItemStack stack) {
        // Кристалл Lux
        if (stack.getItem() == ModItems.LUX_CRYSTAL.get()) {
            return true;
        }
        // Ведро с жидким Lux
        if (stack.getItem() == com.sanbait.luxsystem.ModItems.LIQUID_LUX_BUCKET.get()) {
            return true;
        }
        return false;
    }
    
    /**
     * Проверяет, можно ли зарядить предмет
     */
    private static boolean canChargeItem(ItemStack stack) {
        int currentLux = getLuxFromItem(stack);
        int maxLux = getMaxLuxFromItem(stack);
        return maxLux > 0 && currentLux < maxLux;
    }
    
    /**
     * Получает текущий Lux из предмета
     */
    private static int getLuxFromItem(ItemStack stack) {
        return stack.getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP)
                .map(ILuxStorage::getLuxStored)
                .orElse(0);
    }
    
    /**
     * Получает максимальный Lux из предмета
     */
    private static int getMaxLuxFromItem(ItemStack stack) {
        return stack.getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP)
                .map(ILuxStorage::getMaxLuxStored)
                .orElse(0);
    }
    
    /**
     * Заряжает предмет
     */
    private static void chargeItem(ItemStack stack, int amount) {
        stack.getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP)
                .ifPresent(cap -> {
                    if (cap instanceof com.sanbait.luxsystem.capabilities.LuxCapability impl) {
                        int received = impl.receiveLux(amount, false);
                        // Синхронизируем с NBT для клиента
                        stack.getOrCreateTag().putInt("LuxStored", impl.getLuxStored());
                        stack.getOrCreateTag().putInt("LuxMax", impl.getMaxLuxStored());
                    }
                });
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("fuelSlot", fuelSlot.serializeNBT());
        tag.put("inputSlot", inputSlot.serializeNBT());
        tag.put("outputSlot", outputSlot.serializeNBT());
        if (!chargingItem.isEmpty()) {
            tag.put("chargingItem", chargingItem.save(new CompoundTag()));
        }
        tag.putInt("luxCharged", luxCharged);
        tag.putInt("luxToChargeTotal", luxToChargeTotal);
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fuelSlot.deserializeNBT(tag.getCompound("fuelSlot"));
        inputSlot.deserializeNBT(tag.getCompound("inputSlot"));
        outputSlot.deserializeNBT(tag.getCompound("outputSlot"));
        if (tag.contains("chargingItem")) {
            chargingItem = ItemStack.of(tag.getCompound("chargingItem"));
        } else {
            chargingItem = ItemStack.EMPTY;
        }
        luxCharged = tag.getInt("luxCharged");
        luxToChargeTotal = tag.getInt("luxToChargeTotal");
    }
    
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null || side == net.minecraft.core.Direction.UP) {
                // Сверху - вход предметов
                return inputHandler.cast();
            } else if (side == net.minecraft.core.Direction.DOWN) {
                // Снизу - выход
                return outputHandler.cast();
            } else {
                // С боков - топливо
                return fuelHandler.cast();
            }
        }
        return super.getCapability(cap, side);
    }
    
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fuelHandler.invalidate();
        inputHandler.invalidate();
        outputHandler.invalidate();
    }
}
