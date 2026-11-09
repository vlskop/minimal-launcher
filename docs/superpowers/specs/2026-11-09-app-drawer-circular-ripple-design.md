# App Drawer circular ripple — design

## Intent

Replace the rectangular press feedback on each App Drawer grid item with a circular ripple around its app icon, while keeping the entire item clickable.

## Behavior

- A tap or long press anywhere in the app cell keeps its current action.
- The existing cell-level `combinedClickable` emits interactions through a shared `MutableInteractionSource` without drawing its default rectangular indication.
- A custom circular indication is displayed in the icon's 48 dp bounds and listens to that same interaction source, so presses on the label also show feedback around the icon.
- The ripple begins at the icon center, expands within a circular clip, and fades after release or cancellation. Use the app's primary color with restrained alpha.
- Keep launching apps, toggling favorites, grid geometry, icon size, and the label changes unchanged.

## Approaches

1. **Shared interaction source with a custom indication on the icon (chosen):** preserves the full cell hit target and confines the ripple to the icon bounds.
2. **Custom indication drawn over the full cell:** can place a circle at the icon's coordinates, but requires tracking layout position and drawing outside the icon component.

## Constraints and verification

- Use the current Compose indication node API (`IndicationNodeFactory`); do not add dependencies.
- Verify the build, then check that taps on both the icon and label launch the app, long press still toggles favorites, and the ripple stays circular around the icon.
- Do not commit unless the user explicitly asks.
