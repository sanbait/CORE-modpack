\# Стандарты KubeJS 6.2/6.5 (Minecraft 1.20.1)



\### 1. ГЛАВНОЕ ПРАВИЛО: НИКАКИХ `onEvent`

В версии 1.20.1 используется новый синтаксис `GroupEvents.eventName`. 



❌ \*\*НЕПРАВИЛЬНО:\*\* `onEvent('recipes', event => { ... })`

✅ \*\*ПРАВИЛЬНО:\*\* `ServerEvents.recipes(event => { ... })`



\### 2. СТРУКТУРА СОБЫТИЙ:

\- \*\*Рецепты и Теги:\*\* `ServerEvents.recipes(e => {})` и `ServerEvents.tags('item', e => {})`.

\- \*\*Регистрация блоков/предметов:\*\* `StartupEvents.registry('item', e => {})` или `StartupEvents.registry('block', e => {})`.

\- \*\*Изменение старых предметов:\*\* `ItemEvents.modification(e => {})`.

\- \*\*События игрока:\*\* `PlayerEvents.loggedIn`, `PlayerEvents.chat`, `PlayerEvents.tick`.

\- \*\*События сущностей:\*\* `EntityEvents.death`, `EntityEvents.hurt`, `EntityEvents.spawned`.



\### 3. РАСПОЛОЖЕНИЕ СКРИПТОВ:

\- `kubejs/server\_scripts/` — рецепты, теги, логика мира (перезагрузка через /reload).

\- `kubejs/startup\_scripts/` — новые блоки, предметы, изменение статов (требует перезапуска игры).

\- `kubejs/client\_scripts/` — тултипы, скрытие в JEI.



\### 4. КРИТИЧЕСКИЕ ИЗМЕНЕНИЯ 1.20:

\- \*\*Creative Tabs:\*\* Для добавления предмета в таб используй `StartupEvents.modifyCreativeTab`. Старый метод `item.group('tab')` в билдере больше не работает!

\- \*\*Recipes:\*\* Для удаления рецептов используй `event.remove({output: 'id'})` или `event.remove({id: 'id'})`.

\- \*\*NBT:\*\* Для работы с NBT используй `item.nbt` или `entity.nbt`.



\### 5. ПРОВЕРКА:

Перед написанием кода всегда сверяйся с @docs/KUBEJS\_CHEATSHEET.md, если он есть в проекте.

