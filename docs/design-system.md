# Design System — current implemented baseline

> **Not final visual approval.** This file describes the current Material 3 Expressive implementation. Before any major UI work, follow `AGENTS.md` and `docs/production-quality-gate.md`: study real references, lock the direction, build one representative screen, render it, review the screenshot, fix it, then scale. Material components are a foundation, not RelationshipRadar's product identity.

The current implementation aims to look native to Pixel/Android: Material 3 Expressive, colour from the user's wallpaper, one geometric sans at expressive sizes, large collapsing titles, spring motion, and Material's shape language for people.

## Foundations

| Layer | Current choice | Where |
|---|---|---|
| Theme | `MaterialExpressiveTheme` + `MotionScheme.expressive()` | `ui/theme/Theme.kt` |
| Colour | `dynamicLight/DarkColorScheme` (Material You); `expressiveLightColorScheme` fallback | same |
| Type | Manrope variable (Google-Sans-class geometric); display sizes with tight tracking | same |
| Shapes | Material default expressive shapes; containers `extraLarge` (28 dp) | same |
| Splash / icon | Adaptive + monochrome; system accent colours; splash uses the icon | `res/values*/themes.xml`, `res/drawable/ic_launcher_foreground.xml` |

## Status = scheme roles (current implementation)

| Status | Container | Accent |
|---|---|---|
| On track | `primaryContainer` | `primary` |
| Due soon | `tertiaryContainer` | `tertiary` |
| Overdue | `errorContainer` | `error` |
| Way overdue | `error` | `error` |
| No reminders / paused / snoozed | `surfaceContainerHighest` | `outline` |

Labels: On track · Due soon · Overdue · Way overdue · No reminders · Paused · Snoozed.

## People are shapes — current experiment

`Avatar(id, name, status)` currently gives each person a stable `MaterialShapes` form by id (Cookie12, Clover8, SoftBurst, Sunny, Cookie9, Flower, Puffy, Cookie7) and morphs toward `Cookie4Sided` as they go overdue. Colour and shape both carry status; the morph is spring-animated. Same component at 40 / 56 / 88 / 112 dp.

This is an implemented experiment, not a locked product rule. Revalidate it from real screenshots before expanding it further.

## Current component inventory

| Component | Current use |
|---|---|
| `LargeTopAppBar` (exitUntilCollapsed) | Most top-level and utility screens |
| `ShortNavigationBar` | Bottom tabs: Circle · New people · Settings |
| `MediumExtendedFloatingActionButton` | Primary action: "I saw someone" |
| Connected `ToggleButton` group | Filters on the Circle |
| Connected `Button` + `FilledTonalButton` group | Person actions: Log · Snooze · Pause |
| `CircularWavyProgressIndicator` | Interval used, around the person's avatar |
| `Sheet` = `Card(surfaceContainer, extraLarge)` | Grouped settings/content |
| `ToggleRow` / `LinkRow` = `ListItem` | Rows inside a Sheet |
| `Celebrate` | After logging: Cookie4 → Sunny bloom with a Confirm haptic |

Do **not** turn this inventory into a rule that every screen must use the same composition. Relationship/content screens and utility/settings screens can share tokens without sharing the same structure.

## Motion rules

- Expressive motion scheme is the current baseline.
- Lists use `animateItem()`; the hero count uses `AnimatedContent` slide+fade.
- Onboarding page 1 keeps morphing between five expressive shapes; pages slide.
- Motion should explain state, hierarchy, causality, or continuity rather than decorate inactivity.

## Content rules

- Sentence case, no exclamation marks, contractions OK.
- Every permission ask says what is read and what is never read.
- Every empty state says what will put something there.

## Visual approval rule

This document is **not** a substitute for rendered evidence. A major UI change passes visual review only after representative screens are captured from the app and reviewed against an approved visual direction and the screenshot QA matrix in `docs/production-quality-gate.md`.
