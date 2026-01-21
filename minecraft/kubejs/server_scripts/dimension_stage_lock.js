// Dimension Access Control via Game Stages
// Uses direct Forge Event Bus access to avoid KubeJS version compatibility issues

const $MinecraftForge = Java.loadClass('net.minecraftforge.common.MinecraftForge')
const $EntityTravelToDimensionEvent = Java.loadClass('net.minecraftforge.event.entity.EntityTravelToDimensionEvent')

console.info('[Dimension Lock] ===== СКРИПТ БЛОКИРОВКИ ИЗМЕРЕНИЙ (NATIVE/DIRECT) =====');

// Register listener directly on Forge Event Bus
$MinecraftForge.EVENT_BUS.addListener(event => {
    // Filter for the specific event we care about
    if (event.getClass().getName() == 'net.minecraftforge.event.entity.EntityTravelToDimensionEvent') {
        let entity = event.getEntity()

        // Only check for players
        if (entity.getType() != 'minecraft:player') return;

        let targetDim = event.getDimension().location().toString();
        let player = entity; // In this context, entity is the player (ServerPlayer)

        // Utility to check stage (using KubeJS wrapper if possible, or string check)
        // KubeJS wraps the entity, so .stages might work if 'entity' is wrapped.
        // If 'entity' is raw Java object, we need to use GameStages API or data.
        // Safest is to try KubeJS wrapper:
        let kjsPlayer = entity;
        // Note: Raw event entity might not have .stages helper.
        // We can re-wrap it or use NBT. 
        // But usually KubeJS wraps event arguments? 
        // No, direct addListener gives raw Java event.
        // We need to be careful.

        // Let's try to get KubeJS player wrapper
        // Use NBT or Scoreboard Tags as fallback if stages fail?
        // Actually, GameStages usually adds a capability.
        // But KubeJS usually patches the Entity class.

        // Let's try accessing .stages (it might work on raw object due to mixin).
        // If not, we can check a persistent data tag.
        // Or simply: player.getTags().contains('stage:unlock_nether')? (KubeJS GameStages uses capability)

        // DEBUG: console.info('Player Class: ' + player.getClass().getName())

        // Better approach: Use server.getPlayer(name) to get KubeJS wrapped player?
        // We can get server from entity.
        let server = entity.getServer();
        if (server) {
            let kjsPlayerFound = server.getPlayer(entity.getUUID());
            if (kjsPlayerFound) {
                player = kjsPlayerFound;
            }
        }

        // Log attempt
        console.info(`[Dimension Lock] Игрок ${player.getName().getString()} пытается попасть в ${targetDim}`);

        // Nether check
        if (targetDim == 'minecraft:the_nether') {
            if (!player.stages.has('unlock_nether')) {
                event.setCanceled(true);
                player.tell(Component.literal('§c§l❌ Доступ запрещен: Ад'));
                player.tell(Component.literal('§7Выполните квест для разблокировки доступа в Ад'));
                return;
            }
        }

        // Deep Dark check
        if (targetDim == 'deeperdarker:otherside') {
            if (!player.stages.has('unlock_deep_dark')) {
                event.setCanceled(true);
                player.tell(Component.literal('§c§l❌ Доступ запрещен: Deep Dark'));
                player.tell(Component.literal('§7Выполните квест для разблокировки доступа в Deep Dark'));
                return;
            }
        }

        // End check
        if (targetDim == 'minecraft:the_end') {
            if (!player.stages.has('unlock_end')) {
                event.setCanceled(true);
                player.tell(Component.literal('§c§l❌ Доступ запрещен: Край'));
                player.tell(Component.literal('§7Выполните квест для разблокировки доступа в Край'));
                return;
            }
        }
    }
});
