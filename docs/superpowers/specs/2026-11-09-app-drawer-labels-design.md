# App Drawer labels — design

## Intent

Make application names in the four-column App Drawer grid consistently centered below their icons and keep each name to one line. Long names should end with an ellipsis instead of wrapping.

## Scope

- Update only the application name `Text` in `AppDrawerScreen.kt`.
- Make the text use the available cell width, center its alignment, set `maxLines = 1`, and use `TextOverflow.Ellipsis`.
- Keep the existing grid, icon size, spacing, click behavior, and favorite behavior unchanged.

## Approaches

1. **Fill the cell width and constrain the text (chosen):** centers labels within each grid cell, truncates long names cleanly, and avoids unnecessary fixed sizing.
2. **Reserve a fixed-size label area:** also stabilizes cell geometry, but adds a constraint that is not needed for the current grid and may be less flexible with larger system fonts.

## Constraints and verification

- Preserve the existing four-column layout and Compose setup.
- Verify by building the project and checking short and long app names in the drawer.
- Do not commit unless the user explicitly asks.
