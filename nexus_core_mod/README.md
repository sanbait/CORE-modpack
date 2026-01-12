# Nexus Core Mod - Official Documentation

**Version:** 1.1.18 (Java Forge 1.20.1)  
**Dependencies:** GeckoLib 4.4.2+  

---

## 📖 Overview

**Nexus Core** is the heart of the "Entropy Core" modpack. It introduces a central entity — **The Core** — which serves as:

1. **The "King" to protect:** If it dies (at Level 1), the game is over.
2. **Power Generator:** It generates "Lux" (Light Energy) to charge your tools and armor.
3. **Sanctuary:** It provides buffs (Regeneration, Resistance) to nearby players.

This mod is split into two logical modules:

* `nexuscore`: The Core entity, its AI, and upgrading logic.
* `luxsystem`: The energy system, machines, and fluids associated with Light.

---

## 💎 The Nexus Core Entity

**Entity ID:** `nexuscore:core`  
**Summon Command:** `/summon nexuscore:core`

The Core is a living, growing crystal structure. It is anchored to its spawn position and cannot be moved by pistons or physics.

### 1. Growth & Stats

The Core has **10 Levels**. As it levels up, it grows physically taller, creates a larger protection radius, and generates more energy.

| Feature | Formula / value | Example (Lvl 1) | Example (Lvl 10) |
| :--- | :--- | :--- | :--- |
| **HP** | `Level * 200` | 200 HP | 2000 HP |
| **Height** | `1.0 * Level` blocks | 1 Block | 10 Blocks |
| **Radius** | `12 + (Level * 2)` | 14 Blocks | 32 Blocks |
| **Lux Gen** | `Level * 1` Lux/tick | 1 L/t | 10 L/t |
| **Lux Cap** | `Level * 1000` Lux | 1000 Lux | 10,000 Lux |

### 2. Interaction & Upgrading

**Right-click the Core with an item to interact.**

* **Heal (Priority):** If the Core is damaged, the item is consumed to **Heal 20 HP** (10% of base).
  * *Cost:* 1x Upgrade Item for the *current* level tier.
* **Upgrade:** If the Core is at full health, the item consumes a stack to **Level Up**.
  * *Cost:* Defined in Config (`upgradeCosts`). Default is usually crystals or rare items.

**Default Upgrade Costs (Configurable):**

* **Lvl 1 -> 2:** 4x Lux Crystal
* **Lvl 2 -> 3:** 8x Lux Crystal
* ...
* **Lvl 9 -> 10:** 2x Ancient Lux Orb

### 3. Death Mechanics

* **If Level > 1:** The Core does **NOT** die. It loses 1 Level, plays a "Break" sound, and acts as a checkpoint.
* **If Level = 1:** The Core dies permanently. Game Over condition.

---

## ⚡ The Lux System (Energy)

**Lux** is the magical energy form acting as "Liquid Light".

### 1. Generating & Charging

* **Source:** The Nexus Core passively generates Lux.
* **Charging:** Every **1.0 Second** (20 ticks), the Core scans all players within its **Radius**.
  * It charges items in your **Main Hand**, **Off Hand**, and **Armor Slots**.
  * **Rate:** ~50 Lux per scan (burst).
  * *Note:* Items must have the `LuxCapability`. Vanilla swords/pickaxes are supported by default (configured in TOML).

### 2. Supported Items (Default)

The mod automatically attaches Lux capacity to vanilla items:

* **Wooden Tier:** 100 Lux
* **Diamond Tier:** 2000 Lux
* **Netherite Tier:** 5000 Lux
* *Configurable in `nexuscore-common.toml`*

---

## ⚙️ Configuration Guide

### 1. Core Settings (`nexuscore-common.toml`)

Located in `config/nexuscore-common.toml`.

#### [General]

* `baseRadius` (Default: 12.0): Starting radius.
* `radiusPerLevel` (Default: 2.0): Added radius per level.
* `hpPerLevel` (Default: 200.0): HP multiplier.

#### [Upgrades]

* `upgradeCosts`: A list of strings determining the cost to reach the *next* level.
  * Format: `"modid:item_name|quantity"`
  * *Important:* This lists the cost for 1->2, then 2->3, etc.

#### [Lux System]

* `itemLuxCapacities`: Add custom mod items here to give them "battery" properties.
  * Format: `"modid:item|capacity"`
* `coreLuxGenerationPerLevel`: Lux per tick multiplier.

### 2. Machines (`luxsystem-common.toml`)

Located in `config/luxsystem-common.toml`.

* `extractorSpeed` / `condenserSpeed`: Operation time in ticks.
* `tankCapacity`: Fluid limit (mB) for machines.
* `manualInfusionDuration`: Time in ticks for Phantom Infusion crafting.

---

## 🛠 Troubleshooting & Debug

* **Particles:** If FPS is low, Core particles are culled after 64 blocks distance.
* **Missing Mobs/Entities:** If mobs disappear at close range, check your `EntityCulling` mod settings or `Embeddium` Entity Distance. This mod respects global rendering settings.
* **Core Disappearing:** The Nexus Core has a forced render distance of 512 blocks. If it disappears, ensure `nexuscore:core` is whitelisted in `entityculling.json` (done automatically in v1.1.20).
* **Invisible Golem:** This version uses `GeckoLib` rendering. If the Core is invisible, ensure you have the GeckoLib mod installed.
* **Commands:**
  * `/lux set <amount>` (if enabled in dev) - Set Lux amount of held item.
  * `/summon nexuscore:core` - Spawn a fresh Core.

---
*Maintained by: Entropy Core Dev Team*
*Last Update: Jan 2026*
