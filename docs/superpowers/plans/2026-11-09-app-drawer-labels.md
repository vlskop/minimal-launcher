# App Drawer Labels Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Center each app name below its icon, keep it on one line, and show an ellipsis when it is too long for its grid cell.

**Architecture:** Keep the existing four-column `LazyVerticalGrid` and change only the app-name `Text` in `AppDrawerScreen.kt`. Use the cell's available width, centered text alignment, a one-line limit, and ellipsis overflow.

**Tech Stack:** Kotlin, Jetpack Compose Material 3.

**Spec:** `docs/superpowers/specs/2026-11-09-app-drawer-labels-design.md`

## Global Constraints

- Preserve the existing four-column grid and Compose setup.
- Keep icon size, spacing, click behavior, and favorite behavior unchanged.
- Do not commit unless the user explicitly asks.

## Review Focus

- Long localized app names: remain one line and end in an ellipsis.
- Short app names: remain centered below their icons without unnecessary truncation.
- Large system font: text should stay within the cell width as much as Compose permits.

---

### Task 1: Constrain and center app labels

**Files:**
- Modify: `app/src/main/java/com/example/minimallauncher/launcher/drawer/AppDrawerScreen.kt`

**Interfaces:**
- Consumes: existing `AppInfo.name` string in the drawer item.
- Produces: a centered, one-line label constrained to the grid cell width.

- [ ] **Step 1: Update the app-name `Text`**

  Add `Modifier.fillMaxWidth()`, `textAlign = TextAlign.Center`, `maxLines = 1`, and `overflow = TextOverflow.Ellipsis` to the existing label. Add the required Compose imports and leave the conditional favorite star text intact.

- [ ] **Step 2: Build and inspect the drawer**

  Build the app, then inspect a short name and a long name in the four-column drawer. Confirm short names are centered and long names stay on one line with an ellipsis.
