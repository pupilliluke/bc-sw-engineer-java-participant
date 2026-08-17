Northstar CRM frontend security (Lab 36)

Two projects. crm-api is the Spring backend (the lab 35 copy of lab25-crm
with Spring Security added, lab35-crm is untouched). crm-ui is the React
SPA carried forward from lab35-crm with the auth and hardening work.

START THE API FIRST

  cd crm-api
  mvn spring-boot:run
  # port 8080, in-memory store, seeds Amina CUS-1001 and Ravi CUS-1002
  # /api/customers now needs a bearer token, anonymous gets 401

  mvn test
  # expect Tests run: 13, the 6 service tests plus 7 security rule tests

THEN THE SPA

  cd crm-ui
  npm install
  cp .env.example .env
  npm run dev
  # Vite on http://localhost:5173, sign in as agent1 / agent1

  npm run test -- --run
  # expect Test Files 4 passed, Tests 30 passed, no API needed (fetch mocked)

  npm run build
  # expect dist/ built, no type errors

DEMO LOGINS

  agent1 / agent1   AGENT, can read and write /api/customers
  admin1 / admin1   ADMIN, also allowed on /api/admin/**

Lab-only credentials defined in CrmUserDetailsService. lab-token-001 in the
notes and tests is a fake token.

THE LAB WORK

Frontend: tokenStore.ts holds the access token in one in-memory variable,
never localStorage or sessionStorage. AuthContext models checking /
anonymous / authenticated and owns login, logout and the expired-session
transition. ProtectedRoute renders the loading view while checking, the
login view when anonymous, the workspace when authenticated. http.request
attaches Authorization only when the request origin equals the API origin,
keeps X-Correlation-Id: lab-request-001 on CRM calls, clears the token and
signals expiry on 401, and raises ForbiddenError on 403 without touching
the session. LoginPage uses one generic error, disables repeat submits and
routes returnUrl through an internal-paths-only allowlist. CustomerCard
renders every field as a text child.

Backend: SecurityConfig is stateless with a bearer filter, /api/auth/login
open, /api/customers/** for AGENT or ADMIN, /api/admin/** for ADMIN, 401
from the entry point and 403 from the access denied handler, and the
response headers CSP, nosniff, Referrer-Policy and X-Frame-Options.

Threat model, the token storage decision, the CSRF N/A rationale and the
non-controls are in crm-ui/docs/security-decisions.md.

DELIBERATE DIFFERENCES FROM THE GUIDE

1. No react-router in this tree, the SPA has been single-view since lab 33.
   ProtectedRoute swaps the view instead of rendering Navigate to /login
   with Outlet. Same UX as the GUIDE's replace flag, the blocked view is
   never in history. The GUIDE's state={{from: location.pathname}} becomes
   blockedReturnPath(), which keeps the blocked path and drops an external
   returnUrl, so a deep link survives the sign-in without trusting a query
   parameter.

2. The GUIDE's tokenStore.set takes only the token. Here it takes the token
   and the session user together in the same in-memory variable, so the
   guard can resolve checking without parsing the token client-side.

3. Logout has no server revoke call, the API is stateless bearer with no
   revoke endpoint. The token is discarded and the workspace unmounts with
   its customer state, tested by asserting no PII renders after sign out.

4. CORS moved from WebMvcConfigurer.addCorsMappings to a
   CorsConfigurationSource bean. With Spring Security in the chain one
   source has to own the allowlist or both write the response headers.

5. crm-ui/src/api/customers.test.ts now reads the correlation header with
   Headers.get instead of a plain object index, because the origin check
   needs a Headers object.

6. JwtService signs the claims, not the secret alone. Lab 29's stub built
   the signature from the secret only, so holding one valid token was
   enough to edit its role segment to ADMIN and be served as an admin. A
   live curl proved it before the fix (evidence file 02). The signature is
   now HmacSHA256 over subject.role, compared with MessageDigest.isEqual.
   Still a lab stub, not a real JWT: no exp claim, so the token does not
   expire server-side and the 401 expiry path is exercised by the UI tests
   rather than by a clock.

The GUIDE hard gate names Node 22+. Built and tested here on Node 20.18.0,
everything runs, Vite 5 requires 18+.

TIMED-PATH PASS CRITERIA

| Criterion | Result |
| --------- | ------ |
| Token in memory only, nothing in web storage | Pass |
| ProtectedRoute guards the workspace, UX only | Pass |
| Bearer attached only to the CRM API origin | Pass |
| XSS proof, malicious fullName renders as text | Pass |
| 401 clears the session, 403 does not | Pass |
| CSP and security headers captured | Pass |

CLEANUP

  # stop Vite and Spring with Ctrl+C
  # sign out first, tokenStore.clear() empties the session
  git status --short

node_modules/, dist/, target/ and .env are ignored, .env.example is
committed. Keep lab36-crm, lab 37 designs the PostgreSQL schema while
these browser controls stay in force.

NOTES

Evidence is in java-bootcamp/notes/screenshots/lab-36/, the auth
transcripts, the security headers capture, both test runs and the failure
experiments. Checkpoints and reflection answers are in
notes/Week 4/Module 36/lab36-answers.md. Full GUIDE at
labs/Week 4 - Kafka, React, PostgreSQL and Resilience/module-36/lab36/.
