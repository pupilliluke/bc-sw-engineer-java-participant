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
${NORTHSTAR_API_KEY} with no defaults. ProdSecretsCheck is a @Profile("prod")
component whose @PostConstruct rejects a value that is blank or still holds an
unresolved placeholder, so startup stops during context refresh.

  IllegalStateException: northstar.integration.api-key still holds the
  unresolved placeholder ${NORTHSTAR_API_KEY}; set it from the environment
  before starting prod

With DB_USERNAME, DB_PASSWORD and NORTHSTAR_API_KEY set, the same command starts
normally.

The check is not decoration. Without it prod starts clean, because Boot's
@ConfigurationProperties binder resolves with ignoreUnresolvablePlaceholders
true and hands the literal text ${NORTHSTAR_API_KEY} to the field. The startup
log read apiKeySet=true in that state. A blank password reaches the pool the
same way.

Measured with the guard off, prod profile, no env vars:

| Attempt | Result |
| --- | --- |
| password: ${DB_PASSWORD}, no default | starts |
| password: ${DB_PASSWORD:}, empty default | starts |
| @Validated with @NotBlank on apiKey | starts, the literal is not blank |
| the same placeholder on the int connect-timeout-ms | APPLICATION FAILED TO START |

The leniency is type specific. The literal survives as a String and only fails
where it has to convert, which is why none of the three secrets can raise it and
the int does. Removing the empty default changes nothing, and @NotBlank passes
because ${NORTHSTAR_API_KEY} is 22 non-blank characters.

Environment.getProperty behaves the other way and throws

  Could not resolve placeholder 'DB_PASSWORD'

which is why the check reads the two datasource values through the Environment
and the api key off the bound object.


3. DEFAULT PROFILE

spring.profiles.default dev in application.yml loads application-dev.yml when
nothing is activated. The startup banner still reads

  No active profile set, falling back to 1 default profile: "default"

and getActiveProfiles returns empty, because dev is the default profile and not
an active one. The file loads regardless, confirmed by putting a marker
api-base-url in application-dev.yml and reading it back with no profile set.
