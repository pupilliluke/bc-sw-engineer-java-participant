Northstar CRM build (Lab 26)

  mvn -B spring-boot:run "-Dspring-boot.run.profiles=dev"
  curl -s -H "X-Correlation-Id: lab-request-001" http://localhost:8080/api/customers/CUS-1001
  mvn -B test "-Dspring.profiles.active=test"

  # expect fail without env:
  mvn -B spring-boot:run "-Dspring-boot.run.profiles=prod"

  # PowerShell profile via env:
  # $env:SPRING_PROFILES_ACTIVE="test"
  # mvn -B spring-boot:run
  # Remove-Item Env:SPRING_PROFILES_ACTIVE

  git status --short

PowerShell needs the -D argument quoted. Unquoted, Maven reads
.run.profiles=prod as a lifecycle phase and stops with Unknown lifecycle phase
before the app starts.

Copied from the lab 26 starter, not carried forward from lab25-crm, so the
service here is the flat seeded map again with no repository layer. Base
application.yml carries name, port and the northstar.integration defaults;
application-dev.yml and application-test.yml carry H2 URLs lab26dev and
lab26test; application-prod.yml carries a postgres URL and three env
placeholders with no defaults.

NorthstarIntegrationProperties binds prefix northstar.integration and is enabled
by @EnableConfigurationProperties on CrmApplication. An ApplicationRunner logs
the bound profile, apiBaseUrl and connectTimeoutMs at startup, which is how the
override ladder was measured. It logs whether the api key is set, never its
value.

PROD FAIL-FAST

  mvn -B spring-boot:run "-Dspring-boot.run.profiles=prod"

  The following 1 profile is active: "prod"
  ERROR com.zaxxer.hikari.HikariConfig : Failed to load driver class org.postgresql.Driver
  BUILD FAILURE

prod switches the JDBC URL to postgres and the classpath carries H2 only, so
startup stops at the dataSource bean. dev and test are unaffected.

The three placeholders are not what stops it. Boot's @ConfigurationProperties
binder leaves an unresolved ${...} as literal text rather than failing, so
${DB_PASSWORD} and ${NORTHSTAR_API_KEY} bind as strings and something later
breaks startup instead. Measurements are in docs/profile-notes.md.

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
| @ConfigurationProperties + prod fails to start without the postgres stack | Pass |
| ProfileBindingTest Tests run: 1 under test | Pass, two consecutive runs |
| .env.example only, no secrets staged | Pass |

SECURITY NOTES

sensitive per profile: dev and test have none, the H2 URLs are in-memory and the
password is empty. prod has DB_USERNAME, DB_PASSWORD and NORTHSTAR_API_KEY, all
env only, none written to any file in this repo.

why prod avoids ${DB_PASSWORD:} with a default: an empty default is a blank
password the app would carry into a connection. The placeholders here have no
defaults. Worth knowing that removing the default is not by itself a guard,
since an unresolved ${DB_PASSWORD} binds as literal text either way; what stops
prod on this classpath is the missing driver.

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
