package com.sanbait.shadowgrid.registry;

import com.sanbait.shadowgrid.ShadowGridMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import com.sanbait.shadowgrid.world.block.GatewayBlock;

public class ModBlocks {
        public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
                        ShadowGridMod.MODID);
        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
                        ShadowGridMod.MODID);

        public static final RegistryObject<Block> GATEWAY = registerBlock("gateway",
                        () -> new GatewayBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                                        .strength(-1.0f, 3600000.0f)
                                        .requiresCorrectToolForDrops().noOcclusion()));

        private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
                RegistryObject<T> toReturn = BLOCKS.register(name, block);
                registerBlockItem(name, toReturn);
                return toReturn;
        }

        private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
                return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        }

        public static final DeferredRegister<net.minecraft.world.item.CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
                        .create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, ShadowGridMod.MODID);

        public static final RegistryObject<net.minecraft.world.item.CreativeModeTab> TAB = CREATIVE_MODE_TABS.register(
                        "tab",
                        () -> net.minecraft.world.item.CreativeModeTab.builder()
                                        .icon(() -> new net.minecraft.world.item.ItemStack(GATEWAY.get()))
                                        .title(net.minecraft.network.chat.Component
                                                        .translatable("itemGroup.shadowgrid"))
                                        .displayItems((pParameters, pOutput) -> {
                                                pOutput.accept(GATEWAY.get());
                                        }).build());

        public static void register(IEventBus eventBus) {
                BLOCKS.register(eventBus);
                ITEMS.register(eventBus);
                CREATIVE_MODE_TABS.register(eventBus);
        }
}
