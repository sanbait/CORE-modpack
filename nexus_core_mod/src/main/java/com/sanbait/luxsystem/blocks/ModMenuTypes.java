package com.sanbait.luxsystem.blocks;

import com.sanbait.luxsystem.LuxSystem;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES,
            LuxSystem.MODID);

    public static final RegistryObject<MenuType<LuxExtractorMenu>> LUX_EXTRACTOR_MENU = MENUS.register("lux_extractor",
            () -> IForgeMenuType.create(LuxExtractorMenu::new));

    public static final RegistryObject<MenuType<LuxCondenserMenu>> LUX_CONDENSER_MENU = MENUS.register("lux_condenser",
            () -> IForgeMenuType.create(LuxCondenserMenu::new));
}
