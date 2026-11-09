# App Drawer groups — design

## Intent

Implement the App Drawer screen from the Figma design at node `3:173` in file `30O4sxbfWx1sqTUMMQhKwJ`. Replace the flat four-column grid with a searchable category list while preserving real installed-app icons and current launcher behavior.

## Goals

- Follow the Figma screen: warm off-white background, rounded search field, title and live counts, `ПО КАТЕГОРИЯМ` label, seven category rows, app icon previews, right chevrons, separators, and a bottom hint.
- Categorize installed apps automatically and update the screen as the installed-app list refreshes.
- Search across app names, hide groups without matches, and provide an empty result state.
- Keep launching apps and long-press favorite toggling available on app icons.

## Non-goals

- Manual category editing, drag-and-drop, or persistent user-defined categories.
- Editing the Figma file or changing the home screen.

## Approaches

1. **Automatic grouping (chosen):** combine Android's application category with normalized display-name and package-name keyword rules. This fills the seven Figma groups without setup; classification remains approximate.
2. **Manual grouping:** hard-code memberships for every app. This matches one sample installation but requires ongoing maintenance and does not adapt.

## Layout and behavior

- Use a fixed, design-matched palette: background `#F8F9F7`, primary text `#202420`, secondary text `#838981`, search surface `#ECEEEB`, divider `#E8EBE5`.
- Keep system status and gesture navigation bars native. App content uses 24 dp horizontal margins and the Figma's spacing: 56 dp search field with 22 dp corners, 30 sp title, 13 sp subtitle, 11 sp tracked section label, 76 dp group rows, 143 dp category labels, 44 dp rounded app icons with 10 dp gaps, and a 62 dp bottom hint.
- Category order: Соцсети, Путешествия, Развлечения, Финансы, Покупки, Работа, Инструменты. Show app count per category and live total app/category counts. Empty categories are hidden.
- Show up to three icon previews plus a chevron in each row. Tapping a group expands its full app list; tapping an app launches it. Long-press toggles favorite state and marks favorites with a small star badge.
- Search app names case-insensitively, preserve group order, and omit groups with no matches. The trailing X clears the query. Show `Ничего не найдено` if no app matches.
- Use Android system drawables for search, clear, and sorting controls, as requested. Use the text glyph `›` for the right-facing row affordance because Android's public platform drawables do not provide a matching chevron.
- The category sort control toggles between the Figma's default order and descending app count. Theme colors retain contrast in dark mode.
- The Figma bottom hint mentions moving icons, while this launcher currently uses long-press to toggle favorites; use truthful copy, `Удерживайте значок, чтобы добавить в избранное`.

## Constraints and implementation calls

- Existing `AppInfo` has package name, display name, and icon; add Android's application category.
- Use the existing Kotlin/Compose Material 3 setup and no new dependencies.
- Classification uses category metadata and keyword rules for social, travel, entertainment, finance, shopping, and work apps; remaining apps go into Инструменты.
- Actual app icons remain dynamic from `PackageManager`; Figma sample logos are not copied into the launcher.
- Existing custom circular press feedback stays attached to each app icon through the shared interaction source.

## Verification

- Build the app and inspect the drawer with populated categories, search, empty results, expanded groups, favorites, and light/dark themes.
- Confirm launch and long-press behavior on grouped icons.

## Scope

Modify `AppInfo.kt`, `AppRepository.kt`, and `AppDrawerScreen.kt`; add `AppCategory.kt` if useful. No dependency or Figma-file changes.
