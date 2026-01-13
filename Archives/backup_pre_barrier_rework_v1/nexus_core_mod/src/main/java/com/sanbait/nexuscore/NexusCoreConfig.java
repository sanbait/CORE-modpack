package com.sanbait.nexuscore;

import net.minecraftforge.common.ForgeConfigSpec;

public class NexusCoreConfig {
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec SPEC;

        public static final ForgeConfigSpec.DoubleValue BASE_RADIUS;
        public static final ForgeConfigSpec.DoubleValue RADIUS_PER_LEVEL;
        public static final ForgeConfigSpec.DoubleValue HP_PER_LEVEL;
        public static final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> UPGRADE_COSTS;

        // Lighting
        public static final ForgeConfigSpec.BooleanValue ENABLE_PLAYER_LIGHTS;
        public static final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> PLAYER_LIGHT_ITEMS;

        // Lux System
        public static final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> ITEM_LUX_CAPACITIES;
        public static final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> ITEM_LUX_COSTS;
        public static final ForgeConfigSpec.IntValue DEFAULT_BLOCK_BREAK_COST;
        public static final ForgeConfigSpec.IntValue DEFAULT_ATTACK_COST;
        public static final ForgeConfigSpec.BooleanValue ALLOW_USE_WITHOUT_LUX;
        public static final ForgeConfigSpec.IntValue CORE_LUX_GENERATION_PER_LEVEL;
        public static final ForgeConfigSpec.IntValue CORE_LUX_CAPACITY_PER_LEVEL;

        static {
                BUILDER.push("Nexus Core Settings");

                BASE_RADIUS = BUILDER.comment("Base radius for aggro and buffs at level 1")
                                .defineInRange("baseRadius", 12.0, 1.0, 100.0);

                RADIUS_PER_LEVEL = BUILDER.comment("Extra radius added per level (Radius = Base + Level * PerLevel)")
                                .defineInRange("radiusPerLevel", 2.0, 0.0, 50.0);

                HP_PER_LEVEL = BUILDER.comment("Health Points added per level (MaxHP = Level * HPParLevel)")
                                .defineInRange("hpPerLevel", 200.0, 10.0, 10000.0);

                UPGRADE_COSTS = BUILDER.comment("Upgrade costs for each level. Format: 'modid:item_id|amount'.",
                                "Index 0 = resource to go from Level 1 -> 2",
                                "Index 1 = resource to go from Level 2 -> 3, etc.",
                                "Healing uses the same resource (1 item = 10% Heal).")
                                .defineList("upgradeCosts", java.util.Arrays.asList(
                                                "luxsystem:lux_crystal|4", // 1->2
                                                "luxsystem:lux_crystal|8", // 2->3
                                                "luxsystem:lux_crystal|16", // 3->4
                                                "luxsystem:concentrated_lux_crystal|2", // 4->5
                                                "luxsystem:concentrated_lux_crystal|4", // 5->6
                                                "luxsystem:concentrated_lux_crystal|8", // 6->7
                                                "luxsystem:concentrated_lux_crystal|16", // 7->8
                                                "luxsystem:ancient_lux_orb|1", // 8->9
                                                "luxsystem:ancient_lux_orb|2" // 9->10
                                ), entry -> entry instanceof String); // Simple validator

                BUILDER.push("Lighting Settings");

                ENABLE_PLAYER_LIGHTS = BUILDER.comment(
                                "Enable dynamic lighting for players holding torches (Server-side real light).")
                                .define("enablePlayerLights", true);

                PLAYER_LIGHT_ITEMS = BUILDER.comment("List of items that emit light when held.")
                                .defineList("playerLightItems", java.util.Arrays.asList(
                                                "minecraft:torch",
                                                "minecraft:soul_torch",
                                                "minecraft:lantern",
                                                "minecraft:soul_lantern",
                                                "minecraft:jack_o_lantern",
                                                "minecraft:glowstone",
                                                "minecraft:shroomlight",
                                                "minecraft:sea_lantern",
                                                "minecraft:froglight",
                                                "minecraft:ochre_froglight",
                                                "minecraft:pearlescent_froglight",
                                                "minecraft:verdant_froglight"), entry -> entry instanceof String);

                BUILDER.pop();

                BUILDER.push("Lux System");

                ITEM_LUX_CAPACITIES = BUILDER.comment("Define specific Lux capacities for items.",
                                "Format: 'modid:item_id|capacity'",
                                "Default capacity for tagged items if not listed here is 1000.")
                                .defineList("itemLuxCapacities", java.util.Arrays.asList(
                                                "minecraft:wooden_sword|100",
                                                "minecraft:stone_sword|200",
                                                "minecraft:iron_sword|500",
                                                "minecraft:golden_sword|1000",
                                                "minecraft:diamond_sword|2000",
                                                "minecraft:netherite_sword|5000",

                                                "minecraft:wooden_pickaxe|100",
                                                "minecraft:stone_pickaxe|200",
                                                "minecraft:iron_pickaxe|500",
                                                "minecraft:golden_pickaxe|1000",
                                                "minecraft:diamond_pickaxe|2000",
                                                "minecraft:netherite_pickaxe|5000"), entry -> entry instanceof String);

                ITEM_LUX_COSTS = BUILDER.comment("Define specific Lux costs per use for items.",
                                "Format: 'modid:item_id|cost'",
                                "Default cost if not listed here is defined by DEFAULT_BLOCK_BREAK_COST or DEFAULT_ATTACK_COST.")
                                .defineList("itemLuxCosts", java.util.Arrays.asList(
                                                "minecraft:wooden_sword|1",
                                                "minecraft:stone_sword|2",
                                                "minecraft:iron_sword|3",
                                                "minecraft:golden_sword|5",
                                                "minecraft:diamond_sword|5",
                                                "minecraft:netherite_sword|10",

                                                "minecraft:wooden_pickaxe|1",
                                                "minecraft:stone_pickaxe|2",
                                                "minecraft:iron_pickaxe|3",
                                                "minecraft:golden_pickaxe|5",
                                                "minecraft:diamond_pickaxe|5",
                                                "minecraft:netherite_pickaxe|10"), entry -> entry instanceof String);

                DEFAULT_BLOCK_BREAK_COST = BUILDER
                                .comment("Default Lux cost for breaking blocks (if item not in itemLuxCosts list).")
                                .defineInRange("defaultBlockBreakCost", 5, 0, 1000);

                DEFAULT_ATTACK_COST = BUILDER
                                .comment("Default Lux cost for attacking entities (if item not in itemLuxCosts list).")
                                .defineInRange("defaultAttackCost", 5, 0, 1000);

                ALLOW_USE_WITHOUT_LUX = BUILDER.comment(
                                "Allow using tools/weapons without Lux (true = works without Lux, false = requires Lux).")
                                .define("allowUseWithoutLux", true);

                CORE_LUX_GENERATION_PER_LEVEL = BUILDER.comment("Lux generated per tick per level.")
                                .defineInRange("coreLuxGenerationPerLevel", 1, 0, 1000);

                CORE_LUX_CAPACITY_PER_LEVEL = BUILDER.comment("Maximum Lux stored per level.")
                                .defineInRange("coreLuxCapacityPerLevel", 1000, 100, 100000);

                BUILDER.pop();

                BUILDER.pop();
                SPEC = BUILDER.build();
        }
}
