package com.sanbait.shadowgrid.world.structure;

import com.mojang.serialization.Codec;
import com.sanbait.shadowgrid.registry.ModStructures;
import com.sanbait.shadowgrid.world.structure.piece.GatewayPiece;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

public class GatewayStructure extends Structure {

    public static final Codec<GatewayStructure> CODEC = simpleCodec(GatewayStructure::new);

    public GatewayStructure(Structure.StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        try {
            // Find ground height - используем центр чанка для более надежной генерации
            int x = context.chunkPos().getMinBlockX() + 8; // Центр чанка
            int z = context.chunkPos().getMinBlockZ() + 8; // Центр чанка
            
            int y = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                    context.heightAccessor(), context.randomState());
            
            // Проверяем что высота валидная
            if (y < context.heightAccessor().getMinBuildHeight() || y > context.heightAccessor().getMaxBuildHeight()) {
                return Optional.empty();
            }

            BlockPos pos = new BlockPos(x, y, z);

            return Optional.of(new GenerationStub(pos, builder -> generatePieces(builder, pos)));
        } catch (Exception e) {
            // Если ошибка при генерации - просто не генерируем структуру
            return Optional.empty();
        }
    }

    private void generatePieces(StructurePiecesBuilder builder, BlockPos pos) {
        builder.addPiece(new GatewayPiece(pos));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.GATEWAY_STRUCTURE.get();
    }
}
