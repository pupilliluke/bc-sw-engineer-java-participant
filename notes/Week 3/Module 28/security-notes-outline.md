# Lab 28 — Production IdP Checklist

## IdP note
replace the in-memory agent1/admin1 users with a real IdP. those two are lab
accounts for the timed path, they are never production accounts.

## Key rotation
rotate the JWT signing key on a schedule. the key comes from the environment or
a secret manager, never from a config file in the repo.

## Transport / TTL
short access-token TTL with a refresh flow, HTTPS everywhere, no plain HTTP.

## Logging hygiene
never log the raw bearer token. log failed logins for the audit trail, subject
and outcome only.

## Scope
Pre-lab only. No real secrets. lab 27's transfer routes stay behind auth in the
production narrative too. no OAuth2 authorization server here, outline only.


## Debug / design challenge

Does Lab 28 require standing up Keycloak for Pass?

no. the checklist is an outline, the lab still issues its own JWT.

## Predict the Output / Behavior

What do you do if a JWT signing secret was committed?

treat it as leaked, rotate the key so every token signed with it stops
verifying, then purge it from the repo and reissue from the environment.


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/security-notes-outline.md`
- [ x ] IdP note
- [ x ] Rotation
- [ x ] No secrets