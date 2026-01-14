PlayerEvents.loggedIn(event => {
    // Only run on server side
    if (event.level.isClientSide()) return;

    let pData = event.server.persistentData;

    // Check if we initialized the stack
    if (!pData.contains('shadow_grid_stack_init_v2')) {
        console.info('[Shadow Grid] Configuring Dimension Stack for first login...');
        event.player.tell('§e[Shadow Grid] Configuring Dimension Stack (One-Time Setup)...');

        // 1. Overworld (Floor) <-> Nether (Ceiling)
        // Ensure chunks are loaded or just run global command (it handles it)
        event.server.runCommandSilent('/portal global connect_floor minecraft:overworld minecraft:the_nether');
        event.server.runCommandSilent('/portal global connect_ceil minecraft:the_nether minecraft:overworld');

        // 2. Nether (Floor) <-> Deeper Dark (Ceiling)
        event.server.runCommandSilent('/portal global connect_floor minecraft:the_nether deeperdarker:otherside');
        event.server.runCommandSilent('/portal global connect_ceil deeperdarker:otherside minecraft:the_nether');

        // Mark as done
        pData.putBoolean('shadow_grid_stack_init_v2', true);

        event.player.tell('§a[Shadow Grid] Dimension Stack configured successfully!');
    }
});
