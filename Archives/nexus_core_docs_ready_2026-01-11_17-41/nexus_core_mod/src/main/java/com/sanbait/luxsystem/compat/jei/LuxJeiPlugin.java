package com.sanbait.luxsystem.compat.jei;

import com.sanbait.nexuscore.NexusCore;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.constants.VanillaTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class LuxJeiPlugin implements IModPlugin {
    public static final ResourceLocation PLUGIN_ID = new ResourceLocation(NexusCore.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Add info tabs for machines
        registration.addIngredientInfo(
                new ItemStack(com.sanbait.luxsystem.ModBlocks.LUX_CONDENSER.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.luxsystem.condenser_info"));

        registration.addIngredientInfo(
                new ItemStack(com.sanbait.luxsystem.ModBlocks.LUX_EXTRACTOR.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.luxsystem.extractor_info"));
    }
}
