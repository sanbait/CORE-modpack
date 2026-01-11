ServerEvents.recipes(event => {
    // 1. Mixing: Lux Essence
    event.recipes.create.mixing(
        Fluid.of('luxsystem:liquid_lux', 250),
        [
            'minecraft:glowstone_dust',
            Fluid.of('minecraft:water', 250),
            'kubejs:lux_filter'
        ]
    ).heated()

    // 2. Spouting: Fill Canister
    event.recipes.create.filling(
        'kubejs:lux_canister_full',
        [
            'kubejs:lux_canister',
            Fluid.of('luxsystem:liquid_lux', 1000)
        ]
    )

    // 3. Emptying: Drain Canister
    event.recipes.create.emptying(
        [
            'kubejs:lux_canister',
            Fluid.of('luxsystem:liquid_lux', 1000)
        ],
        'kubejs:lux_canister_full'
    )
})
