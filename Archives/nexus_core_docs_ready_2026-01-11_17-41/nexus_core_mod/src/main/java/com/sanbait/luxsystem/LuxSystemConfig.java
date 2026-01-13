package com.sanbait.luxsystem;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class LuxSystemConfig {
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec SPEC;

        // Extractor Settings
        public static final ForgeConfigSpec.IntValue EXTRACTOR_SPEED;
        public static final ForgeConfigSpec.IntValue EXTRACTOR_TANK_CAPACITY;

        // Condenser Settings
        public static final ForgeConfigSpec.IntValue CONDENSER_SPEED;
        public static final ForgeConfigSpec.IntValue CONDENSER_TANK_CAPACITY;

        // Phantom Mechanics
        public static final ForgeConfigSpec.IntValue MANUAL_INFUSION_DURATION;
        public static final ForgeConfigSpec.IntValue CANISTER_CAPACITY;

        static {
                BUILDER.push("Extractor Settings");
                // Extractor Settings
                EXTRACTOR_SPEED = BUILDER.comment("Extraction speed (ticks per operation)")
                                .defineInRange("extractorSpeed", 100, 1, 1000);
                EXTRACTOR_TANK_CAPACITY = BUILDER.comment("Extractor tank capacity (mB)")
                                .defineInRange("extractorTankCapacity", 10000, 1000, 100000);
                BUILDER.pop();

                BUILDER.push("Condenser Settings");
                CONDENSER_SPEED = BUILDER.comment("Condensing speed (ticks per operation)")
                                .defineInRange("condenserSpeed", 200, 1, 1000);
                CONDENSER_TANK_CAPACITY = BUILDER.comment("Condenser tank capacity (mB)")
                                .defineInRange("condenserTankCapacity", 10000, 1000, 100000);
                BUILDER.pop();

                BUILDER.push("Phantom Mechanics");
                MANUAL_INFUSION_DURATION = BUILDER.comment("Manual infusion duration (ticks, 6000 = 5 minutes)")
                                .defineInRange("manualInfusionDuration", 6000, 100, 72000);
                CANISTER_CAPACITY = BUILDER.comment("Lux Canister capacity (mB)")
                                .defineInRange("canisterCapacity", 1000, 100, 10000);
                BUILDER.pop();

                SPEC = BUILDER.build();
        }

        public static void register() {
                ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "luxsystem-common.toml");
        }
}
