package com.sanbait.luxsystem.compat;

import com.sanbait.luxsystem.ModFluids;
import com.sanbait.luxsystem.capabilities.ILuxStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

/**
 * Обработчик для интеграции переработки Lux предметов через Create станки
 * 
 * Механика:
 * - Перехватывает обработку предметов в Create станках
 * - Извлекает Lux из предметов через Capability
 * - Добавляет Liquid Lux в результат обработки
 * 
 * Работает вместе с KubeJS рецептами для полной интеграции
 */
@Mod.EventBusSubscriber(modid = "luxsystem", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CreateLuxRecyclingHandler {
    
    private static final boolean CREATE_LOADED = ModList.get().isLoaded("create");
    
    /**
     * Получает количество Lux из предмета
     */
    public static int getLuxFromItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        
        return stack.getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP)
            .map(ILuxStorage::getLuxStored)
            .orElse(0);
    }
    
    /**
     * Извлекает весь Lux из предмета
     */
    public static int extractAllLux(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        
        return stack.getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP)
            .map(cap -> {
                int lux = cap.getLuxStored();
                if (lux > 0) {
                    cap.extractLux(lux, false);
                    // Синхронизируем NBT
                    stack.getOrCreateTag().putInt("LuxStored", 0);
                }
                return lux;
            })
            .orElse(0);
    }
    
    /**
     * Конвертирует Lux в Liquid Lux (mB)
     * Соотношение: 1 Lux = 1 mB
     */
    public static FluidStack convertLuxToLiquid(int luxAmount) {
        if (luxAmount <= 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(ModFluids.LIQUID_LUX_SOURCE.get(), luxAmount);
    }
    
    /**
     * Проверяет, можно ли переработать предмет
     */
    public static boolean canRecycle(ItemStack stack) {
        return getLuxFromItem(stack) > 0;
    }
    
    // Примечание: Для полной интеграции с Create нужно подписаться на события Create
    // Например, на события обработки в Mixing Basin, Blasting и т.д.
    // Но Create не предоставляет прямых событий для этого в Forge API
    
    // Альтернатива: Использовать KubeJS рецепты с NBT проверками
    // (см. create_lux_recycling.js)
}
