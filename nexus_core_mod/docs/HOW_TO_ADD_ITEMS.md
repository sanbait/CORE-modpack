image.png# 📦 Руководство: Как добавить новый предмет с механиками

**Версия:** 1.1  
**Для:** Nexus Core Mod (Forge 1.20.1)

---

## ⚠️ КРИТИЧЕСКИ ВАЖНО: Обязательные шаги

**При добавлении ЛЮБОГО нового предмета, блока или рецепта:**

1. ✅ **ОБЯЗАТЕЛЬНО** добавить в Creative Tab (`LuxSystem.java` → метод `addCreative()`)
2. ✅ **ОБЯЗАТЕЛЬНО** добавить информацию в JEI (`kubejs/client_scripts/LUX/jei_info.js`)
3. ✅ **ОБЯЗАТЕЛЬНО** проверить, что рецепты видны в JEI после `/reload`

**Без этих шагов предмет/блок/рецепт не будет доступен игрокам!**

---

## 🎯 Шаг 1: Определи тип предмета

### Вариант A: Простой предмет (без механик)
- Просто ресурс/крафт-материал
- Пример: `lux_crystal`, `ancient_lux_orb`

### Вариант B: Предмет с Lux (энергия)
- Нужна зарядка от Ядра
- Пример: `lux_sword`, `lux_pickaxe`, `lux_armor`

### Вариант C: Предмет с кастомной механикой
- Специальные эффекты/действия
- Пример: ведро с жидкостью, инструмент с бонусами

---

## 📝 Шаг 2: Создай класс предмета (если нужна механика)

### Пример 1: Простой предмет (БЕЗ класса)

**Не создавай класс!** Просто добавь в `ModItems.java`:

```java
public static final RegistryObject<Item> MY_NEW_ITEM = ITEMS.register("my_new_item",
    () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
```

### Пример 2: Предмет с Lux (с классом)

**Создай файл:** `src/main/java/com/sanbait/luxsystem/items/MyLuxItem.java`

```java
package com.sanbait.luxsystem.items;

import com.sanbait.luxsystem.capabilities.ILuxStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MyLuxItem extends Item implements ILuxStorage {
    private final int capacity = 1000; // Максимум Lux

    public MyLuxItem(Properties properties) {
        super(properties);
    }

    // ⚠️ ОБЯЗАТЕЛЬНО: Статические методы для работы с Lux
    public static int getLux(ItemStack stack) {
        return stack.getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP)
                .map(com.sanbait.luxsystem.capabilities.ILuxStorage::getLuxStored)
                .orElse(0);
    }

    public static void setLux(ItemStack stack, int amount) {
        stack.getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP).ifPresent(cap -> {
            if (cap instanceof com.sanbait.luxsystem.capabilities.LuxCapability impl) {
                impl.setLux(amount);
            }
        });
    }

    // ⚠️ ОБЯЗАТЕЛЬНО: Stub-методы интерфейса ILuxStorage
    @Override
    public int getLuxStored() {
        return 0; // Не используется напрямую
    }

    @Override
    public int getMaxLuxStored() {
        return capacity;
    }

    @Override
    public int receiveLux(int maxReceive, boolean simulate) {
        return 0; // Работает через Capability
    }

    @Override
    public int extractLux(int maxExtract, boolean simulate) {
        return 0; // Работает через Capability
    }

    // Визуальная полоска Lux
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getLux(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int max = getMaxLuxStored(stack);
        if (max <= 0) return 0;
        return Math.round(13.0F * (float) getLux(stack) / max);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFFFF00; // Желтый/золотой
    }

    // Подсказка в тултипе
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, 
                                List<Component> tooltip, TooltipFlag flag) {
        if (com.sanbait.nexuscore.util.ClientHooks.isShiftDown()) {
            // Детальная информация при зажатом Shift
            int currentLux = getLux(stack);
            int maxLux = getMaxLuxStored(stack);
            tooltip.add(Component.literal("Lux: " + currentLux + " / " + maxLux)
                    .withStyle(ChatFormatting.AQUA));
        } else {
            // Подсказка "Зажми Shift"
            tooltip.add(Component.translatable("tooltip.nexuscore.hold_shift")
                    .withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    // ⚠️ ВАЖНО: Предотвращаем анимацию переодевания при изменении Lux
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, 
                                              boolean slotChanged) {
        if (!slotChanged && oldStack.getItem() == newStack.getItem()) {
            return false;
        }
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }
}
```

### Пример 3: Инструмент с механикой (например, топор)

```java
package com.sanbait.luxsystem.items;

import com.sanbait.luxsystem.capabilities.ILuxStorage;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
// ... другие импорты

public class MyLuxAxeItem extends AxeItem implements ILuxStorage {
    private final int capacity = 1000;

    public MyLuxAxeItem(Tier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    // ⚠️ ОБЯЗАТЕЛЬНО: Те же методы, что в примере выше
    public static int getLux(ItemStack stack) { /* ... */ }
    public static void setLux(ItemStack stack, int amount) { /* ... */ }

    // Твоя механика: ускорение рубки при наличии Lux
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float base = super.getDestroySpeed(stack, state);
        if (getLux(stack) > 0) {
            return base * 1.5f; // +50% скорости
        }
        return base;
    }

    // Потребление Lux при использовании (через события в NexusCore.java)
    // НЕ потребляй здесь! Это делается в NexusCore.onBlockBreak
}
```

---

## 🔧 Шаг 3: Зарегистрируй предмет в ModItems.java

**Файл:** `src/main/java/com/sanbait/luxsystem/ModItems.java`

```java
// Добавь в конец класса ModItems:

public static final RegistryObject<Item> MY_NEW_ITEM = ITEMS.register("my_new_item",
    () -> new MyLuxItem(new Item.Properties().rarity(Rarity.RARE)));

// Или для инструмента:
public static final RegistryObject<Item> MY_LUX_AXE = ITEMS.register("my_lux_axe",
    () -> new MyLuxAxeItem(
        net.minecraft.world.item.Tiers.DIAMOND,  // Уровень материала
        6.0F,                                     // Урон
        -3.0F,                                    // Скорость атаки
        new Item.Properties().rarity(Rarity.RARE)
    ));
```

**⚠️ ВАЖНО:** 
- Используй `LuxSystem.MODID` (не `nexuscore`)
- Имя регистрации должно быть в `snake_case` (нижний_регистр_с_подчеркиваниями)

---

## 🎨 Шаг 4: Создай модель предмета (JSON)

**Файл:** `src/main/resources/assets/luxsystem/models/item/my_new_item.json`

```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "luxsystem:item/my_new_item"
  }
}
```

**Для инструмента:**
```json
{
  "parent": "item/handheld",
  "textures": {
    "layer0": "luxsystem:item/my_lux_axe"
  }
}
```

**⚠️ ВАЖНО:**
- Путь текстуры: `luxsystem:item/имя_файла`
- Текстура должна быть в: `src/main/resources/assets/luxsystem/textures/item/my_new_item.png`

---

## 🌐 Шаг 5: Добавь локализацию (названия)

**Файл:** `src/main/resources/assets/luxsystem/lang/ru_ru.json`

```json
{
  "item.luxsystem.my_new_item": "Мой Новый Предмет",
  "tooltip.luxsystem.my_new_item_desc": "§7Описание предмета"
}
```

**Файл:** `src/main/resources/assets/luxsystem/lang/en_us.json`

```json
{
  "item.luxsystem.my_new_item": "My New Item",
  "tooltip.luxsystem.my_new_item_desc": "§7Item description"
}
```

**⚠️ ВАЖНО:**
- Ключ: `item.luxsystem.имя_предмета`
- Для тултипов: `tooltip.luxsystem.имя_описания`

---

## ⚙️ Шаг 6: Настрой Capability (если предмет с Lux)

**⚠️ АВТОМАТИЧЕСКИ:** Если класс реализует `ILuxStorage`, Capability добавится автоматически через `AttachCapabilitiesEvent` в `NexusCore.java`.

**НО:** Если хочешь кастомную емкость, добавь в конфиг:

**Файл:** `config/nexuscore-common.toml`

```toml
[Lux System]
itemLuxCapacities = [
    "luxsystem:my_new_item|2000",  # 2000 Lux максимум
    "luxsystem:my_lux_axe|1500"
]
```

---

## 🎮 Шаг 7: ⚠️ ОБЯЗАТЕЛЬНО - Добавь в Creative Tab

**⚠️ КРИТИЧЕСКИ ВАЖНО:** Все новые предметы и блоки ДОЛЖНЫ быть добавлены в Creative Tab!

**Файл:** `src/main/java/com/sanbait/luxsystem/LuxSystem.java`

В методе `addCreative()`:

```java
private void addCreative(net.minecraftforge.event.BuildCreativeModeTabContentsEvent event) {
    if (event.getTabKey() == com.sanbait.nexuscore.NexusCore.NEXUS_TAB.getKey()) {
        // ... существующие предметы ...
        event.accept(ModItems.MY_NEW_ITEM);
        event.accept(ModItems.MY_LUX_AXE);
        event.accept(ModBlocks.MY_NEW_BLOCK); // Если добавляешь блок
    }
}
```

**Не забудь:** Метод `addCreative()` должен быть подписан на событие в конструкторе:
```java
modEventBus.addListener(this::addCreative);
```

---

## 📋 Шаг 8: ⚠️ ОБЯЗАТЕЛЬНО - Добавь информацию в JEI

**⚠️ КРИТИЧЕСКИ ВАЖНО:** Все новые предметы, блоки и рецепты ДОЛЖНЫ иметь информацию в JEI!

**Файл:** `minecraft/kubejs/client_scripts/LUX/jei_info.js`

Добавь информацию о предмете/блоке:

```javascript
JEIEvents.information(event => {
    // Информация о новом предмете
    event.addItem('luxsystem:my_new_item', [
        '§6Название предмета',
        '§7Описание того, что делает предмет',
        '',
        '§9Механика:',
        '§7• Первая особенность',
        '§7• Вторая особенность',
        '§7• Третья особенность'
    ])
    
    // Информация о новом блоке
    event.addItem('luxsystem:my_new_block', [
        '§6Название блока',
        '§7Описание блока',
        '',
        '§9Использование:',
        '§7• Как использовать блок',
        '§7• Что он делает'
    ])
})
```

**Для рецептов Create/KubeJS:**
- Рецепты автоматически появляются в JEI после `/reload`
- Если рецепт не появляется — проверь логи на ошибки
- Убедись, что ID жидкости правильный (например, `liquid_lux_source`, а не `liquid_lux`)

---

## 🔧 Шаг 9: Добавь рецепты (если нужны)

**⚠️ ВАЖНО:** После добавления рецептов:
1. ✅ Проверь, что они видны в JEI (выполни `/reload`)
2. ✅ Убедись, что все ингредиенты существуют
3. ✅ Проверь логи на ошибки (`minecraft/logs/kubejs/server.log`)

### Рецепты через KubeJS

**Файл:** `minecraft/kubejs/server_scripts/LUX/recipes_crafting.js` (или другой подходящий файл)

**Пример: Crafting рецепт:**
```javascript
ServerEvents.recipes(event => {
    event.shaped('luxsystem:my_new_item', [
        'ABC',
        'DEF',
        'GHI'
    ], {
        A: 'minecraft:iron_ingot',
        B: 'luxsystem:lux_crystal',
        // ... остальные ингредиенты
    })
})
```

**Пример: Create Mixing рецепт:**
```javascript
ServerEvents.recipes(event => {
    event.recipes.create.mixing(
        Fluid.of('luxsystem:liquid_lux_source', 1000),
        [
            'luxsystem:my_item',
            Fluid.of('minecraft:water', 1000)
        ]
    ).heated()
})
```

**⚠️ КРИТИЧЕСКИ ВАЖНО для Create рецептов:**
- Используй правильный ID жидкости: `liquid_lux_source` (не `liquid_lux`)
- Проверь логи после `/reload` — не должно быть ошибок `Invalid empty fluid`
- Рецепты должны появиться в JEI автоматически

---

## ⚡ Шаг 10: Добавь механики (если нужно)

### Механика 1: Потребление Lux при использовании

**НЕ добавляй в класс предмета!** Это уже обрабатывается в `NexusCore.java` через события.

**Если нужна кастомная стоимость, добавь в конфиг:**

```toml
[Lux System]
itemLuxCosts = [
    "luxsystem:my_new_item|10",  # 10 Lux за использование
    "luxsystem:my_lux_axe|5"
]
```

### Механика 2: Эффекты при использовании

**В классе предмета:**

```java
@Override
public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
    // Проверяем Lux
    stack.getCapability(com.sanbait.luxsystem.capabilities.LuxProvider.LUX_CAP)
        .ifPresent(cap -> {
            if (cap.getLuxStored() > 0) {
                // Твой эффект
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
            }
        });
    return super.hurtEnemy(stack, target, attacker);
}
```

### Механика 3: Тики (обновление каждый тик)

**⚠️ ОСТОРОЖНО:** Тики могут вызывать лаги!

```java
@Override
public void inventoryTick(ItemStack stack, Level level, Entity entity, 
                          int slotId, boolean isSelected) {
    if (!level.isClientSide && entity instanceof Player player) {
        // Твоя логика (например, каждые 100 тиков = 5 секунд)
        if (level.getGameTime() % 100 == 0) {
            // Что-то делаем
        }
    }
}
```

---

## ✅ Чек-лист перед коммитом

- [ ] Класс предмета создан (если нужен)
- [ ] Предмет зарегистрирован в `ModItems.java`
- [ ] Модель JSON создана в `models/item/`
- [ ] Текстура PNG добавлена в `textures/item/`
- [ ] Локализация добавлена в `lang/ru_ru.json` и `lang/en_us.json`
- [ ] Если предмет с Lux — реализован `ILuxStorage`
- [ ] Если нужна кастомная емкость — добавлена в конфиг
- [ ] ⚠️ **ОБЯЗАТЕЛЬНО:** Предмет добавлен в Creative Tab (`LuxSystem.java`)
- [ ] ⚠️ **ОБЯЗАТЕЛЬНО:** Информация добавлена в JEI (`jei_info.js`)
- [ ] Если есть рецепты — они работают и видны в JEI
- [ ] Код компилируется без ошибок

---

## 🚫 Частые ошибки

### ❌ Ошибка 1: Capability не работает
**Причина:** Не реализован `ILuxStorage` или не добавлен в `AttachCapabilitiesEvent`  
**Решение:** Убедись, что класс реализует `ILuxStorage` (автоматически подхватывается)

### ❌ Ошибка 2: Предмет невидимый/розовый
**Причина:** Нет модели или текстуры  
**Решение:** Проверь пути в JSON и наличие PNG файла

### ❌ Ошибка 3: Название показывает "item.luxsystem.xxx"
**Причина:** Нет локализации  
**Решение:** Добавь ключ в `lang/ru_ru.json`

### ❌ Ошибка 4: Lux не заряжается
**Причина:** Предмет не в радиусе Ядра или нет Capability  
**Решение:** Проверь, что класс реализует `ILuxStorage` и предмет в инвентаре игрока

### ❌ Ошибка 5: Двойное потребление Lux
**Причина:** Потребление и в классе предмета, и в событиях  
**Решение:** Удали потребление из класса, оставь только в `NexusCore.onBlockBreak` / `onLivingHurt`

---

## 📚 Примеры из мода

- **Простой предмет:** `lux_crystal` (без класса)
- **Предмет с Lux:** `lux_sword`, `lux_pickaxe` (с классом)
- **Броня с Lux:** `lux_helmet` (наследуется от `ArmorItem`)

---

**Последнее обновление:** Январь 2026  
**Автор:** Nexus Core Dev Team
