package com.sanbait.luxsystem;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
                        LuxSystem.MODID);

        // Жидкий Lux - ведро
        public static final RegistryObject<Item> LIQUID_LUX_BUCKET = ITEMS.register("liquid_lux_bucket",
                        () -> new BucketItem(ModFluids.LIQUID_LUX_SOURCE,
                                        new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                                                        .rarity(Rarity.EPIC)));

        // Кристалл Lux
        public static final RegistryObject<Item> LUX_CRYSTAL = ITEMS.register("lux_crystal",
                        () -> new Item(new Item.Properties().rarity(Rarity.RARE).fireResistant()));
}
