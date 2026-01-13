BlockEvents.rightClicked(event => {
    let player = event.player
    let item = player.mainHandItem
    let block = event.block

    // DEBUG TOOL: Stick + Shift + RightClick on ANY block
    if (item.id === 'minecraft:stick' && player.isCrouching()) {
        event.cancel()

        player.tell(Text.of(`§d--- BLOCK DEBUG ---`))
        player.tell(Text.of(`§bID: §f${block.id}`))
        player.tell(Text.of(`§bPos: §f${block.x}, ${block.y}, ${block.z}`))

        if (block.entity) {
            player.tell(Text.of(`§a[TileEntity Detected]`))

            // Try standard entityData (KubeJS 1.16/1.18 style)
            if (block.entityData) {
                player.tell(Text.of(`§eEntityData: §f${block.entityData}`))
            } else {
                player.tell(Text.of(`§cNo .entityData`))
            }

            // Try persistentData (KubeJS 1.19/1.20 style)
            // Note: Direct access might differ, printing object keys if possible
        } else {
            player.tell(Text.of(`§c[No TileEntity]`))
        }
        player.tell(Text.of(`§d-------------------`))
    }
})
