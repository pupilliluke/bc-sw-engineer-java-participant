# Lab 28 — JWT Login TODOs

## Login path + body
POST /api/auth/login {username,password} → {accessToken, tokenType}


## Token response
JwtService issueToken / parseSubject / parseRole 


## Bearer header form
Client: Authorization: Bearer <accessToken>

## Lab users/roles
Lab users: agent1 (AGENT), admin1 (ADMIN)

## Secret handling
Secret: env JWT_SECRET → northstar.security.jwt-secret (placeholder in .env.example)


## Debug / design challenge

Where should password encoding happen relative to token issue?
before password transfer into database

## Predict the Output / Behavior

Is putting the JWT in a query string a good default?

no

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab28-jwt-login-todos.md`
- [ x ] Login path
- [ x ] Bearer form
- [ x ] Users/roles
- [ x ] No real secrets
