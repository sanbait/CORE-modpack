package com.sanbait.luxsystem;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
        public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
                        LuxSystem.MODID);

        // Жидкий Lux - блок жидкости
        public static final RegistryObject<LiquidBlock> LIQUID_LUX_BLOCK = BLOCKS.register("liquid_lux",
                        () -> new LiquidBlock(ModFluids.LIQUID_LUX_SOURCE,
                                        BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

        // Зарядник Lux - блок для быстрой зарядки предметов кристаллами
        public static final RegistryObject<Block> LUX_CHARGER = registerBlock("lux_charger",
                        () -> new com.sanbait.luxsystem.blocks.LuxChargerBlock(
                                        BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

        private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
                RegistryObject<T> toReturn = BLOCKS.register(name, block);
                registerBlockItem(name, toReturn);
                return toReturn;
        }

        private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
                return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        }
}
