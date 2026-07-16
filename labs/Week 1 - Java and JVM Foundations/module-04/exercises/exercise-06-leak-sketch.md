# Exercise — Leak Sketch (safe)

**Module 4** · Pre-lab practice · then open [`../../lab4/LAB-4-GUIDE.md`](../lab4/LAB-4-GUIDE.md)

## Goal

Show a growing static `List` that retains objects (cap size—do not OOM).

## Do this

- `static List` holds references
- Stop at e.g. 100_000
- Write root-cause note

## Expected result

You identify a GC root retaining objects.

## Pass criteria

_Mark each row **Pass** or **Fail** in your lab notes (GitHub markdown files are not interactive checklists)._

| # | Confirm | Your notes |
| - | ------- | ---------- |
| 1 | Code compiles and runs (or notes complete if analysis-only) | Pass / Fail |
| 2 | You can explain the result in one sentence | Pass / Fail |
