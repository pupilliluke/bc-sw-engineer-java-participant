# Lab 36 — XSS and CSP Notes

## Step 1 — Danger

a customer name holding `<script>...</script>` rendered through
`dangerouslySetInnerHTML` executes in the app's own origin and can steal
the in-memory access token. it is stored xss, the payload arrives from the
API in a fullName field, not from the URL, so it hits every user who opens
the list.

## Step 2 — Rule

render customer text as plain children and let JSX escaping do the work.
no `dangerouslySetInnerHTML`, no `innerHTML`, no `document.write`, no
`eval` on anything that came from the API or a form. the lab 35 list and
form already pass names through as text children, so this is a rule to
hold, not a change to make.

## Step 3 — CSP

`default-src 'self'; script-src 'self'; object-src 'none'; base-uri
'self'` reduces the damage an injected inline script can do, but it is
defense in depth on top of escaping, not a replacement for it — the lab
documents the header with `curl -I` evidence rather than relying on it.

## Step 4 — Test idea

paper test string `Amina <b>Khan</b>` must show the angle brackets as
literal text and not render bold. the lab's xss.test.tsx does the same
check with `<img onerror=alert(1)>` as a fullName and asserts no img node
exists in the output.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab36-xss-csp.md`
- [ x ] dangerouslySetInnerHTML warning
- [ x ] Prefer-escaping rule
- [ x ] Test string recorded
