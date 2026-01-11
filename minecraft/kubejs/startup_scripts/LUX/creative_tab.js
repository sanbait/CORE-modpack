StartupEvents.registry('creative_mode_tab', event => {
    event.create('lux_system')
        .displayName('Lux System')
        .icon(() => Item.of('kubejs:lux_crystal'))
        .content(() => [
            // KubeJS items (25 предметов)
            'kubejs:lux_canister',
            'kubejs:lux_canister_full',
            'kubejs:lux_crystal',
            'kubejs:concentrated_lux_crystal',
            'kubejs:lux_axe',
            'kubejs:lux_shovel',
            'kubejs:lux_hoe',
            'kubejs:lux_sword',
            'kubejs:lux_bow',
            'kubejs:lux_crossbow',
            'kubejs:lux_helmet',
            'kubejs:lux_chestplate',
            'kubejs:lux_leggings',
            'kubejs:lux_boots',
            'kubejs:lux_flask',
            'kubejs:portable_lux_lantern',
            'kubejs:guardian_lantern',
            'kubejs:lux_scanner',
            'kubejs:lux_wrench',
            'kubejs:lux_filter',
            'kubejs:ancient_lux_vase',
            'kubejs:ancient_lux_orb',
            'kubejs:lux_crystal_fragment',
            'kubejs:ancient_lux_tablet',
            'kubejs:fossilized_lux_amber'
        ])
})
