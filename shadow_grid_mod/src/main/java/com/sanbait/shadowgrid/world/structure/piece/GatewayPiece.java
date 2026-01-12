package com.sanbait.shadowgrid.world.structure.piece;

import com.sanbait.shadowgrid.registry.ModBlocks;
import com.sanbait.shadowgrid.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class GatewayPiece extends StructurePiece {

    public GatewayPiece(BlockPos pos) {
        // Bounding box for 5 blocks high tower
        super(ModStructures.GATEWAY_PIECE.get(), 0, new BoundingBox(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX(), pos.getY() + 4, pos.getZ()));
        this.setOrientation(null);
    }

    public GatewayPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.GATEWAY_PIECE.get(), tag);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
        // Place the Gateway Block as a TOWER (5 blocks high)
        BlockPos basePos = new BlockPos(this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ());

        // Safety check to ensure we are in the generated chunk
        if (box.isInside(basePos)) {
            BlockState state = ModBlocks.GATEWAY.get().defaultBlockState();
            
            // Place 5 blocks vertically (tower)
            for (int y = 0; y < 5; y++) {
                BlockPos towerPos = basePos.above(y);
                if (box.isInside(towerPos)) {
                    level.setBlock(towerPos, state, 2);
                }
            }
        }
    }
}
