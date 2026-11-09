# SDD ledger — plan: docs/superpowers/plans/2026-11-09-app-drawer-dynamic-categories-and-motion.md

- Pre-flight Task 1 -> Task 2: Task 1 provides `AppCategory(label)` and `categoryFor(context, app)`; Task 2 consumes these to group installed apps. Interface matches.
- Pre-flight Task 2 -> Task 3: Task 2 keeps category-modal state in AppDrawerScreen; Task 3 animates the drawer overlay and adds `onClose`. Interfaces are independent except for adding the close callback.
- Ruling: Do not add or run tests in this task because developer instructions explicitly prohibit tests unless requested; use a debug build and the user's manual inspection instead.
