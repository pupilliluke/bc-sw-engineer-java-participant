Northstar CRM build (Lab 26)

  mvn -B spring-boot:run -Dspring-boot.run.profiles=dev
  curl -s -H "X-Correlation-Id: lab-request-001" http://localhost:8080/api/customers/CUS-1001
  mvn -B test -Dspring.profiles.active=test

  # expect fail without env:
  mvn -B spring-boot:run -Dspring-boot.run.profiles=prod

  # PowerShell profile via env:
  # $env:SPRING_PROFILES_ACTIVE="test"
  # mvn -B spring-boot:run
  # Remove-Item Env:SPRING_PROFILES_ACTIVE

  git status --short

Copied from the lab 26 starter, not carried forward from lab25-crm, so the
service here is the flat seeded map again with no repository layer. Base
application.yml carries name, port and the northstar.integration defaults;
application-dev.yml and application-test.yml carry H2 URLs lab26dev and
lab26test; application-prod.yml carries a postgres URL and three env
placeholders with no defaults.

org.postgresql:postgresql runtime is added to the starter pom. prod declares a
postgres URL and the starter ships only the H2 driver, so without it prod stops
on a missing driver class before it reaches the secrets. The host
db.example.internal is not reachable and does not need to be; Hikari opens the
pool on first use and nothing here queries.

NorthstarIntegrationProperties binds prefix northstar.integration and is enabled
by @EnableConfigurationProperties on CrmApplication. An ApplicationRunner logs
the bound profile, apiBaseUrl and connectTimeoutMs at startup, which is how the
override ladder was measured. It logs whether the api key is set, never its
value.

PROD FAIL-FAST

ProdSecretsCheck is a @Profile("prod") component. Its @PostConstruct rejects any
of northstar.integration.api-key, spring.datasource.username or
spring.datasource.password that is blank or still holds an unresolved ${...}
placeholder, so prod stops during context refresh.

It exists because prod does not fail on its own. Boot's @ConfigurationProperties
binder ignores placeholders it cannot resolve and binds the literal text
${NORTHSTAR_API_KEY} into the field, so the app starts and reports the key as
set. Details and the before and after evidence are in docs/profile-notes.md.

OVERRIDE ORDER

| Layer | Source | connect-timeout-ms |
| ----- | ------ | ------------------ |
| Profile YAML | application-test.yml | 100 |
| Env var | NORTHSTAR_INTEGRATION_CONNECT_TIMEOUT_MS | 9999 |
| CLI -D | northstar.integration.connect-timeout-ms | 1234 |

Base application.yml is 2000 underneath all three. Profile activation follows
the same order: env test plus -D dev starts dev.

PASS CRITERIA

| Criterion | Result |
| --------- | ------ |
| lab26-crm under examples/ | Pass |
| Shared application.yml with name northstar-crm | Pass |
| .gitignore covers .env | Pass, git check-ignore matches .gitignore:4 |
| dev / test / prod profile files exist | Pass |
| H2 URLs lab26dev and lab26test, prod host hard-coded | Pass |
| dev CRM smoke for CUS-1001 | Pass, 200 with Amina Khan ACTIVE |
| Activation via -D and via env | Pass, both banners captured |
| docs/profile-notes.md present | Pass |
| @ConfigurationProperties + fail-fast on prod | Pass, ProdSecretsCheck |
| ProfileBindingTest Tests run: 1 under test | Pass, two consecutive runs |
| .env.example only, no secrets staged | Pass |

SECURITY NOTES

sensitive per profile: dev and test have none, the H2 URLs are in-memory and the
password is empty. prod has DB_USERNAME, DB_PASSWORD and NORTHSTAR_API_KEY, all
env only, none written to any file in this repo.

why prod avoids ${DB_PASSWORD:} with a default: an empty default means the app
connects with a blank password instead of stopping, which is the incident the
lab exists to prevent. The placeholders here have no defaults and
ProdSecretsCheck stops startup when they are unresolved.

if a real postgres password were committed: it is exposed the moment it is
pushed, so rotate it first and treat scrubbing as second. Removing the line in a
later commit does not remove it from history, and history rewriting on a shared
repo is a separate decision from rotating the credential.

.env.example carries placeholders only, DB_USERNAME=crm, DB_PASSWORD=change-me,
NORTHSTAR_API_KEY=lab-only-key. .env is ignored and was never committed.

CLEANUP

  mvn -q clean
  git status --short

Ctrl+C spring-boot:run and clear SPRING_PROFILES_ACTIVE,
NORTHSTAR_INTEGRATION_CONNECT_TIMEOUT_MS, DB_USERNAME, DB_PASSWORD and
NORTHSTAR_API_KEY from the shell. target/ is ignored. Keep lab26-crm, lab 27
builds transactional services on this config.

NOTES

Evidence and the failure experiments are in notes/screenshots/lab-26/.
Checkpoints and reflection answers are in notes/Week 3/Module 26/lab26-answers.md.
The precedence notes are docs/profile-notes.md. Full GUIDE at
labs/Week 3 - Spring Framework and Enterprise Patterns/module-26/lab26/.
