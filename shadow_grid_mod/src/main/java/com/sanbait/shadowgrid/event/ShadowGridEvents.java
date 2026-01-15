package com.sanbait.shadowgrid.event;

import com.sanbait.shadowgrid.ShadowGridMod;
import com.sanbait.shadowgrid.network.PacketSyncGrid;
import com.sanbait.shadowgrid.network.ShadowNetwork;
import com.sanbait.shadowgrid.world.BiomeGridConfig;
import com.sanbait.shadowgrid.world.GridSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShadowGridMod.MODID)
public class ShadowGridEvents {

    @SubscribeEvent
    public static void onServerStarting(ServerAboutToStartEvent event) {
        // Load biome config first (creates default file if needed)
        BiomeGridConfig.loadConfig();

        // Capture world seed
        long seed = event.getServer().getWorldData().worldGenOptions().seed();
        BiomeGridConfig.currentWorldSeed = seed;
        BiomeGridConfig.mixinCalled = false; // Reset flag for new world

        System.out.println("[ShadowGrid] Server starting - World seed: " + seed);
        System.out.println("[ShadowGrid] Waiting for biome mixin to activate...");
    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        // Cache biome registry when world loads (this happens AFTER world generation
        // starts)
        if (event.getLevel() instanceof ServerLevel serverLevel && serverLevel.dimension() == Level.OVERWORLD) {
            try {
                net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> biomeRegistry = serverLevel
                        .registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
                BiomeGridConfig.setCachedRegistry(biomeRegistry);
                System.out.println(
                        "[ShadowGrid] Overworld loaded - Biome registry cached (" + biomeRegistry.size() + " biomes)");

                // Force spawn to 0,0 center
                int safeY = serverLevel.getSeaLevel() + 10;
                serverLevel.setDefaultSpawnPos(new net.minecraft.core.BlockPos(0, safeY, 0), 0.0f);

                // Check if mixin was called
                if (!BiomeGridConfig.mixinCalled) {
                    System.out.println(
                            "[ShadowGrid] ⚠ WARNING: Biome mixin not called yet! Terralith may be using different BiomeSource.");
                }
            } catch (Exception e) {
                System.err.println("[ShadowGrid] ERROR: Failed to cache biome registry: " + e.getMessage());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            GridSavedData data = GridSavedData.get(player.level());
            ShadowNetwork.sendToPlayer(new PacketSyncGrid(data.getUnlockedSectors()), player);
        }
    }
}
