# Dynamic App Drawer categories and motion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Generate categories dynamically from Android app metadata, open categories in a modal app grid, and make AppDrawer gestures and transitions predictable.

**Architecture:** Replace the fixed category enum with Android's localized application category plus local inference for undefined packages. Keep grouping and category-sheet state in Compose, and move drawer presentation to an animated overlay with separate opening and dismissal gesture targets.

**Tech Stack:** Kotlin, Android PackageManager, Jetpack Compose Material 3.

**Spec:** `docs/superpowers/specs/2026-11-09-app-drawer-dynamic-categories-and-motion-design.md`

## Global Constraints

- `ApplicationInfo.category` is available from the app's min SDK (26); obtain localized titles with `ApplicationInfo.getCategoryTitle(context, category)`.
- Use local package/name terms only for `CATEGORY_UNDEFINED`; unknown terms go to `Без категории`.
- Reuse the existing Compose Material 3 dependencies, app icon loading, launch callback, and favorite action.
- Replace `LauncherScreen`'s global drag detector with state-specific gestures and `AnimatedVisibility`/slide transitions.

## Review Focus

- Android category is undefined and the package/name does not match known local inference terms → show `Без категории` (classifier task).
- Two category entries resolve to the same localized title → combine their apps into one sheet and one row (grouping task).
- Category contains many apps or long labels → the modal grid remains vertically scrollable, labels truncate, and icons remain tappable (sheet task).
- Downward drag in the app list → scroll the list without closing the drawer; downward drag from the handle closes it (gesture task).
- Back while a category sheet is open → dismiss the sheet first, then let a later Back close the drawer (sheet/navigation task).

---

### Task 1: Replace fixed categories with dynamic package grouping

**Files:**
- Modify: `app/src/main/java/com/example/minimallauncher/launcher/drawer/AppCategory.kt`
- Consume: `AppInfo.category`, `AppInfo.packageName`, `AppInfo.name`

**Interfaces:**
- Produce `data class AppCategory(val label: String)` and `fun categoryFor(context: Context, app: AppInfo): AppCategory`.
- Prefer Android's localized category title when category is defined; use local inference for undefined categories; return `Без категории` when unrecognized.

- [ ] Remove the fixed enum and implement metadata-first grouping plus fallback inference.
- [ ] Normalize localized labels before grouping so identical labels merge; default to the current locale's `Collator` order and retain the existing count-sort toggle.
- [ ] Build with `./gradlew.bat :app:assembleDebug`; expect `BUILD SUCCESSFUL`.

### Task 2: Open categories in a modal app grid

**Files:**
- Modify: `app/src/main/java/com/example/minimallauncher/launcher/drawer/AppDrawerScreen.kt`

**Interfaces:**
- Consume dynamic category rows from Task 1 and existing app/favorite state.
- Produce a Material 3 `ModalBottomSheet` with selected category title and adaptive app grid.

- [ ] Replace fixed-order grouping and inline expansion with `groupBy(categoryFor)` and deterministic localized-title sorting.
- [ ] On category tap, open a titled modal bottom sheet with a vertically scrollable adaptive grid; close it on back, outside tap, or app launch.
- [ ] Preserve live counts, search filtering, app launch, favorite toggling, circular icon feedback, and accessible app labels.
- [ ] Build with `./gradlew.bat :app:assembleDebug`; expect `BUILD SUCCESSFUL`.

### Task 3: Animate AppDrawer and scope its gestures

**Files:**
- Modify: `app/src/main/java/com/example/minimallauncher/launcher/LauncherScreen.kt`
- Modify: `app/src/main/java/com/example/minimallauncher/launcher/drawer/AppDrawerScreen.kt`

**Interfaces:**
- `AppDrawerScreen` consumes `onClose: () -> Unit` for the handle and `LauncherScreen` owns `isDrawerOpen`.

- [ ] Keep Home behind an `AnimatedVisibility` overlay; animate entry from the bottom with a 280 ms ease-out and exit to the bottom with a 220 ms ease-in.
- [ ] Enable Home's swipe-up detector only while the drawer is closed.
- [ ] Add a labeled 44 dp handle target at the top of the drawer; only a downward gesture from that target closes it.
- [ ] Preserve Back handling and ensure the category sheet dismisses before the drawer.
- [ ] Build with `./gradlew.bat :app:assembleDebug`; expect `BUILD SUCCESSFUL`.
- [ ] Manually inspect defined/undefined categories, sheet grid and dismissal, app launch, swipe-up, handle swipe-down, list scrolling, and transition direction.
