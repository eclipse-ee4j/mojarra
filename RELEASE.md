# Releasing Mojarra and the Jakarta Faces API

Since the 5.0 line there are **two** release pipelines, on two different Jenkins instances:

| Pipeline | Jenkins job | Releases | Repo / Jenkinsfile |
| -------- | ----------- | -------- | ------------------ |
| **mojarra-release** | [ci.eclipse.org/mojarra](https://ci.eclipse.org/mojarra/job/mojarra-release/) | the **impl** `org.glassfish:jakarta.faces` | `eclipse-ee4j/mojarra`, [`Jenkinsfile`](https://github.com/eclipse-ee4j/mojarra/blob/master/Jenkinsfile) at the repo root |
| **faces-api-release** | [ci.eclipse.org/faces](https://ci.eclipse.org/faces/job/faces-api-release/) | the standalone **API** `jakarta.faces:jakarta.faces-api` | `jakartaee/faces`, [`api/Jenkinsfile`](https://github.com/jakartaee/faces/blob/5.0/api/Jenkinsfile) |

On the **4.x** lines the API spec classes are bundled inside the impl jar, so there is no standalone API artifact and only the **mojarra-release** job runs. Everything about the API job and the cross-job coordination below applies to **5.0+ only**.

## How the two jobs coordinate: the TCK gate

The API can only be conformant if a matching impl passes the full Jakarta Faces TCK against it. So the API job refuses to publish until Mojarra says so:

- On a green **full** TCK run (any line), **mojarra-release** writes machine-readable records and pushes them to the orphan [**`tck-status`**](https://github.com/eclipse-ee4j/mojarra/tree/tck-status) branch of `eclipse-ee4j/mojarra`. It writes **two purpose-keyed files**: an impl reuse record `tck-validation-impl-<impl-version>.json` (every line), and — 5.0+, only when the API is built from the submodule because it is not yet on Central — an API gate record `tck-validation-api-<api-version>.json`. Keying the API record by API version lets the Faces gate find it even when the impl and API versions differ (e.g. impl `5.0.3` against API `5.0.1`). The record carries `line`, `apiVersion`, `facesSha` (the `jakartaee/faces` commit the TCK ran against), `mojarraTree` (the impl source identity), `dryRun`, and the TCK/GlassFish versions; its mere existence attests a green full TCK, so there is no separate result flag. A `SMOKE_TEST` or `SKIP_OLD_TCK` run drops TCK coverage and deliberately writes no record.
- **faces-api-release** reads the API record from the public raw URL (no credentials) in its Prepare stage. It publishes only if a record exists whose `line`, `apiVersion` and `facesSha` match the exact API version and commit it is about to release. On a real publish a missing or mismatched record **aborts**; on a DRY_RUN it only **warns**. The gate is skippable via `SKIP_TCK_GATE` (emergency use only).

Because the gate is satisfied by a Mojarra **DRY_RUN** (the record's `dryRun` flag is not part of the match), the API can be validated and released *before* the impl is released — which is exactly the ordering the SNAPSHOT case below needs.

## The decision that drives a 5.0 release

Look at `impl/pom.xml`'s `jakarta.faces-api` dependency:

- **It is a `-SNAPSHOT`** → a new API version is being cut (not on Maven Central yet); the API version is taken from the dep, not the impl version, so it need not equal the impl's. The normal case for a fresh `5.0.0`, `5.0.0-M3`, etc. This is the **three-run dance** across both jobs; see [Releasing a new 5.0 API + impl](#releasing-a-new-50-api--impl).
- **It is a concrete GA** (e.g. impl `5.0.1` still depends on API `5.0.0`) → an **impl-only patch**; the API already exists on Central. A **single** mojarra-release run, no API job; see [Releasing a 5.0 impl-only patch](#releasing-a-50-impl-only-patch).

Milestone vs GA is orthogonal to this — it is just a `MILESTONE_VERSION` suffix (`M1`, `RC1`, …) applied to whichever path.

## Pipeline stages

### mojarra-release

1. **Prepare** — checkout (incl. the `faces/` submodule, tracked to the API branch tip on 5.0+), JDK selection, version resolution, credential checks. Resolves the API version from `impl/pom.xml`'s `jakarta.faces-api` dep: a concrete version is used as-is; a `-SNAPSHOT` resolves to that dep's own base version (with this run's milestone suffix reapplied on a milestone run). On a real release that version must already be on Maven Central (else Prepare fails fast, pointing you at the API job). On a real `SKIP_TCK` release, Prepare gates on a matching green `tck-status` record instead (see [SKIP_TCK](#skipping-the-tck-on-a-real-release-skip_tck)).
2. **Build & install** — one Maven reactor, `-pl impl -am`. On a **DRY_RUN** whose API dep is an unreleased `-SNAPSHOT`, adds `-Papi` to build the API from the `faces/` submodule into the reactor so the impl can still be validated; otherwise impl-only, API consumed from Maven Central. Tags locally; pushes happen later.
3. **TCK** *(skipped when `SKIP_TCK`)* — 4.x downloads the published TCK zip from `download.eclipse.org/jakartaee/faces/<line>/`; 5.0 builds the TCK from the `faces/tck` submodule (`tckVersion=5.0.0-SNAPSHOT`). Runs it against the locally-built impl, fails on any failure/error, renders `summary.txt`, and on a green full-TCK run (any line) writes `tck-validation.json` and pushes it to the `tck-status` branch as an impl reuse record (keyed by impl version) plus, for an unreleased 5.0+ API, an API gate record (keyed by API version). Archives `run.log`, `summary.txt`, `tck-validation.json`.
4. **Deploy to Maven Central** *(skipped on `DRY_RUN`)* — `mvn -Poss-release -pl impl -am deploy`, auto-publishing the impl bundle. Impl-only; the API resolves from Central.
5. **Bump to next snapshot** *(GA only)* — `versions:set` the impl reactor to the next `-SNAPSHOT` and commit.
6. **Publish to GitHub** *(skipped on `DRY_RUN`)* — push the tag (and, on GA, the release branch); on GA also squash-merge a `<version> has been released` PR back to the source branch, close the milestone, open the next one, and draft+publish a GitHub release. Milestone runs push only the tag. The mojarra job never touches `jakartaee/faces`.

### faces-api-release

1. **Prepare** — checkout the API branch, JDK selection, derive the release version from `api/pom.xml`, run the [TCK gate](#how-the-two-jobs-coordinate-the-tck-gate), credential checks.
2. **Build & install** — `versions:set` + `mvn -f api/pom.xml install` (validates without publishing; Central publication is instant and irreversible). Tags locally.
3. **Deploy to Maven Central** *(skipped on `DRY_RUN`)* — `mvn -f api/pom.xml -Dcentral.autoPublish=true deploy`.
4. **Bump to next snapshot** *(GA only)* — advance `api/pom.xml` to the next `-SNAPSHOT`.
5. **Publish to GitHub** *(skipped on `DRY_RUN`)* — push the tag (and, on GA, advance the source branch).

## How to run

### Releasing a 4.x version

A single **mojarra-release** run; the API is bundled, so no API job.

1. Confirm the version isn't already on [Maven Central](https://repo1.maven.org/maven2/org/glassfish/jakarta.faces/); if it is, bump `pom.xml` first.
2. Open [mojarra-release](https://ci.eclipse.org/mojarra/job/mojarra-release/) → **Build with parameters**, set `RELEASE_LINE` = `4.0` or `4.1`, leave `DRY_RUN` checked for a rehearsal.
3. Uncheck `DRY_RUN` for the real release. Everything else auto-infers from the [per-branch defaults](#per-branch-defaults).

Because the 4.x TCK runs for ~3h, you can split it: a full DRY_RUN records to `tck-status`, then the real run with `SKIP_TCK` reuses it instead of re-running (see [SKIP_TCK](#skipping-the-tck-on-a-real-release-skip_tck)). Keep the DRY_RUN full — a `SMOKE_TEST` or `SKIP_OLD_TCK` run writes no record.

### Releasing a new 5.0 API + impl

Use this when `impl/pom.xml`'s `jakarta.faces-api` dep is a `-SNAPSHOT` (the API version does not exist on Central yet). Three runs, in order:

**0. Pin the API source.** Only necessary because the API dep is a `-SNAPSHOT`: point mojarra's `faces/` submodule at the `jakartaee/faces` commit you will release the API from, and commit + push it to mojarra, so the `facesSha` in the TCK record matches what the API job will release. (Skip this step entirely for an impl-only patch — its API dep is concrete.)

**1. mojarra-release, DRY_RUN — produce the gate record.**
   - `RELEASE_LINE=5.0`, leave `DRY_RUN` checked, optionally set `MILESTONE_VERSION` for a milestone.
   - Builds the API from the submodule (`-Papi`), runs the full TCK, and on green pushes the impl and API records (`tck-validation-impl-<impl>.json` + `tck-validation-api-<api>.json`) to `tck-status`. Nothing reaches Maven Central.

**2. faces-api-release — publish the API.**
   - On [faces-api-release](https://ci.eclipse.org/faces/job/faces-api-release/): `RELEASE_LINE=5.0`, uncheck `DRY_RUN`, optionally the same `MILESTONE_VERSION` as step 1.
   - Its TCK gate reads Mojarra's record; on a match it publishes `jakarta.faces-api <version>` to Maven Central and tags `jakartaee/faces`.

**3. mojarra-release, real — publish the impl.**
   - `RELEASE_LINE=5.0`, uncheck `DRY_RUN`, optionally the same `MILESTONE_VERSION` as step 1.
   - The API is now on Central, so Prepare's api-check passes and the impl pins it. To avoid re-running the TCK that step 1 already ran on identical bits, check `SKIP_TCK` — Prepare will verify a matching green `tck-status` record and skip the TCK stage. Leave `SKIP_TCK` unchecked to re-run it.

### Releasing a 5.0 impl-only patch

When `impl/pom.xml` pins a concrete GA `jakarta.faces-api` already on Central (e.g. impl `5.0.1` → API `5.0.0`). A **single** mojarra-release run — no submodule pin bump, no API job. Run it exactly like a [4.x release](#releasing-a-4x-version), including the optional full-DRY_RUN-then-`SKIP_TCK` split.

## Skipping the TCK on a real release (SKIP_TCK)

`SKIP_TCK` skips the TCK stage. On a **DRY_RUN** it just skips. On a **real release** it is honoured only when the `tck-status` branch already carries a green record for this release version whose `line`, `mojarraTree` (impl source) **and** `facesSha` (the exact `faces/` commit built, empty on 4.x) match this build — i.e. a prior DRY_RUN already ran the full TCK against identical bits. No matching record aborts the release in Prepare, before anything is built or published. Works on every line, so a 4.x release can dry-run the full ~3h TCK once and then reuse it. Because records come only from full TCK runs, a `SMOKE_TEST` or `SKIP_OLD_TCK` dry-run can't enable a real `SKIP_TCK` release. This is what makes step 3 above cheap without letting an unvalidated build reach Central.

## Parameters

Set only `RELEASE_LINE`; everything else auto-infers. Override when fine-tuning.

### mojarra-release

- `RELEASE_LINE` — `4.0`, `4.1`, `5.0`.
- `MILESTONE_VERSION` — blank = GA; `M1`/`RC1`/… derives `<pom-base>-<suffix>`, tags exactly that, and skips PR-merge, milestones, GitHub release, and snapshot bump.
- `JDK` / `TCK_JDK` — build / TCK JDK. Default per-branch.
- `TCK_VERSION` — a released value downloads the zip; a `-SNAPSHOT` builds the TCK from the `faces/tck` submodule. Default per-branch.
- `GF_VERSION` / `GF_BUNDLE_URL` — GlassFish coordinate / explicit zip URL.
- `THREAD_COUNT` — 5.0+ only. Maven `-T` and `gf.pool.size` for the TCK reactor.
- `DRY_RUN` *(default on)* — skip Maven Central deploy and GitHub push.
- `SMOKE_TEST` — requires `DRY_RUN` and unset `SKIP_TCK`. Filters the TCK to a tiny subset (~10 min) for pipeline iteration; never publishable.
- `SKIP_TCK` — skip the TCK stage. See [above](#skipping-the-tck-on-a-real-release-skip_tck).
- `SKIP_OLD_TCK` — requires unset `SKIP_TCK`. 4.x only; drop the old-tck JavaTest modules (~3 h faster).
- `SKIP_DEPLOY` — requires `DRY_RUN` off. Skip only the Central deploy (still tags/pushes/releases). For resuming after Central already published.
- `SKIP_CRED_CHECK` — skip the Prepare credential probes. Pipeline-debug only.

### faces-api-release

- `RELEASE_LINE` — `5.0`.
- `MILESTONE_VERSION` — same as mojarra-release: blank = GA; `M1`/`RC1`/… derives `<pom-base>-<suffix>`, tags exactly that, and leaves the source branch on its `-SNAPSHOT`.
- `JDK` — default per-branch.
- `DRY_RUN` *(default on)* — skip Central deploy and GitHub push.
- `SKIP_DEPLOY` — requires `DRY_RUN` off. Skip only the deploy.
- `SKIP_CRED_CHECK` — skip the Prepare credential probes.
- `SKIP_TCK_GATE` — **emergency use only.** Skip the Mojarra-TCK gate and let an unvalidated API reach Central. Only when the gate infrastructure itself is broken and the TCK is known green by other means.

## Per-branch defaults

Maintained in `BRANCH_CONFIG` at the top of each `Jenkinsfile`; adding a release line = one entry.

**mojarra-release:**

| Release | Impl branch | API branch | Build JDK | TCK JDK | API version | TCK version | GF version | Selenium | Threads |
| ------- | ----------- | ---------- | --------- | ------- | ----------- | ----------- | ---------- | -------- | ------- |
| `4.0`   | `4.0`       | — (bundled) | 11 | 17 | 4.0.1 | 4.0.4 | 7.1.1 | off | 1 |
| `4.1`   | `4.1`       | — (bundled) | 17 | 21 | 4.1.0 | 4.1.2 | 8.0.3 | on | 1 |
| `5.0`   | `master`    | `5.0`       | 17 | 21 | resolved | 5.0.0-SNAPSHOT (from submodule) | 9.0.0-M2 | on | 2 |

- **Impl branch**: the mojarra git branch with the impl source — `master` for 5.0, since the head of development lives there, not on a `5.x` branch.
- **API version**: 4.x bundles the spec into the impl jar, so this is a Central GA passed as `-Dfaces.version`. On 5.0 it is resolved from `impl/pom.xml` (a `-SNAPSHOT` resolves to the dep's own base version, milestone suffix reapplied).
- **TCK version**: a released value downloads the zip; `-SNAPSHOT` builds the TCK from the `faces/tck` submodule.
- **Selenium**: `on` runs BaseITNG against the agent pod's Chrome; `off` self-skips when the TCK pins a CDP major outside Selenium's range (4.0's v108).
- **Threads**: Maven `-T` for the TCK reactor; `>1` only on lines whose TCK ships `gf-pool` (5.0+). 4.x runs one managed GlassFish per module and must stay at 1. The 5.0 value of `2` is capped by the CI agent's hardware, not the TCK; better hardware can go higher via `THREAD_COUNT`.

**faces-api-release:** `5.0` → branch `5.0`, JDK 17.

## Troubleshooting

- **Release branch / tag already exists on origin.** The conflict check fails fast (it runs even on DRY_RUN). Bump `pom.xml` / `api/pom.xml`, or use a fresh `MILESTONE_VERSION`. Central is immutable; never reuse a published version.
- **`jakarta.faces-api <v> is not on Maven Central`** (mojarra Prepare, real release). The API isn't released yet. Run the [three-run dance](#releasing-a-new-50-api--impl): a mojarra DRY_RUN to seed the gate, then faces-api-release, then the real mojarra run.
- **`[tck-gate] ERROR: … does not match this commit`** (faces Prepare). No matching green Mojarra record for this API version + `faces` commit. Ensure mojarra's `faces/` submodule was pinned to this commit and a mojarra DRY_RUN with the TCK ran green against it. `SKIP_TCK_GATE` is the emergency override.
- **`[tck-reuse] ERROR: no matching … record`** (mojarra Prepare, `SKIP_TCK` real release). No green `tck-status` record for this release version matches this impl tree + faces commit. Run a full DRY_RUN (not `SMOKE_TEST`/`SKIP_OLD_TCK`) first, or unset `SKIP_TCK` to re-run the TCK inline.
- **TCK failures.** The TCK stage fails on the failsafe exit code; deploy and push are skipped, so nothing external was published. `run.log` and `summary.txt` are archived.
- **Rehearse without publishing.** `DRY_RUN=true` (the default) does the full build, tag, and TCK but skips Central deploy and `git push`. Combine with `SMOKE_TEST=true` (mojarra) for a ~10-min TCK subset when iterating on the pipeline itself.
