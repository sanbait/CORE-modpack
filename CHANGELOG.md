# Changelog

All notable changes to the Entropy Core project will be documented in this file.

## [Unreleased]

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
