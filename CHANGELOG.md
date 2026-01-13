# Changelog

All notable changes to the Entropy Core project will be documented in this file.

## [Unreleased]

## [1.1.21] - 2026-01-12

### Optimized

- **Performance**: Removed `LuxItemDecorator` which was causing massive FPS drops due to expensive per-frame logic.
- **Rendering**: Switched to **Vanilla Item Bars** (`isBarVisible`, `getBarColor`) for Lux display. Zero lag, native performance.
- **Sync**: Enabled NBT mirroring for `LuxMax` to ensure client-side rendering is accurate without capability lookups.

### Added

- **Config**: Full configuration integration for Lux Consumption logic (Tools, Armor, Weapons).
- **Logic**: Implemented `ALLOW_USE_WITHOUT_LUX` game rule.
- **Docs**: Updated `CONFIG_GUIDE.md` and `CHANGELOG_CONFIG.md` with final details.

### Fixed

- **FPS**: Fixed critical FPS drop caused by `DistExecutor` garbage generation in particle logic.
- **KubeJS**: Fixed missing Item registrations (`lux_filter`, `lux_canister`) which caused script errors.
- **Build**: Fixed `UPDATE_MOD.bat` to properly clean old JARs before building.
- **Cleanup**: Removed redundant build scripts (including duplicate UPDATE_MOD.bat).

### Fixed

- **FPS**: Fixed critical FPS drop caused by `DistExecutor` garbage generation in particle logic.
- **KubeJS**: Fixed missing Item registrations (`lux_filter`, `lux_canister`) which caused script errors.
- **Build**: Fixed `UPDATE_MOD.bat` to properly clean old JARs before building.
- **Cleanup**: Removed redundant build scripts from mod directory.

## [2026-01-11] - Session Update

### Added

- **Docs**: New `EXIT PROTOCOL` in `.agent/rules/Main Rules.md` to enforce documentation updates.
- **Docs**: Updated `docs/TechGD/TECH_DESIGN_CORE.md` to reflect the Java Mod architecture (GeckoLib, Capabilities).
- **Docs**: Added root `.cursorrules` pointing to `TECH_DESIGN_CORE.md`.

### Changed

- **Mod**: Bumped `nexus_core` version to `1.1.18`.
- **Mod**: Split `luxsystem` and `nexuscore` localization files into correct `assets` folders.
- **Cleanup**: Deleted `bash.exe.stackdump` and legacy `DEV_HANDOVER.md`.

### Fixed

- **Mod**: `PhantomBlockHandler` event bus ID corrected to `luxsystem`.
- **Config**: Fixed `NexusCoreConfig` default `baseRadius` to **12.0** (was 10.0) to match Tech Design.
- **Docs**: Rewrote `nexus_core_mod/README.md` to be the "Single Source of Truth" for mechanics and config.

### Fixed (Session 2)

- **Crash**: Fixed critical server-side crashes (`NoClassDefFoundError`) in Lux Items and NexusCoreEntity by abstracting client code.
- **Bug**: Fixed `LuxCondenser` consuming stacked buckets (now rejects stacks > 1).
- **Bug**: Fixed Lux Items (Pickaxe, Armor, Sword) not charging. Refactored from NBT to Capabilities to match Core logic.
- **Visual**: Fixed "mojibake" (unicode squares) in Lux tooltips by replacing characters with ASCII bars.
- **Balance**: Improved Lux charging frequency to 5 ticks (80 Lux/sec) for smoother visual feedback.

## [1.1.19] - 2026-01-11

### Added

- **GUI**: Implemented custom GUI for Nexus Core (`NexusCoreScreen`, `NexusCoreMenu`) with stats and upgrade slot.
- **Render**: Increased Core render distance to 512 blocks.

### Changed

- **Config**: Removed `ClothConfig` dependency to simplify build process.
- **Visuals**: Added Lux Bar to GUI.

## [1.1.20] - 2026-01-11

### Fixed

- **Critical Compilation Fix**: Resolved `RegisterMenuScreensEvent` symbol error by switching to `MenuScreens.register`.
- **Render Distance**: Fixed visual disappearance of the Core using infinite bounding box and `noCulling` flag.
- **Entity Culling Compatibility**: added `nexuscore:core` to `entityculling.json` whitelist.
- **GUI**: Corrected coordinates for Upgrade Slot and Lux Bar.
- **Functionality**: Restored `LuxItemDecorator` for all items.
