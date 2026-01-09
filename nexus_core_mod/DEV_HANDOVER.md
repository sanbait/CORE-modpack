# Nexus Core Mod: Developer Handover & Technical Summary

**Mod Version**: 1.1.17
**State**: Stable, Feature-Complete (Growth, Physics, Upgrades, Healing, Config, HUD).

## 1. Core Architecture (The "Winning" Approach)

We moved away from `BlockEntity` and standard models. The Nexus Core is a **`PathfinderMob` (Entity)** using **GeckoLib** for animation and rendering.

* **Entity Class**: `NexusCoreEntity` (extends `PathfinderMob`, implements `GeoEntity`).
* **Renderer**: `NexusCoreRenderer` (extends `GeoEntityRenderer`).
* **Model**: `NexusCoreModel` (extends `GeoModel`).

### Why this works

* **Segments**: We don't scale the whole model. We use **10 static bones** (`root`, `level2`... `level10`) in `nexus_core.geo.json`. We toggle their visibility in `NexusCoreModel.setCustomAnimations` based on `entity.getCurrentLevel()`.
* **Hitbox**: We override `getDimensions` and call `refreshDimensions()` whenever the level changes. `EntityDimensions.fixed(1.5f, 1.0f * level)`.

## 2. Key Solutions to "Hard" Problems

### A. The "Anti-Gravity" (Physics Anchor)

**Problem**: Explosions/Pistons moved the core. `isPushable() = false` wasn't enough.
**Solution**:
We implemented an **Anchor System** in `tick()`:

```java
// On first tick, save blockPosition to anchorPos.
// On every tick, if distance(anchorPos) > 0.01:
//     setPos(anchorPos);
//     setDeltaMovement(0,0,0);
```

*Crucial*: Save `anchorPos` to NBT (`addAdditionalSaveData`) so it persists across restarts.

### B. Segmented Textures (The Atlas)

**Problem**: User wanted unique textures per level.
**Solution**:

* `nexus_core.geo.json` texture size set to **64x640**.
* Each cube maps to a unique vertical UV strip (0-64, 64-128, etc.).
* User provides one long `nexus_core.png` (Atlas).

### C. Healing vs Upgrading

**Problem**: Context interactions and avoiding accidental upgrades.
**Solution**:
In `mobInteract`:

1. **Check Health First**: If `hp < max`:
    * Calculate `missing = max - current`.
    * Cap heal amount: `Math.min(20.0f, missing)`.
    * **FIX**: Use `serverLevel.sendParticles(...)` for hearts because `spawnAtLocation` throws items.
2. **Else (Full HP)**: Attempt Upgrade logic.

### D. The Visuals (Particles & HUD)

* **Particles**: Logic runs in `clientTick`. We use a static flag `NexusCore.RENDER_PARTICLES` toggled by Keybind `P`.
* **HUD**: `NexusCoreOverlay` does a raycast check (`mc.hitResult`).
  * Dynamic Text: Shows "Repair Cost" (Orange) if damaged, "Upgrade Cost" (Yellow) if full.

### E. AI / Aggro Logic (Smart Targeting)

**Problem**: Mobs ignored players and only hit the core.
**Solution**:
In `attractMobs()`:

* We check `mob.getTarget()`.
* If target is **Player**, we **DO NOT** override it. This preserves the "Revenge" mechanic (mobs fight back).
* Only if target is `null` or `!Player`, we force target to Core.

## 3. Configuration System

`NexusCoreConfig` uses `ForgeConfigSpec`.

* **Stats**: `BASE_RADIUS`, `RADIUS_PER_LEVEL`, `HP_PER_LEVEL`.
* **Costs**: `UPGRADE_COSTS` (List<String> in format `modid:item|amount`).
  * *Note*: The same item defined for "Level X to Y" is used to Repair Level X.

## 4. Compilation "Gotchas" (Do Not Repeat)

1. **Missing Abstract Methods**: `NexusCoreEntity` MUST implement:
    * `registerControllers()` (GeckoLib requirement).
    * `createAttributes()` (Minecraft Mob requirement).
2. **Particle Spawning**: Do NOT use `spawnAtLocation(ParticleType)` (it doesn't exist). Use `level.addParticle` (Client) or `ServerLevel.sendParticles` (Server).

## 5. Files Manifest

* **Entity**: `src/com/sanbait/nexuscore/NexusCoreEntity.java`
* **Config**: `src/com/sanbait/nexuscore/NexusCoreConfig.java`
* **Overlay**: `src/com/sanbait/nexuscore/NexusCoreOverlay.java`
* **Model/Geo**: `resources/assets/nexuscore/geo/nexus_core.geo.json`
* **Lang**: `resources/assets/nexuscore/lang/en_us.json` & `ru_ru.json`

## 6. Next Steps (Integrations)

For the next chat:

* **Integrations**: Look at `NexusCore.java` -> `soulbound` logic implies we might want Curios API support properly (currently just event handling).
* **Modpack**: Ensure recipe compatibility with other mods (e.g. creating the Core Item).
* **Advanced AI Priority System (Planned)**:
  * **Type A**: `CORE_FOCUSED` (Kamikaze). Ignores players, rushes Core.
  * **Type B**: `PLAYER_FOCUSED` (Guardian). Ignores Core, rushes Players.
  * **Type C**: `MIXED` (Default). Attacks Core, but switches to Player on revenge.
  * *Implementation*: Config lists/tags for each category.
