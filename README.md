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

Two purpose-keyed files per validated release:

    tck-validation-impl-<implVersion>.json    e.g. tck-validation-impl-5.0.3.json
    tck-validation-api-<apiVersion>.json      e.g. tck-validation-api-5.0.1.json

- The **impl reuse record** (`-impl-`, every line) is keyed by the impl release
  version and read by Mojarra's own `SKIP_TCK` gate.
- The **API gate record** (`-api-`, 5.0+ only, and only when the API is built from
  the `faces/` submodule because it is not yet on Central) is keyed by the API
  version and read by the Faces API release gate.

Keying the API record by API version lets the Faces gate find it even when the impl
and API versions differ — e.g. an impl line iterating (impl `5.0.1`, `5.0.2`,
`5.0.3`) against a stable API (`5.0.0`, then `5.0.1`). The two versions coincide
only when a fresh API is cut alongside the impl.

- **Written** by [Mojarra's release pipeline](https://github.com/eclipse-ee4j/mojarra/blob/master/Jenkinsfile)
  (TCK stage) on any release line, but only from a **full** green TCK run — a
  `SMOKE_TEST` or `SKIP_OLD_TCK` run drops TCK coverage and deliberately writes no
  record, so it can neither gate an API release nor enable a `SKIP_TCK` reuse.
- **Read** by the [Faces API release pipeline](https://github.com/jakartaee/faces/blob/5.0/api/Jenkinsfile)
  (TCK gate, 5.0+) at the `-api-` record, and by Mojarra's own `SKIP_TCK` gate at
  the `-impl-` record:

      https://raw.githubusercontent.com/eclipse-ee4j/mojarra/tck-status/tck-validation-impl-<implVersion>.json
      https://raw.githubusercontent.com/eclipse-ee4j/mojarra/tck-status/tck-validation-api-<apiVersion>.json

## Record format

| field         | meaning                                                              |
|---------------|----------------------------------------------------------------------|
| `line`        | release line (e.g. `5.0`)                                            |
| `apiVersion`  | the `jakarta.faces-api` version validated (the bundled spec version on 4.x) |
| `facesSha`    | jakartaee/faces commit the TCK ran against (empty on 4.x)            |
| `mojarraSha`  | Mojarra commit that ran the TCK                                      |
| `mojarraTree` | Mojarra source tree sha — the impl source + `faces/` submodule pin identity |
| `dryRun`      | whether the Mojarra run was a dry-run                               |
| `tckVersion`  | Faces TCK version used                                              |
| `glassfish`   | GlassFish version the TCK ran on                                    |
| `passed`      | passed test count                                                  |
| `buildUrl`    | Mojarra CI build that produced the record                          |
| `timestamp`   | UTC build time                                                     |

A record exists only on a green **full** TCK run, so its presence attests success —
the gates check identity only, there is no status flag to read:

- The **Faces API gate** (reads the `-api-` record) requires matching `line`,
  `apiVersion`, and `facesSha`.
- The **`SKIP_TCK` reuse gate** (reads the `-impl-` record) requires matching
  `line`, `mojarraTree`, and `facesSha`.
