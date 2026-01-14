# Overworld Bottom - УВЕЛИЧЕННАЯ ОБЛАСТЬ
# Разбиваем на несколько команд для надежности (Minecraft имеет ограничение на размер fill)
execute in minecraft:overworld run fill -30000 -64 -30000 30000 -55 30000 minecraft:air replace minecraft:bedrock

# Nether - УДАЛЯЕМ БЕДРОК НА ВСЕХ УРОВНЯХ Y (0-128)
# Пол (y=0-10)
execute in minecraft:the_nether run fill -30000 0 -30000 30000 10 30000 minecraft:air replace minecraft:bedrock
# Потолок (y=120-128)
execute in minecraft:the_nether run fill -30000 120 -30000 30000 128 30000 minecraft:air replace minecraft:bedrock
# Средние уровни (y=10-120) - на случай если бедрок там
execute in minecraft:the_nether run fill -30000 10 -30000 30000 120 30000 minecraft:air replace minecraft:bedrock

# End Floor - УВЕЛИЧЕННАЯ ОБЛАСТЬ
execute in minecraft:the_end run fill -30000 0 -30000 30000 5 30000 minecraft:air replace minecraft:bedrock

# Deeper Darker Otherside Floor & Ceiling - УВЕЛИЧЕННАЯ ОБЛАСТЬ
execute in deeperdarker:otherside run fill -30000 0 -30000 30000 5 30000 minecraft:air replace minecraft:bedrock
execute in deeperdarker:otherside run fill -30000 123 -30000 30000 128 30000 minecraft:air replace minecraft:bedrock

# Set Global Flag
scoreboard players set #GLOBAL dim_stack_track 1

# Notify
tellraw @a {"text":"[Dimension Stack] Бедрок полностью удален в области -30000 до 30000!","color":"green"}
