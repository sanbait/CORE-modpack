package com.sanbait.shadowgrid.mixin;

import com.sanbait.shadowgrid.client.ClientGridData;
import com.sanbait.shadowgrid.world.BiomeGridConfig;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChunkRenderDispatcher.RenderChunk.class, priority = 1000)
public class MixinRenderChunk {

    @Inject(method = "getCompiledChunk()Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;", at = @At("HEAD"), cancellable = true)
    private void shadowgrid_checkChunkVisibility(CallbackInfoReturnable<ChunkRenderDispatcher.CompiledChunk> cir) {
        ChunkRenderDispatcher.RenderChunk self = (ChunkRenderDispatcher.RenderChunk) (Object) this;
        
        // Try to get chunk position using reflection (field name may vary)
        BlockPos origin = null;
        try {
            // Try different possible field names
            String[] possibleNames = {"origin", "f_112828_", "origin_", "chunkPos"};
            for (String fieldName : possibleNames) {
                try {
                    java.lang.reflect.Field field = self.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(self);
                    if (value instanceof BlockPos) {
                        origin = (BlockPos) value;
                        break;
                    }
                } catch (NoSuchFieldException e) {
                    // Try next name
                }
            }
        } catch (Exception e) {
            // If all fails, try to get from getOrigin() method if exists
            try {
                java.lang.reflect.Method getOrigin = self.getClass().getMethod("getOrigin");
                Object result = getOrigin.invoke(self);
                if (result instanceof BlockPos) {
                    origin = (BlockPos) result;
                }
            } catch (Exception ex) {
                // Give up, return normally
                return;
            }
        }
        
        if (origin != null) {
            final int SECTOR_SIZE = BiomeGridConfig.SECTOR_SIZE;
            final int HALF_SIZE = SECTOR_SIZE / 2;
            
            // Calculate sector from chunk origin (chunk is 16x16, so use center)
            int chunkCenterX = origin.getX() + 8;
            int chunkCenterZ = origin.getZ() + 8;
            
            int sectorX = Math.floorDiv(chunkCenterX + HALF_SIZE, SECTOR_SIZE);
            int sectorZ = Math.floorDiv(chunkCenterZ + HALF_SIZE, SECTOR_SIZE);
            
            if (!ClientGridData.isSectorUnlocked(sectorX, sectorZ)) {
                // Don't return null - let it compile but mark as empty
                // The LevelRenderer mixin will filter it out before rendering
                // Just return normally, but the chunk won't be in render list
                return;
            }
        }
    }
}
