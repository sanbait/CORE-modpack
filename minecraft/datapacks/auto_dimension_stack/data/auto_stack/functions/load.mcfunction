# Bedrock removal is now handled by KubeJS script auto_bedrock_replace.js
# This datapack function is disabled to avoid duplicates
# scoreboard objectives add dim_stack_track dummy
# execute unless score #GLOBAL dim_stack_track matches 1 run function auto_stack:replace_bedrock_global
