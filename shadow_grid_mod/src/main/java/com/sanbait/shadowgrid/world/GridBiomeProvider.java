package com.sanbait.shadowgrid.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class GridBiomeProvider extends BiomeSource {
    // Simple CODEC - we don't need serialization for runtime replacement
    public static final Codec<GridBiomeProvider> CODEC = Codec.unit(() -> {
        throw new UnsupportedOperationException("GridBiomeProvider should not be serialized");
    });

    private final long seed;
    private final List<Holder<Biome>> biomes;

    public GridBiomeProvider(long seed, List<Holder<Biome>> biomes) {
        // BiomeSource constructor takes HolderSet<Biome>
        super(net.minecraft.core.HolderSet.direct(biomes.toArray(new Holder[0])));
        this.seed = seed;
        this.biomes = new ArrayList<>(biomes);
        System.err.println("[ShadowGrid] =========================================");
        System.err.println("[ShadowGrid] ✓ GridBiomeProvider created! Seed: " + seed + ", Biomes: " + biomes.size());
        System.err.println("[ShadowGrid] =========================================");
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        // Координаты биомов в 4 раза меньше координат блоков (quart coordinates)
        int blockX = x << 2; // x * 4
        int blockZ = z << 2; // z * 4
        
        // Определяем квадрат (сектор)
        int sectorSize = BiomeGridConfig.SECTOR_SIZE;
        int halfSize = sectorSize / 2;
        int sectorX = Math.floorDiv(blockX + halfSize, sectorSize);
        int sectorZ = Math.floorDiv(blockZ + halfSize, sectorSize);
        
        // Генерируем детерминированный биом для сектора
        ResourceKey<Biome> biomeKey = BiomeGridConfig.generateRandomBiome(sectorX, sectorZ, seed);
        
        // Ищем биом в списке доступных
        for (Holder<Biome> biomeHolder : biomes) {
            if (biomeHolder.is(biomeKey)) {
                return biomeHolder;
            }
        }
        
        // Если не нашли, возвращаем первый доступный
        if (!biomes.isEmpty()) {
            return biomes.get(0);
        }
        
        // Fallback - не должно случиться
        return null;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return biomes.stream();
    }
}
