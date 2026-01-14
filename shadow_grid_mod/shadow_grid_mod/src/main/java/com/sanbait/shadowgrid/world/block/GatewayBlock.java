package com.sanbait.shadowgrid.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GatewayBlock extends Block {
    public GatewayBlock(Properties properties) {
        super(properties);
    }

    @Override
    public net.minecraft.world.InteractionResult use(BlockState state, Level level, BlockPos pos,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand,
            net.minecraft.world.phys.BlockHitResult hit) {
        if (level.isClientSide) {
            return net.minecraft.world.InteractionResult.SUCCESS;
        }

        // Logic to Unlock Sector
        // Assuming we have a GridManager or similar.
        // For now, let's call the server-side command logic or simulate it.
        // Use GridSavedData to unlock manually here? Yes.

        com.sanbait.shadowgrid.world.GridSavedData data = com.sanbait.shadowgrid.world.GridSavedData.get(level);

        // Determine nearby border
        int SECTOR_SIZE = com.sanbait.shadowgrid.world.BiomeGridConfig.SECTOR_SIZE;
        int HALF_SIZE = SECTOR_SIZE / 2;

        int sectorX = Math.floorDiv(pos.getX() + HALF_SIZE, SECTOR_SIZE);
        int sectorZ = Math.floorDiv(pos.getZ() + HALF_SIZE, SECTOR_SIZE);

        int centerX = sectorX * SECTOR_SIZE;
        int centerZ = sectorZ * SECTOR_SIZE;

        int distEast = Math.abs(pos.getX() - (centerX + HALF_SIZE));
        int distWest = Math.abs(pos.getX() - (centerX - HALF_SIZE));
        int distSouth = Math.abs(pos.getZ() - (centerZ + HALF_SIZE));
        int distNorth = Math.abs(pos.getZ() - (centerZ - HALF_SIZE));

        // Determine target sector
        int targetX = sectorX;
        int targetZ = sectorZ;

        // Find closest border
        int minX = Math.min(distEast, distWest);
        int minZ = Math.min(distSouth, distNorth);

        if (minX <= minZ) {
            // Wall is along X-axis border (East or West)
            if (distEast < distWest)
                targetX++;
            else
                targetX--;
        } else {
            // Wall is along Z-axis border (South or North)
            if (distSouth < distNorth)
                targetZ++;
            else
                targetZ--;
        }

        // CHECK IF ALREADY UNLOCKED
        if (data.isSectorUnlocked(targetX, targetZ)) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§eЭтот сектор уже открыт."),
                    true);

            // Still activate visuals for repair
            activateVisuals(level, pos, (net.minecraft.server.level.ServerPlayer) player);
            return net.minecraft.world.InteractionResult.CONSUME;
        }

        // CALCULATE COST
        int unlockedCount = data.getUnlockedSectors().size();
        int cost = calculateCost(unlockedCount);

        // CHECK FOR CURRENCY (from config)
        String currencyItemId = com.sanbait.shadowgrid.config.GatewayConfig.CURRENCY_ITEM.get();
        net.minecraft.resources.ResourceLocation crystalId = new net.minecraft.resources.ResourceLocation(
                currencyItemId);
        net.minecraft.world.item.Item currency = net.minecraftforge.registries.ForgeRegistries.ITEMS
                .getValue(crystalId);

        if (currency == null || currency == net.minecraft.world.item.Items.AIR) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§cОшибка: Lux Crystal не найден!"),
                    true);
            return net.minecraft.world.InteractionResult.FAIL;
        }

        int playerCrystals = player.getInventory().countItem(currency);
        if (playerCrystals < cost) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component
                            .literal("§cТребуется: " + cost + " Lux Crystals (У вас: " + playerCrystals + ")"),
                    true);
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE,
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
            return net.minecraft.world.InteractionResult.FAIL;
        }

        // DEDUCT CRYSTALS
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

        // UNLOCK SECTOR
        data.unlockSector(targetX, targetZ, level);
        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                        "§aОплачено " + cost + " Lux Crystals. Сектор " + targetX + ":" + targetZ + " открыт!"),
                true);
        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.END_PORTAL_SPAWN,
                net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);

        // ACTIVATE VISUALS
        activateVisuals(level, pos, (net.minecraft.server.level.ServerPlayer) player);

        return net.minecraft.world.InteractionResult.CONSUME;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // ЛУЧ В НЕБО (Визуальное дополнение к реальному маяку)
        double xc = pos.getX() + 0.5;
        double zc = pos.getZ() + 0.5;
        double y = pos.getY();

        // 1. Центральный поток частиц (густой)
        for (int i = 0; i < 20; i += 2) {
            if (random.nextFloat() < 0.5f) { // 50% шанс на каждый шаг
                level.addParticle(ParticleTypes.END_ROD, xc, y + i + random.nextDouble(), zc, 0, 0.05, 0);
            }
        }

        // 2. Яркие вспышки у основания
        if (random.nextFloat() < 0.1f) {
            level.addParticle(ParticleTypes.FLASH, xc, y + 1, zc, 0, 0, 0);
        }

        // 3. Магическая аура (портал)
        for (int k = 0; k < 2; k++) {
            level.addParticle(ParticleTypes.PORTAL,
                    xc + (random.nextDouble() - 0.5),
                    y + random.nextDouble() * 2,
                    zc + (random.nextDouble() - 0.5),
                    (random.nextDouble() - 0.5) * 0.5,
                    (random.nextDouble() - 0.5) * 0.5,
                    (random.nextDouble() - 0.5) * 0.5);
        }
    }

    private int calculateCost(int totalUnlocked) {
        int n = totalUnlocked - 1;
        if (n < 0)
            return 0;
        if (n == 0)
            return com.sanbait.shadowgrid.config.GatewayConfig.BASE_COST_1.get();
        if (n == 1)
            return com.sanbait.shadowgrid.config.GatewayConfig.BASE_COST_2.get();
        if (n == 2)
            return com.sanbait.shadowgrid.config.GatewayConfig.BASE_COST_3.get();
        int baseCost4 = com.sanbait.shadowgrid.config.GatewayConfig.BASE_COST_4_PLUS.get();
        return baseCost4 * (int) Math.pow(2, n - 3);
    }

    private void activateVisuals(Level level, BlockPos pos, net.minecraft.server.level.ServerPlayer player) {
        // 1. Local Visual
        BlockPos signalPos = pos;
        while (level.getBlockState(signalPos.above()).getBlock() instanceof GatewayBlock) {
            signalPos = signalPos.above();
        }
        signalPos = signalPos.above();
        boolean placedLocal = level.setBlock(signalPos,
                net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE.defaultBlockState(), 3);
        if (com.sanbait.shadowgrid.config.GatewayConfig.DEBUG_MODE.get()) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§6[DEBUG] Local Fire at: " + signalPos.toShortString() + " Success=" + placedLocal),
                    false);
        }

        // 2. Partner Scan
        if (com.sanbait.shadowgrid.config.GatewayConfig.DEBUG_MODE.get()) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component
                            .literal("§6[DEBUG] Scanning nearby gateways (5 block radius)..."),
                    false);
        }

        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
                for (int dz = -5; dz <= 5; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0)
                        continue;

                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    if (level.getBlockState(checkPos).getBlock() instanceof GatewayBlock) {
                        BlockPos top = checkPos;
                        while (level.getBlockState(top.above()).getBlock() instanceof GatewayBlock) {
                            top = top.above();
                        }

                        BlockPos firePos = top.above();
                        if (level.isLoaded(firePos)) {
                            boolean placed = level.setBlock(firePos,
                                    net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE.defaultBlockState(), 3);
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                                    "§d[DEBUG] Nearby gateway lit at: " + firePos.toShortString() + " Success="
                                            + placed),
                                    false);
                        }
                    }
                }
            }
        }
    }
}
