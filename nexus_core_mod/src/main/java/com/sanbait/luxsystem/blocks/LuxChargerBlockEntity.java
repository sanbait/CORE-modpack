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
        return Math.min(100, (luxCharged * 100) / luxToChargeTotal);
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
        return new LuxChargerMenu(id, playerInv, this);
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
            // Нет предмета - сбрасываем зарядку
            entity.chargingItem = ItemStack.EMPTY;
            entity.luxCharged = 0;
            entity.luxToChargeTotal = 0;
            return;
        }
        
        // Если еще не начали зарядку или предмет изменился - начинаем новую зарядку
        if (entity.chargingItem.isEmpty() || !ItemStack.isSameItemSameTags(entity.chargingItem, inputStack)) {
            // Получаем текущий Lux предмета
            int currentLux = getLuxFromItem(inputStack);
            int maxLux = getMaxLuxFromItem(inputStack);
            
            if (currentLux >= maxLux) {
                // Предмет уже полностью заряжен
                entity.chargingItem = ItemStack.EMPTY;
                entity.luxCharged = 0;
                entity.luxToChargeTotal = 0;
                return;
            }
            
            // Определяем сколько Lux дает топливо
            int luxFromFuel = 0;
            if (fuelStack.getItem() == ModItems.LUX_CRYSTAL.get()) {
                luxFromFuel = LUX_PER_CRYSTAL;
            } else if (fuelStack.getItem() == com.sanbait.luxsystem.ModItems.LIQUID_LUX_BUCKET.get()) {
                luxFromFuel = LUX_PER_BUCKET;
            }
            
            if (luxFromFuel <= 0) {
                return;
            }
            
            // Вычисляем сколько нужно зарядить (не больше, чем может вместить предмет)
            int luxToCharge = Math.min(luxFromFuel, maxLux - currentLux);
            
            // Начинаем новую зарядку
            entity.chargingItem = inputStack.copy();
            entity.chargingItem.setCount(1);
            entity.luxCharged = 0;
            entity.luxToChargeTotal = luxToCharge;
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
            // Добавляем Lux за этот тик
            int luxToAddThisTick = Math.min(chargeSpeed, entity.luxToChargeTotal - entity.luxCharged);
            chargeItem(entity.chargingItem, luxToAddThisTick);
            entity.luxCharged += luxToAddThisTick;
            entity.setChanged();
        }
        
        // Если зарядка завершена - перемещаем предмет в выходной слот
        if (entity.luxCharged >= entity.luxToChargeTotal) {
            // Уменьшаем входной стак
            inputStack.shrink(1);
            if (inputStack.isEmpty()) {
                entity.inputSlot.setStackInSlot(0, ItemStack.EMPTY);
            }
            
            // Добавляем в выходной слот
            if (outputStack.isEmpty()) {
                entity.outputSlot.setStackInSlot(0, entity.chargingItem);
            } else {
                // Проверяем, что NBT совпадает (кроме количества)
                if (ItemStack.isSameItemSameTags(outputStack, entity.chargingItem)) {
                    outputStack.grow(1);
                }
            }
            
            // Уменьшаем топливо
            if (fuelStack.getItem() == com.sanbait.luxsystem.ModItems.LIQUID_LUX_BUCKET.get()) {
                // Если это ведро - возвращаем пустое ведро
                entity.fuelSlot.setStackInSlot(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BUCKET));
            } else {
                // Кристалл - просто уменьшаем
                fuelStack.shrink(1);
                if (fuelStack.isEmpty()) {
                    entity.fuelSlot.setStackInSlot(0, ItemStack.EMPTY);
                }
            }
            
            // Сбрасываем зарядку
            entity.chargingItem = ItemStack.EMPTY;
            entity.luxCharged = 0;
            entity.luxToChargeTotal = 0;
            entity.setChanged();
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
