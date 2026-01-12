# CODING STANDARDS & PERFORMANCE RULES

> **STATUS:** CRITICAL
> **ENFORCEMENT:** STRICT

## 1. PERFORMANCE FIRST (The Golden Rule)

**NO TICK EVENTS ALLOWED.**

* **FORBIDDEN:** `PlayerTickEvent`, `WorldTickEvent`, `ServerTickEvent`, `LivingUpdateEvent`.
* **REASON:** These events run 20 times per second for every entity. Even a small check destroys TPS.
* **REQUIRED PATTERN:** Use **Event-Driven Logic**.
  * Movement -> `PlayerChangedChunkEvent`
  * Login -> `PlayerLoggedInEvent`
  * Interaction -> `BlockInteractEvent`
  * Damage -> `LivingDamageEvent`

## 2. Code Philosophy

* **Modularity:** Scripts for Entropy separation from Nexus Core.
* **Logging:** `console.log` for important state changes.
* **Compatibility:** Check Create/Jei compatibility before changing recipes.

## 3. Mechanics

* **Entropy:** Use soft ticks or random ticks, never hard loops.
* **Physics:** Consider rotation and stress (Create) in all block designs.
