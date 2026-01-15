# Shadow Grid Mod - Documentation

## Overview

This mod implements a grid-based progression system where the Minecraft world is divided into 256x256 block sectors. Players start in a central safe zone and must unlock adjacent sectors using resources (Lux Crystals).

---

## Features

### 1. Global Grid System

- **Description**: The Overworld is mathematically divided into square sectors of 256x256 blocks.
- **Logic**:
  - Coordinate `0,0` is the center of the initial sector (Sector `0:0`).
  - Borders are strictly aligned to chunk boundaries (16 chunks per sector).
- **Technical**:
  - Defined in `BiomeGridConfig.SECTOR_SIZE = 256`.
  - Sector coordinates are calculated as `floorDiv((blockPos + HALF_SIZE) / SECTOR_SIZE)`.

### 2. Sector Locking & Persistence

- **Description**: All sectors except the starting one (`0:0`) are initially "Locked". Locked sectors cannot be entered or seen.
- **Logic**:
  - Server maintains a list of unlocked sector IDs (e.g., "0:0", "1:0").
  - Data is saved to the world file (`shadow_grid_data.dat`).
  - Synced to all connected clients on join and upon any update.
- **Technical**:
  - `GridSavedData` (extends `SavedData`) handles server-side storage and NBT serialization.
  - `PacketSyncGrid` sends the `Set<String>` of unlocked sectors to `ClientGridData` on the client.

### 3. Proximity Unlock System

- **Description**: Players can unlock adjacent sectors by approaching the border and paying a cost.
- **Logic**:
  - When a player is within **8 blocks** of a locked sector's border, an Action Bar prompt appears: `[Sneak + Right Click] Unlock...`.
  - Holding `Shift` (Sneak) + `Right Click` (on air, block, or with item) triggers the unlock.
  - Cost scales with progress: 10 -> 50 -> 100 -> 200... Lux Crystals.
- **Technical**:
  - `BorderUnlockHandler`:
    - `PlayerTickEvent`: Checks distance to nearest border every 10 ticks.
    - `PlayerInteractEvent` (RightClickItem/Block/Empty): Handles the unlock trigger.
    - Deducts items from inventory and calls `GridSavedData.unlockSector()`.

### 4. Chunk Protection (Client Side)

- **Description**: Chunks belonging to locked sectors are completely prevented from loading or rendering on the client.
- **Logic**:
  - Before a chunk is created or processed by the client, the mod checks if its coordinates belong to an unlocked sector.
  - If locked, the chunk source returns `null` or an empty chunk.
- **Technical**:
  - `MixinClientChunkCache` injects into `getChunk`.
  - Queries `ClientGridData.isChunkUnlocked(x, z)`.
  - Effectively masks the world beyond the unlocked territory.

### 5. Visual Barriers (Particle Walls)

- **Description**: A visual boundary made of particles appears at the edges between unlocked and locked sectors.
- **Logic**:
  - The client scans generic range around the player.
  - Generates particles at `y` levels around the player if `x` or `z` aligns with a sector border AND the adjacent sector is locked.
- **Technical**:
  - `ParticleBorderHandler` subscribes to `RenderLevelStageEvent`.
  - Uses `ClientGridData` to determine where walls should be drawn.

### 6. Void Fog (Occlusion)

- **Description**: Dense black fog covers locked areas to hide the void/unloaded chunks.
- **Logic**:
  - If a player somehow enters a locked area (or looks into it), the fog density is set to maximum (100% black).
- **Technical**:
  - `FogHandler` subscribes to `ViewportEvent.RenderFog` and `ComputeFogColor`.
  - Checks `ClientGridData.isSectorUnlocked` for the camera position.

### 7. Biome Overrides (Structure Integ.)

- **Description**: Biomes are fixed per sector to ensure logical progression and prevent abrupt biome cuts mid-structure (legacy/planned feature).
- **Logic**:
  - The mod intercepts biome generation to force a single biome ID for the entire 256x256 area.
- **Technical**:
  - `BiomeGridConfig` stores the biome map.
  - (Note: Implementation details vary based on active Mixin/Datapack approach).
