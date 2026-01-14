package com.sanbait.shadowgrid.mixin;

import com.sanbait.shadowgrid.client.ClientGridData;
import com.sanbait.shadowgrid.world.BiomeGridConfig;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionRenderDispatcher.RenderSection.class)
public class MixinRenderChunk {

    @Shadow
    public BlockPos getOrigin() {
        throw new IllegalStateException("Mixin failed to shadow getOrigin()");
    }

    // Блокируем создание геометрии чанка, если он в закрытой зоне
    @Inject(method = "hasTranslucentGeometry", at = @At("HEAD"), cancellable = true)
    private void onHasTranslucentGeometry(CallbackInfoReturnable<Boolean> cir) {
        if (shouldHide()) {
            cir.setReturnValue(false);
        }
    }

    // Блокируем сплошную геометрию
    @Inject(method = "isEmpty", at = @At("HEAD"), cancellable = true)
    private void onIsEmpty(net.minecraft.client.renderer.RenderType p_112816_, CallbackInfoReturnable<Boolean> cir) {
        if (shouldHide()) {
            cir.setReturnValue(true); // "Пусто" = ничего не рисовать
        }
    }

    private boolean shouldHide() {
        BlockPos origin = this.getOrigin();

        // Берем центр чанка
        int centerX = origin.getX() + 8;
        int centerZ = origin.getZ() + 8;

        int SECTOR_SIZE = BiomeGridConfig.SECTOR_SIZE;
        int HALF_SIZE = SECTOR_SIZE / 2;

        int sectorX = Math.floorDiv(centerX + HALF_SIZE, SECTOR_SIZE);
        int sectorZ = Math.floorDiv(centerZ + HALF_SIZE, SECTOR_SIZE);

        return !ClientGridData.isSectorUnlocked(sectorX, sectorZ);
    }
}
