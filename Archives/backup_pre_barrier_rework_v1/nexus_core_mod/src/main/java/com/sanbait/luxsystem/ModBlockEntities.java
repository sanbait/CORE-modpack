package com.sanbait.luxsystem;

import com.sanbait.luxsystem.blocks.LuxChargerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
        public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
                        .create(ForgeRegistries.BLOCK_ENTITY_TYPES, LuxSystem.MODID);

        public static final RegistryObject<BlockEntityType<LuxChargerBlockEntity>> LUX_CHARGER_BE = BLOCK_ENTITIES
                        .register("lux_charger_be",
                                        () -> BlockEntityType.Builder
                                                        .of(LuxChargerBlockEntity::new, ModBlocks.LUX_CHARGER.get())
                                                        .build(null));
}
