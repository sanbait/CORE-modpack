package com.sanbait.shadowgrid.mixin;

import com.sanbait.shadowgrid.world.BiomeGridConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiNoiseBiomeSource.class)
public class MixinMultiNoiseBiomeSource {

    static {
        System.err.println("[ShadowGrid] ========================================");
        System.err.println("[ShadowGrid] ✓ MixinMultiNoiseBiomeSource CLASS LOADED!");
        System.err.println("[ShadowGrid] ========================================");
    }

    @Inject(method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;", at = @At("HEAD"), cancellable = true)
    private void onGetNoiseBiome(int x, int y, int z, net.minecraft.world.level.biome.Climate.Sampler sampler,
            CallbackInfoReturnable<Holder<Biome>> cir) {
        if (!BiomeGridConfig.mixinCalled) {
            System.err.println("[ShadowGrid] ========================================");
            System.err.println("[ShadowGrid] ✓✓✓ MixinMultiNoiseBiomeSource.onGetNoiseBiome WORKING!");
            System.err.println("[ShadowGrid] ========================================");
            BiomeGridConfig.mixinCalled = true;
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
                // Try to get seed from server if 0
                try {
                    if (net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer() != null) {
                        worldSeed = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer().getWorldData()
                                .worldGenOptions().seed();
                        BiomeGridConfig.currentWorldSeed = worldSeed;
                        System.out.println("[ShadowGrid] Retrieved seed from ServerLifecycleHooks: " + worldSeed);
                    }
                } catch (Exception ex) {
                }

                if (worldSeed == 0L)
                    return; // Still 0, cannot generate
            }

            // Generate biome deterministically based on seed and sector coordinates
            ResourceKey<Biome> biomeKey = BiomeGridConfig.generateRandomBiome(sectorX, sectorZ, worldSeed);

            // DEBUG LOGGING
            if (Math.random() < 0.001) {
                System.out.println(
                        "[ShadowGrid] Generating for " + sectorX + "," + sectorZ + " -> " + biomeKey.location());
            }

            // Try to get from cache first
            Holder<Biome> cachedBiome = BiomeGridConfig.getBiomeFromCache(biomeKey);
            if (cachedBiome != null) {
                cir.setReturnValue(cachedBiome);
                return;
            }

            // FINAL FALLBACK: Try to get registry from Forge server hooks
            try {
                net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks
                        .getCurrentServer();
                if (server != null) {
                    net.minecraft.server.level.ServerLevel overworld = server
                            .getLevel(net.minecraft.world.level.Level.OVERWORLD);
                    if (overworld != null) {
                        Registry<Biome> biomeRegistry = overworld.registryAccess().registryOrThrow(Registries.BIOME);
                        BiomeGridConfig.setCachedRegistry(biomeRegistry);
                        try {
                            Holder<Biome> biomeHolder = biomeRegistry.getHolderOrThrow(biomeKey);
                            BiomeGridConfig.cacheBiome(biomeKey, biomeHolder);
                            cir.setReturnValue(biomeHolder);
                            return;
                        } catch (Exception e) {
                            System.err.println("[ShadowGrid] Biome not found in registry: " + biomeKey.location());
                        }
                    } else {
                        System.err.println("[ShadowGrid] Overworld level is null!");
                    }
                } else {
                    // System.err.println("[ShadowGrid] Server is null!");
                    // This happens frequently on worker threads, don't spam
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
