package com.sanbait.shadowgrid.event;

import com.sanbait.shadowgrid.ShadowGridMod;
import com.sanbait.shadowgrid.registry.ModBlocks;
import com.sanbait.shadowgrid.world.GridSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShadowGridMod.MODID)
public class GatewayHandler {

    // private static final int SECTOR_SIZE = 512; (Use BiomeGridConfig)

    @SubscribeEvent
    public static void onBlockBreak(net.minecraftforge.event.level.BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof net.minecraft.world.level.Level level) || level.isClientSide)
            return;

        BlockPos pos = event.getPos();

        // Проверяем сам Gateway блок
        if (event.getState().is(ModBlocks.GATEWAY.get())) {
            event.setCanceled(true);
            if (event.getPlayer() != null) {
                event.getPlayer().sendSystemMessage(
                        Component.literal("Gateway blocks cannot be destroyed!")
                                .withStyle(ChatFormatting.RED));
            }
            return;
        }

        // Проверяем блоки структуры Gateway (в радиусе 5x5x5 блоков от Gateway)
        // Ищем ближайший Gateway блок в радиусе
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
                for (int dz = -5; dz <= 5; dz++) {
                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    if (level.getBlockState(checkPos).is(ModBlocks.GATEWAY.get())) {
                        // Найден Gateway блок рядом - защищаем все блоки структуры
                        event.setCanceled(true);
                        if (event.getPlayer() != null) {
                            event.getPlayer().sendSystemMessage(
                                    Component.literal("Gateway structure cannot be destroyed!")
                                            .withStyle(ChatFormatting.RED));
                        }
                        return;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide || event.getHand() != InteractionHand.MAIN_HAND)
            return;

        if (event.getLevel().getBlockState(event.getPos()).is(ModBlocks.GATEWAY.get())) {
            handleGatewayClick(event.getLevel(), event.getPos(), (ServerPlayer) event.getEntity());
            event.setCanceled(true); // Consume logic
        }
    }

    private static void handleGatewayClick(Level level, BlockPos pos, ServerPlayer player) {
        GridSavedData data = GridSavedData.get(level);
        final int SECTOR_SIZE = com.sanbait.shadowgrid.world.BiomeGridConfig.SECTOR_SIZE;
        final int HALF_SIZE = SECTOR_SIZE / 2;

        // 1. Determine the sector where the gateway is located
        int currentSectorX = Math.floorDiv(pos.getX() + HALF_SIZE, SECTOR_SIZE);
        int currentSectorZ = Math.floorDiv(pos.getZ() + HALF_SIZE, SECTOR_SIZE);

        // 2. Gateway is on the BORDER - determine direction by which border it's on
        // Gateways spawn at specific positions: X=±256, Z=0 (vertical) or X=0, Z=±256
        // (horizontal)
        // Determine which border this gateway is on by checking position relative to
        // sector boundaries

        // 2. Gateway is on the BORDER - Determine which sector to unlock
        // Logic: The gateway connects two sectors. We should unlock the one that is
        // currently LOCKED.

        int targetX = currentSectorX;
        int targetZ = currentSectorZ;
        String dirName = "Unknown";

        // Define neighbors
        int east = currentSectorX + 1;
        int west = currentSectorX - 1;
        int south = currentSectorZ + 1;
        int north = currentSectorZ - 1;

        // Check which border we are closest to
        // Check which border we are closest to (Relative to sector local coords 0..511)
        int borderX = Math.floorMod(pos.getX() + HALF_SIZE, SECTOR_SIZE);
        int borderZ = Math.floorMod(pos.getZ() + HALF_SIZE, SECTOR_SIZE);

        // Distances to borders (0/512 are borders)
        // 0 is West/North Edge of the sector
        // 511 is East/South Edge of the sector
        int distToWest = borderX;
        int distToEast = SECTOR_SIZE - 1 - borderX;
        int distToNorth = borderZ;
        int distToSouth = SECTOR_SIZE - 1 - borderZ;

        // Find minimum distance to identify the face
        int minX = Math.min(distToEast, distToWest);
        int minZ = Math.min(distToSouth, distToNorth);

        boolean isVerticalBorder = minX < minZ;

        // Candidate sectors
        int sectorA = currentSectorX;
        int sectorB = currentSectorX;
        int sectorsZ_A = currentSectorZ;
        int sectorsZ_B = currentSectorZ;

        if (isVerticalBorder) {
            // We are on a vertical border (East/West)
            // Determine the OTHER sector logic
            // Ideally, we just check which neighbor is relevant.

            if (distToEast < distToWest) {
                // Near East (Positive X relative to sector start) -> Connects Current and East
                // Neighbor
                sectorB = currentSectorX + 1;
                dirName = "EAST";
            } else {
                // Near West -> Connects Current and West Neighbor
                // Caution: If we are at West edge of Sector 0 (pos -250), neighbor is -1.
                // If pos is -270 (Sector -1), West edge is -768.
                // Wait. 'borderX' near 0 means West edge of the block's sector.
                sectorB = currentSectorX - 1;
                dirName = "WEST";
            }
        } else {
            // Horizontal border
            if (distToSouth < distToNorth) {
                sectorA = currentSectorX; // Just use sectorA/B for X/Z logic separation? No.

                // South -> Current and South Neighbor
                sectorsZ_B = currentSectorZ + 1;
                dirName = "SOUTH";
            } else {
                sectorsZ_B = currentSectorZ - 1;
                dirName = "NORTH";
            }
            // Sync X
            sectorB = sectorA;
        }

        // Now we have two sectors:
        // 1. (currentSectorX, currentSectorZ)
        // 2. (sectorB, sectorsZ_B)

        boolean currentLocked = !data.isSectorUnlocked(currentSectorX, currentSectorZ);
        boolean neighborLocked = !data.isSectorUnlocked(sectorB, sectorsZ_B);

        if (neighborLocked) {
            // Priority: Unlock the neighbor
            targetX = sectorB;
            targetZ = sectorsZ_B;
        } else if (currentLocked) {
            // If neighbor is unlocked but we are in a locked sector (unlikely but
            // possible), unlock current
            targetX = currentSectorX;
            targetZ = currentSectorZ;
        } else {
            // Both unlocked. Default to neighbor to show "Already unlocked" message for the
            // intended target
            targetX = sectorB;
            targetZ = sectorsZ_B;
        }

        // 3. CHECK IF ALREADY UNLOCKED - PREVENT DOUBLE SPENDING!
        if (data.isSectorUnlocked(targetX, targetZ)) {
            player.sendSystemMessage(
                    Component.literal("Region " + dirName + " (" + targetX + ":" + targetZ + ") is already unlocked!")
                            .withStyle(ChatFormatting.YELLOW));
            // Sync to client
            com.sanbait.shadowgrid.network.ShadowNetwork
                    .sendToPlayer(new com.sanbait.shadowgrid.network.PacketSyncGrid(data.getUnlockedSectors()), player);
            return;
        }

        // 4. Calculate Cost and check crystals
        int unlockedCount = data.getUnlockedSectors().size();
        int cost = calculateCost(unlockedCount);

        // Item Lookup (Soft dependency)
        net.minecraft.resources.ResourceLocation crystalId = new net.minecraft.resources.ResourceLocation("luxsystem",
                "lux_crystal");
        net.minecraft.world.item.Item currency = net.minecraftforge.registries.ForgeRegistries.ITEMS
                .getValue(crystalId);

        if (currency == null || currency == net.minecraft.world.item.Items.AIR) {
            player.sendSystemMessage(
                    Component.literal(
                            "Error: 'luxsystem:lux_crystal' item not found! Make sure LuxSystem mod is installed.")
                            .withStyle(ChatFormatting.RED));
            return;
        }

        int playerCrystals = player.getInventory().countItem(currency);
        if (playerCrystals < cost) {
            player.sendSystemMessage(Component
                    .literal("LOCKED! Requires: " + cost + " Lux Crystals (You have " + playerCrystals + ")")
                    .withStyle(ChatFormatting.RED));
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE,
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
            return;
        }

        // 5. DOUBLE CHECK - prevent race condition
        if (data.isSectorUnlocked(targetX, targetZ)) {
            player.sendSystemMessage(
                    Component.literal("Region was just unlocked by another player!")
                            .withStyle(ChatFormatting.YELLOW));
            com.sanbait.shadowgrid.network.ShadowNetwork
                    .sendToPlayer(new com.sanbait.shadowgrid.network.PacketSyncGrid(data.getUnlockedSectors()), player);
            return;
        }

        // 6. Deduct crystals
        int crystalsFound = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == currency) {
                int take = Math.min(stack.getCount(), cost - crystalsFound);
                stack.shrink(take);
                crystalsFound += take;
                if (crystalsFound >= cost)
                    break;
            }
        }

        // 7. Unlock
        data.unlockSector(targetX, targetZ, level);

        // 8. SYNC TO CLIENT IMMEDIATELY - CRITICAL!
        com.sanbait.shadowgrid.network.ShadowNetwork
                .sendToPlayer(new com.sanbait.shadowgrid.network.PacketSyncGrid(data.getUnlockedSectors()), player);

        player.sendSystemMessage(
                Component
                        .literal("PAID " + cost + " LUX CRYSTALS. UNLOCKED REGION: " + dirName + " (" + targetX + ":"
                                + targetZ + ")")
                        .withStyle(ChatFormatting.GREEN));
        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.END_PORTAL_SPAWN,
                net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);

        // VISUAL UPDATE: Set Campfire
        level.setBlock(pos, net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE.defaultBlockState(), 3);
    }

    private static int calculateCost(int totalUnlocked) {
        // Base count is 1 (0:0).
        // If totalUnlocked == 1 (Only start), we are unlocking the 1st extra region.
        // Sequence requested: 10, 50, 100, 200, ...

        // n is the index of the "Extra" region we are buying (0-based)
        // if totalUnlocked is 1, n = 0.
        // if totalUnlocked is 2, n = 1.
        int n = totalUnlocked - 1;

        if (n < 0)
            return 0; // Should not happen

        if (n == 0)
            return 10;
        if (n == 1)
            return 50;
        if (n == 2)
            return 100;

        // For n >= 3 (4th unlock and beyond), double the previous or 200 * 2^(n-3)?
        // "четвертый 200 итд" -> 4th is 200.
        // n=3 -> 200.
        // n=4 -> 400.
        return 200 * (int) Math.pow(2, n - 3);
    }
}
