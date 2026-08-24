# Lab 43 — Fill ci.yml TODOs

## Step 1 — Skeleton

`ci.yml.skeleton`, blanks marked:

```yaml
on:
  pull_request:
  push:
    branches: [main]
    tags: ["v*"]

jobs:
  verify:
    steps:
      - uses: actions/setup-java@v4
        with: { java-version: "_____" }
      - run: _____ -B clean verify
      - uses: actions/upload-artifact@v4
        if: _____
        with:
          path: _____

  package:
    needs: verify
    if: _____
    steps:
      - run: _____ -B -DskipTests package
      - run: sha256sum target/*.jar > _____
```

## Step 2 — Fill

```yaml
# secrets via GitHub Actions secrets — never hardcode
on:
  pull_request:
  push:
    branches: [main]
    tags: ["v*"]

jobs:
  verify:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "21"
          cache: maven
          cache-dependency-path: examples/lab43-crm/pom.xml
      - run: mvn -B clean verify
        working-directory: examples/lab43-crm
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-reports
          path: |
            examples/lab43-crm/target/surefire-reports/
            examples/lab43-crm/target/failsafe-reports/

  package:
    needs: verify
    if: github.ref == 'refs/heads/main' || startsWith(github.ref, 'refs/tags/v')
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "21"
          cache: maven
          cache-dependency-path: examples/lab43-crm/pom.xml
      - run: mvn -B -DskipTests package
        working-directory: examples/lab43-crm
      - run: sha256sum target/*.jar > target/SHA256SUMS
        working-directory: examples/lab43-crm
      - uses: actions/upload-artifact@v4
        with:
          name: crm-jar
          path: |
            examples/lab43-crm/target/*.jar
            examples/lab43-crm/target/SHA256SUMS
```

`java-version` is `"21"` quoted, so YAML does not read it as the number 21.
The package `if:` is the main-or-tag condition from Exercise 1. The upload in
`verify` uses `if: always()` so reports survive a failing run; the upload in
`package` has no condition because the job only reaches it on success.

The live file is `java-bootcamp/.github/workflows/crm-ci.yml`.

## Step 3 — Secrets comment

`# secrets via GitHub Actions secrets — never hardcode` is the first line of the
filled workflow above.

## Step 4 — Scope

Pushing this workflow and getting it green is Lab 43. This exercise produces the
skeleton and the filled version in notes only; nothing is committed to
`.github/workflows/` here.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [x] File exists at `notes/lab43-workflow-todos.md`
- [x] Blanks filled for Java/verify/package
- [x] Secrets comment present
- [x] Pre-lab scope stated
