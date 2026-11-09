# Dynamic App Drawer categories and motion — design

## Intent

Make AppDrawer categories reflect the apps installed on each device, open a category in a modal app grid, and make drawer gestures and transitions predictable.

## Goals

- Remove the fixed `AppCategory` enum and seven-category ordering.
- Prefer each package's Android `ApplicationInfo.category`, displayed with Android's localized category title.
- When Android reports `CATEGORY_UNDEFINED`, use local display-name/package-name inference for recognizable functional groups; put unresolved packages into `Без категории`.
- Show only categories present among installed apps; sort them by localized title by default while retaining the existing count-sort control.
- On category tap, open a modal bottom sheet with the category title and an adaptive grid of all its apps. Tapping an app launches it; back/outside tap dismisses the sheet.
- Animate the full-screen AppDrawer in from and out to the bottom (enter 280 ms ease-out, exit 220 ms ease-in); preserve back-to-close.
- Keep swipe-up to open from Home. Remove swipe-down detection from the whole drawer; allow swipe-down dismissal from a dedicated 44 dp top handle so vertical list scrolling never closes the drawer.

## Non-goals

- Network lookups or downloading a category database/model.
- User-defined categories or manual package recategorization.
- Changing the Figma file, home screen layout, or app-launch behavior.

## Approaches

1. **Android metadata with local fallback (chosen):** use Android's per-app category and localized title, infer only when the field is undefined, and use `Без категории` for unresolved apps. This adapts to device and installer metadata without network access; fallback inference remains heuristic.
2. **Android metadata only:** no inference rules. This is simpler but can leave many launchable apps in one undefined bucket.

## Layout and behavior

- Keep the current Figma AppDrawer layout and styling.
- Category rows are dynamically generated from the installed package list. Counts reflect apps in each group; the default order follows localized titles, and the existing sort control can order groups by app count.
- A tapped row opens a Material 3 `ModalBottomSheet` with its title and a vertically scrollable adaptive icon grid; the category row does not expand inline.
- A small visible handle at the top is the only downward-swipe dismiss target. It has a 44 dp touch region and a descriptive accessibility label.
- Home's upward-swipe detector is active only while the drawer is closed. Back closes the category sheet first, then the drawer.
- Respect Android system animation scale by using Compose's standard animation APIs; keep the existing theme and no new dependencies.

## Constraints and implementation calls

- `ApplicationInfo.category` is available from the app's min SDK (26); obtain localized titles with `ApplicationInfo.getCategoryTitle(context, category)`.
- Use local package/name terms only for `CATEGORY_UNDEFINED`; unknown terms go to `Без категории`.
- Reuse the existing Compose Material 3 dependencies, app icon loading, launch callback, and favorite action.
- Replace `LauncherScreen`'s global drag detector with state-specific gestures and `AnimatedVisibility`/slide transitions.

## Verification

- Build and inspect dynamic category rows with defined and undefined categories, category modal grid, outside/back dismissal, app launch, swipe-up open, handle swipe-down close, and list scrolling without dismissal.
- Confirm back closes the category sheet before the drawer and the transition enters/exits from the bottom.

## Scope

Modify `AppCategory.kt`, `AppDrawerScreen.kt`, and `LauncherScreen.kt`; adjust `AppInfo.kt`/`AppRepository.kt` only if category metadata needs reshaping. No dependencies or Figma edits.
