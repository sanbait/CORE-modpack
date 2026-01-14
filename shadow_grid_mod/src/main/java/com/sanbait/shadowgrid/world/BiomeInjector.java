package com.sanbait.shadowgrid.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.List;

@Mod.EventBusSubscriber(modid = "shadowgrid")
public class BiomeInjector {
    
    @SubscribeEvent
    public static void onWorldCreate(LevelEvent.CreateSpawnPosition event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (serverLevel.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
                try {
                    injectGridBiomeSource(serverLevel);
                } catch (Exception e) {
                    System.err.println("[ShadowGrid] ERROR: Failed to inject biome provider: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    private static void injectGridBiomeSource(ServerLevel level) throws Exception {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        
        if (generator instanceof NoiseBasedChunkGenerator noiseGen) {
            // Получаем все биомы из оригинального источника
            List<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>> originalBiomes = 
                new java.util.ArrayList<>();
            noiseGen.getBiomeSource().possibleBiomes().forEach(originalBiomes::add);
            
            // Создаем наш grid biome source
            GridBiomeProvider gridProvider = new GridBiomeProvider(level.getSeed(), originalBiomes);
            
            // Заменяем biome source через рефлексию
            // Пробуем разные варианты имени поля (MCP и obfuscated)
            Field biomeSourceField = null;
            String[] fieldNames = {"biomeSource", "f_226623_", "f_64317_"};
            for (String fieldName : fieldNames) {
                try {
                    biomeSourceField = NoiseBasedChunkGenerator.class.getDeclaredField(fieldName);
                    biomeSourceField.setAccessible(true);
                    break;
                } catch (NoSuchFieldException e) {
                    // Пробуем следующий вариант
                }
            }
            
            if (biomeSourceField == null) {
                throw new RuntimeException("Could not find biomeSource field in NoiseBasedChunkGenerator");
            }
            
            biomeSourceField.set(noiseGen, gridProvider);
            
            System.err.println("[ShadowGrid] =========================================");
            System.err.println("[ShadowGrid] ✓✓✓ Biome Grid успешно внедрён! Квадраты: 512x512");
            System.err.println("[ShadowGrid] =========================================");
        } else {
            System.err.println("[ShadowGrid] WARNING: ChunkGenerator is not NoiseBasedChunkGenerator: " + generator.getClass().getName());
        }
    }
}
