# RelationshipRadar Agent Rules

These rules apply to any AI coding agent working in this repository.

## Read first

Before substantial work, read:

1. `README.md`
2. relevant files in `docs/`
3. `docs/production-quality-gate.md`
4. the current implementation before proposing replacements

## Completion language

Do **not** call RelationshipRadar finished, production-ready, ready to ship, or complete because:

- the requested features exist
- the project builds
- unit tests pass
- an emulator happy path works
- a design-system document exists

Use the maturity labels in `docs/production-quality-gate.md` and provide evidence for every claimed gate.

## UI and redesign workflow

For any substantial UI build or redesign:

1. Inspect the existing app and product intent.
2. Study strong real-product references when visual direction is not already locked.
3. Write a concise visual direction and explicit anti-patterns.
4. Define representative screens/states.
5. Implement **one representative screen first**.
6. Render it on the target Pixel-sized environment.
7. Review the actual screenshot for hierarchy, density, rhythm, originality, usability, and AI-template feel.
8. Fix the representative screen before spreading its patterns across the rest of the app.
9. Capture and review the major screens again after implementation.

Do **not** rewrite the visual-direction document and roll the entire redesign out in the same unreviewed pass.

Material 3, Material You, Compose components, or any other UI kit are foundations. They do not count as a product identity by themselves.

## Product meaning over component convenience

Do not turn every screen into the same top bar + card/list + button recipe merely because those components are available.

RelationshipRadar is a relationship-maintenance product. Primary relationship screens should be composed around people, time, context, and action. Utility/settings screens may use more conventional platform structures.

If removing the app name and accent color makes a screen indistinguishable from an unrelated generated app, the screen is not visually finished.

## Testing

At minimum during ordinary changes, run the relevant build and unit tests.

For product-completion claims, follow the larger gate: integration, Compose UI/instrumentation, end-to-end, screenshot, accessibility, performance, and real-device evidence where relevant.

Do not treat missing tests as a successful test result.

## Privacy and data promises

Preserve RelationshipRadar's privacy rules and verify implementation against them.

Never silently weaken a documented promise. If documentation promises encryption, backup behavior, data minimization, or restricted message-content handling, verify that the implementation really does it or flag the mismatch as a blocker.

## Real Pixel safety

Follow the repository README: do not install or drive test builds on Mike's real Pixel without explicit authorization. Use the `Pixel_Radar` emulator first.

## Before handing work back

- inspect the diff
- run the relevant verification
- render/review UI when UI changed
- list genuine blockers
- distinguish prototype, feature-complete, beta-ready, and production-ready accurately

Evidence beats confidence.