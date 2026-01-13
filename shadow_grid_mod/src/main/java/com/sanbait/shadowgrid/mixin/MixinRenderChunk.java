package com.sanbait.shadowgrid.mixin;

import com.sanbait.shadowgrid.client.ClientGridData;
import com.sanbait.shadowgrid.world.BiomeGridConfig;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChunkRenderDispatcher.RenderChunk.class, priority = 1000)
public abstract class MixinRenderChunk {

    // Use getOrigin() method shadow instead of direct field access to avoid mapping
    // issues
    @Shadow
    public abstract BlockPos getOrigin();

    @Inject(method = "getCompiledChunk()Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;", at = @At("HEAD"), cancellable = true)
    private void shadowgrid_checkChunkVisibility(CallbackInfoReturnable<ChunkRenderDispatcher.CompiledChunk> cir) {
        // Direct access to origin via Shadow
        BlockPos origin = getOrigin();
        if (origin != null) {
            final int SECTOR_SIZE = BiomeGridConfig.SECTOR_SIZE;
            final int HALF_SIZE = SECTOR_SIZE / 2;

            // Calculate sector from chunk origin (chunk is 16x16, so use center)
            int chunkCenterX = origin.getX() + 8;
            int chunkCenterZ = origin.getZ() + 8;

            // Use Math.floorDiv for stable sector calculation (handles negatives correctly)
            int sectorX = Math.floorDiv(chunkCenterX + HALF_SIZE, SECTOR_SIZE);
            int sectorZ = Math.floorDiv(chunkCenterZ + HALF_SIZE, SECTOR_SIZE);

            if (!ClientGridData.isSectorUnlocked(sectorX, sectorZ)) {
                // Return null to prevent rendering
                // This simulates the chunk being "empty" or not ready
                // DEBUG: Log occusion (rarely, to avoid spam)
                // System.out.println("[ShadowGrid] Hiding chunk at " + sectorX + ":" +
                // sectorZ);
                // Return UNCOMPILED to prevent rendering safely (null might cause NPE)
                cir.setReturnValue(ChunkRenderDispatcher.CompiledChunk.UNCOMPILED);
            }
        }
    }
}
