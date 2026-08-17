Lab 36 frontend security for the CRM SPA (reflection questions, checkpoints)

built as examples\lab36-crm, copied from lab35-crm so the lab 35 artifact
stays as submitted. crm-ui gained the in-memory tokenStore, AuthContext
with checking / anonymous / authenticated, ProtectedRoute, LoginPage, the
origin-scoped Authorization header with the 401 and 403 split, complete
logout, CustomerCard and the XSS and security suites. crm-api gained
Spring Security ported from lab29-crm, the bearer filter, the auth and
admin endpoints, the role rules and the response headers. 30 UI tests and
14 API tests green on two consecutive runs, build green, plus a live pass
through the real stack in the browser.

same environment note as labs 33 to 35, the GUIDE hard gate names Node
22+, this machine runs Node 20.18.0, everything works, recorded as found.


REFLECTION QUESTIONS

1. Which design decision most affected correctness?

putting the origin check and the status branching in the one request()
boundary rather than in the callers. the token is attached only when
url.origin equals the API origin, so a third-party URL through the same
helper carries nothing, and 401 and 403 are decided once. every caller
inherits both, and the alternative, each api module deciding for itself,
is how a bearer header ends up on a CDN call or how a 403 quietly logs
someone out.

2. What evidence proves the implementation works?

the 401 and 403 tests carry the most weight, a UI that treats them the same
logs someone out for lacking a role. beyond the suites, the live
pass covered what mocks cannot, the preflight carrying
Access-Control-Request-Headers: authorization, an empty Application tab
after login, and the XSS payload going out to Spring, coming back and
rendering as text with zero img nodes. the abuse curls close it, anonymous
401, agent on admin 403, garbage signature 401.

3. Which failure was hardest to diagnose?

the token format inherited from lab 29 signed the secret and nothing else,
so the fourth segment is identical in every token the server issues.
editing my own agent token's role segment to ADMIN and keeping the
signature returned 200 from /api/admin/ping. it took an abuse probe to
find, the rest of the lab looked correct while the backend check could be
bypassed from the browser. the signature now covers subject and role with
HmacSHA256,
compared with MessageDigest.isEqual, and a regression test pins it.
transcript in notes/screenshots/lab-36/02-token-forgery.txt.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab36-crm copied from Lab 35 | Pass, crm-ui and crm-api, lab35-crm untouched |
| A2 | Threat model written, guards not authorization stated | Pass, crm-ui/docs/security-decisions.md, non-controls section |
| A3 | AuthState includes checking | Pass, checking / anonymous / authenticated in AuthContext |
| B1 | In-memory tokenStore only | Pass, one module variable, storage empty in the browser |
| B2 | Bearer attached only to CRM API origin | Pass, url.origin check, third-party test |
| B3 | Login generic errors, ProtectedRoute UX | Pass, one message for both failures, guard swaps the view, blocked deep link kept |
| B4 | 401 clears session, 403 does not, logout complete | Pass, split in request(), no PII after sign out or back |
| C1 | XSS RTL proof green | Pass, 3 payloads, no img/b/script nodes, live probe too |
| C2 | CSRF evidence or documented N/A | Pass, bearer-only N/A with cookie-mode controls written out |
| C3 | CSP and security headers evidence | Pass, 03-security-headers.txt, curl -I |
| C4 | Abuse tests and build green twice | Pass, 30 and 30 UI, 14 and 14 API, build green |
| D1 | No tokens or passwords in Git or evidence | Pass, signatures redacted, demo logins only |
| D2 | Security decisions doc complete | Pass, assets, boundaries, controls, non-controls |
| D3 | lab-request-001 on authenticated CRM calls | Pass, set in request(), asserted in the security suite |
