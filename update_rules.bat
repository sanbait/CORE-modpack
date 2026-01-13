@echo off
move /Y "docs\Main_Rules_Update.md" ".agent\rules\Main Rules.md"
del "docs\Main_Rules_Update.md"
echo Rules updated successfully.
