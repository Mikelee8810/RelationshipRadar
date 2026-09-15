# RelationshipRadar — Production Quality Gate

RelationshipRadar is **not production-ready merely because the requested features exist or the app builds**.

Use four labels honestly:

- **Prototype** — the idea works on a narrow happy path.
- **Feature-complete** — the requested capabilities exist.
- **Beta-ready** — major flows work and failures are tolerable for testing.
- **Production-ready** — every gate below has evidence.

## 1. Product-flow gate

Verify the promised journeys end to end on the target Android version and representative Pixel hardware:

- first run and permission education
- contact import and new-contact handling
- calls, SMS, calendar, notification-based messaging connectors
- identity matching and merge decisions
- manual/in-person logging
- category and reminder changes
- snooze, pause, archive, restore
- notification roundup and individual reminders
- widget entry points
- restart, reboot, permission loss, and recovery

Happy-path emulator testing is necessary, but not sufficient.

## 2. Visual-direction gate

Before a large redesign is implemented:

1. Study strong real-product references.
2. Write a short visual direction that explains hierarchy, typography, spacing, color roles, surfaces, imagery/avatars, motion, navigation, and explicit anti-patterns.
3. Define 3–6 representative screens/states.
4. Build **one representative screen first**.
5. Render it on a Pixel-sized target and review the screenshot before scaling the pattern across the app.

Material 3 Expressive is a platform foundation, **not the product identity**.

Do not approve a redesign from Kotlin code or a design-system Markdown file alone.

## 3. Screenshot QA gate

Capture and review actual rendered output for every major screen in relevant variants:

- populated
- empty / first run
- permission denied / connector unavailable
- error / stale data where relevant
- light and dark
- normal and large font scaling
- representative Pixel phone sizes

Check hierarchy, density, alignment, cropping, legibility, touch targets, repeated-template feel, and whether the primary task is obvious within a few seconds.

Where practical, add screenshot/golden regression tests so unapproved visual drift fails automatically.

## 4. Data-integrity gate

Verify:

- Room migrations from every supported prior schema
- duplicate protection and identity reconciliation
- destructive actions and recovery
- backup / restore / export expectations
- database corruption and failed-migration behavior
- sensitive-data retention rules

### Current blocker

The implementation plan promises **local-first encrypted storage**, but `AppDatabase` currently uses a normal `Room.databaseBuilder(...)` with no database encryption layer. That promise and implementation must be reconciled before production readiness.

## 5. Security and privacy gate

Review every sensitive permission, exported component, privileged Shizuku path, notification listener path, log, backup path, and locally stored identifier.

RelationshipRadar processes sensitive relationship and communications metadata. Privacy behavior must be tested, not merely documented.

## 6. Testing gate

Production readiness requires evidence beyond unit tests:

- unit tests for rules and mapping
- integration tests for repository/database/connectors
- Compose UI / instrumentation tests for critical flows
- end-to-end tests for the core product journeys
- regression tests for every previously found production bug
- screenshot tests for representative visual states

## 7. Performance and stability gate

Test the release build, not only debug:

- startup
- scrolling / animation jank
- background work
- battery behavior
- memory
- crash / ANR risk
- long-running connector behavior
- reboot and process-death recovery

Use release optimization and Baseline Profiles where they materially improve startup and common journeys.

## 8. Accessibility and adaptability gate

Verify:

- TalkBack navigation and labels
- touch targets
- contrast
- font scaling
- dark/light themes
- dynamic color behavior
- long names and localized-length text
- key phone sizes and rotations if supported

## 9. Release-engineering gate

Before shipping:

- release build configuration reviewed
- minification / shrinking decision explicit
- versioning and signing path defined
- CI builds and tests every change
- changelog / release notes prepared
- reproducible build path documented
- rollback / recovery plan understood

## 10. Evidence rule

A gate is **not passed** because an agent says it probably works.

Each critical row needs evidence such as a test result, screenshot, benchmark, device run, checked-in artifact, or explicit review record.

## Current known production blockers

- encrypted-storage promise does not match the current plain Room database implementation
- no `androidTest` source set is currently checked in
- no Compose UI / end-to-end test suite is currently checked in
- no screenshot/golden test suite is currently checked in
- minimum GitHub Actions CI now exists for debug build, unit tests, and lint; instrumented/UI, screenshot, and release-mode checks are still missing
- release build currently has `isMinifyEnabled = false`
- backup/export/recovery behavior is not documented as a product flow

Do not call the app production-ready until these are resolved or explicitly accepted with evidence.