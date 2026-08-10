Northstar CRM build (Lab 28)

  mvn -B test
  mvn -B spring-boot:run

  curl -s -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"agent1","password":"agent1"}'
  # expect {"accessToken":"lab.agent1.AGENT.<sig>","tokenType":"Bearer"}

  curl -s http://localhost:8080/api/customers/CUS-1001 \
    -H "Authorization: Bearer <token>" \
    -H "X-Correlation-Id: lab-request-001"
  # expect 200 Amina Khan ACTIVE

  curl -s -i http://localhost:8080/api/admin/ping \
    -H "Authorization: Bearer <agent1 token>"
  # expect HTTP 403

  git status --short

Copied from the lab 28 starter. Spring Security protects the customer and admin
APIs that lab 27 left open. Login issues a lab stub token, JwtAuthenticationFilter
validates it on every request and sets the SecurityContext, and the matchers do
the role checks. Redact the token before pasting a transcript anywhere.

JWT_SECRET is read from the environment and falls back to lab-only-change-me in
application.yml. .env.example holds the name and the lab value. There is no .env
in the project and .env is gitignored.

DEMO USERS

| username | password | role |
| -------- | -------- | ---- |
| agent1 | agent1 | AGENT |
| admin1 | admin1 | ADMIN |

Both are in-memory in CrmUserDetailsService with BCrypt encoded passwords.
Lab-only accounts.

MATCHERS

| Route | Rule |
| ----- | ---- |
| /api/auth/login, /actuator/health, /error | permitAll |
| OPTIONS /** | permitAll |
| /api/admin/** | hasRole ADMIN |
| /api/customers/** | hasAnyRole AGENT, ADMIN |
| everything else | authenticated |

Rules match in declaration order, so the OPTIONS line has to sit above the two
role lines or a preflight to /api/customers/** matches the AGENT rule first.

/error is permitAll because Boot dispatches sendError(403) through it. With
/error authenticated, agent1 on the admin route comes back 401 on live Tomcat
instead of 403.

The chain is STATELESS, csrf is off, httpBasic and formLogin are disabled, and
the entry point is HttpStatusEntryPoint(UNAUTHORIZED). Without that entry point
Spring Security falls back to Http403ForbiddenEntryPoint and a missing token
returns 403 rather than 401.

TESTS

  mvn -B test    Tests run: 3

missingTokenIs401 calls the customer route with no header and again with a
tampered signature, 401 both times. agentCanReadCustomerButNotAdmin logs in as
agent1, reads CUS-1001 as Amina Khan ACTIVE, then gets 403 on /api/admin/ping.
adminCanPing logs in as admin1 and gets 200 with role ADMIN.

All three log in through /api/auth/login instead of @WithMockUser, so the token
format and the filter are covered by the assertions.

SECURITY NOTES

untrusted: the login body, the Authorization header and the customer id in the
path. the header is parsed by string split and the id goes straight into a map
lookup, so an unknown id is a 500 from IllegalArgumentException rather than a 404.

authn/authz: both in the filter chain. JwtAuthenticationFilter authenticates,
the requestMatchers authorize. no @PreAuthorize anywhere, so there is nothing on
the service or the controller and the matchers are the only rule.

sensitive: JWT_SECRET, the bearer tokens and the two lab passwords. the secret
is an environment variable with a lab default, the tokens exist only in the
response body and the Authorization header, and the passwords are BCrypt encoded
in memory. nothing is logged.

The token is a lab stub and not a real JWT. docs/security-notes.md has what it
does not prove and the production checklist.

CLEANUP

  mvn -q clean
  git status --short

Ctrl+C spring-boot:run. target/ and .env are ignored. Keep lab28-crm, lab 29
adds Bean Validation and ErrorResponse on this secured API.

NOTES

Evidence and the failure experiments are in notes/screenshots/lab-28/.
Checkpoints and reflection answers are in notes/Week 3/Module 28/lab28-answers.md.
The 401/403 write-up and the production checklist are docs/security-notes.md.
Full GUIDE at
labs/Week 3 - Spring Framework and Enterprise Patterns/module-28/lab28/.
