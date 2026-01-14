package com.sanbait.shadowgrid.mixin;

/**
 * This mixin is disabled - getNoiseBiome doesn't exist in base BiomeSource
 * Terralith likely uses MultiNoiseBiomeSource or its own implementation
 * We rely on MixinMultiNoiseBiomeSource to catch it
 */
public class MixinNoiseBasedBiomeSource {

    // Mixin disabled - see comment above
}
