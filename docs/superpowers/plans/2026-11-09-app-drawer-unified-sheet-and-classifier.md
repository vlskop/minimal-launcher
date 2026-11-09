# AppDrawer unified sheet and classifier Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make AppDrawer a native, finger-following modal sheet and improve offline classification of apps without Android category metadata.

**Architecture:** The launcher only controls whether the drawer sheet is composed; Material 3 owns its anchors, nested-scroll handoff, drag-to-dismiss, and settle animation. AppDrawer keeps one sheet state and switches between its category list and selected-category grid. Category inference uses Android's localized category first, then weighted token rules for app labels and package IDs.

**Tech Stack:** Kotlin, Android PackageManager, Jetpack Compose Material 3.

**Spec:** `docs/superpowers/specs/2026-11-09-app-drawer-unified-sheet-and-classifier-design.md`

## Global Constraints

- Keep classification offline and update it when installed packages refresh.
- Do not inspect or transmit the user's installed-app list.
- Use existing `ModalBottomSheet`, `ModalBottomSheetState`, `AnimatedContent`, and current Compose Material 3 dependencies; add no dependencies.
- Preserve search, counts, app launch, favorite toggling, and circular icon feedback.
- Do not build or run tests in this task; the user will build the app.

## Review Focus

- Down swipe while list is scrolled → list returns to top without dismissing the sheet.
- Down swipe at list top → sheet follows finger and dismisses only after threshold; short drag settles back.
- Back from category grid → return to category list; another Back closes the sheet.
- Overlapping classifier terms → highest specific score wins; ambiguous low-confidence results remain in `Без категории`.
- Punctuation, digits, package separators, and Cyrillic `ё` → tokenization remains stable and language-aware.

---

### Task 1: Improve local category inference

**Files:**
- Modify: `app/src/main/java/com/example/minimallauncher/launcher/drawer/AppCategory.kt`

**Interfaces:**
- Keep `data class AppCategory(val label: String)` and `fun categoryFor(context: Context, app: AppInfo): AppCategory`.
- Defined Android categories continue to use `ApplicationInfo.getCategoryTitle`; only undefined categories enter weighted local inference.

- [ ] Normalize app labels and package IDs into tokens and phrases, splitting package dots, underscores, dashes, digits, and punctuation; normalize `ё` to `е` for matching.
- [ ] Replace first-substring matching with weighted category rules covering common social, travel, entertainment/media, finance, shopping, work, health, education, news, photo, food, utilities, and system terms.
- [ ] Resolve overlap using specific phrase/brand weights and a confidence/margin threshold; return `Без категории` below threshold.

### Task 2: Use one native modal sheet for Drawer and category detail

**Files:**
- Modify: `app/src/main/java/com/example/minimallauncher/launcher/drawer/AppDrawerScreen.kt`

**Interfaces:**
- `AppDrawerScreen` owns `ModalBottomSheetState`, search state, and selected-category route; it consumes `onClose` and app/favorite state.
- Category list and category grid render as alternate content inside the same modal sheet.

- [ ] Move the main AppDrawer content under `ModalBottomSheet(skipPartiallyExpanded = true)` and remove the custom handle-only dismissal recognizer.
- [ ] Preserve a `LazyListState` for the category list and rely on ModalBottomSheet nested-scroll handoff: list scroll wins away from the top; at the top, a downward drag moves the sheet with the finger and settles/dismisses natively.
- [ ] On category tap, switch to a titled adaptive app grid inside the same sheet; add a back affordance and `BackHandler` so Back first returns to categories.
- [ ] Animate category/detail changes with a short directional slide and fade; preserve search, app launch, favorites, and category counts.

### Task 3: Let the sheet own opening and closing motion

**Files:**
- Modify: `app/src/main/java/com/example/minimallauncher/launcher/LauncherScreen.kt`

**Interfaces:**
- `LauncherScreen` owns `isDrawerOpen` and passes `onClose` to the conditionally composed `AppDrawerScreen`.

- [ ] Remove the full-screen `AnimatedVisibility` drawer wrapper and let the Material 3 modal sheet provide scrim, drag, settle, and dismissal motion.
- [ ] Keep swipe-up opening active only while the sheet is closed; ensure the drawer content cannot trigger the Home swipe detector.
- [ ] Preserve Back behavior and outside-tap dismissal.

### Completion checks

- [ ] Run `git diff --check` and statically inspect the updated gesture ownership and classifier scoring.
- [ ] Do not build or run tests; the user will build the app.
