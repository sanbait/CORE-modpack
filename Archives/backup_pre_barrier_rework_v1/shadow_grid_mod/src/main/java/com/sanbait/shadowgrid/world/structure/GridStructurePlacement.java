package com.sanbait.shadowgrid.world.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sanbait.shadowgrid.registry.ModStructures;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;

public class GridStructurePlacement extends StructurePlacement {

    public static final Codec<GridStructurePlacement> CODEC = RecordCodecBuilder
            .create(instance -> placementCodec(instance).apply(instance, GridStructurePlacement::new));

    public GridStructurePlacement(Vec3i locateOffset, FrequencyReductionMethod frequencyReductionMethod,
            float frequency, int salt, Optional<ExclusionZone> exclusionZone) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState structState, int chunkX, int chunkZ) {
        // Grid Logic:
        // Sector Size = 512 blocks = 32 chunks.
        // We want gateways on the BORDERS.
        // The safe zone is centered at 0,0 (Range -256 to +256).
        // So borders are at X = +/- 256, +/- 768, etc.
        // 256 blocks = 16 chunks.

        // We want a Gateway exactly at the CENTER of the border edge?
        // Or just at the "Corner" where 4 sectors meet?
        // User said: "На середине каждой стороны границы стоит ... Пограничный Маяк"
        // (At the middle of each border side).

        // Side Centers:
        // East Border of Center Sector: X=256, Z=0.
        // 256 / 16 = Chunk 16.
        // So we want (16, 0), (-16, 0), (0, 16), (0, -16).

        // General formula:
        // Gateways should appear where:
        // (X % 32 == 16 AND Z % 32 == 0) <-- Vertical Borders
        // OR
        // (X % 32 == 0 AND Z % 32 == 16) <-- Horizontal Borders

        // Math adjustment for negative numbers (Java modulo can be negative)
        int xMod = Math.floorMod(chunkX, 32);
        int zMod = Math.floorMod(chunkZ, 32);

        boolean isVerticalBorder = (xMod == 16 && zMod == 0);
        boolean isHorizontalBorder = (xMod == 0 && zMod == 16);

        return isVerticalBorder || isHorizontalBorder;
    }

    @Override
    public StructurePlacementType<?> type() {
        return ModStructures.GRID_PLACEMENT.get();
    }
}
