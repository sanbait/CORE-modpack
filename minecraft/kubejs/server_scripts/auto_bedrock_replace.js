// КРИТИЧНО: bedrockReplacement НЕ РАБОТАЕТ в Immersive Portals 1.20.1 (баг мода)
// АГРЕССИВНОЕ удаление бедрока - удаляем при появлении и через команды

console.info('[Dimension Stack] ===== СКРИПТ ЗАГРУЖЕН =====');

// АГРЕССИВНОЕ удаление бедрока при его появлении
BlockEvents.placed(event => {
    if (event.level.isClientSide()) return;
    
    // Если размещается бедрок - сразу удаляем
    if (event.block.id == 'minecraft:bedrock') {
        event.cancel(); // Отменяем размещение
        
        let pos = event.block.pos;
        let level = event.level;
        let server = event.server;
        
        // Удаляем бедрок через команду (более надежно)
        let dim = level.dimension.location().toString();
        let cmd = `execute in ${dim} run setblock ${pos.x} ${pos.y} ${pos.z} minecraft:air`;
        server.runCommandSilent(cmd);
        
        console.info(`[Dimension Stack] АГРЕССИВНО: Удален бедрок на ${pos.x}, ${pos.y}, ${pos.z} в ${dim}`);
    }
});

// Функция удаления бедрока через команды
function removeBedrock(server, player) {
    console.info('[Dimension Stack] ===== НАЧИНАЮ УДАЛЕНИЕ БЕДРОКА =====');
    
    if (player) {
        player.tell('§6[Dimension Stack] Удаление бедрока...');
    }
    
    try {
        // NETHER - УДАЛЯЕМ БЕДРОК ВЕЗДЕ (0-128 по Y, НЕ ОТРИЦАТЕЛЬНЫЕ!)
        console.info('[Dimension Stack] УДАЛЯЮ БЕДРОК В NETHER (ВСЕ УРОВНИ Y=0-128)...');
        // Простая команда для всего Nether сразу (y=0 до 128, НЕ -128!)
        // Разбиваем только по Y для надежности
        for (let y = 0; y <= 128; y += 10) {
            let endY = Math.min(y + 9, 128);
            let cmd = `execute in minecraft:the_nether run fill -30000 ${y} -30000 30000 ${endY} 30000 minecraft:air replace minecraft:bedrock`;
            server.runCommandSilent(cmd);
            console.info(`[Dimension Stack] Nether: удаляю бедрок на y=${y} до y=${endY}`);
        }
        console.info('[Dimension Stack] Nether: бедрок удален везде (y=0-128, НЕ отрицательные!)');
        
        // OVERWORLD - УДАЛЯЕМ БЕДРОК ВЕЗДЕ (-64 до -50 по Y, ОТРИЦАТЕЛЬНЫЕ!)
        console.info('[Dimension Stack] УДАЛЯЮ БЕДРОК В OVERWORLD (y=-64 до -50)...');
        for (let y = -64; y <= -50; y += 5) {
            let endY = Math.min(y + 4, -50);
            server.runCommandSilent(`execute in minecraft:overworld run fill -30000 ${y} -30000 30000 ${endY} 30000 minecraft:air replace minecraft:bedrock`);
            console.info(`[Dimension Stack] Overworld: удаляю бедрок на y=${y} до y=${endY}`);
        }
        console.info('[Dimension Stack] Overworld: бедрок удален');
        
        // END - УДАЛЯЕМ БЕДРОК ВЕЗДЕ
        console.info('[Dimension Stack] УДАЛЯЮ БЕДРОК В END...');
        for (let y = 0; y <= 70; y += 10) {
            let endY = Math.min(y + 9, 70);
            server.runCommandSilent(`execute in minecraft:the_end run fill -30000 ${y} -30000 30000 ${endY} 30000 minecraft:air replace minecraft:bedrock`);
        }
        console.info('[Dimension Stack] End: бедрок удален');
        
        // DEEPER DARKER - УДАЛЯЕМ БЕДРОК ВЕЗДЕ
        console.info('[Dimension Stack] УДАЛЯЮ БЕДРОК В DEEPER DARKER...');
        for (let y = 0; y <= 128; y += 10) {
            let endY = Math.min(y + 9, 128);
            server.runCommandSilent(`execute in deeperdarker:otherside run fill -30000 ${y} -30000 30000 ${endY} 30000 minecraft:air replace minecraft:bedrock`);
        }
        console.info('[Dimension Stack] Deeper Darker: бедрок удален');
        
        if (player) {
            player.tell('§a[Dimension Stack] ✅ БЕДРОК УДАЛЕН ВЕЗДЕ (y=0-128 в Nether)!');
        }
        console.info('[Dimension Stack] ===== ✅ БЕДРОК УДАЛЕН ВЕЗДЕ =====');
        return true;
    } catch (e) {
        if (player) {
            player.tell('§c[Dimension Stack] ОШИБКА при удалении бедрока!');
        }
        console.error('[Dimension Stack] ОШИБКА при удалении бедрока: ' + e);
        console.error('[Dimension Stack] Стек ошибки: ' + e.stack);
        return false;
    }
}

// ТРИГГЕР 1: При входе игрока
PlayerEvents.loggedIn(event => {
    if (event.level.isClientSide()) return;
    
    let server = event.server;
    let data = server.persistentData;
    
    console.info('[Dimension Stack] ===== ТРИГГЕР 1: ИГРОК ЗАШЕЛ =====');
    
    // Удаляем бедрок при каждом входе игрока (без флага, чтобы работало всегда)
    removeBedrock(server, event.player);
});

// ТРИГГЕР 2: При загрузке сервера
ServerEvents.loaded(event => {
    let server = event.server;
    let data = server.persistentData;
    
    console.info('[Dimension Stack] ===== ТРИГГЕР 2: СЕРВЕР ЗАГРУЖЕН =====');
    
    // Удаляем бедрок при загрузке сервера (без флага, чтобы работало всегда)
    removeBedrock(server, null);
});

