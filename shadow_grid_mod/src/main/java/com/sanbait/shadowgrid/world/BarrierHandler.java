package com.sanbait.shadowgrid.world;

import com.sanbait.shadowgrid.ShadowGridMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

    // Барьеры через ПОЛЕ - проверяем при движении игрока, не ставим физические блоки!
    // НЕ ИСПОЛЬЗУЕМ ТИКИ! Только события!

    @SubscribeEvent
    public static void onEntitySectionChange(EntityEvent.EnteringSection event) {
        if (event.getEntity().level().isClientSide)
            return;
        
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        
        // Проверяем когда игрок меняет секцию чанка (двигается)
        BlockPos currentPos = player.blockPosition();
        BlockPos lastPos = lastBlockPos.get(player);
        
        // Если позиция изменилась (игрок двигается)
        if (lastPos == null || !lastPos.equals(currentPos)) {
            lastBlockPos.put(player, currentPos);
            
            // Если игрок в заблокированном секторе - телепортируем назад
            if (isBlockedPosition(player.level(), currentPos)) {
                teleportPlayerBack(player, currentPos);
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLogin(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            lastBlockPos.put(player, player.blockPosition());
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            lastBlockPos.remove(player);
        }
    }
    
    // Телепортируем игрока назад к ближайшему разблокированному сектору
    private static void teleportPlayerBack(ServerPlayer player, BlockPos blockedPos) {
        GridSavedData data = GridSavedData.get(player.level());
        
        // Ищем ближайший разблокированный сектор
        BlockPos safePos = findNearestUnlockedPosition(player.level(), blockedPos, data);
        
        if (safePos != null) {
            // Телепортируем игрока назад
            player.teleportTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
            player.setDeltaMovement(0, 0, 0);
            
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§cСтена слишком прочная! Откройте сектор."), 
                true
            );
        }
    }
    
    // Находит ближайшую разблокированную позицию
    private static BlockPos findNearestUnlockedPosition(Level level, BlockPos from, GridSavedData data) {
        int fromSectorX = getSectorX(from.getX());
        int fromSectorZ = getSectorZ(from.getZ());
        
        // Проверяем соседние секторы
        int[] offsets = {-1, 0, 1};
        BlockPos bestPos = null;
        double bestDist = Double.MAX_VALUE;
        
        for (int dx : offsets) {
            for (int dz : offsets) {
                int checkX = fromSectorX + dx;
                int checkZ = fromSectorZ + dz;
                
                if (data.isSectorUnlocked(checkX, checkZ)) {
                    // Центр разблокированного сектора
                    int centerX = checkX * SECTOR_SIZE;
                    int centerZ = checkZ * SECTOR_SIZE;
                    BlockPos centerPos = new BlockPos(centerX, from.getY(), centerZ);
                    
                    double dist = from.distSqr(centerPos);
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestPos = centerPos;
                    }
                }
            }
        }
        
        return bestPos != null ? bestPos : from; // Если не нашли - возвращаем исходную (не должно быть)
    }
    
    // Проверка при телепорте
    @SubscribeEvent
    public static void onEntityTeleport(net.minecraftforge.event.entity.EntityTeleportEvent event) {
        if (event.getEntity().level().isClientSide)
            return;
        
        if (event.getEntity() instanceof ServerPlayer player) {
            BlockPos targetPos = new BlockPos((int)event.getTargetX(), (int)event.getTargetY(), (int)event.getTargetZ());
            if (isBlockedPosition(player.level(), targetPos)) {
                event.setCanceled(true);
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§cТелепортация заблокирована! Откройте сектор."), 
                    true
                );
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide())
            return;

        BlockPos pos = event.getPos();
        // Если блок на границе заблокированного сектора - отменяем
        if (isBlockedPosition(event.getLevel(), pos)) {
            event.setCanceled(true);
            if (event.getPlayer() != null) {
                event.getPlayer().displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§cСтена слишком прочная! Откройте сектор."), 
                    true
                );
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide())
            return;

        BlockPos pos = event.getPos();
        if (isBlockedPosition(event.getLevel(), pos)) {
            event.setCanceled(true);
            if (event.getEntity() instanceof Player player) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§cСтена слишком прочная! Откройте сектор."), 
                    true
                );
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
        
        // Проверяем границы - граница заблокирована ТОЛЬКО если ОБА сектора заблокированы
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
