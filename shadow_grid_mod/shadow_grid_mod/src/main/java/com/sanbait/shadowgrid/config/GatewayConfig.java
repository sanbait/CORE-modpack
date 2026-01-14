package com.sanbait.shadowgrid.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class GatewayConfig {
    public static final ForgeConfigSpec COMMON_SPEC;

    public static final ForgeConfigSpec.ConfigValue<String> CURRENCY_ITEM;
    public static final ForgeConfigSpec.IntValue BASE_COST_1;
    public static final ForgeConfigSpec.IntValue BASE_COST_2;
    public static final ForgeConfigSpec.IntValue BASE_COST_3;
    public static final ForgeConfigSpec.IntValue BASE_COST_4_PLUS;
    public static final ForgeConfigSpec.BooleanValue DEBUG_MODE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Gateway Unlock Configuration").push("gateway");

        CURRENCY_ITEM = builder
                .comment("Item ID for unlocking gateways (e.g., 'luxsystem:lux_crystal')")
                .define("currencyItem", "luxsystem:lux_crystal");

        BASE_COST_1 = builder
                .comment("Cost for unlocking the FIRST additional sector")
                .defineInRange("baseCost1", 10, 0, Integer.MAX_VALUE);

        BASE_COST_2 = builder
                .comment("Cost for unlocking the SECOND additional sector")
                .defineInRange("baseCost2", 50, 0, Integer.MAX_VALUE);

        BASE_COST_3 = builder
                .comment("Cost for unlocking the THIRD additional sector")
                .defineInRange("baseCost3", 100, 0, Integer.MAX_VALUE);

        BASE_COST_4_PLUS = builder
                .comment("Base cost for FOURTH sector onwards (doubles each time: 200, 400, 800, ...)")
                .defineInRange("baseCost4Plus", 200, 0, Integer.MAX_VALUE);

        DEBUG_MODE = builder
                .comment("Enable debug messages in chat when activating gateways")
                .define("debugMode", false);

        builder.pop();

        COMMON_SPEC = builder.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC, "shadowgrid-gateway.toml");
    }
}
