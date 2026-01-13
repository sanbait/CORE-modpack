package com.sanbait.shadowgrid.client;

import com.sanbait.shadowgrid.ShadowGridMod;
import com.sanbait.shadowgrid.world.BiomeGridConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ShadowGridMod.MODID, value = Dist.CLIENT)
public class ParticleBorderHandler {

    private static final int SECTOR_SIZE = BiomeGridConfig.SECTOR_SIZE;
    private static final int HALF_SIZE = SECTOR_SIZE / 2;
    private static final Random random = new Random();
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;

        if (player == null || level == null)
            return;

        // Рендерим каждые 5 тиков для оптимизации
        if (++tickCounter % 5 != 0)
            return;

        BlockPos playerPos = player.blockPosition();
        int renderDist = 128; // Увеличен радиус для видимости издалека

        // Проходим по близким границам
        for (int x = playerPos.getX() - renderDist; x <= playerPos.getX() + renderDist; x += 4) {
            for (int z = playerPos.getZ() - renderDist; z <= playerPos.getZ() + renderDist; z += 4) {
                if (isBorder(x, z)) {
                    // Проверяем есть ли граница между открытым/закрытым сектором
                    if (shouldRenderBorder(x, z)) {
                        // Спавним частицы по вертикали
                        for (int y = playerPos.getY() - 5; y <= playerPos.getY() + 10; y += 1) { // Шаг 1 вместо 2
                            if (random.nextFloat() < 0.5f) { // 50% шанс (было 30%)
                                level.addParticle(
                                        ParticleTypes.FLAME,
                                        x + 0.5 + (random.nextFloat() - 0.5) * 0.3,
                                        y + random.nextFloat(),
                                        z + 0.5 + (random.nextFloat() - 0.5) * 0.3,
                                        0, 0, 0);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isBorder(int x, int z) {
        return ((x + HALF_SIZE) % SECTOR_SIZE == 0) || ((z + HALF_SIZE) % SECTOR_SIZE == 0);
    }

    private static boolean shouldRenderBorder(int x, int z) {
        // ПРАВИЛЬНАЯ проверка: рендерить только если С ОДНОЙ стороны границы open, с
        // ДРУГОЙ closed
        int sectorX = Math.floorDiv(x + HALF_SIZE, SECTOR_SIZE);
        int sectorZ = Math.floorDiv(z + HALF_SIZE, SECTOR_SIZE);

        // Проверяем границу по X
        if ((x + HALF_SIZE) % SECTOR_SIZE == 0) {
            int leftSectorX = sectorX - 1;
            int rightSectorX = sectorX;
            boolean leftUnlocked = ClientGridData.isSectorUnlocked(leftSectorX, sectorZ);
            boolean rightUnlocked = ClientGridData.isSectorUnlocked(rightSectorX, sectorZ);
            // Рендерим если один открыт, другой закрыт
            if (leftUnlocked != rightUnlocked)
                return true;
        }

        // Проверяем границу по Z
        if ((z + HALF_SIZE) % SECTOR_SIZE == 0) {
            int topSectorZ = sectorZ - 1;
            int bottomSectorZ = sectorZ;
            boolean topUnlocked = ClientGridData.isSectorUnlocked(sectorX, topSectorZ);
            boolean bottomUnlocked = ClientGridData.isSectorUnlocked(sectorX, bottomSectorZ);
            // Рендерим если один открыт, другой закрыт
            if (topUnlocked != bottomUnlocked)
                return true;
        }

        return false; // Обе стороны одинаковые (обе открыты или обе закрыты) - н е рендерим
    }
}
