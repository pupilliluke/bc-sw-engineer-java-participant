# Lab 26 — Profile Purposes

| Profile | Purpose |
| --- | --- |
| dev | Local CRM smoke; relaxed logging; H2-friendly |
| test | Surefire / BootTest isolation  |
| prod |Deployed settings; secrets via env; fail fast|

## One risk if prod uses dev YAML
Customers will be confused by in memory database

## Scope
Pre-lab only.

## Debug / design challenge

Should test profile point at a shared teammate’s laptop H2 file?

no, it is a dev file

## Predict the Output / Behavior

Name one setting that must differ between dev and prod.

Database connection string

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/profiles.md`
- [ x ] Three purposes
- [ x ] Prod risk noted
