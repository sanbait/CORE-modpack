package com.sanbait.shadowgrid;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ShadowGridMod.MODID)
public class ShadowGridMod {
    public static final String MODID = "shadowgrid";

    public ShadowGridMod() {
        // Verify mixins are loaded
        System.err.println("[ShadowGrid] =========================================");
        System.err.println("[ShadowGrid] ShadowGridMod constructor called");
        System.err.println("[ShadowGrid] Checking if mixins are available...");
        try {
            Class.forName("com.sanbait.shadowgrid.mixin.MixinMultiNoiseBiomeSource");
            System.err.println("[ShadowGrid] ✓ MixinMultiNoiseBiomeSource class found!");
        } catch (ClassNotFoundException e) {
            System.err.println("[ShadowGrid] ✗ MixinMultiNoiseBiomeSource class NOT found!");
        }
        try {
            Class.forName("com.sanbait.shadowgrid.mixin.MixinCheckerboardBiomeSource");
            System.err.println("[ShadowGrid] ✓ MixinCheckerboardBiomeSource class found!");
        } catch (ClassNotFoundException e) {
            System.err.println("[ShadowGrid] ✗ MixinCheckerboardBiomeSource class NOT found!");
        }
        System.err.println("[ShadowGrid] =========================================");
        
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
            // Load biome config early to create default config file
            com.sanbait.shadowgrid.world.BiomeGridConfig.loadConfig();
        });
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public void onRegisterCommands(net.minecraftforge.event.RegisterCommandsEvent event) {
        com.sanbait.shadowgrid.command.ShadowGridCommands.register(event.getDispatcher());
    }
}
