# Lab 36 — Token Storage Options

## Reference

| Option | XSS risk | CSRF risk | Risk / note |
| --- | --- | --- | --- |
| In-memory variable | medium | low | Lost on refresh; nothing persists for an injected script to read later |
| sessionStorage | high | low | Per-tab; readable by any injected script |
| localStorage | high | low | Survives refresh; readable by any injected script; banned in Lab 36 |
| HttpOnly cookie | low | high | Not JS-readable; needs a CSRF strategy |

## Step 1 — Study table

no row is low on both columns. HttpOnly cookie trades the xss read for
csrf exposure, the two web storage rows are readable by any script that
gets injected, in-memory is medium on xss because a script in the page can
still use the app's own fetch path even when it cannot read the variable.

## Step 2 — Lab choice

in-memory variable for the access token. a stolen page context is bounded
to the lifetime of that page, nothing survives a refresh for an attacker
to come back and read, and lab 36 bans localStorage outright. the cost is
real and documented, not shared across tabs and gone on refresh, which
means a re-login instead of a silent restore.

## Step 3 — Never

never commit a real token, never put DB passwords or signing secrets in a
Vite env file — lab 35's .env holds only VITE_API_BASE_URL and everything
in Vite env ships to every browser. never mirror the token to
localStorage or sessionStorage, never log the full token, log
lab-request-001 instead, and clear the token on logout.

## Step 4 — Fixture

fake token `lab-token-001` in notes only, never a real secret.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab36-token-storage.md`
- [ x ] Choice + justification
- [ x ] Never-commit rule
- [ x ] Fake token example only
