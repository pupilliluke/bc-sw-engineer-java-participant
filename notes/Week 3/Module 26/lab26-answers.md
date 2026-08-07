Lab 26 spring profiles and configuration (reflection questions, checkpoints)

built under examples\lab26-crm, copied from the lab 26 starter rather than
carried forward from lab25-crm, so the service is the flat seeded map again.
full path: base plus dev, test and prod YAML, NorthstarIntegrationProperties
bound on prefix northstar.integration, activation proved with -D and with
SPRING_PROFILES_ACTIVE, and the step 6 override ladder measured. 1 test green on
two consecutive runs. dev smoke captured on port 8080.


REFLECTION QUESTIONS

1. Which design decision most affected correctness — YAML split or typed binding?

neither on its own. the split decides which values load and the typed class
decides whether a wrong key is caught, and experiment 3 shows the gap between
them: renaming connect-timeout-ms to connectTimeout in the YAML binds nothing,
falls back to the base 2000 and never warns. the split without validation is
what lets a config mistake look healthy.

2. What evidence proves prod cannot start with blank credentials?

experiment 1, in two halves. before ProdSecretsCheck, prod started with
apiKeySet=true because the binder had put the literal ${NORTHSTAR_API_KEY} in
the field. after it, the same command exits 1 on IllegalStateException naming
the unresolved placeholder, and starts normally once DB_USERNAME, DB_PASSWORD
and NORTHSTAR_API_KEY are set.

3. Which failure was hardest (missing prop, wrong profile, override confusion)?

none of those, because they all announce themselves. experiment 3 was the hard
one, and experiment 1 before the check for the same reason. both bind a wrong or
missing value silently and start clean, so the app is only wrong later and
somewhere else. an override that surprises you at least shows a number you can
read.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab26-crm under examples/ | Pass, copied from starter/ |
| A2 | Shared application.yml with name northstar-crm | Pass, plus port 8080 and the northstar.integration defaults |
| A3 | .gitignore covers .env / secrets | Pass, git check-ignore matches .gitignore:4 |
| B1 | application-dev.yml / -test.yml / -prod.yml exist | Pass |
| B2 | H2 URLs lab26dev / lab26test, prod URL hard-coded host | Pass, prod is db.example.internal with env placeholders |
| B3 | dev CRM smoke for CUS-1001 works | Pass, 200 Amina Khan ACTIVE, CUS-1002 also 200 |
| C1 | Activation via -D and via env evidenced | Pass, both banners in 02-activation-and-smoke.txt |
| C2 | docs/profile-notes.md present | Pass |
| C3 | @ConfigurationProperties class + fail-fast on prod | Pass, and the fail-fast needed ProdSecretsCheck to be real |
| D1 | ProfileBindingTest Tests run: 1 under test | Pass, two consecutive runs |
| D2 | .env.example only, no secrets staged | Pass, experiment 4 planted and reverted hunter2 |
| D3 | README / notes runbook complete | Pass |

FULL PATH

| Item | Result |
| --- | --- |
| Step 6 override ladder measured | Pass, 100 then 9999 then 1234 |
| Profile activation precedence | Pass, env test plus -D dev starts dev |
| Default profile behaviour | Pass, spring.profiles.default dev loads dev yml with nothing set |
| All five failure experiments | Pass |

SECURITY AND PRODUCTION REVIEW

1. which config values are sensitive per profile, and where stored?

dev and test have none. the H2 URLs are in-memory and the password is empty.
prod has DB_USERNAME, DB_PASSWORD and NORTHSTAR_API_KEY, env only, and no file
in the repo holds a value for any of them. .env.example holds placeholders and
.env is ignored.

2. why must application-prod.yml avoid defaults for DB username/password?

${DB_PASSWORD:} with an empty default means a missing variable becomes a blank
password and the app connects instead of stopping. the incident is silent, and
it is the one the lab scenario names. no default means the value has to arrive
from the environment.

3. what if a real postgres password is committed?

rotate first, scrub second. it is exposed from the moment it is pushed, so the
credential is burned whether or not the history is cleaned. removing the line in
a later commit leaves it in history, and rewriting a shared repo's history is a
separate decision with its own cost.

WINDOWS HOW-TO PASS CRITERIA

| # | Confirm | Result |
| - | --- | --- |
| 1 | workspace open in IntelliJ with SDK 21 | Pass, temurin-21.0.4 |
| 2 | lab project under examples/lab26-crm | Pass |
| 3 | GUIDE deliverables and checkpoints complete | Pass, full path |
| 4 | commands succeed | Pass, mvn -B test and spring-boot:run |
| 5 | evidence under notes/screenshots/lab-26 | Pass, kept in the project as since lab 14 |
