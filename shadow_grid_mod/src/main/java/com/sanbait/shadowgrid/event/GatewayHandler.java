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
        
        if (event.getState().is(ModBlocks.GATEWAY.get())) {
            // Gateway blocks cannot be broken
            event.setCanceled(true);
            if (event.getPlayer() != null) {
                event.getPlayer().sendSystemMessage(
                    Component.literal("Gateway blocks cannot be destroyed!")
                        .withStyle(ChatFormatting.RED));
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
        // Gateways spawn at specific positions: X=±256, Z=0 (vertical) or X=0, Z=±256 (horizontal)
        // Determine which border this gateway is on by checking position relative to sector boundaries
        
        int targetX = currentSectorX;
        int targetZ = currentSectorZ;
        String dirName = "Unknown";
        
        // Check which border the gateway is on by position
        // Vertical borders: X is at ±256, ±768, etc. (multiples of 256)
        // Horizontal borders: Z is at ±256, ±768, etc.
        int borderX = Math.floorMod(pos.getX(), SECTOR_SIZE);
        int borderZ = Math.floorMod(pos.getZ(), SECTOR_SIZE);
        
        // Gateway is on border if X or Z is close to 0 or 256 (sector boundary)
        // Check if we're on a vertical border (X is at boundary)
        if (borderX < 2 || borderX > (SECTOR_SIZE - 2)) {
            // On vertical border
            if (pos.getX() >= 0) {
                // East border - unlock East neighbor
                targetX = currentSectorX + 1;
                dirName = "EAST";
            } else {
                // West border - unlock West neighbor
                targetX = currentSectorX - 1;
                dirName = "WEST";
            }
        } else if (borderZ < 2 || borderZ > (SECTOR_SIZE - 2)) {
            // On horizontal border
            if (pos.getZ() >= 0) {
                // South border - unlock South neighbor
                targetZ = currentSectorZ + 1;
                dirName = "SOUTH";
            } else {
                // North border - unlock North neighbor
                targetZ = currentSectorZ - 1;
                dirName = "NORTH";
            }
        } else {
            // Fallback: determine by distance from center
            int sectorCenterX = (currentSectorX * SECTOR_SIZE);
            int sectorCenterZ = (currentSectorZ * SECTOR_SIZE);
            int dx = pos.getX() - sectorCenterX;
            int dz = pos.getZ() - sectorCenterZ;
            
            if (Math.abs(dx) > Math.abs(dz)) {
                targetX = dx > 0 ? currentSectorX + 1 : currentSectorX - 1;
                dirName = dx > 0 ? "EAST" : "WEST";
            } else {
                targetZ = dz > 0 ? currentSectorZ + 1 : currentSectorZ - 1;
                dirName = dz > 0 ? "SOUTH" : "NORTH";
            }
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
        net.minecraft.resources.ResourceLocation crystalId = new net.minecraft.resources.ResourceLocation("luxsystem", "lux_crystal");
        net.minecraft.world.item.Item currency = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(crystalId);

        if (currency == null || currency == net.minecraft.world.item.Items.AIR) {
            player.sendSystemMessage(
                    Component.literal("Error: 'luxsystem:lux_crystal' item not found! Make sure LuxSystem mod is installed.")
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
        data.unlockSector(targetX, targetZ);
        player.sendSystemMessage(
                Component.literal("PAID " + cost + " LUX CRYSTALS. UNLOCKED REGION: " + dirName + " (" + targetX + ":" + targetZ + ")")
                        .withStyle(ChatFormatting.GREEN));
        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.END_PORTAL_SPAWN,
                net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
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
