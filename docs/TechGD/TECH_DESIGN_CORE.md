# Technical Documentation: Nexus Core Mod

**Version:** 1.1.18 (Java Forge Mod)
**State:** Stable, Feature-Complete
**Namespace:** `nexuscore` (and `luxsystem`)

## 1. Concept

The **Nexus Core** is a central "Tower Defense" entity and a power generator.
Unlike the previous KubeJS prototype, this is a fully custom **Java Mod Entity**.

---

## 2. Architecture: Custom Entity + GeckoLib

We do NOT use a Block + Proxy system anymore.
The Core is a single `PathfinderMob` with a `GeckoLib` model.

### 2.1 Entity: `nexuscore:core`

* **Class:** `NexusCoreEntity` (extends `PathfinderMob`, `GeoEntity`)
* **Model:** `nexus_core.geo.json` (10 static bones `level1`...`level10`).
* **Rendering:** `NexusCoreRenderer`. Animations handle the visual "growth" by toggling bone visibility based on Level.
* **Physics:** Custom "Anchor System" prevents displacement by pistons/explosions (`anchorPos` in NBT).

### 2.2 Lux System (Power)

* **Implementation:** Forge Capabilities (`ILuxStorage`).
* **Context:** The Core is a generator (Gen Rate depends on Level).
* **Mechanic:**
  * **Items:** Vanilla items (Diamond Sword, etc.) act as batteries if they have the Capability attached.
  * **Charging:** Core charges nearby items in player inventories every second.
  * **Visuals:** No custom textures needed. Vanilla items show a "Lux Bar" in tooltips via `ItemTooltipEvent`.

---

## 3. Game Logic

### 3.1 Upgrades & Healing

Interaction handled in `mobInteract`:

1. **Check HP:** If damaged, item is used to **Heal** (Priority).
2. **Check Level:** If full HP, item is used to **Upgrade** (Level 1 -> 10).
3. **Cost:** Configurable in `nexuscore-common.toml` (`modid:item|amount`).

### 3.2 AI & Targeting

Custom AI Goal: `CoreAttackGoal`

* Monsters utilize standard "Nearest Attackable Target".
* Priority logic ensures mobs target the Core but switch to Player if provoked ("Revenge" mechanic).
* **Fix:** `PhantomBlockHandler` manages interaction events for Lux-related blocks using the `luxsystem` mod ID.

---

## 4. File Structure & Assets

* **Jar:** `nexus_core-1.1.18.jar`
* **Config:** `config/nexuscore-common.toml`
* **Assets:**
  * `assets/nexuscore/geo/nexus_core.geo.json` (Model)
  * `assets/nexuscore/textures/entity/nexus_core.png` (Texture Atlas)
  * `assets/luxsystem/lang/` (Localization for Lux features)

---

## 5. Maintenance Notes

* **Localization:** Strictly separated. `nexuscore` keys in its own folder, `luxsystem` keys in its own.
* **Visuals:** If "Invisible Golem" issues arise, check `NexusCoreRenderer`. The current code uses a GeoRenderer, so vanilla rendering issues shouldn't apply.
