# App Drawer groups Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the Figma App Drawer screen with automatic app grouping while preserving launch and favorite behavior.

**Architecture:** Add Android application-category metadata to `AppInfo`; classify apps in a stable seven-category enum using metadata and name/package keywords; render grouped rows, search, counters, and expansion state in Compose.

**Tech Stack:** Kotlin, Android PackageManager, Jetpack Compose Material 3.

**Spec:** `docs/superpowers/specs/2026-11-09-app-drawer-groups-design.md`

## Global Constraints

- Follow the Figma frame's light palette: `#F8F9F7`, `#202420`, `#838981`, `#ECEEEB`, and `#E8EBE5`.
- Keep the existing stack and add no dependencies.
- Preserve app launch, long-press favorite toggling, and circular press feedback.
- Use 44 dp previews, 76 dp category rows, a 143 dp label column, and 24 dp horizontal margins.
- Use Android system drawables for search, clear, and sorting; use a `›` text glyph for the row chevron.

## Review Focus

- Apps with unknown category and no keyword match → place in Инструменты.
- Several apps in one category → show up to three previews, then allow expansion to reach all apps.
- Search matching multiple groups → retain only matching icons and hide empty groups.
- Empty search results and clear action → show empty state, then restore the full list.
- Small screens and long localized names → keep rows usable and expanded app labels truncated.

---

### Task 1: Add category metadata and classifier

**Files:**
- Modify: `app/src/main/java/com/example/minimallauncher/core/model/AppInfo.kt`
- Modify: `app/src/main/java/com/example/minimallauncher/data/repository/AppRepository.kt`
- Create: `app/src/main/java/com/example/minimallauncher/launcher/drawer/AppCategory.kt`

**Interfaces:**
- `AppInfo(packageName: String, name: String, icon: Drawable, category: Int)`.
- `enum class AppCategory(val label: String)` in Figma order: Соцсети, Путешествия, Развлечения, Финансы, Покупки, Работа, Инструменты.
- `fun categoryFor(app: AppInfo): AppCategory`.

- [ ] Add and populate `ApplicationInfo.category` from `resolveInfo.activityInfo.applicationInfo.category`.
- [ ] Implement deterministic matching from normalized package/display name keywords, then Android categories; map unmatched apps to Инструменты.
- [ ] Build with `./gradlew.bat :app:assembleDebug`; expect `BUILD SUCCESSFUL`.

### Task 2: Build the Figma App Drawer in Compose

**Files:**
- Modify: `app/src/main/java/com/example/minimallauncher/launcher/drawer/AppDrawerScreen.kt`

**Interfaces:**
- Consumes `AppCategory.categoryFor`, installed apps, existing favorite state, and `CircularRippleIndication`.
- Produces Figma-matched search/header/category rows, live counts, expandable app lists, and empty state.

- [ ] Group filtered apps in the enum order and calculate app/category counts and Russian plural labels.
- [ ] Render the Figma palette, search field, 30 sp title, 13 sp counts, section header, 76 dp rows, 44 dp icon previews, dividers, and bottom hint.
- [ ] Use system search/clear/sort drawables and a right-chevron text glyph; implement clear, sort toggle, group expansion, launch, and long-press favorite interactions.
- [ ] Keep the bottom hint consistent with the current long-press favorite action rather than implying unsupported drag-and-drop.
- [ ] Keep dynamic app icons and 24 dp insets; ensure dark-mode text and surfaces remain legible.
- [ ] Build with `./gradlew.bat :app:assembleDebug`; expect `BUILD SUCCESSFUL`.
- [ ] Inspect the requested App Drawer screen in the emulator for populated rows, search, empty state, expanded groups, favorites, launch, and light/dark palettes.
