# Changelog

## [0.0.1] - 2026-01-12

### Added

- **Grid System:** World divided into 512x512 sectors. Central sector unlocked by default.
- **Occlusion:**
  - `ParticleBorderHandler` renders walls between unlocked and locked areas.
  - `FogHandler` renders thick black fog inside locked sectors.
- **Unlock System:**
  - `GatewayHandler` handles interaction with Gateway blocks.
  - Progressive cost system using `luxsystem:lux_crystal`.
  - Cost scaling: 10 -> 50 -> 100 -> 200 -> 400...
- **Biome Grid:** `BiomeGridConfig` assigns specific biomes to sectors (Ice, Desert, Jungle, Badlands).
- **Networking:** `PacketSyncGrid` synchronizes unlocked sectors to clients.
- **Commands:** `/shadowgrid unlock` command for admins.

### Fixed

- Fixed crash related to `ClientGridData` synchronization.
- Fixed particle rendering range issues.
