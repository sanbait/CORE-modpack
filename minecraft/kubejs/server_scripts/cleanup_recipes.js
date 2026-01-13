// Cleanup Recipes Script
// Removes recipes for items that don't fit the Hardcore/Horror theme of Entropy Core
// Based on MOD_CLEANUP_AUDIT.md

ServerEvents.recipes(event => {
    // List of items to remove recipes for
    const removedItems = [
        // --- Theurgy (Magic Wands & EZ Magic) ---
        'theurgy:divination_rod_t1',
        'theurgy:divination_rod_t2',
        'theurgy:divination_rod_t3',
        'theurgy:divination_rod_t4',
        'theurgy:sulfur_colored_item', // Often used for replication

        // --- Create (Silly/OP Items) ---
        'create:potato_cannon',
        'create:extendo_grip',
        'create:handheld_worldshaper',
        'create:wand_of_symmetry',

        // --- Dark Utilities (Magic Charms) ---
        'darkutils:charm_sleep',
        'darkutils:charm_experience',
        'darkutils:charm_gluttony',
        'darkutils:charm_agression', // sic
        'darkutils:charm_null',
        'darkutils:charm_portal',
        'darkutils:charm_warding',
        'darkutils:ender_tether',
        'darkutils:player_filter',

        // --- Sophisticated Backpacks (EZ Mode Upgrades) ---
        'sophisticatedbackpacks:auto_feeding_upgrade',
        'sophisticatedbackpacks:feeding_upgrade',
        'sophisticatedbackpacks:void_upgrade',
        'sophisticatedbackpacks:everlasting_upgrade',
        'sophisticatedbackpacks:advanced_feeding_upgrade',

        // --- Supplementaries (Decorative Junk) ---
        // Some of these might be disabled in config, but removing recipes is a safe double-tap
        'supplementaries:bubble_blower',
        'supplementaries:slingshot',
        'supplementaries:candy',
        'supplementaries:flute',
        'supplementaries:pancake'
    ]

    removedItems.forEach(item => {
        event.remove({ output: item })
    })

    // Specific Theurgy Rods by pattern if they have specific IDs
    event.remove({ output: /theurgy:divination_rod.*/ })

    console.log(`[EntropyCore] Removed recipes for ${removedItems.length} non-hardcore items.`)
})
