# Shadow Grid Mod - Official Documentation

**Version:** 0.0.1 (Alpha)
**Dependencies:** Nexus Core (Optional, for unlocking currency), GeckoLib, Curios

**Last Updated:** January 2026

---

## 🌎 Overview

**Shadow Grid** transforms the world into a strictly partitioned grid of **Sectors**.

* The world is divided into **512x512 block Sectors**.
* Only the **Central Sector (0:0)** is unlocked at the start.
* The rest of the world is locked in **Shadow** (Visually hidden by Fog and Particle Walls).
* Players must physically unlock adjacent sectors to expand the playable area.

---

## 🧩 The Grid System

### 1. Sectors

* **Size:** 512 x 512 blocks.
* **Coordinates:** Represented as `X:Z` (e.g., `0:0`, `1:0`, `0:-1`).
* **Border:** A visual particle wall separates Unlocked sectors from Locked ones.

### 2. Biomes per Sector

Each sector can have a distinct biome assigned to it, creating a "Patchwork" world.

* **0:0 (Start):** Default World Generation (Forest/Plains mix).
* **North (0:-1):** Ice Spikes.
* **South (0:1):** Desert.
* **East (1:0):** Jungle.
* **West (-1:0):** Badlands.
*(More configurations available in `BiomeGridConfig.java`)*

### 3. Visual Occlusion

To create a true "Fog of War" effect:

* **Particle Walls:** A dense wall of particles appears at the border of the unlocked region.
* **Black Fog:** If a player crosses into a locked sector (via cheating or glitching), they are blinded by thick black fog.

---

## 🔓 Unlocking Regions

You cannot simply walk into a new region. You must **pay** to unlock it.

### 1. The Gateway

* **Structure:** Ancient Gateway blocks spawn (or are placed) at the borders of sectors (center of the 512-block edge).
* **Interaction:** Right-click the Gateway block to attempt an unlock.

### 2. Cost System (Lux Crystals)

Unlocking requires **Lux Crystals** (from the **Nexus Core** mod).
The cost increases progressively based on how many regions you have already unlocked:

| Unlock Order | Cost |
| :--- | :--- |
| **1st Unlock** | 10 Crystals |
| **2nd Unlock** | 50 Crystals |
| **3rd Unlock** | 100 Crystals |
| **4th Unlock** | 200 Crystals |
| **5th+ Unlock** | 200 * 2^(n-3) (Doubles each time: 400, 800, etc.) |

### 3. Interaction Logic

1. Hold **Lux Crystals** in your inventory.
2. Right-click the **Gateway**.
3. If you have enough crystals, they are consumed, and the adjacent sector is permanently unlocked for everyone on the server.
4. The particle wall dissolves, revealing the new land.

---

## ⚙️ Configuration & Commands

### Admin Commands

* `/shadowgrid unlock <x> <z>` - Instantly unlock a specific sector (Admin only).
  * Example: `/shadowgrid unlock 1 0` unlocks the East sector.

### Technical Details

* **Data Storage:** Unlocked sectors are saved in `shadow_grid_data` (World Saved Data).
* **Sync:** Data is synchronized to clients on login and on update to ensure borders render correctly.

---

*Maintained by: Entropy Core Dev Team*
