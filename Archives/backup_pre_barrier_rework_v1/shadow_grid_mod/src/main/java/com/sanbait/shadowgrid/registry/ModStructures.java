package com.sanbait.shadowgrid.registry;

import com.sanbait.shadowgrid.ShadowGridMod;
import com.sanbait.shadowgrid.world.structure.GatewayStructure;
import com.sanbait.shadowgrid.world.structure.GridStructurePlacement;
import com.sanbait.shadowgrid.world.structure.piece.GatewayPiece;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModStructures {

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister
            .create(Registries.STRUCTURE_TYPE, ShadowGridMod.MODID);
    public static final DeferredRegister<StructurePlacementType<?>> PLACEMENT_TYPES = DeferredRegister
            .create(Registries.STRUCTURE_PLACEMENT, ShadowGridMod.MODID);
    public static final DeferredRegister<StructurePieceType> PIECE_TYPES = DeferredRegister
            .create(Registries.STRUCTURE_PIECE, ShadowGridMod.MODID);

    public static final RegistryObject<StructureType<GatewayStructure>> GATEWAY_STRUCTURE = STRUCTURE_TYPES
            .register("gateway", () -> () -> GatewayStructure.CODEC);

    public static final RegistryObject<StructurePlacementType<GridStructurePlacement>> GRID_PLACEMENT = PLACEMENT_TYPES
            .register("grid_placement", () -> () -> GridStructurePlacement.CODEC);

    public static final RegistryObject<StructurePieceType> GATEWAY_PIECE = PIECE_TYPES.register("gateway_piece",
            () -> GatewayPiece::new);

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
        PLACEMENT_TYPES.register(eventBus);
        PIECE_TYPES.register(eventBus);
    }
}
