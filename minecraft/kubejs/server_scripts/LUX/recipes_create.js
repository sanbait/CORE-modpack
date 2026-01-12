ServerEvents.recipes(event => {
    // 1. Mixing: Lux Essence
    event.recipes.create.mixing(
        Fluid.of('luxsystem:liquid_lux', 250),
        [
            'minecraft:glowstone_dust',
            Fluid.of('minecraft:water', 250),
            'luxsystem:lux_filter'
        ]
    ).heated()

    // 2. Spouting: Fill Canister
    event.recipes.create.filling(
        'luxsystem:lux_canister_full',
        [
            'luxsystem:lux_canister',
            Fluid.of('luxsystem:liquid_lux', 1000)
        ]
    )

    // 3. Emptying: Drain Canister
    event.recipes.create.emptying(
        [
            'luxsystem:lux_canister',
            Fluid.of('luxsystem:liquid_lux', 1000)
        ],
        'luxsystem:lux_canister_full'
    )
})
