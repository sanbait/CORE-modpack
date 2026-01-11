package com.sanbait.luxsystem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

public class ModFluids {
        public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister
                        .create(ForgeRegistries.Keys.FLUID_TYPES, LuxSystem.MODID);
        public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS,
                        LuxSystem.MODID);

        public static final ResourceLocation LIQUID_LUX_STILL_RL = new ResourceLocation(LuxSystem.MODID,
                        "block/liquid_lux_still");
        public static final ResourceLocation LIQUID_LUX_FLOWING_RL = new ResourceLocation(LuxSystem.MODID,
                        "block/liquid_lux_flowing");

        public static final RegistryObject<FluidType> LIQUID_LUX_TYPE = FLUID_TYPES.register("liquid_lux",
                        () -> new FluidType(FluidType.Properties.create()
                                        .lightLevel(15)
                                        .density(1500)
                                        .viscosity(2000)
                                        .temperature(300)
                                        .rarity(Rarity.EPIC)
                                        .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                                        .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                                        .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {
                                @Override
                                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                                        consumer.accept(new IClientFluidTypeExtensions() {
                                                @Override
                                                public ResourceLocation getStillTexture() {
                                                        return LIQUID_LUX_STILL_RL;
                                                }

                                                @Override
                                                public ResourceLocation getFlowingTexture() {
                                                        return LIQUID_LUX_FLOWING_RL;
                                                }

                                                @Override
                                                public int getTintColor() {
                                                        return 0xFFFFFFFF; // Pure White
                                                }
                                        });
                                }
                        });

        public static final RegistryObject<FlowingFluid> LIQUID_LUX_SOURCE = FLUIDS.register("liquid_lux_source",
                        () -> new ForgeFlowingFluid.Source(ModFluids.LIQUID_LUX_PROPERTIES));

        public static final RegistryObject<FlowingFluid> LIQUID_LUX_FLOWING = FLUIDS.register("liquid_lux_flowing",
                        () -> new ForgeFlowingFluid.Flowing(ModFluids.LIQUID_LUX_PROPERTIES));

        public static final ForgeFlowingFluid.Properties LIQUID_LUX_PROPERTIES = new ForgeFlowingFluid.Properties(
                        LIQUID_LUX_TYPE, LIQUID_LUX_SOURCE, LIQUID_LUX_FLOWING)
                        .bucket(ModItems.LIQUID_LUX_BUCKET)
                        .block(ModBlocks.LIQUID_LUX_BLOCK);
}
