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
        // REMOVED: Render Lock logic.
    }
}
