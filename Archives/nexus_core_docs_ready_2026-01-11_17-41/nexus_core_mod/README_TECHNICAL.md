# Nexus Core - Technical Guide

## Key Systems

### 1. Universal Lux

Items can store "Lux" energy if they are:

- A recognized mod item (LuxPickaxe, LuxArmor).
- Tagged with `#nexuscore:lux_receptive` (JSON tag).
- Listed in the `itemLuxCapacities` configuration.

**How it works:**

- `AttachCapabilitiesEvent` adds a `LuxCapability` to valid items.
- Capacity is determined by Config > Item Class > Default (1000).

### 2. Event-Based Optimization

To prevent lag, we strictly avoid `onUpdate` or `tick` methods for constant polling.

- **Lighting**: Uses `LivingEquipmentChangeEvent` to track players holding light sources.
- **Charging**: `NexusCoreEntity` periodically (every 1s) scans nearby players and pushes charge to them (Server Side).

### 3. Configuration

Located at `config/nexus-core-common.toml`.

- **Lighting**: Toggle player lights, list of light items.
- **Lux System**:
  - `itemLuxCapacities`: List of "modid:item|capacity".
  - `coreLuxGenerationPerLevel`: Lux/tick per level (Default: 1).
  - `coreLuxCapacityPerLevel`: Max Lux per level (Default: 1000).

## Dev Usage

To build: `Batch Build` is deprecated. Please use **manual Gradle build** (`gradlew build`) or your IDE.
To commit: use `COMMIT_AND_PUSH.bat` (simplified version).
