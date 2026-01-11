package com.sanbait.luxsystem.client;

import com.sanbait.luxsystem.LuxSystem;
import com.sanbait.luxsystem.blocks.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = LuxSystem.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.LUX_EXTRACTOR_MENU.get(), LuxExtractorScreen::new);
            MenuScreens.register(ModMenuTypes.LUX_CONDENSER_MENU.get(), LuxCondenserScreen::new);
        });
    }
}
