package com.sanbait.shadowgrid.mixin;

import com.sanbait.shadowgrid.world.GridSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetherPortalBlock.class)
public class MixinNetherPortalBlock {

    private static final int SECTOR_SIZE = 512;
    private static final int HALF_SIZE = 256;

    /**
     * Portal sector checking with 1:1 dimension scale.
     * Since all dimensions now have coordinate_scale=1.0 via dimensions_1to1
     * datapack,
     * we just check if current position's sector is unlocked.
     */
    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void onEntityInside(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (level.isClientSide())
            return;
        if (!(entity instanceof ServerPlayer player))
            return;

        // With 1:1 scale, portal coordinates = target coordinates (roughly)
        // We check if the sector at the portal's location is unlocked.
        int sectorX = Math.floorDiv(pos.getX() + HALF_SIZE, SECTOR_SIZE);
        int sectorZ = Math.floorDiv(pos.getZ() + HALF_SIZE, SECTOR_SIZE);

        // Get grid data (always from Overworld storage)
        GridSavedData data = GridSavedData.get(level);

        if (!data.isSectorUnlocked(sectorX, sectorZ)) {
            // Block teleportation - sector is locked
            ci.cancel();

            player.displayClientMessage(
                    Component.literal("§cСектор [" + sectorX + ":" + sectorZ + "] закрыт! Откройте его сначала."),
                    true);

            // Push player back slightly
            player.setDeltaMovement(player.getLookAngle().reverse().scale(0.5));
            player.hurtMarked = true;
        }
    }
}
