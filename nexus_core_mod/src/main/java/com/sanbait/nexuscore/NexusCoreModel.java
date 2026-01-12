package com.sanbait.nexuscore;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NexusCoreModel extends GeoModel<NexusCoreEntity> {
    @Override
    public ResourceLocation getModelResource(NexusCoreEntity object) {
        return new ResourceLocation(NexusCore.MODID, "geo/nexus_core.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NexusCoreEntity object) {
        // Используем текстуру-атлас с разными блоками для разных уровней
        // В модели UV координаты настроены для использования разных частей текстуры
        // Пока используем железный блок как временную текстуру
        // TODO: Создать текстуру-атлас 640x64 с блоками: медь, железо, золото, алмаз, изумруд, незерит и т.д.
        return new ResourceLocation("minecraft", "textures/block/iron_block");
    }

    @Override
    public ResourceLocation getAnimationResource(NexusCoreEntity object) {
        return new ResourceLocation(NexusCore.MODID, "animations/nexus_core.animation.json");
    }

    @Override
    public void setCustomAnimations(NexusCoreEntity animatable, long instanceId,
            software.bernie.geckolib.core.animation.AnimationState<NexusCoreEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        int level = animatable.getCurrentLevel();
        // Loop through levels 2 to 10
        for (int i = 2; i <= 10; i++) {
            // Bones are named "level2", "level3", etc.
            var bone = this.getAnimationProcessor().getBone("level" + i);
            if (bone != null) {
                // Show if current level >= i, else hide
                bone.setHidden(level < i);
            }
        }
    }
}
