# Lab 26 — Activation Command Drill

## -D / Maven run (dev)
mvn -B spring-boot:run -Dspring-boot.run.profiles=dev
mvn -B spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=dev

## Env activation (your OS)
Windows PowerShell: $env:SPRING_PROFILES_ACTIVE="dev"
macOS / Linux: export SPRING_PROFILES_ACTIVE=dev

never start the prod profile without the required env vars set. prod has no
defaults for them, so it fails fast at startup instead of connecting with a
blank password.

## Tests (test profile)
mvn -B test -Dspring.profiles.active=test


## Debug / design challenge

If you export SPRING_PROFILES_ACTIVE in one terminal and run Maven in another, what happens?

nothing carries over. the variable belongs to the first shell, the second one
never sees it and falls back to the default profile.

## Predict the Output / Behavior

Which profile should Surefire use by default for this lab?

test


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/activation-commands.md`
- [ x ] -D command
- [ x ] Env command
- [ x ] Test command
