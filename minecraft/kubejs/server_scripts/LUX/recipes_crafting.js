ServerEvents.recipes(event => {
    // 1. Lux Filter
    event.shaped(
        Item.of('luxsystem:lux_filter', 2),
        [
            ' S ',
            'PGP',
            ' S '
        ],
        {
            S: 'minecraft:string',
            P: 'minecraft:paper',
            G: 'minecraft:gold_nugget'
        }
    )

    // 2. Lux Canister (Empty)
    event.shaped(
        'luxsystem:lux_canister',
        [
            ' I ',
            'GBG',
            ' I '
        ],
        {
            I: 'minecraft:iron_ingot',
            G: 'minecraft:glass_pane',
            B: 'minecraft:glass_bottle'
        }
    )

    // 3. Guardian Lantern
    event.shaped(
        'kubejs:guardian_lantern',
        [
            ' N ',
            'GCG',
            ' N '
        ],
        {
            N: 'minecraft:iron_nugget',
            G: 'minecraft:glass',
            C: 'luxsystem:lux_canister_full'
        }
    )
})
