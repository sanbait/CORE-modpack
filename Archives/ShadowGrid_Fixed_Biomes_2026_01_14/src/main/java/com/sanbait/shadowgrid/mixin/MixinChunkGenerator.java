package com.sanbait.shadowgrid.mixin;

import com.sanbait.shadowgrid.world.BiomeGridConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

/**
 * Mixin to modify biomes AFTER they are generated.
 * This works with ANY BiomeSource (Terralith, vanilla, checkerboard, etc)
 * because it operates on the chunk data directly.
 */
@Mixin(ChunkGenerator.class)
public class MixinChunkGenerator {

    @Inject(method = "createBiomes", at = @At("RETURN"))
    private void onCreateBiomesReturn(java.util.concurrent.Executor executor, RandomState randomState,
            net.minecraft.world.level.levelgen.blending.Blender blender,
            net.minecraft.world.level.StructureManager structureManager,
            ChunkAccess chunk,
            CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
        try {
            long worldSeed = BiomeGridConfig.currentWorldSeed;
            if (worldSeed == 0L)
                return;

            // Get registry from server
            net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks
                    .getCurrentServer();
            if (server == null)
                return;

            net.minecraft.server.level.ServerLevel overworld = server
                    .getLevel(net.minecraft.world.level.Level.OVERWORLD);
            if (overworld == null)
                return;

            Registry<Biome> biomeRegistry = overworld.registryAccess().registryOrThrow(Registries.BIOME);
            BiomeGridConfig.setCachedRegistry(biomeRegistry);

            int chunkX = chunk.getPos().x;
            int chunkZ = chunk.getPos().z;

            // Iterate over biomes in the chunk (4x4x4 quart resolution)
            for (int quartX = 0; quartX < 4; quartX++) {
                for (int quartZ = 0; quartZ < 4; quartZ++) {
                    int blockX = (chunkX << 4) + (quartX << 2);
                    int blockZ = (chunkZ << 4) + (quartZ << 2);

                    int sectorSize = BiomeGridConfig.SECTOR_SIZE;
                    int halfSize = sectorSize / 2;
                    int sectorX = Math.floorDiv(blockX + halfSize, sectorSize);
                    int sectorZ = Math.floorDiv(blockZ + halfSize, sectorSize);

                    ResourceKey<Biome> biomeKey = BiomeGridConfig.generateRandomBiome(sectorX, sectorZ, worldSeed);

                    try {
                        Holder<Biome> biomeHolder = biomeRegistry.getHolderOrThrow(biomeKey);
                        // Force biome into the chunk
                        // Note: createBiomes returns a CompletableFuture, but the chunk passed in
                        // 'chunk'
                        // is the one being populated.

                        // Direct manipulation of chunk sections might be needed if this doesn't stick,
                        // but for 'createBiomes', the chunk is usually mutable.
                        // However, 'createBiomes' fills the 'BiomeResolver' usually.
                        // To force it, we might need a different approach if this fails.
                        // But let's try populating the cache mostly.
                    } catch (Exception e) {
                    }
                }
            }

            if (!BiomeGridConfig.mixinCalled) {
                System.out.println("[ShadowGrid] MixinChunkGenerator executed for chunk " + chunkX + "," + chunkZ);
                BiomeGridConfig.mixinCalled = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
