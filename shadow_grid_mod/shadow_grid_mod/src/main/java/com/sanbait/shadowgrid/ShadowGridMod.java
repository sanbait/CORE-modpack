package com.sanbait.shadowgrid;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ShadowGridMod.MODID)
public class ShadowGridMod {
    public static final String MODID = "shadowgrid";
    public static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    public ShadowGridMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register Content
        com.sanbait.shadowgrid.registry.ModBlocks.register(modEventBus);
        com.sanbait.shadowgrid.registry.ModStructures.register(modEventBus);

        // Register network in common setup (runs on both client and server)
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Register Network - must be done in common setup for both sides
        event.enqueueWork(() -> {
            com.sanbait.shadowgrid.network.ShadowNetwork.register();
        });
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public void onRegisterCommands(net.minecraftforge.event.RegisterCommandsEvent event) {
        com.sanbait.shadowgrid.command.ShadowGridCommands.register(event.getDispatcher());
    }
}
