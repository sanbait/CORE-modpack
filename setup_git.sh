#!/bin/bash

# Ensure we are in the project root
# (User should run this from the project root)

echo "Staging files..."
# Add only specific project directories
git add docs/
git add .agent/rules/
git add nexus_core_mod/
git add minecraft/kubejs/
git add .gitignore

echo "Committing..."
git commit -m "feat: initial commit of core mechanics, documentation, and agent rules"

echo "Done! Initial commit created."
git status
