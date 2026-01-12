package com.sanbait.luxsystem;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(LuxSystem.MODID)
public class LuxSystem {
    public static final String MODID = "luxsystem";

    public LuxSystem() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register config
        LuxSystemConfig.register();

        // Register registries
        ModFluids.FLUIDS.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        com.sanbait.luxsystem.blocks.ModMenuTypes.MENUS.register(modEventBus);

        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(net.minecraftforge.event.BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == com.sanbait.nexuscore.NexusCore.NEXUS_TAB.getKey()) {
            event.accept(ModBlocks.LUX_EXTRACTOR);
            event.accept(ModBlocks.LUX_CONDENSER);
            event.accept(ModItems.LUX_PICKAXE);
            event.accept(ModItems.LUX_SWORD);
            event.accept(ModItems.LUX_HELMET);
            event.accept(ModItems.LUX_CHESTPLATE);
            event.accept(ModItems.LUX_LEGGINGS);
            event.accept(ModItems.LUX_BOOTS);
            event.accept(ModItems.LIQUID_LUX_BUCKET);
            event.accept(ModItems.LUX_CRYSTAL);
            event.accept(ModItems.CONCENTRATED_LUX_CRYSTAL);
            event.accept(ModItems.ANCIENT_LUX_VASE);
            event.accept(ModItems.ANCIENT_LUX_ORB);
            event.accept(ModItems.LUX_CRYSTAL_FRAGMENT);
            event.accept(ModItems.FOSSILIZED_LUX_AMBER);
            event.accept(ModItems.LUX_FILTER);
            event.accept(ModItems.LUX_CANISTER);
            event.accept(ModItems.LUX_CANISTER_FULL);
        }
    }
}
