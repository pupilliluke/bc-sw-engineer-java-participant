Lab 28 security notes

401 AND 403

401 is authentication. No Authorization header, a token that does not start with
lab., or a signature that does not match the secret, and JwtAuthenticationFilter
never sets the SecurityContext. The request stays anonymous and the entry point
returns 401.

403 is authorization. The token parsed, the SecurityContext holds agent1 with
ROLE_AGENT, and /api/admin/** requires ROLE_ADMIN. The caller is known and the
route is refused anyway.

The matcher table is in README.md. Both statuses are captured live in
notes/screenshots/lab-28/02-auth-demo.txt and asserted in SecurityPathTest.

THE LAB TOKEN

lab.<subject>.<role>.<hex(secret.hashCode())>, for example
lab.agent1.AGENT.f5784034. This is not a JWT. It has no header, no claims, no
issuer and no expiry, and the last segment is Integer.toHexString of the secret's
hashCode rather than a MAC. The same signature appears on every token, so it
proves the issuer knew the secret and nothing about the subject or the role. Any
holder of the secret can mint lab.anyone.ADMIN.<sig> and the filter accepts it.

Experiment 4 in notes/screenshots/lab-28/03-failure-experiments.txt tampers the
signature and gets 401. Tampering the role while keeping the signature is the
case that is not caught.

The secret is northstar.security.jwt-secret, bound from ${JWT_SECRET} with the
default lab-only-change-me in application.yml. .env.example carries the name and
the lab value. No .env is committed.

PRODUCTION CHECKLIST

| Item | Why |
| ---- | --- |
| Replace agent1 / admin1 with an enterprise IdP | The in-memory users are lab-only accounts, hardcoded in CrmUserDetailsService with the username as the password |
| Verify real JWTs against the IdP's JWKS endpoint | Signature verification moves to a published public key, so the API never holds a signing secret |
| Keep signing keys in a secret manager and rotate on a schedule and on incident | A leaked key mints valid tokens for every subject and role until it is rotated |
| Short access-token TTL with a refresh flow | This token never expires, so a captured one is valid for the life of the secret |
| HTTPS everywhere, TLS 1.2 or later | The token travels in a plaintext header and a captured header is a full session |
| Never log raw bearer tokens | Log the subject and the outcome. A token in an access log is a credential in an access log |
| Audit failed logins | AuthController returns 401 on a bad password and records nothing today |
| No plaintext passwords at rest | BCryptPasswordEncoder is already in use, but CrmUserDetailsService re-encodes a literal on every call, which a real user store does not do |

Fixtures: CUS-1001 Amina Khan ACTIVE, CUS-1002 Ravi Singh PROSPECT, correlation
header lab-request-001. The correlation id is not a credential and nothing in the
filter chain reads it.
