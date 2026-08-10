Lab 26 profile and precedence notes

1. WHICH SOURCE WON IN THE OVERRIDE EXPERIMENT

Measured on northstar.integration.connect-timeout-ms under the test profile,
read from the startup log line in CrmApplication.

| Layer | Source | connect-timeout-ms |
| ----- | ------ | ------------------ |
| Profile YAML | application-test.yml | 100 |
| Env var | NORTHSTAR_INTEGRATION_CONNECT_TIMEOUT_MS | 9999 |
| CLI -D | northstar.integration.connect-timeout-ms | 1234 |

Each row keeps the one above it in place, so the third run still has the env var
at 9999 and the system property wins anyway. CLI beats env beats profile YAML
beats base application.yml at 2000.

Profile activation follows the same order. SPRING_PROFILES_ACTIVE=test with
-Dspring.profiles.active=dev on the same command starts dev.


2. EVIDENCE THAT PROD REFUSES MISSING SECRETS

application-prod.yml binds ${DB_USERNAME}, ${DB_PASSWORD} and
${NORTHSTAR_API_KEY} with no defaults, and switches the JDBC URL to postgres.

  mvn -B spring-boot:run "-Dspring-boot.run.profiles=prod"

  The following 1 profile is active: "prod"
  ERROR com.zaxxer.hikari.HikariConfig : Failed to load driver class org.postgresql.Driver
  BeanCreationException: Error creating bean with name 'dataSource'
  BUILD FAILURE

prod refuses to stay up without the prod database stack. dev and test start
normally on H2. There is no APPLICATION FAILED TO START banner for this one;
Boot has no failure analyzer for a missing driver class, so the output is the
BeanCreationException and the Maven BUILD FAILURE.

What does not stop it is the placeholders. Boot's @ConfigurationProperties
binder resolves with ignoreUnresolvablePlaceholders true, so an unresolved
${NORTHSTAR_API_KEY} binds as that literal text and the startup log reads
apiKeySet=true. Measured on the prod profile with no env vars:

| Attempt | Result |
| --- | --- |
| password: ${DB_PASSWORD}, no default | binds the literal, no failure |
| password: ${DB_PASSWORD:}, empty default | same |
| @Validated with @NotBlank on apiKey | passes, the literal is not blank |
| the same placeholder on the int connect-timeout-ms | APPLICATION FAILED TO START |

The leniency is type specific. The literal survives as a String and only fails
where it has to convert, which is why none of the three secrets can raise it and
the int does. Removing the empty default changes nothing on its own, and
@NotBlank passes because ${NORTHSTAR_API_KEY} is 22 non-blank characters.

Environment.getProperty behaves the other way and throws

  Could not resolve placeholder 'DB_PASSWORD'

so the value is only unreachable through that path, not through binding.


3. DEFAULT PROFILE

spring.profiles.default dev in application.yml loads application-dev.yml when
nothing is activated. The startup banner still reads

  No active profile set, falling back to 1 default profile: "default"

and getActiveProfiles returns empty, because dev is the default profile and not
an active one. The file loads regardless, confirmed by putting a marker
api-base-url in application-dev.yml and reading it back with no profile set.
