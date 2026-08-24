# Lab 43 — Package-Once Identity

## Step 1 — Steps

1. `package` job runs after `verify` succeeds, `needs: verify`.
2. `mvn -B -DskipTests package` produces one JAR.
3. `sha256sum target/*.jar > target/SHA256SUMS`.
4. Append `GITHUB_SHA` and the run number to that file.
5. `actions/upload-artifact@v4` uploads the JAR and `SHA256SUMS` as `crm-jar`.

Tests are skipped in this job because `verify` already ran them. Running them
twice does not make the artifact safer; it only makes the build slower and
introduces a second chance for a flaky test to block a promotion.

## Step 2 — Check the reference

Jobs do not share disks, so the `package` job re-runs Maven even though `verify`
already built classes. That re-run is the reason the checksum matters: it is the
only record tying the uploaded bytes to a commit.

Lab 44 downloads `crm-jar`. A third `mvn package` on the deploy agent would
produce a different file that nothing verified, and the chain from tested code
to deployed artifact would be broken without anything failing.

Lab 41 already demonstrated how easily identity drifts. Rebuilding the same
source produced a different manifest-list digest every time, because BuildKit
attaches a provenance attestation whose content varies per build. The layer
`diff_id` values were stable; the digest was not. Identity has to be captured
once and carried, not recomputed and assumed equal.

## Step 3 — Example lines

`target/SHA256SUMS`, fake hashes:

```
a1b2c3d4e5f60718293a4b5c6d7e8f90a1b2c3d4e5f60718293a4b5c6d7e8f90  lab43-crm-0.0.1-SNAPSHOT.jar
commit=8f3a91c2b7de4f10aa55c6d9e2b1f0a3c4d5e6f7
run=147
```

The commit id is what makes the hash useful. A checksum alone proves two files
are the same file; the commit line says which source produced it.

## Step 4 — Anti-pattern

Packaging differently in deploy than in CI. A deploy agent that runs its own
`mvn package` produces an artifact that no test ever saw, from a working tree
that may not match the commit under test, and the checksum recorded in CI now
describes a file nobody is running.

The Lab 41 form of the same mistake: rebuilding an image to redeploy it rather
than promoting the one that was tested. It looks identical and is not, and
nothing in the process reports a difference.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [x] File exists at `notes/lab43-immutable-jar.md`
- [x] Checksum + commit recorded
- [x] Promotion link stated
- [x] Anti-pattern named
