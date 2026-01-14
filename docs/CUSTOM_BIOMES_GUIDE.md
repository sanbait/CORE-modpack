# Custom Biomes Guide for Shadow Grid

Shadow Grid allows you to customize which biomes appear in the grid sectors. You can use Vanilla biomes, biomes from other mods (e.g., Biomes O' Plenty, Terralith), or even create your own custom biomes using Data Packs.

## 1. Configuring Biome Weights

All biome settings are located in:
`config/shadowgrid_biomes.json`

This file determines **which** biomes spawn and **how often**.

- **Higher weight** = More frequent appearance.
- **Weight 0** = Biome disabled.

### Example Configuration

```json
{
  "biome_weights": {
    "minecraft:plains": 10,
    "minecraft:desert": 10,
    "biomesoplenty:origin_valley": 15,
    "terralith:moonlight_valley": 5
  }
}
```

## 2. Adding Modded Biomes

To add biomes from other mods:

1. **Install the Mod**: Add the biome mod (e.g., Biomes O' Plenty) to your `mods` folder.
2. **Find the Biome ID**:
    - Press `F3` in-game while standing in the biome.
    - Or look up the mod's wiki/documentation.
    - Example: `biomesoplenty:origin_valley`
3. **Add to Config**: Open `shadowgrid_biomes.json` and add the ID with a weight.

    ```json
    "biomesoplenty:origin_valley": 20
    ```

4. **Restart Game**: The grid will now include this biome in new sectors.

## 3. Creating Completely New Biomes (Data Packs)

If you want a unique biome that doesn't exist in any mod (e.g., a "Void Sector" or "Toxic Waste"), you can add it using a standard Minecraft **Data Pack**.

### Step 1: Create Data Pack Folder Structure

Create a folder inside your world's `datapacks` folder (e.g., `saves/New World/datapacks/my_custom_biomes/`)

Structure:

```
my_custom_biomes/
  pack.mcmeta
  data/
    my_mod_id/
      worldgen/
        biome/
          toxic_wastes.json
```

### Step 2: pack.mcmeta

```json
{
  "pack": {
    "pack_format": 15,
    "description": "My Custom Biomes"
  }
}
```

### Step 3: Biome Definition (toxic_wastes.json)

You can copy a vanilla biome JSON file and edit the sky color, fog, and blocks.
Minimal example:

```json
{
  "has_precipitation": false,
  "temperature": 2.0,
  "downfall": 0.0,
  "effects": {
    "fog_color": 329011,
    "sky_color": 0,
    "water_color": 4159204,
    "water_fog_color": 329011
  },
  "spawners": {},
  "spawn_costs": {},
  "carvers": {},
  "features": []
}
```

### Step 4: Register in Shadow Grid

Once your data pack is loaded, add your custom biome ID to `shadowgrid_biomes.json`:

```json
"my_mod_id:toxic_wastes": 50
```

## Troubleshooting

- **Biome not appearing?** Check the logs for `[ShadowGrid] Biome not found in registry`.
- **Typo?** Ensure the ID matches exactly (e.g., `minecraft:plains`, not `minecraft:plain`).
- **Data Pack failed?** Run `/datapack list` in-game to see if it's enabled.
