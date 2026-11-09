# AppDrawer unified sheet and classifier — design

## Intent

Give the main AppDrawer the same modal, drag-to-dismiss behavior as category views and improve local categorization for apps whose Android category is undefined.

## Goals

- Present AppDrawer as a Material 3 modal bottom sheet, opened from Home by the existing upward gesture.
- Use the sheet's native handle, downward drag, outside-tap scrim, and Back behavior to close it.
- Keep category selection in the same sheet: tapping a category changes to its titled app grid; Back returns to the category list; dragging down or tapping outside closes the whole AppDrawer.
- Animate sheet entry/exit with Material 3's bottom-sheet motion. Animate category-list/detail changes with a short slide-and-fade transition.
- Prefer Android's localized `ApplicationInfo.category` title. For undefined categories, classify using tokenized app labels and package identifiers with weighted local rules and a confidence threshold.
- Broaden fallback groups to cover common app functions (social, travel, media, finance, shopping, work, health, education, news, photos, food, system/utilities, and related categories); keep unresolved apps in `Без категории`.
- Keep all classification offline and update it when installed packages refresh.

## Non-goals

- Network lookups, external AI services, or added model/dependency downloads.
- Manual package assignments or custom category editing.
- Inspecting or transmitting the user's installed-app list.

## Approaches

1. **One modal sheet with weighted local inference (chosen):** use Android category metadata first; score normalized words from app name/package ID only when metadata is undefined. Use a confidence threshold and a single detail view within the same modal sheet. This avoids nested modal sheets, keeps gestures consistent, and needs no network.
2. **Two nested modal sheets:** keep the category popup on top of a modal AppDrawer. This preserves two separate surfaces but creates competing drag/back behavior and layered scrims.
3. **Remote package-category lookup:** could cover more packages but adds network/privacy and service availability concerns, contrary to the offline device-adaptive goal.

## Layout and behavior

- Keep the current AppDrawer visual layout and category rows.
- The main sheet expands from the bottom when opened and dismisses using native sheet gestures, Back, or outside tap.
- Selecting a category shows a back affordance, the category name, and a scrollable adaptive app grid inside the same sheet. Back from this view returns to the category list; the sheet itself stays open.
- Category view transitions use a 220 ms ease-in/out slide with a brief fade. Respect system animation scale.
- Category rows derive from the installed app set; Android-provided localized titles remain dynamic. Local inference adds broader fallback labels only when Android metadata is undefined.
- Tokenization separates dotted package segments, underscores, dashes, digits, and label words; weighted matching favors specific multiword/brand terms over broad substrings. Below-threshold results go to `Без категории`.

## Constraints and implementation calls

- Use existing `ModalBottomSheet`, `ModalBottomSheetState`, `AnimatedContent`, and current Compose Material 3 dependencies; add no dependencies.
- Remove the custom Drawer drag handle and full-screen `AnimatedVisibility` once the main sheet owns open/close motion.
- Keep the Home swipe-up listener active only while the sheet is closed.
- Preserve search, counts, app launch, favorite toggling, and circular icon feedback.
- ADB is unavailable in this environment, so fallback rules remain general and will not use a device-specific package list.

## Verification

- Manually inspect swipe-up opening; native sheet drag, outside tap, and Back dismissal; category selection and Back return; grid scrolling and app launch.
- Review classifier behavior for defined Android categories, each fallback group, overlapping keyword matches, package-token boundaries, and the confidence fallback.
- Do not build or run tests in this task; the user will build the app.

## Scope

Modify `AppCategory.kt`, `AppDrawerScreen.kt`, and `LauncherScreen.kt`. No dependencies, network calls, Figma edits, or package-list collection.
