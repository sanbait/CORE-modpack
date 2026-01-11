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

        static {
                BUILDER.push("Nexus Core Settings");

                BASE_RADIUS = BUILDER.comment("Base radius for aggro and buffs at level 1")
                                .defineInRange("baseRadius", 10.0, 1.0, 100.0);

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

                BUILDER.pop();
                SPEC = BUILDER.build();
        }
}
