package com.sanbait.luxsystem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> REQUIRES_LUX = tag("requires_lux");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(LuxSystem.MODID, name));
        }
    }
}
