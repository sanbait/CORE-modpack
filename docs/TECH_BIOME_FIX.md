# Biome Generation Fix - Technical Post-Mortem

## The Problem

Minecraft 1.20.1 introduces complex biome blending and multi-noise generation that makes standard `BiomeSource` mixins unreliable. Even when a custom `BiomeSource` returns a specific biome, the game's blending logic can override or smooth it out, destroying sharp sector borders.

## The Solution: "Nuclear" MixinChunkGenerator

Instead of fighting the `BiomeSource` logic, we implemented a mixin that operates **after** biome generation is technically "complete" but before the chunk is finalized.

### Key Components

1. **`MixinChunkGenerator.java`**:
    - Targets `ChunkGenerator.createBiomes` (at RETURN).
    - Iterates through every 4x4x4 quart in the generated chunk.
    - Calculates the sector for that quart.
    - Queries `BiomeGridConfig` for the deterministic biome for that sector.
    - **Forcefully overwrites** the biome in the valid `Registry<Biome>`.
    - This bypasses `TerraBlender`, vanilla blending, and any other noise functions.

2. **`mixins.shadowgrid.json`**:
    - Includes `MixinChunkGenerator`.
    - Keeps `MixinNetherPortalBlock` for portal management.
    - Excludes `MixinFrustum` (caused rendering artifacts/square text).

## Configuration

- **Sector Size**: 512 blocks.
- **Config File**: `config/shadowgrid/biomes.json` (defines weights).

## Verification

- Validated via in-game screenshot showing perfect 512x512 biome squares.
