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
        // Efficient sector size calculation
        int sectorSizeBlocks = com.sanbait.shadowgrid.world.BiomeGridConfig.SECTOR_SIZE;
        int chunksPerSector = sectorSizeBlocks >> 4; // / 16

        // Ensure valid size (must be at least 1 chunk)
        if (chunksPerSector < 1)
            chunksPerSector = 1;

        int halfChunks = chunksPerSector / 2;
        int borderInner = halfChunks - 1; // e.g., 3 for size 8
        int borderOuter = halfChunks; // e.g., 4 for size 8

        // Calculate position within sector
        int modX = Math.floorMod(chunkX, chunksPerSector);
        int modZ = Math.floorMod(chunkZ, chunksPerSector);

        // Gateway on Vertical Wall (East/West)
        // Must be at the border X (inner or outer chunk) AND center Z (chunk 0)
        boolean isVerticalWall = (modX == borderInner || modX == borderOuter) && modZ == 0;

        // Gateway on Horizontal Wall (North/South)
        // Must be center X (chunk 0) AND border Z (inner or outer chunk)
        boolean isHorizontalWall = (modZ == borderInner || modZ == borderOuter) && modX == 0;

        return isVerticalWall || isHorizontalWall;

    }

    @Override
    public StructurePlacementType<?> type() {
        return ModStructures.GRID_PLACEMENT.get();
    }
}
