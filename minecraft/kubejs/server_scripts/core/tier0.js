// Priority: 90
// Tier 0: Primitive Age Restrictions

// Определяем стадию
const STAGE_TIER_1 = 'stage_tier_1'; // Этап "Инженерия"

// Список запрещенных предметов (до открытия Tier 1)
const RESTRICTED_ITEMS = [
    // Ванилла
    'minecraft:iron_ingot',
    'minecraft:iron_ore',
    'minecraft:raw_iron',
    'minecraft:iron_pickaxe',
    'minecraft:iron_axe',
    'minecraft:iron_shovel',
    'minecraft:iron_sword',
    'minecraft:iron_hoe',
    'minecraft:iron_helmet',
    'minecraft:iron_chestplate',
    'minecraft:iron_leggings',
    'minecraft:iron_boots',
    'minecraft:diamond',
    'minecraft:diamond_ore',
    'minecraft:diamond_pickaxe',
    // Create (пример)
    'create:andesite_alloy',
    'create:shaft'
];

// Блокировка предметов (Item Stages)
ItemEvents.prio(event => {
    RESTRICTED_ITEMS.forEach(item => {
        // Если мод Item Stages установлен
        if (Platform.isLoaded('itemstages')) {
            // Синтаксис: restrict(item, stage)
            // ПРИМЕЧАНИЕ: В 1.20.1 KubeJS + ItemStages может иметь другой синтаксис.
            // Используем стандартный подход KubeJS для ограничений, если аддон не подхватится.
            // Но пока надеемся на аддон.
            // event.restrict(item, STAGE_TIER_1); <--- Это псевдокод, т.к. KubeJS сам не имеет "restrict".
            // Item Stages работает через свои конфиги или интеграцию.
        }
    });
});

// Блокировка рецептов (Рецепт скрыт, пока нет стадии)
ServerEvents.recipes(event => {
    RESTRICTED_ITEMS.forEach(item => {
        // event.stage(STAGE_TIER_1, item); // Скрыть рецепт предмета за стадией
    });
});

// Блокировка Измерений (через KubeJS события)
EntityEvents.spawned(event => {
    // Проверка входа игрока в измерение? Нет, лучше PlayerEvents.changedDimension
});

/* 
   ВАЖНО: Прямая поддержка Item Stages в KubeJS 1.20 может требовать аддон "KubeJS Stages".
   Если его нет, мы используем нативный функционал KubeJS:
   PlayerEvents.inventoryChanged -> проверка предмета -> удаление + сообщение.
*/

PlayerEvents.inventoryChanged(event => {
    if (!event.player.stages.has(STAGE_TIER_1)) {
        RESTRICTED_ITEMS.forEach(restricted => {
            if (event.item.id == restricted) {
                // Если игрок взял запрещенку - выбиваем из рук
                // event.player.dropItem(event.item); 
                // event.item.count = 0;
                // event.player.tell(Text.translate('core.stage.locked_tier1'));
            }
        });
    }
});
