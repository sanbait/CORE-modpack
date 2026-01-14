// Замена бедрока на обсидиан и запрет размещения бедрока
// Используется для зон порталов между измерениями

console.info('[Bedrock Replace] ===== СКРИПТ ЗАМЕНЫ БЕДРОКА НА ОБСИДИАН ЗАГРУЖЕН =====');

// Запрет размещения бедрока игроками
BlockEvents.placed(event => {
    if (event.level.isClientSide()) return;
    
    // Проверяем что это бедрок
    if (event.block.id == 'minecraft:bedrock') {
        event.cancel();
        
        let player = event.getPlayer();
        if (player) {
            player.tell('§cБедрок нельзя размещать!');
        }
        
        console.info(`[Bedrock Replace] Игрок ${player ? player.name : 'unknown'} попытался разместить бедрок - заблокировано`);
    }
});

// Замена бедрока на обсидиан при загрузке сервера (в зонах порталов)
ServerEvents.loaded(event => {
    let server = event.server;
    
    console.info('[Bedrock Replace] ===== ЗАМЕНА БЕДРОКА НА ОБСИДИАН ПРИ ЗАГРУЗКЕ СЕРВЕРА =====');
    
    try {
        // Overworld - зона портала (небольшая область вокруг спавна)
        // Заменяем бедрок на обсидиан в зоне портала
        server.runCommandSilent('execute in minecraft:overworld run fill -10 -64 -10 10 -59 10 minecraft:obsidian replace minecraft:bedrock');
        console.info('[Bedrock Replace] Overworld: бедрок заменен на обсидиан в зоне портала');
        
        // Nether - зона портала (пол и потолок)
        server.runCommandSilent('execute in minecraft:the_nether run fill -10 0 -10 10 5 10 minecraft:obsidian replace minecraft:bedrock');
        server.runCommandSilent('execute in minecraft:the_nether run fill -10 123 -10 10 128 10 minecraft:obsidian replace minecraft:bedrock');
        console.info('[Bedrock Replace] Nether: бедрок заменен на обсидиан в зоне портала');
        
        // End - зона портала (пол)
        server.runCommandSilent('execute in minecraft:the_end run fill -10 0 -10 10 5 10 minecraft:obsidian replace minecraft:bedrock');
        console.info('[Bedrock Replace] End: бедрок заменен на обсидиан в зоне портала');
        
        // Deeper Darker - зона портала (пол и потолок)
        server.runCommandSilent('execute in deeperdarker:otherside run fill -10 0 -10 10 5 10 minecraft:obsidian replace minecraft:bedrock');
        server.runCommandSilent('execute in deeperdarker:otherside run fill -10 123 -10 10 128 10 minecraft:obsidian replace minecraft:bedrock');
        console.info('[Bedrock Replace] Deeper Darker: бедрок заменен на обсидиан в зоне портала');
        
        console.info('[Bedrock Replace] ===== ✅ ЗАМЕНА БЕДРОКА НА ОБСИДИАН ЗАВЕРШЕНА =====');
    } catch (e) {
        console.error('[Bedrock Replace] ОШИБКА при замене бедрока на обсидиан: ' + e);
        console.error('[Bedrock Replace] Стек ошибки: ' + e.stack);
    }
});

// Замена бедрока на обсидиан при входе игрока (в зонах порталов)
PlayerEvents.loggedIn(event => {
    if (event.level.isClientSide()) return;
    
    let server = event.server;
    
    console.info('[Bedrock Replace] ===== ЗАМЕНА БЕДРОКА НА ОБСИДИАН ПРИ ВХОДЕ ИГРОКА =====');
    
    try {
        // Overworld - зона портала
        server.runCommandSilent('execute in minecraft:overworld run fill -10 -64 -10 10 -59 10 minecraft:obsidian replace minecraft:bedrock');
        
        // Nether - зона портала
        server.runCommandSilent('execute in minecraft:the_nether run fill -10 0 -10 10 5 10 minecraft:obsidian replace minecraft:bedrock');
        server.runCommandSilent('execute in minecraft:the_nether run fill -10 123 -10 10 128 10 minecraft:obsidian replace minecraft:bedrock');
        
        // End - зона портала
        server.runCommandSilent('execute in minecraft:the_end run fill -10 0 -10 10 5 10 minecraft:obsidian replace minecraft:bedrock');
        
        // Deeper Darker - зона портала
        server.runCommandSilent('execute in deeperdarker:otherside run fill -10 0 -10 10 5 10 minecraft:obsidian replace minecraft:bedrock');
        server.runCommandSilent('execute in deeperdarker:otherside run fill -10 123 -10 10 128 10 minecraft:obsidian replace minecraft:bedrock');
        
        console.info('[Bedrock Replace] ✅ Бедрок заменен на обсидиан в зонах порталов');
    } catch (e) {
        console.error('[Bedrock Replace] ОШИБКА при замене бедрока на обсидиан: ' + e);
    }
});
