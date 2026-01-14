package com.sanbait.shadowgrid.mixin;

import com.sanbait.shadowgrid.world.BiomeGridConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiNoiseBiomeSource.class)
public class MixinMultiNoiseBiomeSource {

    // Static block - runs when class is loaded (BEFORE constructor)
    static {
        System.err.println("[ShadowGrid] =========================================");
        System.err.println("[ShadowGrid] ✓ MixinMultiNoiseBiomeSource CLASS LOADED!");
        System.err.println("[ShadowGrid] =========================================");
    }

    // Constructor to verify mixin is being applied
    public MixinMultiNoiseBiomeSource() {
        // Log once when mixin is applied - THIS SHOULD APPEAR IN LOGS
        System.err.println("[ShadowGrid] =========================================");
        System.err.println("[ShadowGrid] ✓ MixinMultiNoiseBiomeSource CONSTRUCTOR CALLED!");
        System.err.println("[ShadowGrid] =========================================");
    }

    @Inject(method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;", at = @At("HEAD"), cancellable = true)
    private void onGetNoiseBiome(int x, int y, int z, net.minecraft.world.level.biome.Climate.Sampler sampler,
            CallbackInfoReturnable<Holder<Biome>> cir) {
        // Log first successful call (once per world generation)
        if (!com.sanbait.shadowgrid.world.BiomeGridConfig.mixinCalled) {
            System.err.println("[ShadowGrid] =========================================");
            System.err.println("[ShadowGrid] ✓✓✓ MixinMultiNoiseBiomeSource.onGetNoiseBiome WORKING! x=" + x + " z=" + z);
            System.err.println("[ShadowGrid] =========================================");
            com.sanbait.shadowgrid.world.BiomeGridConfig.mixinCalled = true;
        }
        try {
            // Convert quart coordinates to block coordinates
            int blockX = x << 2; // x * 4
            int blockZ = z << 2; // z * 4

            // Calculate sector coordinates
            int sectorSize = BiomeGridConfig.SECTOR_SIZE;
            int halfSize = sectorSize / 2;
            int sectorX = Math.floorDiv(blockX + halfSize, sectorSize);
            int sectorZ = Math.floorDiv(blockZ + halfSize, sectorSize);

            // Use seed directly for deterministic biome generation
            long worldSeed = BiomeGridConfig.currentWorldSeed;
            if (worldSeed == 0L) {
                System.err.println("[ShadowGrid] ERROR: worldSeed is 0! Biome generation will not work!");
                return;
            }
            
            // DEBUG: Log first few calls to verify mixin is working
            if (sectorX == 0 && sectorZ == 0 && Math.random() < 0.01) {
                System.out.println("[ShadowGrid] Biome mixin called for sector " + sectorX + ":" + sectorZ);
            }

            // Generate biome deterministically based on seed and sector coordinates
            ResourceKey<Biome> biomeKey = BiomeGridConfig.generateRandomBiome(sectorX, sectorZ, worldSeed);

            // Try to get from cache first
            Holder<Biome> cachedBiome = BiomeGridConfig.getBiomeFromCache(biomeKey);
            if (cachedBiome != null) {
                cir.setReturnValue(cachedBiome);
                return;
            }
            
            // If cache is empty, try to fill it from available sources
            // Try BiomeLevelContext first (most reliable during generation)
            net.minecraft.server.level.ServerLevel level = com.sanbait.shadowgrid.world.BiomeLevelContext.getCurrentLevel();
            if (level != null) {
                Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
                BiomeGridConfig.setCachedRegistry(biomeRegistry);
                try {
                    Holder<Biome> biomeHolder = biomeRegistry.getHolderOrThrow(biomeKey);
                    BiomeGridConfig.cacheBiome(biomeKey, biomeHolder);
                    cir.setReturnValue(biomeHolder);
                    return;
                } catch (Exception e) {
                    // Biome not in registry, fall back to vanilla
                }
            }
            
            // Try cached registry (filled by LevelEvent.Load)
            Registry<Biome> cachedReg = BiomeGridConfig.getCachedRegistry();
            if (cachedReg != null) {
                try {
                    Holder<Biome> biomeHolder = cachedReg.getHolderOrThrow(biomeKey);
                    BiomeGridConfig.cacheBiome(biomeKey, biomeHolder);
                    cir.setReturnValue(biomeHolder);
                    return;
                } catch (Exception e) {
                    // Biome not found
                }
            }
            
            // FINAL FALLBACK: Try to get registry from Forge server hooks
            // This ensures we ALWAYS have a way to get biomes
            try {
                net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
                if (server != null) {
                    net.minecraft.server.level.ServerLevel overworld = server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
                    if (overworld != null) {
                        Registry<Biome> biomeRegistry = overworld.registryAccess().registryOrThrow(Registries.BIOME);
                        BiomeGridConfig.setCachedRegistry(biomeRegistry);
                        try {
                            Holder<Biome> biomeHolder = biomeRegistry.getHolderOrThrow(biomeKey);
                            BiomeGridConfig.cacheBiome(biomeKey, biomeHolder);
                            cir.setReturnValue(biomeHolder);
                            return;
                        } catch (Exception e) {
                            // Biome not in registry
                        }
                    }
                }
            } catch (Exception e) {
                // Server not available - this should NEVER happen during world generation
                // If it does, we fall back to vanilla (which is acceptable)
            }
        } catch (Exception e) {
            // Fall back to vanilla behavior silently
        }
    }
}
