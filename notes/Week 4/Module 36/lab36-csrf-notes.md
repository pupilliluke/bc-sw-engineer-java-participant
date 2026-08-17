# Lab 36 — CSRF Notes

## Step 1 — Cookie sessions

if the auth cookie is sent automatically, csrf is in scope. the attacker
never reads anything, a malicious page fires a request at the CRM api and
the browser attaches the session cookie on its own, so the request looks
legitimate on the server. the vectors are an `<img>` tag, an
auto-submitted hidden form, or cross-site javascript, and the controls are
SameSite on the cookie, a csrf token on every unsafe method, POST rather
than GET for state changes, and an Origin or Referer check.

## Step 2 — Bearer header

if the token lives only in an explicit Authorization header set by
javascript, classic csrf is reduced. a page on another origin can make the
browser send a request, but it cannot make it carry a header the app's own
code adds, and it cannot read the token out of the CRM app's memory
either.

csrf is not cors. cors is the browser refusing to hand a cross-origin
response back to script; csrf is the server trusting a request the browser
sent by itself. a forged request can succeed even when the attacker never
sees the response, which is why cors is no defence against it.

## Step 3 — Lab stance

bearer-only. lab 36 keeps the access token in memory and adds
`Authorization: Bearer` in http.request for the CRM api origin, there is
no session cookie in the flow, so csrf is N/A with the rationale written
out rather than "csrf does not exist". the moment auth moves to a cookie
the browser sends automatically, csrf is back in scope and step 1's
controls apply.

## Step 4 — Checklist

if the app ever uses cookies: `SameSite=Lax` or `Strict`, plus `Secure`
and `HttpOnly`, a csrf token on unsafe methods, and an origin check on the
server.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab36-csrf-notes.md`
- [ x ] Cookie vs bearer contrast
- [ x ] Lab stance stated
- [ x ] SameSite checklist item
