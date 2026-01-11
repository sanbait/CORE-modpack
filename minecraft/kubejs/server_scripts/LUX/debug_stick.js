BlockEvents.rightClicked(event => {
    let player = event.player
    let item = player.mainHandItem

    // DEBUG TOOL: Stick + Shift + RightClick = Print Block ID
    if (item.id === 'minecraft:stick' && player.isCrouching()) {
        event.cancel()
        let block = event.block
        player.tell(Text.of(`§d[DEBUG] Block ID: §f${block.id}`))

        // Also check fluid if present
        let fluid = event.level.getFluidState(block.pos)
        if (!fluid.isEmpty()) {
            player.tell(Text.of(`§b[DEBUG] Fluid State: §f${fluid.type}`))
        }
    }
})
