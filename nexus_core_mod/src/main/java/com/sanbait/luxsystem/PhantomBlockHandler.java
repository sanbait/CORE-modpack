package com.sanbait.luxsystem;

import com.sanbait.nexuscore.NexusCore;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.core.particles.ParticleTypes;

@Mod.EventBusSubscriber(modid = NexusCore.MODID)
public class PhantomBlockHandler {

    // Tag for blocks that require Lux to function
    public static final TagKey<net.minecraft.world.level.block.Block> REQUIRES_LUX = BlockTags
            .create(new ResourceLocation("luxsystem", "requires_lux"));

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide)
            return;

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        // Check if block requires Lux
        if (state.is(REQUIRES_LUX)) {
            // Check if inside any Nexus Core radius
            if (!CoreRadiusManager.isInRadius(level, pos)) {
                // IT IS PHANTOM! Cancel interaction.
                event.setCanceled(true);

                // Visual & Audio Feedback
                if (level.isClientSide) {
                    // Spawn "Smoke" or "Ghost" particles
                    for (int i = 0; i < 5; i++) {
                        level.addParticle(ParticleTypes.SMOKE,
                                pos.getX() + 0.5 + (Math.random() - 0.5),
                                pos.getY() + 0.5 + (Math.random() - 0.5),
                                pos.getZ() + 0.5 + (Math.random() - 0.5),
                                0, 0, 0);
                    }
                }

                // Inform player (only once per second to avoid spam)
                if (level.getGameTime() % 20 == 0) {
                    event.getEntity().displayClientMessage(
                            Component.translatable("message.luxsystem.phantom_warning"),
                            true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide)
            return;

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (state.is(REQUIRES_LUX)) {
            if (!CoreRadiusManager.isInRadius(level, pos)) {
                event.setCanceled(true); // Cannot break phantom blocks
            }
        }
    }
}
