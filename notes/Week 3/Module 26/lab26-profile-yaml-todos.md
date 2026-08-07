# Lab 26 — Profile YAML TODOs

Files: application.yml, application-dev.yml, application-test.yml, application-prod.yml
Base: spring.application.name, server.port
dev: logging.level DEBUG (example)
prod: spring.datasource.password: ${DB_PASSWORD} (no default)
Never commit real DB_PASSWORD.

# Lab 26 — Profile YAML TODOs

## Required files
application.yml, application-dev.yml, application-test.yml, application-prod.yml

## Base keys
spring.application.name, server.port

## dev example key
logging.level DEBUG

## prod secret pattern
spring.datasource.password: ${DB_PASSWORD} (no default)


## Debug / design challenge

What happens if you name the file application.dev.yml instead of application-dev.yml?

A: No file  identified by spring profiles / wrong name

## Predict the Output / Behavior

Is ${DB_PASSWORD:} with empty default acceptable in prod?

no

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab26-profile-yaml-todos.md`
- [ x ] Four files listed
- [ x ] prod secret pattern
- [ x ] No real secrets
