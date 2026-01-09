package com.sanbait.nexuscore;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NexusCoreRenderer extends GeoEntityRenderer<NexusCoreEntity> {
    public NexusCoreRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new NexusCoreModel());
    }
}
