# TCK validation records (`tck-status`)

This is an orphan branch **managed by CI** — it holds no source code, only JSON
records certifying that a Mojarra release candidate passed the Jakarta Faces TCK.
Do not hand-edit it and do not merge it into a code branch.

## Why it exists

Two pipelines read these records:

1. **The Faces API release gate.** Since Faces 5.0 the standalone
   `jakarta.faces-api` is released from [jakartaee/faces](https://github.com/jakartaee/faces)
   on a separate CI, but the full Faces TCK only runs in **Mojarra's** release
   pipeline. So the API must not be published to Maven Central unless Mojarra's
   TCK passed against that exact API version and source commit. The two pipelines
   run on separate Eclipse CI controllers with no shared auth, so Mojarra
   publishes a record here and the Faces API job reads it over the public raw URL.
2. **Mojarra's own `SKIP_TCK` reuse gate.** A green DRY_RUN records here; a
   later real release of the same version can then set `SKIP_TCK` to reuse that
   record instead of re-running the TCK, provided the impl source and faces commit
   are identical. This is what lets a 4.x release pay the ~3h TCK once (in the
   DRY_RUN) and skip it on the real run.

## What's here

One file per validated **release version**:

    tck-validation-<releaseVersion>.json     e.g. tck-validation-5.0.0-M3.json

The key is the **impl release version**. On the 5.0 lockstep releases (impl and
API cut at the same version) it equals the API version the Faces gate looks up; on
a 4.x or impl-only-patch release it is just that release's version.

- **Written** by [Mojarra's release pipeline](https://github.com/eclipse-ee4j/mojarra/blob/master/Jenkinsfile)
  (TCK stage) on any release line, but only from a **full** green TCK run — a
  `SMOKE_TEST` or `SKIP_OLD_TCK` run drops TCK coverage and deliberately writes no
  record, so it can neither gate an API release nor enable a `SKIP_TCK` reuse.
- **Read** by the [Faces API release pipeline](https://github.com/jakartaee/faces/blob/5.0/api/Jenkinsfile)
  (TCK gate, 5.0+) and by Mojarra's own `SKIP_TCK` gate, at:

      https://raw.githubusercontent.com/eclipse-ee4j/mojarra/tck-status/tck-validation-<version>.json

## Record format

| field         | meaning                                                              |
|---------------|----------------------------------------------------------------------|
| `line`        | release line (e.g. `5.0`)                                            |
| `apiVersion`  | the `jakarta.faces-api` version validated (the bundled spec version on 4.x) |
| `facesSha`    | jakartaee/faces commit the TCK ran against (empty on 4.x)            |
| `mojarraSha`  | Mojarra commit that ran the TCK                                      |
| `mojarraTree` | Mojarra source tree sha — the impl source + `faces/` submodule pin identity |
| `dryRun`      | whether the Mojarra run was a dry-run                               |
| `runTck`      | always `true` (only written on a TCK run)                           |
| `tckVersion`  | Faces TCK version used                                              |
| `glassfish`   | GlassFish version the TCK ran on                                    |
| `passed`      | passed test count                                                  |
| `result`      | always `SUCCESS` (only written on a green run)                     |
| `buildUrl`    | Mojarra CI build that produced the record                          |
| `timestamp`   | UTC build time                                                     |

- The **Faces API gate** requires `result == SUCCESS`, `runTck == true`, and
  matching `line`, `apiVersion`, and `facesSha`.
- The **`SKIP_TCK` reuse gate** requires `result == SUCCESS` and matching `line`,
  `mojarraTree`, and `facesSha`.
