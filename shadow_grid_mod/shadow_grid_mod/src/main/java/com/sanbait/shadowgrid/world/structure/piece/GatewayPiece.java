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
        // Bounding box for 3x3x5 structure (platform + pillars + frame)
        // Структура: 3x3 платформа, колонны 4 блока высотой, рамка на высоте 4
        // Уменьшено до 3x3 чтобы поместиться в один чанк (чунк = 16x16 блоков)
        super(ModStructures.GATEWAY_PIECE.get(), 0, new BoundingBox(
                pos.getX() - 1, pos.getY(), pos.getZ() - 1,
                pos.getX() + 1, pos.getY() + 4, pos.getZ() + 1));
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
        // Place the Gateway Block as a RUINED PORTAL (разрушенный портал)
        // Используем Calcite (мрамор) и другие блоки для заметной структуры
        // Все блоки неразрушимы

        // Используем this.boundingBox для определения позиций (не pos из параметра!)
        // Используем this.boundingBox для определения позиций (не pos из параметра!)
        // НО: Мы должны игнорировать дефолтное "центрование" Jigsaw и сдвинуть
        // структуру К СТЕНЕ.

        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;
        int modX = Math.floorMod(chunkX, 32);
        int modZ = Math.floorMod(chunkZ, 32);

        int surfaceY = this.boundingBox.minY();
        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();

        // По умолчанию ставим по центру (на всякий случай)
        int startX = chunkMinX + 7;
        int startZ = chunkMinZ + 7;

        // Если это вертикальная стена (mod 15) -> сдвигаем вправо на x+11 (3 блока от
        // края 16)
        if (modX == 15) {
            startX = chunkMinX + 11;
            startZ = chunkMinZ + 7;
        }
        // Если это ВНЕШНЯЯ сторона (mod 16) -> сдвигаем влево на x+2 (2 блока от края
        // 0)
        else if (modX == 16) {
            startX = chunkMinX + 2;
            startZ = chunkMinZ + 7;
        }

        // Если это горизонтальная стена (mod 15) -> сдвигаем вниз на z+11
        else if (modZ == 15) {
            startX = chunkMinX + 7;
            startZ = chunkMinZ + 11;
        }
        // Внешняя сторона (mod 16) -> сдвигаем вверх на z+2
        else if (modZ == 16) {
            startX = chunkMinX + 7;
            startZ = chunkMinZ + 2;
        }

        // Обновляем startX/startZ для цикла генерации (переопределяем локальные
        // переменные)
        // Внимание: мы не меняем this.boundingBox, так как он final-ish в контексте
        // генерации,
        // но мы используем вычисленные координаты для setBlock.

        // Игнорируем проверку box.intersects, так как мы смещаем структуру вручную
        // if (!box.intersects(this.boundingBox)) return;

        // Блоки для структуры (мраморный вид)
        BlockState calcite = net.minecraft.world.level.block.Blocks.CALCITE.defaultBlockState();
        BlockState gateway = ModBlocks.GATEWAY.get().defaultBlockState();
        BlockState deepslateBricks = net.minecraft.world.level.block.Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState deepslateTiles = net.minecraft.world.level.block.Blocks.DEEPSLATE_TILES.defaultBlockState();

        // Платформа 3x3 - ставим ЖЕЛЕЗНЫЕ БЛОКИ (для маяка)
        BlockState ironBlock = net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState();
        BlockState beacon = net.minecraft.world.level.block.Blocks.BEACON.defaultBlockState();

        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                BlockPos platformPos = new BlockPos(startX + x, surfaceY, startZ + z);
                if (box.isInside(platformPos)) {
                    // Основание из железа для работы маяка
                    level.setBlock(platformPos, ironBlock, 2);
                }
            }
        }

        // Колонны по углам (4 блока высотой) - только 4 угла 3x3 платформы
        int[] cornersX = { 0, 2, 0, 2 };
        int[] cornersZ = { 0, 0, 2, 2 };

        for (int i = 0; i < 4; i++) {
            BlockPos cornerBase = new BlockPos(startX + cornersX[i], surfaceY, startZ + cornersZ[i]);

            // Колонна из Calcite
            for (int y = 1; y <= 4; y++) {
                BlockPos pillarPos = cornerBase.above(y);
                if (box.isInside(pillarPos)) {
                    if (y == 4) {
                        // Верх колонны - Deepslate Tiles
                        level.setBlock(pillarPos, deepslateTiles, 2);
                    } else {
                        level.setBlock(pillarPos, calcite, 2);
                    }
                }
            }
        }

        // Верхняя рамка между колоннами (на высоте 4) - для 3x3
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                if ((x == 0 || x == 2 || z == 0 || z == 2) && !(x == 0 && z == 0) && !(x == 2 && z == 0) &&
                        !(x == 0 && z == 2) && !(x == 2 && z == 2)) {
                    BlockPos framePos = new BlockPos(startX + x, surfaceY + 4, startZ + z);
                    if (box.isInside(framePos)) {
                        level.setBlock(framePos, deepslateTiles, 2);
                    }
                }
            }
        }

        // Центральный блок - НАСТОЯЩИЙ МАЯК (на высоте 1, над железом)
        BlockPos centerPos = new BlockPos(startX + 1, surfaceY + 1, startZ + 1);
        if (box.isInside(centerPos)) {
            level.setBlock(centerPos, beacon, 2);

            // Над маяком воздух (для луча) - очищаем до высоты +20
            for (int k = 1; k <= 20; k++) {
                BlockPos airPos = centerPos.above(k);
                if (box.isInside(airPos)) {
                    level.setBlock(airPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                }
            }

            // ВЕРНЕМ наш GatewayBlock для функционала клика?
            // Нет, клик по маяку откроет GUI маяка.
            // Нам нужно чтобы клик открывал GUI Grid.
            // Поэтому поставим GatewayBlock НАД маяком? Нет, перекроет луч.
            // Поставим GatewayBlock ВМЕСТО маяка, но заставим его светить?
            // User: "ГДЕ ЛУЧИ?!".
            // Vanilla Beacon - это самый надежный способ.
            // Но мы теряем функционал клика (открытие сектора).
            // РЕШЕНИЕ: Ставим GatewayBlock РЯДОМ (например, в рамку).
            // Или ставим GatewayBlock ПОД маяком (вместо железа по центру)?
            // Маяк требует 3x3 базу. Центр базы может быть любым (iron).

            // ЛУЧШЕЕ РЕШЕНИЕ:
            // Ставим GatewayBlock В РАМКУ (например вместо одной из колонн или в центре
            // рамки наверху).
            // Пусть GatewayBlock будет НА ВЕРХУ (y+4, center).
            // Маяк бьет сквозь него?
            // Если GatewayBlock "не полный", то да.
            // В ModBlocks мы поставили noOcclusion().
            // Значит луч пройдет.

            BlockPos topCenter = new BlockPos(startX + 1, surfaceY + 4, startZ + 1);
            if (box.isInside(topCenter)) {
                level.setBlock(topCenter, ModBlocks.GATEWAY.get().defaultBlockState(), 2);
            }
        }
    }
}
