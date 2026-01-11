package com.sanbait.luxsystem;

import com.sanbait.luxsystem.blocks.LuxCoreBlock;
import com.sanbait.luxsystem.blocks.LuxExtractorBlock;
import com.sanbait.luxsystem.blocks.LuxCondenserBlock;
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

        public static final RegistryObject<LiquidBlock> LIQUID_LUX_BLOCK = BLOCKS.register("liquid_lux",
                        () -> new LiquidBlock(ModFluids.LIQUID_LUX_SOURCE,
                                        BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

        public static final RegistryObject<Block> LUX_CORE = registerBlock("lux_core",
                        () -> new LuxCoreBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

        public static final RegistryObject<Block> LUX_EXTRACTOR = registerBlock("lux_extractor",
                        () -> new LuxExtractorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

        public static final RegistryObject<Block> LUX_CONDENSER = registerBlock("lux_condenser",
                        () -> new LuxCondenserBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

        private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
                RegistryObject<T> toReturn = BLOCKS.register(name, block);
                registerBlockItem(name, toReturn);
                return toReturn;
        }

        private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
                return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        }
}
