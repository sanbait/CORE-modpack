# Entropy Core Modpack

> **"Reality is a privilege. Defend it or become a Shadow."**

Welcome to the development repository for **Entropy Core**, a hardcore survival modpack for Minecraft 1.20.1 centered around light mechanics, physics-based defense, and the struggle against entropy.

---

## 📚 Documentation Hub

### 🚀 For Developers (Start Here)

1. **[Workflow Guide](docs/WORKFLOW_GUIDE.md)** — **READ FIRST.** The strict protocol for saving, commiting, and pushing code.
2. **[Tech Design Core](docs/TechGD/TECH_DESIGN_CORE.md)** — The Source of Truth for the current mod architecture.
3. **[Rules & Vision](.agent/rules/)** — Coding standards and Game Design Vision.

### 📦 Components

* **Nexus Core Mod:** [Read Documentation](nexus_core_mod/README.md)
* **Lux System (Roadmap):** [Future Plans](docs/TechGD/ROADMAP_LUX_SYSTEM.md)
* **Modpack Vision:** [Concept Document](.agent/rules/modpack-vision.md)

---

## 🛠 Project Structure

* `nexus_core_mod/` — Source code for the custom Core entity and mechanics.
* `kubejs/` — Scripts for recipes, events, and mod glue.
* `docs/` — Design documents and guides.
* `Archives/` — Local backups.

---

## ⚠️ Key Protocols

* **Backup First:** Never edit code without running a backup script.
* **Save & Push:** Work is not done until `git push` is executed.
* **Single Truth:** If code contradicts `docs/`, the docs are right (and code is buggy) OR you must update docs first.

*(Maintained by the Agentic Dev Pair)*
