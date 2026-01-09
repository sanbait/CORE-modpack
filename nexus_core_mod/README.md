# Nexus Core Mod - User Guide

## Installation Instructions

to play with this mod with your friends, follow these steps:

1. **Install Minecraft Forge** (Version 1.20.1 - 47.x.x).
2. **Download the Dependencies**:
    * You need **GeckoLib** for 1.20.1 (Version 4.4.2 or newer).
    * *Without GeckoLib, the game will crash!*
3. **Install the Mod**:
    * Place `nexus_core-1.1.17.jar` into your `mods` folder.
    * Place the `geckolib` jar into your `mods` folder.
4. **Launch the Game!**

---

## Configuration Guide

You can customize the Nexus Core balance without reinstalling the mod.

**Config File Location**:  
`config/nexuscore-common.toml`  
*(This file appears after you launch the game with the mod once)*

### Available Settings

#### 1. General Stats

* **`baseRadius`**: The protection radius at Level 1 (Default: 12.0 blocks).
* **`radiusPerLevel`**: How much the radius grows per level (Default: 2.0 blocks).
  * *Example*: At Level 10, Radius = 12 + (10 * 2) = 32 blocks.
* **`hpPerLevel`**: Health per level (Default: 200 HP).
  * *Example*: Level 10 has 2000 HP.

#### 2. Upgrade & Repair Costs

* **`upgradeCosts`**: A list defining what item is needed for each level.
* **Format**: `"modid:item_name|amount"`
* **Default Values**:
  * Level 1 -> 2: Copper Ingot
  * Level 2 -> 3: Iron Ingot
  * ...
  * Level 9 -> 10: Beacon

**How to change:**
Open the `.toml` file with Notepad.
Change `"minecraft:copper_ingot|1"` to whatever you want, e.g., `"minecraft:diamond|5"`.
Restart the game to apply changes.

### Visuals

* **Toggle Particles**: Press **`P`** (default) in-game to turn the radius particles on/off.
