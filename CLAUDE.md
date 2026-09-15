# Claude — RelationshipRadar Workflow

Read `AGENTS.md` and `docs/production-quality-gate.md` before substantial work. They are the repository's completion and quality rules.

For major UI changes, use this order:

**references → visual direction → one representative screen → rendered screenshot review → correction → scale across app → final screenshot QA**

Do not redesign the whole app from a text design-system file in one pass.

Do not call the app finished, production-ready, or ready to ship because the build passes or requested features exist. Use the evidence gates in `docs/production-quality-gate.md`.

When UI changes, inspect actual rendered output. When behavior changes, add/run the appropriate tests. When documentation makes a privacy or data-safety promise, verify the implementation rather than repeating the promise.

Material 3 Expressive is a platform foundation, not a substitute for RelationshipRadar's product identity.