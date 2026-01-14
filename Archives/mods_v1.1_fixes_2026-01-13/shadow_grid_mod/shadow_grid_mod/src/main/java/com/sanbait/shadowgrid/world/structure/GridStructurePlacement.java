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
        // We want gateways ONLY on the borders of Sector 0:0 (starting zone)
        // NOT on all borders of all sectors!

        // Sector 0:0 borders are at:
        // East: chunkX = 16, chunkZ = 0 (and nearby)
        // West: chunkX = -16, chunkZ = 0 (and nearby)
        // South: chunkX = 0, chunkZ = 16 (and nearby)
        // North: chunkX = 0, chunkZ = -16 (and nearby)

        // Генерируем маяки НА КАЖДОЙ границе каждого сектора
        // Сектор 32x32 чанка. Границы проходят через каждые 32 чанка.
        // Центр стены - это середина границы (offset 16)

        // У нас есть 2 типа стен:
        // 1. Вертикальные (вдоль Z): X % 32 == 16, Z % 32 == 0 (центр)
        // 2. Горизонтальные (вдоль X): Z % 32 == 16, X % 32 == 0 (центр)

        // Используем Math.floorMod для корректной работы с отрицательными координатами
        int modX = Math.floorMod(chunkX, 32);
        int modZ = Math.floorMod(chunkZ, 32);

        // Маяк на вертикальной стене (Восток/Запад)
        // 15 = Внутренняя сторона (Восток сектора)
        // 16 = Внешняя сторона (Запад следующего сектора)
        boolean isVerticalWall = (modX == 15 || modX == 16) && modZ == 0;

        // Маяк на горизонтальной стене (Север/Юг)
        boolean isHorizontalWall = (modZ == 15 || modZ == 16) && modX == 0;

        return isVerticalWall || isHorizontalWall;

    }

    @Override
    public StructurePlacementType<?> type() {
        return ModStructures.GRID_PLACEMENT.get();
    }
}
