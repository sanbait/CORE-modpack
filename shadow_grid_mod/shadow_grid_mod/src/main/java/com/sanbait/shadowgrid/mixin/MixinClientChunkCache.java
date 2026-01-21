package com.sanbait.shadowgrid.mixin;

import com.sanbait.shadowgrid.client.ClientGridData;
import com.sanbait.shadowgrid.world.BiomeGridConfig;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public class MixinClientChunkCache {

    @Inject(method = "replaceWithPacketData", at = @At("HEAD"), cancellable = true)
    private void onReplaceWithPacketData(int x, int z, net.minecraft.network.FriendlyByteBuf buffer,
            net.minecraft.nbt.CompoundTag tag,
            java.util.function.Consumer<net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer,
            CallbackInfoReturnable<net.minecraft.world.level.chunk.LevelChunk> cir) {
        if (!shouldAllowChunk(x, z)) {
            cir.setReturnValue(null);
        }
    }

    // Also try to catch light updates or other accesses?
    // replaceWithPacketData is the main entry point for chunk data packets in
    // 1.19/1.20

    private boolean shouldAllowChunk(int x, int z) {
        // Use strict CHUNK-based math to avoid rounding errors
        // Sector Size = 256 blocks = 16 chunks.
        // Half Size = 128 blocks = 8 chunks.
        // Formula: (ChunkCoord + 8) / 16

        int sectorX = Math.floorDiv(x + 8, 16);
        int sectorZ = Math.floorDiv(z + 8, 16);

        // CRITICAL FIX: If ClientGridData is not yet synced from server (empty),
        // allow ALL chunks to prevent race condition on world load
        if (ClientGridData.getUnlockedCount() == 0) {
            return true; // Not yet synced, allow everything
        }

        boolean unlocked = ClientGridData.isSectorUnlocked(sectorX, sectorZ);
        if (!unlocked) {
            // System.out.println("Void blocking chunk: " + x + "," + z); // Uncomment for
            // debug
            return false;
        }
        return true;
    }
}
