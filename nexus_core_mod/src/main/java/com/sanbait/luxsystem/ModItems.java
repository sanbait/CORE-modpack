package com.sanbait.luxsystem;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.sanbait.luxsystem.items.LuxPickaxeItem;

public class ModItems {
        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
                        LuxSystem.MODID);

        public static final RegistryObject<Item> LIQUID_LUX_BUCKET = ITEMS.register("liquid_lux_bucket",
                        () -> new BucketItem(ModFluids.LIQUID_LUX_SOURCE,
                                        new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
                                                        .rarity(Rarity.EPIC)));

        public static final RegistryObject<Item> LUX_PICKAXE = ITEMS.register("lux_pickaxe",
                        () -> new LuxPickaxeItem(net.minecraft.world.item.Tiers.DIAMOND, 1, -2.8F,
                                        new Item.Properties().rarity(Rarity.RARE)));

        public static final RegistryObject<Item> LUX_CRYSTAL = ITEMS.register("lux_crystal",
                        () -> new Item(new Item.Properties().rarity(Rarity.RARE).fireResistant()));

        public static final RegistryObject<Item> CONCENTRATED_LUX_CRYSTAL = ITEMS.register("concentrated_lux_crystal",
                        () -> new Item(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

        public static final RegistryObject<Item> ANCIENT_LUX_VASE = ITEMS.register("ancient_lux_vase",
                        () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

        public static final RegistryObject<Item> ANCIENT_LUX_ORB = ITEMS.register("ancient_lux_orb",
                        () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

        public static final RegistryObject<Item> LUX_CRYSTAL_FRAGMENT = ITEMS.register("lux_crystal_fragment",
                        () -> new Item(new Item.Properties().rarity(Rarity.COMMON)));

        public static final RegistryObject<Item> FOSSILIZED_LUX_AMBER = ITEMS.register("fossilized_lux_amber",
                        () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

        public static final RegistryObject<Item> LUX_FILTER = ITEMS.register("lux_filter",
                        () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

        public static final RegistryObject<Item> LUX_CANISTER = ITEMS.register("lux_canister",
                        () -> new Item(new Item.Properties().rarity(Rarity.COMMON).stacksTo(16)));

        public static final RegistryObject<Item> LUX_CANISTER_FULL = ITEMS.register("lux_canister_full",
                        () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1)));

        // Lux Tools & Armor
        public static final RegistryObject<Item> LUX_SWORD = ITEMS.register("lux_sword",
                        () -> new com.sanbait.luxsystem.items.LuxSwordItem(net.minecraft.world.item.Tiers.DIAMOND, 3,
                                        -2.4F,
                                        new Item.Properties().rarity(Rarity.RARE)));

        public static final RegistryObject<Item> LUX_HELMET = ITEMS.register("lux_helmet",
                        () -> new com.sanbait.luxsystem.items.LuxArmorItem(
                                        net.minecraft.world.item.ArmorMaterials.DIAMOND,
                                        net.minecraft.world.item.ArmorItem.Type.HELMET,
                                        new Item.Properties().rarity(Rarity.RARE), 400));

        public static final RegistryObject<Item> LUX_CHESTPLATE = ITEMS.register("lux_chestplate",
                        () -> new com.sanbait.luxsystem.items.LuxArmorItem(
                                        net.minecraft.world.item.ArmorMaterials.DIAMOND,
                                        net.minecraft.world.item.ArmorItem.Type.CHESTPLATE,
                                        new Item.Properties().rarity(Rarity.RARE), 800));

        public static final RegistryObject<Item> LUX_LEGGINGS = ITEMS.register("lux_leggings",
                        () -> new com.sanbait.luxsystem.items.LuxArmorItem(
                                        net.minecraft.world.item.ArmorMaterials.DIAMOND,
                                        net.minecraft.world.item.ArmorItem.Type.LEGGINGS,
                                        new Item.Properties().rarity(Rarity.RARE), 600));

        public static final RegistryObject<Item> LUX_BOOTS = ITEMS.register("lux_boots",
                        () -> new com.sanbait.luxsystem.items.LuxArmorItem(
                                        net.minecraft.world.item.ArmorMaterials.DIAMOND,
                                        net.minecraft.world.item.ArmorItem.Type.BOOTS,
                                        new Item.Properties().rarity(Rarity.RARE), 400));
}
