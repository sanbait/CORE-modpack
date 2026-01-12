package com.sanbait.shadowgrid.mixin;

import com.sanbait.shadowgrid.client.ClientGridData;
import com.sanbait.shadowgrid.world.BiomeGridConfig;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Frustum.class, priority = 1000)
public class MixinFrustum {

    @Inject(method = "isVisible(Lnet/minecraft/world/phys/AABB;)Z", at = @At("HEAD"), cancellable = true)
    private void shadowgrid_checkVisible(AABB aabb, CallbackInfoReturnable<Boolean> cir) {
        // AGGRESSIVE: Check if ANY part of the AABB is in a locked sector
        // Check multiple points across the AABB to catch all cases
        final int SECTOR_SIZE = BiomeGridConfig.SECTOR_SIZE;
        final int HALF_SIZE = SECTOR_SIZE / 2;
        
        // Check center
        double centerX = aabb.minX + (aabb.maxX - aabb.minX) / 2.0;
        double centerZ = aabb.minZ + (aabb.maxZ - aabb.minZ) / 2.0;
        
        // Check all 4 corners
        double[] testX = {aabb.minX, aabb.maxX, aabb.minX, aabb.maxX, centerX};
        double[] testZ = {aabb.minZ, aabb.minZ, aabb.maxZ, aabb.maxZ, centerZ};
        
        // Also check edge midpoints for better coverage
        testX = new double[]{aabb.minX, aabb.maxX, aabb.minX, aabb.maxX, 
                             centerX, centerX, aabb.minX, aabb.maxX};
        testZ = new double[]{aabb.minZ, aabb.minZ, aabb.maxZ, aabb.maxZ,
                             aabb.minZ, aabb.maxZ, centerZ, centerZ};
        
        for (int i = 0; i < testX.length; i++) {
            int sectorX = Math.floorDiv((int) testX[i] + HALF_SIZE, SECTOR_SIZE);
            int sectorZ = Math.floorDiv((int) testZ[i] + HALF_SIZE, SECTOR_SIZE);
            
            if (!ClientGridData.isSectorUnlocked(sectorX, sectorZ)) {
                // If ANY point is in locked sector, hide the entire AABB
                cir.setReturnValue(false);
                return;
            }
        }
        
        // Additional check: if AABB spans across sector boundary, check both sectors
        int minSectorX = Math.floorDiv((int) aabb.minX + HALF_SIZE, SECTOR_SIZE);
        int maxSectorX = Math.floorDiv((int) aabb.maxX + HALF_SIZE, SECTOR_SIZE);
        int minSectorZ = Math.floorDiv((int) aabb.minZ + HALF_SIZE, SECTOR_SIZE);
        int maxSectorZ = Math.floorDiv((int) aabb.maxZ + HALF_SIZE, SECTOR_SIZE);
        
        // If AABB spans multiple sectors, check all of them
        for (int sx = minSectorX; sx <= maxSectorX; sx++) {
            for (int sz = minSectorZ; sz <= maxSectorZ; sz++) {
                if (!ClientGridData.isSectorUnlocked(sx, sz)) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
    }
}
