# Design System

One small layer of primitives in `ui/Components.kt`; screens compose them and never redraw their own.

## Tokens (`ui/theme/Theme.kt`)

| Token | Value | Used for |
|---|---|---|
| `Radar.sp1..sp4` | 4 / 8 / 16 / 32 dp | All spacing |
| `Radar.dotSm/Md/Lg` | 10 / 12 / 14 dp | Signal dots |
| `Radar.fabClearance` | 96 dp | Bottom padding under the FAB |
| `StatusColors.good / dueSoon / overdue / veryOverdue / quiet` | fixed, not dynamic | The traffic light. Meaning must never shift with wallpaper. |

`StatusColors.of(RadarStatus)` and `StatusColors.of(Health.Level)` map both signal types onto the same three meanings: fine / look at this / broken. The widget uses the same values.

## Primitives

| Component | Rule |
|---|---|
| `SignalDot` / `StatusDot` / `HealthDot` | The only dot. Never draw a circle in a screen. |
| `StatusChip` | Dot + label. Used wherever a status is named in text. |
| `Avatar` | Initials tinted by status. Lists scan by colour before they're read. |
| `ToggleRow` | Title + subtitle + switch. Every on/off setting. |
| `SectionHeader` | Uppercase label. Every grouped list. |
| `EmptyState` | Title + one sentence. Every list that can be empty. |
| `Hint` | Small muted explanation under a control. |

## Content rules

- Names: single line, ellipsis. Avatar handles long/odd names (`?` fallback).
- Every permission ask says what is read and what is never read.
- Every empty state says what will put something there.
- Colours: Material You dynamic for chrome; status colours fixed.
