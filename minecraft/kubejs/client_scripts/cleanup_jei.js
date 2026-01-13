// Cleanup JEI Script
// Hides items from JEI that don't fit the Hardcore/Horror theme
// Based on MOD_CLEANUP_AUDIT.md

JEIEvents.hideItems(event => {
    const hiddenItems = [
        // --- Theurgy (Magic Wands) ---
        /theurgy:divination_rod.*/,

        // --- Create ---
        'create:potato_cannon',
        'create:extendo_grip',
        'create:handheld_worldshaper',
        'create:wand_of_symmetry',

        // --- Dark Utilities ---
        /darkutils:charm_.*/,
        'darkutils:ender_tether',
        'darkutils:player_filter',

        // --- Sophisticated Backpacks ---
        'sophisticatedbackpacks:auto_feeding_upgrade',
        'sophisticatedbackpacks:feeding_upgrade',
        'sophisticatedbackpacks:void_upgrade',
        'sophisticatedbackpacks:everlasting_upgrade',
        'sophisticatedbackpacks:advanced_feeding_upgrade',

        // --- Supplementaries ---
        'supplementaries:bubble_blower',
        'supplementaries:slingshot',
        'supplementaries:candy',
        'supplementaries:flute',
        'supplementaries:pancake'
    ]

    hiddenItems.forEach(item => {
        event.hide(item)
    })

    console.info('[EntropyCore] Hidden non-hardcore items from JEI.')
})
