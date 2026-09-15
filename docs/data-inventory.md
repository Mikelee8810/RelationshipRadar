# Data Inventory

What the app stores, why, where it comes from, and how long it lives. Everything is local (Room database on the device). Nothing leaves the phone.

| Field | Why | Source | Kept until |
|---|---|---|---|
| Person name, category, reminder settings, notes | The relationship profile | Contacts + user | Archived on contact deletion; never auto-deleted |
| Phone numbers, emails, contact lookup key | Match calls/texts to a person | Contacts | With the person |
| Chat handles (`app:DisplayName`) | Match chat notifications to a person | Notification listener, confirmed by user | With the person |
| Interaction: person, source app, type, time, direction, counts-toward-timer | The whole point: "when did I last reach out" | Call log, SMS, notifications, manual log | With the person |
| Connector cursor (last scanned time) | Cheap idempotent re-scans | App | Overwritten each run |
| Pending identity (unmatched number / chat name) | Ask the user once, remember the answer | Connectors | Until resolved or ignored |
| Reminder state (how many nudges sent this cycle) | Stop after 4 backup pushes | App | Overwritten each cycle |

## Deliberately NOT stored

- SMS body — the `BODY` column is never queried.
- Chat message text — the notification listener reads sender + timestamp only.
- Call log phone numbers as free text — only used to look up a person, then dropped. Unmatched numbers sit in *pending identities* until you decide.
- Anything from apps not in the messaging allow-list.

## Revoked permissions

Each connector checks its permission on every run and silently skips when it's gone. Notification access can be flipped off in Android settings; the listener just stops receiving.
