# Lab 23 — Auto-Config Versus Ownership

| Boot / auto-config gift | Still owned by the team |
| --- | --- |
| Embedded Tomcat + DispatcherServlet | Customer create/get rules |
| Jackson JSON mapping | Fixture IDs CUS-1001 / CUS-1002 |
| Actuator health infrastructure | Which endpoints to expose |

Auto-config ≠ business rules.

## Debug / design challenge

If health is UP but POST create always returns 500, is that an auto-config failure or ownership?

ownership


## Predict the Output / Behavior

Name one thing removing starter-web would take away from Lab 23

embedded tomcat, dispatcher servlet


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/autoconfig-ownership.md`
- [ x ] Three pairs
- [ x ] Ownership of CRM rules clear
- [ x ] One-sentence rule
