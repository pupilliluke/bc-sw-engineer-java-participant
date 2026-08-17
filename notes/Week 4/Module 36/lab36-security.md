# Lab 36 — Threat Sketch

## Step 1 — Assets

What attackers want: session tokens, customer PII for Amina/Ravi, admin
actions.

| Asset | Where it lives | Why it is wanted |
| --- | --- | --- |
| access token | one in-memory variable after login | replay it and be the user against the Spring API |
| customer PII | CUS-1001 Amina Khan ACTIVE, CUS-1002 Ravi Singh PROSPECT, rendered in the browser | the data itself, name and status are on screen |
| admin actions | privileged routes and buttons in the SPA | act as the user instead of only reading |

the SPA is where all three meet, the token and the PII are in the same
javascript context as any script the page loads.

## Step 2 — Threats

xss, token theft, csrf (if a cookie session is used), open redirects.

| Threat | How it lands here |
| --- | --- |
| stored xss | a customer fullName from the API carries `<script>`, the UI renders it as HTML instead of text |
| token theft | injected script runs in the app's own context, so it can call the app's fetch path and exfiltrate the response even without reading the token variable |
| csrf | only in scope if auth ever moves to a cookie the browser attaches automatically, a malicious page cannot forge an explicit Authorization header |
| open redirect | a returnUrl on the login route pointing off-origin, login succeeds and hands the user to the attacker's page |

lab 35's http.request already sets its headers explicitly and carries
X-Correlation-Id: lab-request-001, so the auth header in lab 36 is
something the code adds on purpose, not something the browser sends by
itself.

## Step 3 — UI vs API

hiding a button is not authorization — Spring must enforce it. this is
OWASP A01, a user can edit the URL or devtools past ProtectedRoute and the
API still has to reject the request.

## Step 4 — Notes

threat list and the UI-vs-API sentence saved to notes/lab36-security.md,
the graded version becomes docs/security-decisions.md in lab 36.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab36-security.md`
- [ x ] ≥3 threats named
- [ x ] Authorization boundary stated
- [ x ] Notes saved
