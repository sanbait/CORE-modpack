package com.sanbait.shadowgrid.mixin;

import com.sanbait.shadowgrid.client.ClientGridData;
import com.sanbait.shadowgrid.world.BiomeGridConfig;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 1000)
public class MixinLevelRenderer {


    @Inject(method = "updateRenderChunks", at = @At("HEAD"))
    private void shadowgrid_filterChunksFromRenderList(
            java.util.LinkedHashSet<ChunkRenderDispatcher.RenderChunk> chunksToUpdate,
            Object renderInfoMap, // LevelRenderer$RenderInfoMap - not public
            net.minecraft.world.phys.Vec3 cameraPos,
            java.util.Queue<ChunkRenderDispatcher.RenderChunk> queue,
            boolean skipPlayerChunk,
            CallbackInfo ci) {
        
        // Filter out chunks in locked sectors BEFORE they are added to render list
        final int SECTOR_SIZE = BiomeGridConfig.SECTOR_SIZE;
        final int HALF_SIZE = SECTOR_SIZE / 2;
        
        chunksToUpdate.removeIf(chunk -> {
            // Get chunk position
            BlockPos origin = null;
            try {
                // Try to get origin using reflection
                java.lang.reflect.Field originField = chunk.getClass().getDeclaredField("origin");
                originField.setAccessible(true);
                Object value = originField.get(chunk);
                if (value instanceof BlockPos) {
                    origin = (BlockPos) value;
                }
            } catch (Exception e) {
                // Try method
                try {
                    java.lang.reflect.Method getOrigin = chunk.getClass().getMethod("getOrigin");
                    Object result = getOrigin.invoke(chunk);
                    if (result instanceof BlockPos) {
                        origin = (BlockPos) result;
                    }
                } catch (Exception ex) {
                    // Can't determine position, allow rendering
                    return false;
                }
            }
            
            if (origin != null) {
                // Calculate sector from chunk origin (chunk is 16x16, so use center)
                int chunkCenterX = origin.getX() + 8;
                int chunkCenterZ = origin.getZ() + 8;
                
                int sectorX = Math.floorDiv(chunkCenterX + HALF_SIZE, SECTOR_SIZE);
                int sectorZ = Math.floorDiv(chunkCenterZ + HALF_SIZE, SECTOR_SIZE);
                
                // Remove chunk if sector is locked
                return !ClientGridData.isSectorUnlocked(sectorX, sectorZ);
            }
            
            return false; // Keep chunk if we can't determine position
        });
    }
}
