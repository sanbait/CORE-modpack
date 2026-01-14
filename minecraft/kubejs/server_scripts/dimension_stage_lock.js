// Dimension Access Control via Game Stages
// Blocks dimension travel BEFORE it happens (not after)
// This is the CORRECT approach - prevents teleportation before it occurs

console.info('[Dimension Lock] ===== СКРИПТ БЛОКИРОВКИ ИЗМЕРЕНИЙ ЗАГРУЖЕН =====');

ForgeEvents.onEvent('net.minecraftforge.event.entity.EntityTravelToDimensionEvent', event => {
    let entity = event.entity;

    // Only check for players
    if (entity.type != 'minecraft:player') return;

    let player = entity;
    let targetDim = event.dimension.location().toString();
    
    console.info(`[Dimension Lock] Игрок ${player.name} пытается попасть в ${targetDim}`);

    // Nether requires stage
    if (targetDim == 'minecraft:the_nether') {
        if (!player.stages.has('unlock_nether')) {
            event.cancel();
            console.info(`[Dimension Lock] Доступ в Ад запрещен для ${player.name}`);
            player.tell('§c§l❌ Доступ запрещен: Ад');
            player.tell('§7Выполните квест для разблокировки доступа в Ад');
            return;
        }
        console.info(`[Dimension Lock] Доступ в Ад разрешен для ${player.name}`);
    }

    // Deep Dark requires stage
    if (targetDim == 'deeperdarker:otherside') {
        if (!player.stages.has('unlock_deep_dark')) {
            event.cancel();
            console.info(`[Dimension Lock] Доступ в Deep Dark запрещен для ${player.name}`);
            player.tell('§c§l❌ Доступ запрещен: Deep Dark');
            player.tell('§7Выполните квест для разблокировки доступа в Deep Dark');
            return;
        }
        console.info(`[Dimension Lock] Доступ в Deep Dark разрешен для ${player.name}`);
    }

    // End requires stage
    if (targetDim == 'minecraft:the_end') {
        if (!player.stages.has('unlock_end')) {
            event.cancel();
            console.info(`[Dimension Lock] Доступ в Край запрещен для ${player.name}`);
            player.tell('§c§l❌ Доступ запрещен: Край');
            player.tell('§7Выполните квест для разблокировки доступа в Край');
            return;
        }
        console.info(`[Dimension Lock] Доступ в Край разрешен для ${player.name}`);
    }
});

// Quest rewards in FTB Quests (command rewards):
// /gamestage add @p unlock_nether
// /gamestage add @p unlock_deep_dark
// /gamestage add @p unlock_end
