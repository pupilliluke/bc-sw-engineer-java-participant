# Lab 21 — Actuator Allow-List

## Candidates
health, info, metrics, prometheus, env, beans, configprops

## Lab allow
health, metrics for demos

## Lock / deny
env, beans, configprops

## Prod auth note
authenticate/network-restrict management port
Lab exposure ≠ production exposure.

Should /actuator/beans be on the lab allow-list for the graded demo?

sure

What happens if exposure.include=* in production YAML?

Caution!


- [ x ] File exists at `notes/lab21-actuator-allowlist.md`
- [ x ] Candidates listed
- [ x ] Lab allow stated
- [ x ] Prod caution present
