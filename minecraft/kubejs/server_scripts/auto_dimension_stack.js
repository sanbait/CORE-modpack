// MODPACK AUTOMATION - NOT PART OF SHADOW GRID MOD
// This script automatically configures Dimension Stack for all new worlds

PlayerEvents.loggedIn(event => {
    if (event.level.isClientSide()) return;

    let data = event.server.persistentData;

    if (!data.contains('auto_dimstack_v1')) {
        // First login in this world - configure dimension stack
        event.player.tell('§6[Modpack] Auto-configuring Dimension Stack...');

        // Create vertical connections between dimensions
        event.server.runCommandSilent('/portal global connect_floor minecraft:overworld minecraft:the_nether');
        event.server.runCommandSilent('/portal global connect_ceil minecraft:the_nether minecraft:overworld');
        event.server.runCommandSilent('/portal global connect_floor minecraft:the_nether deeperdarker:otherside');
        event.server.runCommandSilent('/portal global connect_ceil deeperdarker:otherside minecraft:the_nether');

        data.putBoolean('auto_dimstack_v1', true);
        event.player.tell('§a[Modpack] Dimension Stack configured! Dig down to travel between dimensions.');
    }
});
