ServerEvents.recipes(event => {
    // Рецепт крафта Зарядника Lux
    event.shaped('luxsystem:lux_charger', [
        'III',
        'ICI',
        'III'
    ], {
        I: 'minecraft:iron_ingot',
        C: 'luxsystem:lux_crystal'
    })
})
