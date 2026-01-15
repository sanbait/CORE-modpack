package com.sanbait.shadowgrid.registry;

import com.sanbait.shadowgrid.ShadowGridMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

public class ModStructures {

        // Structure system disabled - using proximity-based unlock instead
        public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister
                        .create(Registries.STRUCTURE_TYPE, ShadowGridMod.MODID);
        public static final DeferredRegister<StructurePlacementType<?>> PLACEMENT_TYPES = DeferredRegister
                        .create(Registries.STRUCTURE_PLACEMENT, ShadowGridMod.MODID);
        public static final DeferredRegister<StructurePieceType> PIECE_TYPES = DeferredRegister
                        .create(Registries.STRUCTURE_PIECE, ShadowGridMod.MODID);

        // Gateway structures removed - borders now use proximity unlock
        // See: BorderUnlockHandler.java

        public static void register(IEventBus eventBus) {
                STRUCTURE_TYPES.register(eventBus);
                PLACEMENT_TYPES.register(eventBus);
                PIECE_TYPES.register(eventBus);
        }
}
