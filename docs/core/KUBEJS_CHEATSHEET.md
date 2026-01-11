# KubeJS 1.20.1 (Forge) Cheat Sheet

Документ для фиксации особенностей синтаксиса KubeJS на версии 1.20.1.
**Цель:** Избегать ошибок с устаревшими методами из 1.16/1.18.

## 1. Регистрация Блоков (`StartupEvents.registry('block')`)

### ❌ УСТАРЕЛО (Не использовать)

* `.material('metal')` — Метод удален.
* `.type('basic')` — Обычно не требуется, `create` создает стандартный блок.

### ✅ АКТУАЛЬНО (Использовать)

* `.soundType('metal')` — Звуки шагов/ломания.
* `.mapColor('metal')` — Цвет на карте.
* `.tagBlock('mineable/pickaxe')` — Инструмент для добычи.
* `.requiresTool(true)` — Требует инструмент для дропа.

**Пример:**

```javascript
event.create('example_block')
    .soundType('wood')
    .mapColor('wood')
    .hardness(1.0)
    .displayName('Example Block')
```

## 2. События (Server Events)

### Предметы и Инвентарь

* Вместо модификации NBT напрямую, часто нужно использовать хелперы `Item.of()`.

### Сущности

* `level.createEntity('minecraft:zombie')` работает, но требует `.spawn()`.
* `.mergeNbt({...})` — надежный способ задать параметры (NoAI, Silent).

## 3. Ссылки и Документация

* Официальная вики (1.19+): <https://kubejs.com/wiki>
* Forge Wrapper: В 1.20 KubeJS сильнее зависит от нативных методов Minecraft/Forge.
