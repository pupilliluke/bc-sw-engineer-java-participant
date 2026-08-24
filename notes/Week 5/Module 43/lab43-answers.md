Lab 43 github actions CI for the CRM (evidence log, reflection questions,
checkpoints)

built as examples\lab43-crm, a copy of lab41-crm, with the workflow at the
repository root as .github/workflows/crm-ci.yml. the work was a verify job on
JDK 21 with a Maven cache and a Postgres service container, a package job
producing a JAR and SHA256SUMS on main and tags only, an Actions secret for
JWT_SECRET, and a forced test failure with restore.

two things in the starter did not fit this build and both would have failed the
first run. the paths assumed examples/lab43-crm, but this project keeps the
Maven module at examples/lab43-crm/crm-api. and the env block supplied only
SPRING_DATASOURCE_*, while this build carries Spring Security and declares
jwt-secret with no default, so 19 of 26 tests cannot load a context without
JWT_SECRET.


EVIDENCE LOG

- Repo (must be java-bootcamp): pupilliluke/bc-sw-engineer-java-participant,
  the local java-bootcamp working tree. nothing was written under labs/.
- Workflow path (must be .github/workflows/crm-ci.yml): correct, at the
  repository root. GitHub reads workflows from that one location on the default
  branch; a copy nested under examples/lab43-crm/.github/ would be inert with no
  error and no warning.
- Java/Maven versions: java 21.0.4 Temurin, Maven 3.9.9 locally. the workflow
  uses actions/setup-java@v4 with distribution temurin and java-version "21",
  cache maven keyed on examples/lab43-crm/crm-api/pom.xml.
- Local verify: mvn -B -ntp clean verify, BUILD SUCCESS in 1m18s, 26 tests,
  19 unit and 7 integration, against database crm_lab43 on crm-postgres-lab41
  at host port 5433.
- Actions URL (sanitized): repository actions/runs/<id>. run 32755890696 green,
  run 32756661443 red.
- Forced test failure + restore: CustomerServiceTest.closedIsTerminal inverted
  from assertThrows(IllegalStateException) to assertDoesNotThrow, asserting a
  CLOSED customer accepts a transition to ACTIVE. verify failed naming the test,
  package was skipped rather than failed because of needs: verify, and
  test-reports still uploaded at 18,446 bytes because of if: always(). restored
  by reverting the single assertion; the file is byte-identical to the lab41
  original either side, verified by diff.
- JAR checksum / GITHUB_SHA: artifact crm-jar, 47,848,959 bytes, containing the
  JAR and target/SHA256SUMS with commit=${GITHUB_SHA} appended. produced by the
  package job on main only.


REFLECTION QUESTIONS

1. Which design decision most affected correctness (root workflow, image pin, or package-once)?


2. What evidence proves the JAR matches the commit?


3. Which failure was hardest to diagnose (path vs tests vs secrets)?



CHECKPOINTS

| # | Confirm | Result |
| - | ------- | ------ |
| A1 | examples/lab43-crm is a copy of Lab 41 and has a pom.xml | Pass, at examples/lab43-crm/crm-api/pom.xml rather than the folder root, and the workflow paths were corrected to match |
| A2 | java-bootcamp/.github/workflows/crm-ci.yml exists | Pass, at the repository root, sibling of examples/ |
| A3 | local mvn -B clean verify green, no mvnw required | Pass, 26 tests in 1m18s. a wrapper does exist here, carried from Lab 41, but plain mvn was used to match the workflow |
| B1 | PR verify + main/tag package paths | Pass by configuration, on: pull_request plus push to main and v* tags, package gated by the ref condition. observed on main pushes only; no pull request has been opened |
| B2 | Surefire published with if: always() | Pass, and proven on the red run: test-reports uploaded at 18,446 bytes from a failing build |
| B3 | package-once checksum with GITHUB_SHA | Pass, sha256sum into target/SHA256SUMS with commit=${GITHUB_SHA} appended, uploaded as crm-jar |
| C1 | scan step or residual risk documented | Pass by documenting the risk. the Dependency-Check step is left commented out and the reason is recorded: the Lab 40 gate fails at failBuildOnCVSS 7 with 70 findings, so enabling it unconditionally makes CI permanently red |
| C2 | secret names only in the runbook | Pass, JWT_SECRET and NVD_API_KEY appear by name with no values anywhere in tracked files |
| C3 | no deploy job, no JAR rebuild planned for Lab 44 | Pass, the workflow has no deploy job and the runbook states Lab 44 downloads crm-jar rather than running mvn package again |
| D1 | controlled failure then restore | Pass for the failure, run 32756661443 red with package skipped. the restore commit has not been pushed yet |
| D2 | docs/ci-runbook.md complete | Pass, policy, secrets by name, verify job, re-run recipe, failure experiment with observed numbers, schema portability, artifact identity, residual risks |
| D3 | no secrets in Git, pushes went to your remote | Pass, no .env, kubeconfig or token in any tracked path, and origin is pupilliluke/bc-sw-engineer-java-participant |


CORRECTIONS TO THE PRE-LAB PREDICTIONS

lab43-java21-verify.md said the verify job would need a services block for
PostgreSQL because CustomerRepositoryIT opens a real connection. that was right,
and the starter already contained one.

the same file recorded the exercise's claim that Lab 41 has no mvnw. it does;
mvnw and mvnw.cmd were generated during Lab 41 because the Dockerfile needed
them. the guide is the accurate source here, since it says to use ./mvnw only if
the project already has a wrapper.

lab43-workflow-todos.md wrote path: examples/lab43-crm/target/... for the
artifacts. the real paths are examples/lab43-crm/crm-api/target/..., because the
Maven module sits one level down.

predictions made during the lab and how they came out:

the workflow was predicted not to run on the first push because the files had to
be committed first. it ran immediately. the repository is not a fork, Actions
was already enabled, and pushing is committing; there was no gate.

application.yml was predicted to win over the workflow's SPRING_DATASOURCE_*
environment variables. environment variables sit above application.yml in Spring
Boot's property order, so they win. proven by two runs of the same code: without
them the build failed on the datasource, with them it failed later on
JWT_SECRET.

none of the 26 tests were predicted to pass without .env. seven do.
CustomerServiceTest builds the service directly over a Mockito mock and loads no
Spring context.

the first CI run was predicted to fail because a fresh Postgres has none of the
Lab 37 DDL. the concern was correct and is exactly why the qualified
crm_app.customer had to be removed from CustomerRepositoryIT, but with that
already fixed the run went green.

package was correctly predicted to be skipped rather than failed when verify
failed. the test-reports artifact was predicted not to be produced; it was,
because of if: always().


SCOPE HONESTY

D1 is not yet fully Pass. the red run exists; the restoring commit has not been
pushed, so there is no green run proving the file returned to its original
state.

B1 is proven by configuration rather than observation. every run so far has been
a push to main. no pull request has been opened, so the PR path has never
executed and the claim that package does not run on a PR is read from the
condition rather than seen.

the Dependency-Check step is not enabled. the threshold this pipeline actually
enforces today is none, and that is a decision rather than an oversight.

the pinned actions are deprecated. actions/setup-java@v4 is superseded by v5 and
the Node 20 actions are being forced onto Node 24. both are warnings on every
run and neither was changed, because the starter pins v4.

main has no branch protection, so verify can fail on main itself rather than
being caught on a pull request first.

examples/lab43-crm/crm-api/src/test/.../CustomerRepositoryIT.java differs from
the lab41 copy by one line, the removed crm_app qualifier. lab41-crm is
unchanged.
