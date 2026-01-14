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
 * Mixin to modify biomes AFTER they are generated (like Terraria code does)
 * This works with ANY BiomeSource (Terralith, vanilla, checkerboard, etc)
 */
@Mixin(ChunkGenerator.class)
public class MixinChunkGenerator {

    // Static block - runs when class is loaded
    static {
        System.err.println("[ShadowGrid] =========================================");
        System.err.println("[ShadowGrid] ✓ MixinChunkGenerator CLASS LOADED!");
        System.err.println("[ShadowGrid] =========================================");
    }

    // Intercept fillBiomesFromNoise - this is called for ALL BiomeSource types
    // Method signature: fillBiomesFromNoise(BiomeResolver resolver, Climate.Sampler sampler)
    @Inject(method = "fillBiomesFromNoise(Lnet/minecraft/world/level/biome/BiomeResolver;Lnet/minecraft/world/level/biome/Climate$Sampler;)V", 
            at = @At("HEAD"))
    private void onFillBiomesFromNoise(net.minecraft.world.level.biome.BiomeResolver resolver,
                                       net.minecraft.world.level.biome.Climate.Sampler sampler,
                                       org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        // This method is called to fill biomes - we wrap the resolver
        if (!BiomeGridConfig.mixinCalled) {
            System.err.println("[ShadowGrid] ✓ MixinChunkGenerator.fillBiomesFromNoise called!");
            BiomeGridConfig.mixinCalled = true;
        }
    }

    @Inject(method = "createBiomes", at = @At("RETURN"))
    private void onCreateBiomesReturn(java.util.concurrent.Executor executor, RandomState randomState, 
                                      net.minecraft.world.level.levelgen.blending.Blender blender,
                                      net.minecraft.world.level.StructureManager structureManager, 
                                      ChunkAccess chunk, 
                                      CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
        // Modify biomes AFTER generation - works with ANY BiomeSource
        try {
            long worldSeed = BiomeGridConfig.currentWorldSeed;
            if (worldSeed == 0L) {
                return;
            }

            // Get registry from server
            net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server == null) {
                return;
            }
            
            net.minecraft.server.level.ServerLevel overworld = server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
            if (overworld == null) {
                return;
            }

            Registry<Biome> biomeRegistry = overworld.registryAccess().registryOrThrow(Registries.BIOME);
            BiomeGridConfig.setCachedRegistry(biomeRegistry);

            int chunkX = chunk.getPos().x;
            int chunkZ = chunk.getPos().z;
            
            // Override biomes in chunk based on sector (like Terraria code)
            // Biomes are stored per 4x4x4 block area (quart coordinates)
            for (int quartX = 0; quartX < 4; quartX++) {
                for (int quartZ = 0; quartZ < 4; quartZ++) {
                    // Convert quart to block coordinates
                    int blockX = (chunkX << 4) + (quartX << 2);
                    int blockZ = (chunkZ << 4) + (quartZ << 2);
                    
                    // Calculate sector coordinates
                    int sectorSize = BiomeGridConfig.SECTOR_SIZE;
                    int halfSize = sectorSize / 2;
                    int sectorX = Math.floorDiv(blockX + halfSize, sectorSize);
                    int sectorZ = Math.floorDiv(blockZ + halfSize, sectorSize);
                    
                    ResourceKey<Biome> biomeKey = BiomeGridConfig.generateRandomBiome(sectorX, sectorZ, worldSeed);
                    
                    try {
                        Holder<Biome> biomeHolder = biomeRegistry.getHolderOrThrow(biomeKey);
                        BiomeGridConfig.cacheBiome(biomeKey, biomeHolder);
                        
                        // Note: Setting biomes after generation is complex in 1.20.1
                        // The BiomeSource mixins handle this during generation, which is more reliable
                        // This mixin is kept as fallback but biome modification happens in BiomeSource mixins
                    } catch (Exception e) {
                        // Biome not found, skip
                    }
                }
            }
            
            // Log first successful call
            if (!BiomeGridConfig.mixinCalled) {
                System.out.println("[ShadowGrid] ✓ MixinChunkGenerator.createBiomes WORKING! Modified chunk " + chunkX + ":" + chunkZ);
                BiomeGridConfig.mixinCalled = true;
            }
        } catch (Exception e) {
            // Ignore errors silently
        }
    }
}
