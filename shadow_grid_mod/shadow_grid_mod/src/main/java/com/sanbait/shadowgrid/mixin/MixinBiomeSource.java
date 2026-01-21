package com.sanbait.shadowgrid.mixin;

import com.sanbait.shadowgrid.world.BiomeGridConfig;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeSource.class)
public class MixinBiomeSource {

    @Inject(method = "getNoiseBiome", at = @At("HEAD"), cancellable = true)
    private void onGetNoiseBiome(int x, int y, int z, RandomState randomState,
            CallbackInfoReturnable<Holder<Biome>> cir) {
        // x, y, z are in Quart Coordinates (1 quart = 4 blocks)

        long worldSeed = BiomeGridConfig.currentWorldSeed;
        if (worldSeed == 0L) {
            // Not yet initialized
            return;
        }

        int blockX = x << 2; // Multiply by 4
        int blockZ = z << 2; // Multiply by 4

        // Strict Grid Calculation (no offsets)
        int sectorSize = BiomeGridConfig.SECTOR_SIZE;
        int sectorX = Math.floorDiv(blockX, sectorSize);
        int sectorZ = Math.floorDiv(blockZ, sectorSize);

        // Generate Biome Key
        // Note: For now we apply this to ALL dimensions using BiomeSource.
        // To restrict to Overworld, we would need to check more context,
        // but typically modpacks using Shadow Grid want this global or control via
        // dimensions.
        // Since we only have Overworld biomes in config, strange things might happen in
        // Nether if we force Overworld biomes.
        // Ideally we should check if BiomeGridConfig is active for this dimension.
        // But for this fix, let's assume global override or filter by biome validity
        // later.

        // Let's rely on generateRandomBiome returning a valid key.
        // It uses a seed.

        try {
            // Access cached registry
            // If registry is not available, we can't do much.
            // But getNoiseBiome is called heavily during WorldGen.

            // CRITICAL: We need the registry to get the Holder.
            // BiomeGridConfig caches it upon first chunk load in MixinChunkGenerator?
            // Or we need a better way to access the registry here.

            // Actually, the easiest way might be to ask the vanilla implementation for a
            // fallback?
            // But we want to OVERRIDE.

            // If we don't have the registry mapping, we skip.
            if (BiomeGridConfig.getCachedRegistry() != null) {
                var key = BiomeGridConfig.generateRandomBiome(sectorX, sectorZ, worldSeed);
                var holder = BiomeGridConfig.getBiomeFromCache(key);
                if (holder != null) {
                    cir.setReturnValue(holder);
                }
            }
        } catch (Exception e) {
            // Fallback to vanilla logic
        }
    }
}
