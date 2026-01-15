package com.sanbait.shadowgrid.mixin;

import com.sanbait.shadowgrid.world.BiomeGridConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeSource.class)
public class MixinBiomeSource {

    @Inject(method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;", at = @At("HEAD"), cancellable = true)
    private void onGetNoiseBiome(int x, int y, int z, net.minecraft.world.level.biome.Climate.Sampler sampler,
            CallbackInfoReturnable<Holder<Biome>> cir) {

        // Проверка: мы в Overworld?
        // BiomeSource не знает про Level, но мы используем BiomeLevelContext
        // Или проверяем, что это не Nether/End по косвенным признакам (сид?)
        // В данном случае, BiomeGridConfig управляет этим.

        try {
            // Convert quart coordinates to block coordinates
            int blockX = x << 2;
            int blockZ = z << 2;

            // Calculate sector coordinates
            int sectorSize = BiomeGridConfig.SECTOR_SIZE;
            int halfSize = sectorSize / 2;
            int sectorX = Math.floorDiv(blockX + halfSize, sectorSize);
            int sectorZ = Math.floorDiv(blockZ + halfSize, sectorSize);

            long worldSeed = BiomeGridConfig.currentWorldSeed;
            if (worldSeed == 0L) {
                // Try to get seed from server
                try {
                    if (net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer() != null) {
                        worldSeed = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer().getWorldData()
                                .worldGenOptions().seed();
                        BiomeGridConfig.currentWorldSeed = worldSeed;
                    }
                } catch (Exception ex) {
                }

                if (worldSeed == 0L)
                    return;
            }

            // Generate biome
            ResourceKey<Biome> biomeKey = BiomeGridConfig.generateRandomBiome(sectorX, sectorZ, worldSeed); // <>

            // Cache lookup
            Holder<Biome> cachedBiome = BiomeGridConfig.getBiomeFromCache(biomeKey);
            if (cachedBiome != null) {
                cir.setReturnValue(cachedBiome);
                return;
            }

            // Registry lookup (Fallback)
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
                        } catch (Exception e) {
                        }
                    }
                }
            } catch (Exception e) {
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
