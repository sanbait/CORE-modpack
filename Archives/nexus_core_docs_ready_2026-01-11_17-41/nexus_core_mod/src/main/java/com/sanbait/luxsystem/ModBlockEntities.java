package com.sanbait.luxsystem;

import com.sanbait.luxsystem.blocks.LuxCoreBlockEntity;
import com.sanbait.luxsystem.blocks.LuxExtractorBlockEntity;
import com.sanbait.luxsystem.blocks.LuxCondenserBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
        public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
                        .create(ForgeRegistries.BLOCK_ENTITY_TYPES, LuxSystem.MODID);

        public static final RegistryObject<BlockEntityType<LuxCoreBlockEntity>> LUX_CORE_BE = BLOCK_ENTITIES
                        .register("lux_core_be",
                                        () -> BlockEntityType.Builder
                                                        .of(LuxCoreBlockEntity::new, ModBlocks.LUX_CORE.get())
                                                        .build(null));

        public static final RegistryObject<BlockEntityType<LuxExtractorBlockEntity>> LUX_EXTRACTOR_BE = BLOCK_ENTITIES
                        .register("lux_extractor_be",
                                        () -> BlockEntityType.Builder
                                                        .of(LuxExtractorBlockEntity::new, ModBlocks.LUX_EXTRACTOR.get())
                                                        .build(null));

        public static final RegistryObject<BlockEntityType<LuxCondenserBlockEntity>> LUX_CONDENSER_BE = BLOCK_ENTITIES
                        .register("lux_condenser_be",
                                        () -> BlockEntityType.Builder
                                                        .of(LuxCondenserBlockEntity::new, ModBlocks.LUX_CONDENSER.get())
                                                        .build(null));
}
