# Lab 36 security decisions

Northstar CRM SPA, examples/lab36-crm. Threat model first, then the controls
that follow from it, then the two decisions that are deliberately not controls.

## Assets

| Asset | Where it lives | What an attacker gains |
| --- | --- | --- |
| Access token | one in-memory variable in `src/auth/tokenStore.ts` | replay as agent1 against every `/api/customers` call |
| Customer PII | CUS-1001 Amina Khan (ACTIVE), CUS-1002 Ravi Singh (PROSPECT), rendered in the browser | the data itself, names, emails and status |
| Demo credentials | typed into the login form, never stored | a session without stealing a token |
| Admin surface | `/api/admin/**`, ADMIN role only | privileged actions rather than reads |

## Browser inputs (all untrusted)

Login form fields, the customer form, the search box, the `returnUrl` query
parameter, and every field of every API response, including `name`. Response
data is not treated as clean.

## Trust boundaries

| Boundary | Rule |
| --- | --- |
| Browser to Spring | the browser is untrusted territory; Spring Security authorizes every `/api` call |
| Vite origin to API origin | `http://localhost:5173` to `http://localhost:8080`, CORS allowlists the UI origin exactly |
| Bearer destinations | the token is attached only when the request origin equals the API origin |

## Attacker goals to controls

| Attacker goal | Control | Where |
| --- | --- | --- |
| Steal the token from storage | in-memory only, never localStorage or sessionStorage | `src/auth/tokenStore.ts` |
| Exfiltrate the token to a third party | attach `Authorization` only when `url.origin === apiOrigin` | `src/api/http.ts` |
| Execute a payload in a customer name | text children and JSX escaping, no HTML sinks | `src/components/CustomerCard.tsx`, `src/security/xss.test.tsx` |
| Enumerate accounts through login | one generic message, "Invalid username or password" | `src/pages/LoginPage.tsx` |
| Land the user on an attacker page after login | `returnUrl` allowlist, internal paths only, blocked path kept instead | `src/auth/returnUrl.ts` |
| Keep using a session after logout | token cleared, workspace unmounts with its customer state | `src/auth/AuthContext.tsx` |
| Read PII with an expired token | 401 clears the token and returns to sign-in | `src/api/http.ts` |
| Reach admin data with an agent token | Spring `hasRole("ADMIN")`, 403 to the UI | `crm-api SecurityConfig` |
| Frame the app or sniff content types | CSP, `frame-ancestors 'none'`, nosniff, `Referrer-Policy: no-referrer` | `crm-api SecurityConfig` |

## Token storage decision

| Option | XSS risk | CSRF risk | Verdict |
| --- | --- | --- | --- |
| In-memory variable | medium | low | chosen |
| sessionStorage | high | low | banned in this lab |
| localStorage | high | low | banned in this lab |
| HttpOnly cookie | low | high | the production alternative, needs a CSRF strategy |

In-memory keeps nothing for an injected script to read after a reload, and this
lab bans web storage for tokens outright. The cost is documented, not hidden:
the token is lost on refresh and is not shared across tabs, so both cases end at
the sign-in screen. An `HttpOnly; Secure; SameSite` cookie plus CSRF tokens is
the stronger production answer; it is not what this lab builds.

## CSRF: N/A for this build, with the rationale

This SPA is bearer-only. The token lives in memory and `http.request` adds
`Authorization: Bearer` itself, and the API is stateless with no session cookie
(`SessionCreationPolicy.STATELESS`, `csrf.disable()`). A malicious page can make
a browser send a request to the API, but it cannot make the browser add a header
that only this app's code sets, so classic CSRF does not apply to this design.

If this app ever moves to cookie sessions, CSRF is immediately in scope and the
controls are: `SameSite=Lax` or `Strict` with `Secure` and `HttpOnly`, a CSRF
token on every unsafe method (`X-XSRF-TOKEN` header against the `XSRF-TOKEN`
cookie, `credentials: "include"` on the fetch), POST/PUT/DELETE rather than GET
for state changes, an Origin or Referer check on the server, and re-authentication
for sensitive actions. `csrf.disable()` in `SecurityConfig` would have to be
removed at the same time.

CSRF is also not CORS. CORS is the browser refusing to hand a cross-origin
response back to script; CSRF is the server trusting a request the browser sent
on its own. A forged request can succeed even when the attacker never reads the
response.

## Response headers

Configured in `SecurityConfig`, so they apply to API responses:

```text
Content-Security-Policy: default-src 'self'; object-src 'none'; frame-ancestors 'none'; base-uri 'self'
X-Content-Type-Options: nosniff
Referrer-Policy: no-referrer
X-Frame-Options: DENY
```

CSP limits what an injected script can reach, the JSX escaping is what stops the
injection. In production the SPA host needs the same headers plus HTTPS and HSTS
(`Strict-Transport-Security`); Spring Security only emits HSTS on HTTPS
requests, and this lab runs on plain `http://localhost`. Evidence is in
notes/screenshots/lab-36/03-security-headers.txt.

## Not controls

Route guards are not authorization. `ProtectedRoute` stops the browser
rendering a view that cannot load; it runs in the part of the system the
attacker controls. A user can edit the URL or the devtools state past it, and
the API must still answer 401 or 403 — that is OWASP A01, and it is why the
401/403 behaviour and the Spring rules are tested on both sides.

Hiding a button by role is also not authorization. It removes the affordance and
nothing else; the endpoint behind it stays protected by `hasRole`.

## Lab-only credentials

`agent1/agent1` (AGENT) and `admin1/admin1` (ADMIN) are course demo logins
defined in `CrmUserDetailsService`, and `lab-token-001` is a fake token used in
notes and tests. The signing secret comes from `JWT_SECRET` with a lab default.
No real credential, token or key is in this tree, and Authorization headers are
redacted in the evidence files.
