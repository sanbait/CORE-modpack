package com.sanbait.nexuscore;

import net.minecraftforge.common.ForgeConfigSpec;

public class NexusCoreConfig {
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec SPEC;

        public static final ForgeConfigSpec.DoubleValue BASE_RADIUS;
        public static final ForgeConfigSpec.DoubleValue RADIUS_PER_LEVEL;
        public static final ForgeConfigSpec.DoubleValue HP_PER_LEVEL;
        public static final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> UPGRADE_COSTS;

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
                                                "minecraft:copper_ingot|1", // 1->2
                                                "minecraft:iron_ingot|1", // 2->3
                                                "minecraft:gold_ingot|1", // 3->4
                                                "minecraft:diamond|1", // 4->5
                                                "minecraft:emerald|1", // 5->6
                                                "minecraft:netherite_ingot|1", // 6->7
                                                "minecraft:nether_star|1", // 7->8
                                                "minecraft:dragon_egg|1", // 8->9
                                                "minecraft:beacon|1" // 9->10
                                ), entry -> entry instanceof String); // Simple validator

                BUILDER.pop();
                SPEC = BUILDER.build();
        }
}
