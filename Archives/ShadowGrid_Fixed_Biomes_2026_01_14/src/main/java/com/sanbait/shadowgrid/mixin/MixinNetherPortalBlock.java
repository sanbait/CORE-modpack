package com.sanbait.shadowgrid.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetherPortalBlock.class)
public class MixinNetherPortalBlock {

    // Disable the portal block from updating or spawning entities/teleporting
    // Note: Creating the portal usually happens in PortalShape, but we can kill it
    // if it spawns.
    // However, better to stop it from being placed.
    // Since we can't easily mixin to PortalShape (it's logic), we'll try to break
    // it on tick?
    // Or just preventing players from entering it?

    // Actually, "onPlace" is in BaseFireBlock for creation.
    // To disable creation completely, we'd mixin native PortalShape.

    // Simplest 'effective' disable: Prevent entity collision / teleportation.
    // If the portal frames are built, users might be confused.

    // Let's Mixin into 'BaseFireBlock' using a separate file?
    // No, I'll stick to a simpler strategy:
    // If a portal block exists, destroy it immediately or make it do nothing.

    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void onEntityInside(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.Entity entity,
            CallbackInfo ci) {
        // Disable teleportation functionality completely
        ci.cancel();
    }
}
