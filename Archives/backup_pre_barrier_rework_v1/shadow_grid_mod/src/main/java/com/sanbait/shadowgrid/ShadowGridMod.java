package com.sanbait.shadowgrid;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ShadowGridMod.MODID)
public class ShadowGridMod {
    public static final String MODID = "shadowgrid";

    public ShadowGridMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register Network
        com.sanbait.shadowgrid.network.ShadowNetwork.register();

        // Register Content
        com.sanbait.shadowgrid.registry.ModBlocks.register(modEventBus);
        com.sanbait.shadowgrid.registry.ModStructures.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public void onRegisterCommands(net.minecraftforge.event.RegisterCommandsEvent event) {
        com.sanbait.shadowgrid.command.ShadowGridCommands.register(event.getDispatcher());
    }
}
