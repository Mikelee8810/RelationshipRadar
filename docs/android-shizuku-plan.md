# Android + Shizuku

## Target

Pixel-first Android app. minSdk 31, compileSdk 37.

## What Shizuku is used for (Phase 3)

Shizuku lets the app run a small helper process with the `shell` user's powers. We use it for
**background reliability and one-tap setup only** — never to read other apps' data.

| Command | Why |
|---|---|
| `dumpsys deviceidle whitelist +pkg` | Doze exemption so the 9am reminder and 6-hourly scan fire on time |
| `am set-standby-bucket pkg active` | Stop Android from deferring WorkManager for hours |
| `appops set pkg RUN_ANY_IN_BACKGROUND allow` | Explicit background allowance |
| `cmd notification allow_listener pkg/Listener` | Enable the chat-app listener without a Settings trip |
| `pm grant pkg <declared permission>` | Grant our own runtime permissions without dialogs |

## Trust boundary

- The elevated process (`ShellService`) accepts **integer IDs only**. The allow-list lives in
  `ShellCommands.kt`. No strings, no user input, no person names ever cross the boundary.
- Commands are `exec`'d as argv arrays, never via `sh -c`.
- `ShellCommandsTest` enforces: every command references only our package, no shell
  metacharacters, only declared permissions can be granted.
- Everything works without Shizuku; the Health screen offers the manual Settings path instead.

## Root (future)

`ShizukuBridge` exposes `run(Command)` / `query(Query)`. A `RootBridge` with the same shape
could be swapped in behind the same allow-list.

## Health screen

Measures rather than assumes: notification permission, battery-optimisation exemption,
standby bucket, last reminder-job run, last scan run, listener state, call/SMS grants, Shizuku
state. Green = nothing to do. Only exceptions get a Fix button.
