Lab 28 Spring Security (reflection questions, checkpoints)

built under examples\lab28-crm, copied from the lab 28 starter. login issues the
lab stub token, JwtAuthenticationFilter validates it and sets the
SecurityContext, and the matchers do the roles. 3 tests green on two consecutive
clean runs. app captured on port 8080, experiments 1 to 4 run, 5 skipped as
optional.


REFLECTION QUESTIONS

1. Which design decision most affected correctness (stateless token vs session)?

stateless. the token carries the subject and the role, so every request is
authenticated on its own and nothing is kept between them. SessionCreationPolicy
is STATELESS and no HttpSession is created, which means a lost or replayed
request cannot depend on server state that is no longer there.

2. What evidence proves role separation works?

the same route answering differently for two tokens. agent1 gets 403 on
/api/admin/ping and admin1 gets 200, on live Tomcat and in
agentCanReadCustomerButNotAdmin and adminCanPing. both read CUS-1001 fine, so
the difference is the matcher and not the login.

3. Which failure was hardest to diagnose (401 vs 403 vs filter order)?

a valid token returning 401 while the code was already correct. spring-boot:run
compiles once at startup, so the running app still had parseSubject throwing
UnsupportedOperationException, the filter caught it and never set the context.
nothing in the response says the build is stale. restarting fixed it.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab28-crm under examples/ | Pass, copied from starter/ |
| A2 | northstar.security.jwt-secret / JWT_SECRET configured | Pass, env var with lab-only-change-me as the default |
| A3 | .env.example present, real .env not staged | Pass, no .env in the project and .env is gitignored |
| B1 | stateless chain, permitAll login + health + /error | Pass, csrf off, httpBasic and formLogin disabled |
| B2 | CrmUserDetailsService loads agent1 / admin1 | Pass, in memory, BCrypt, unchanged from the starter |
| B3 | login returns {accessToken, tokenType} | Pass, lab.agent1.AGENT.f5784034 |
| C1 | Bearer access to CUS-1001 as AGENT | Pass, Amina Khan ACTIVE |
| C2 | missing or invalid token gives 401 | Pass, missing, malformed and tampered signature all 401 |
| C3 | AGENT on admin 403, ADMIN 200, matcher only | Pass, checked on live Tomcat as well as MockMvc |
| D1 | SecurityPathTest Tests run: 3 | Pass, two consecutive clean runs |
| D2 | production IdP / rotation notes | Pass, docs/security-notes.md |
| D3 | no tokens in logs or Git | Pass, nothing logs the header, target/ and .env ignored |

FULL PATH

| Item | Result |
| --- | --- |
| Failure experiments 1 to 4 | Pass |
| Experiment 5, real HS256 swap | not done, the GUIDE marks it optional and the stub is the timed contract |
| @PreAuthorize method security | not added, the GUIDE states matcher-only for the timed path |
| Secret rotation measured, not described | Pass, f5784034 to 7c3360d4 and the old token 401 |

SECURITY AND PRODUCTION REVIEW

1. which inputs are untrusted?

the login body, the Authorization header and the customer id in the path.
username and password go straight to loadUserByUsername and the encoder, the
header is split on dots with no length limit, and the id is a map key, so an id
that is not seeded is a 500 rather than a 404.

2. where are authn/authz enforced?

both in the filter chain. JwtAuthenticationFilter authenticates and the
requestMatchers authorize. no @PreAuthorize anywhere, so the controllers and the
service carry no rules and the matcher list is the whole policy.

3. which values are sensitive, and where stored?

JWT_SECRET, the bearer tokens and the two lab passwords. the secret is an
environment variable with a lab default in application.yml, tokens exist only in
the response body and the request header, and the passwords are BCrypt encoded
in memory. all lab-only and none of it is production.

WHAT THE STUB TOKEN DOES NOT PROVE

the signature is a hash of the secret, so it is the same on every token.
lab.agent1.ADMIN.f5784034 reaches /api/admin/ping and returns 200. the role can
be rewritten because the signature does not cover it. a real HS256 JWT signs the
claims, which is the difference. experiment 4 in
notes/screenshots/lab-28/03-failure-experiments.txt.

WINDOWS HOW-TO PASS CRITERIA

| # | Confirm | Result |
| - | --- | --- |
| 1 | workspace open in IntelliJ with SDK 21 | Pass, temurin-21.0.4 |
| 2 | lab project under examples/lab28-crm | Pass |
| 3 | GUIDE deliverables and checkpoints complete | Pass |
| 4 | commands succeed | Pass, mvn -B test and spring-boot:run |
| 5 | evidence under notes/screenshots/lab-28 | Pass, kept in the project as since lab 14 |
