package com.sanbait.shadowgrid.world;

import com.sanbait.shadowgrid.ShadowGridMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ShadowGridMod.MODID)
public class BarrierHandler {

    private static final int SECTOR_SIZE = BiomeGridConfig.SECTOR_SIZE; // 512
    private static final int HALF_SIZE = SECTOR_SIZE / 2; // 256

    // Отслеживаем последнюю позицию игрока для проверки изменения блока
    private static final Map<ServerPlayer, BlockPos> lastBlockPos = new HashMap<>();

    // Барьеры через ПОЛЕ - проверяем при движении игрока, не ставим физические
    // блоки!
    // БАРЬЕР ЧЕРЕЗ ПРОВЕРКУ КАЖДЫЙ ТИК - блокируем ПЕРЕД входом!
    @SubscribeEvent
    public static void onLivingTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide)
            return;

        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        // Проверяем каждые 5 тиков (не каждый тик для производительности)
        if (player.tickCount % 5 != 0)
            return;

        BlockPos currentPos = player.blockPosition();

        // 1. Если позиция БЕЗОПАСНАЯ -> сохраняем её как последнюю безопасную
        if (!isBlockedPosition(player.level(), currentPos)) {
            lastBlockPos.put(player, currentPos);
            return;
        }

        // 2. Если позиция ЗАБЛОКИРОВАНА -> откидываем назад к безопасной
        BlockPos lastSafe = lastBlockPos.get(player);

        // Проверяем, что lastSafe действительно безопасная (на всякий случай)
        if (lastSafe != null && isBlockedPosition(player.level(), lastSafe)) {
            lastSafe = null; // Если сохраненная точка тоже плохая - забываем её
        }

        if (lastSafe != null) {
            // Вектор БЕЗОПАСНОСТИ: от нас -> к безопасной точке
            Vec3 direction = Vec3.atCenterOf(lastSafe).subtract(player.position()).normalize();

            // Сила толчка: 0.8 (сильный толчок, отбросит на 3-4 блока)
            player.setDeltaMovement(direction.x * 0.8, 0.4, direction.z * 0.8); // +0.4 вверх чтобы подбросить
            player.hurtMarked = true;

            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§cСтена слишком прочная! Откройте сектор."),
                    true);
        } else {
            // Фолбэк: если нет безопасной точки, толкаем ПРОТИВ взгляда (но тоже сильно)
            Vec3 look = player.getLookAngle();
            player.setDeltaMovement(-look.x * 0.8, 0.4, -look.z * 0.8);
            player.hurtMarked = true;

            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§cДоступ закрыт!"),
                    true);
        }
    }

    // Проверка при телепорте
    @SubscribeEvent
    public static void onEntityTeleport(net.minecraftforge.event.entity.EntityTeleportEvent event) {
        if (event.getEntity().level().isClientSide)
            return;

        if (event.getEntity() instanceof ServerPlayer player) {
            BlockPos targetPos = new BlockPos((int) event.getTargetX(), (int) event.getTargetY(),
                    (int) event.getTargetZ());
            if (isBlockedPosition(player.level(), targetPos)) {
                event.setCanceled(true);
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§cТелепортация заблокирована! Откройте сектор."),
                        true);
            }
        }
    }

    // Проверяет заблокирована ли позиция (через поле, без физических блоков)
    private static boolean isBlockedPosition(net.minecraft.world.level.LevelAccessor level, BlockPos pos) {
        if (!(level instanceof Level l))
            return false;

        GridSavedData data = GridSavedData.get(l);

        // Определяем сектор позиции
        int sectorX = getSectorX(pos.getX());
        int sectorZ = getSectorZ(pos.getZ());

        // Если сектор заблокирован - позиция заблокирована
        if (!data.isSectorUnlocked(sectorX, sectorZ))
            return true;

        // Проверяем границы - граница заблокирована ТОЛЬКО если ОБА сектора
        // заблокированы
        // Если хотя бы один сектор разблокирован - граница открыта
        if (isBorderX(pos.getX())) {
            int sLeft = getSectorX(pos.getX() - 1);
            int sRight = getSectorX(pos.getX());
            // Граница заблокирована только если ОБА сектора заблокированы
            if (!data.isSectorUnlocked(sLeft, sectorZ) && !data.isSectorUnlocked(sRight, sectorZ))
                return true;
        }

        if (isBorderZ(pos.getZ())) {
            int sUp = getSectorZ(pos.getZ() - 1);
            int sDown = getSectorZ(pos.getZ());
            // Граница заблокирована только если ОБА сектора заблокированы
            if (!data.isSectorUnlocked(sectorX, sUp) && !data.isSectorUnlocked(sectorX, sDown))
                return true;
        }

        return false;
    }

    private static int getSectorX(int blockX) {
        return Math.floorDiv(blockX + HALF_SIZE, SECTOR_SIZE);
    }

    private static int getSectorZ(int blockZ) {
        return Math.floorDiv(blockZ + HALF_SIZE, SECTOR_SIZE);
    }

    private static boolean isBorderX(int x) {
        return (x + HALF_SIZE) % SECTOR_SIZE == 0;
    }

    private static boolean isBorderZ(int z) {
        return (z + HALF_SIZE) % SECTOR_SIZE == 0;
    }
}
