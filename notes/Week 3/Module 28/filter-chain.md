# Lab 28 — SecurityFilterChain Sketch

## Session policy
STATELESS

## Login matcher
/api/auth/login → permitAll


## Customers matcher + roles
/api/customers/** → hasAnyRole(AGENT, ADMIN)


## Admin matcher + roles
Other APIs → authenticated (default deny extras)


## Debug / design challenge

Should CSRF stay enabled for a pure Bearer JWT API?
CSRF is a security feature, not a design challenge.

## Predict the Output / Behavior

What goes wrong if /api/customers/** is permitAll?
It would allow unauthorized access to customer data.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/filter-chain.md`
- [ x ] Login permitAll
- [ x ] Customers rosles
- [ x ] Admin ADMIN
