# Releasing a new Mojarra version

The release pipeline is the single [`Jenkinsfile`](https://github.com/eclipse-ee4j/mojarra/blob/master/Jenkinsfile) at the repo root, run as a Jenkins pipeline job. It does, in one run:

1. **Prepare** — checkout, JDK selection, version resolution (impl, and on 5.0+ also `jakarta.faces-api` via the `faces/` submodule), CSP-backport TCK exclusion fallback for 4.0.17+ / 4.1.8+ (mirrors `compute-csp-backport-flags` in the TCK pom for the existing TCK zips that predate the script).
2. **Build & install** — single Maven reactor (`-pl impl -am`); on 5.0+ adds `-Papi` to also build `jakarta.faces-api` from the submodule. Tags are created locally; pushes happen later.
3. **TCK** — downloads the published TCK zip from `download.eclipse.org/jakartaee/faces/<branch>/`, runs the TCK against the locally-installed impl, fails the build on any TCK failure or error, then renders `summary.txt` (passed/failed/error counts, SHAs of TCK zip and the produced impl jar, JDK and OS info). Pass/fail counts are aggregated from two sources: per-module `target/failsafe-reports/failsafe-summary.xml` (covers new-TCK *and* old-tck-selenium since both are failsafe-driven), and the JavaTest harness's "Completed running N tests" / "Number of Tests Passed/Failed/Errors" lines in `run.log` (covers old-tck on 4.x when `SKIP_OLD_TCK` is unchecked). Archives `run.log` and `summary.txt`. Selenium/Chrome is provided by the agent pod (`eclipsecbijenkins/basic-ubuntu-chrome`); branches whose TCK pins a CDP major outside Selenium's fudge range (e.g. 4.0 pins CDP v108) skip the BaseITNG suite via `-Dtest.selenium=false` per `BRANCH_CONFIG.seleniumEnabled`.
4. **Deploy to Maven Central** *(skipped on `DRY_RUN`)* — `mvn deploy -Dcentral.autoPublish=true`, so the bundle auto-publishes on success rather than parking in the Portal staging area.
5. **Bump to next snapshot** *(GA only)* — `versions:set` to the next `-SNAPSHOT` and commit on the release branch (and the `faces/` submodule on 5.0+). Skipped on milestone/RC runs so the source branch keeps its current `-SNAPSHOT`.
6. **Publish to GitHub** *(skipped on `DRY_RUN`)* — push the release branch and tag (and the same for the `jakarta.faces-api` submodule on 5.0+); on GA runs, also open & squash-merge a PR back to the source branch as `<version> has been released`, close the matching milestone, open the next snapshot's milestone, and draft+publish a GitHub release at the just-pushed tag with auto-generated notes prepended by a one-line summary, the Maven Central link, and the closed-milestone link. On milestone/RC runs only the tag is pushed; PR-merge, milestones, and GitHub release creation are all skipped.

Maven Central deploy and GitHub push only run after the TCK passes, so a failed TCK leaves no half-published external state.

## How to run

In the example below we assume releasing **Mojarra 4.0.17**.

1. Sanity-check the version isn't already in [Maven Central](https://repo1.maven.org/maven2/org/glassfish/jakarta.faces/) — if it is, bump the version in `pom.xml` first.
2. Go to [Mojarra CI](https://ci.eclipse.org/mojarra/) and [log in](https://ci.eclipse.org/mojarra/login?from=%2Fmojarra%2F).
3. Open the [`mojarra-release`](https://ci.eclipse.org/mojarra/job/mojarra-release/) job and click **Build with parameters**. For a normal release set only `RELEASE_LINE`; every other parameter can be left at its default and is auto-inferred:
   - `RELEASE_LINE` = `4.0` (choices: `4.0`, `4.1`, `5.0`)
4. *(Optional)* In case you wish to fine-tune the run, override one or more of:
   - `MILESTONE_VERSION` — leave blank for a GA release; set to `M1` / `M2` / `RC1` / etc. for a milestone or release candidate. When set, the release version is auto-derived as `<pom-base>-<MILESTONE_VERSION>` (e.g. `5.0.0-M2`), tagged exactly that (no `-RELEASE` suffix), and the source branch is left untouched: PR-merge, milestone management, GitHub release creation, and snapshot bump are all skipped.
   - `JDK` — build JDK. Default: per-branch (see table below).
   - `TCK_JDK` — JDK that runs the TCK (the GlassFish container can need a newer one than the spec). Default: per-branch.
   - `TCK_VERSION`, `GF_VERSION` — TCK and GlassFish coordinate versions. Default: per-branch.
   - `GF_BUNDLE_URL` — alternative GlassFish zip URL. If set, also set `GF_VERSION` to match the artifact version inside the zip.
   - `API_RELEASE_VERSION` — 5.0+ only. Default: stripped from `faces/api/pom.xml`. Ignored when `impl/pom.xml` already pins `jakarta.faces-api` to a GA version (impl-only patch release, no new API artifact cut), or when `MILESTONE_VERSION` is set.
   - `RUN_TCK` — uncheck to skip the TCK stage. Default: checked.
   - `SKIP_OLD_TCK` — check to skip the old-tck JavaTest modules on 4.0/4.1 (excluded from the reactor entirely via `-pl`; cuts nearly 3 hours off the TCK run). No-op on 5.0+ where these modules no longer exist. The old-tck-selenium failsafe-driven modules are unaffected. Default: unchecked.
   - `DRY_RUN` — check to do everything except Maven Central deploy and GitHub push. Default: checked. Useful for rehearsals.
   - `SMOKE_TEST` — only meaningful as a sub-toggle of `DRY_RUN`: filters the TCK to a tiny representative subset (one failsafe IT + one sigtest IT + one old-tck-selenium IT, plus one old-tck JavaTest path when `SKIP_OLD_TCK` is unchecked). Drops run time to ~10 min. Hard-gated: silently ignored when `DRY_RUN` is unchecked, since the run is not TCK-conformant and must never produce a published release. Default: unchecked.
5. Click **Build**.
6. Wait for the run to finish. The build description shows a one-line summary, e.g. `4.0 → 4.0.17 (impl-only) (JDK11, GF 7.0.25, TCK 4.0.3)`. Optional toggles surface as comma-separated suffixes (`, old-TCK skipped`, `, smoke-test`, `, milestone`, `, dry-run`).
7. On success, verify:
   - Artifact in [Maven Central](https://repo1.maven.org/maven2/org/glassfish/jakarta.faces/) (may take up to an hour to surface).
   - Release branch `4.0.17` and tag `4.0.17-RELEASE` on [GitHub](https://github.com/eclipse-ee4j/mojarra/branches/active) (GA only; milestone runs only push the tag). Once everything checks out, the release branch can be deleted (the squash-merge doesn't auto-delete it).
   - The squash-merged "Mojarra 4.0.17 has been released" commit landed on the `4.0` source branch, the `4.0.17` release branch is closed, and the GitHub release at `4.0.17-RELEASE` is published with auto-generated notes (GA only).
   - Closed milestone `4.0.17` and a fresh open milestone for the next snapshot (GA only).
   - On 5.0+ releases that also cut the API: matching tag in [jakartaee/faces](https://github.com/jakartaee/faces/tags) for the `jakarta.faces-api` version.

## Per-branch defaults

Maintained in `BRANCH_CONFIG` at the top of the `Jenkinsfile`. Adding a new release line means adding one entry there. Current entries:

| Release | Impl branch | API branch  | Build JDK | TCK JDK | API version | TCK version | GF version | Selenium | Threads |
| ------- | ----------- | ----------- | --------- | ------- | ----------- | ----------- | ---------- | -------- | ------- |
| `4.0`   | `4.0`       | — (bundled) | 11        | 11      | 4.0.1       | 4.0.3       | 7.0.25     | off      | 1       |
| `4.1`   | `4.1`       | — (bundled) | 17        | 21      | 4.1.0       | 4.1.0       | 8.0.0-M6   | on       | 1       |
| `5.0`   | `master`    | `5.0`       | 17        | 21      | 5.0.0       | 5.0.0       | 9.0.0-M2   | on       | 4       |

The `Release` column is the release line dropdown value. The mojarra git branch holding the impl source is the next column over — `master` for the 5.0 line because the head of mojarra development sits there, not on a `5.x` branch.

`API version` is the value passed as `-Dfaces.version` to the TCK build (the published `jakarta.faces-api` jar version that the TCK compiles against). On 4.x the API was bundled with the impl, so this is just the GA on Maven Central. On 5.0+ it tracks the standalone `jakarta.faces-api` artifact — bump it together with the matching API release.

`Selenium` is `seleniumEnabled` in the config: `on` means BaseITNG runs against the agent pod's Chrome via `-Dtest.selenium=true`; `off` means the BaseITNG suite self-skips because the TCK pins a CDP major (e.g. 4.0's v108) that's outside Selenium's fudge range against current Chrome — browser-driven tests then fall back to HtmlUnit only.

`Threads` is `threadCount` in the config — the Maven `-T` value for the TCK reactor. Set `>1` only on release lines whose TCK ships `gf-pool` (5.0+) and can therefore share GlassFish across parallel module builds; 4.x TCKs start a single managed GlassFish per module and must stay at `1`. The build description appends e.g. ` -T4` to the TCK label whenever this is `>1`.

## Troubleshooting

- **Release branch / tag already exists on origin.** The pipeline fails fast at the `Build & install` stage. Bump `pom.xml` (or set `MILESTONE_VERSION` to a fresh suffix) and re-run; Maven Central is immutable, so reusing a published version is never the right call.
- **TCK failures.** The TCK stage fails on the failsafe exit code; Maven Central deploy and GitHub push are skipped, so no external state was published. `run.log` and `summary.txt` are archived for diagnosis.
- **Java version mismatch on a developer rerun.** The pipeline picks the JDK from `BRANCH_CONFIG` (or the `JDK` / `TCK_JDK` overrides). If you're reproducing a failure locally, match those.
- **Need to rehearse without publishing.** `DRY_RUN=true` (the default) does the full build, tagging, and TCK run, but skips the Maven Central deploy and the `git push origin` of the release branch / tag. The conflict check still runs against origin so a stale tag fails fast instead of after burning the whole TCK.
- **Iterating on the pipeline itself.** Combine `DRY_RUN=true` with `SMOKE_TEST=true` to run a tiny representative TCK subset, dropping run time to ~10 min. The pipeline still does the full build, tagging, and report aggregation, so changes to those steps are exercised end-to-end without burning the full TCK. `SMOKE_TEST` is silently ignored without `DRY_RUN`, so it can't accidentally release a non-conformant build.
- **Releasing a milestone or RC.** Set `MILESTONE_VERSION=M1` (or `M2` / `RC1` / etc.). The version becomes `<pom-base>-<MILESTONE_VERSION>` (e.g. `5.0.0-M2`), tagged exactly that on both `mojarra` and `jakartaee/faces`, with both impl and API published to Maven Central at that version. The source branch is left untouched — no PR-merge, no milestone close/open, no GitHub release, no snapshot bump.
