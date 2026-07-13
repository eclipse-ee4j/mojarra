# TCK validation records (`tck-status`)

This is an orphan branch **managed by CI** — it holds no source code, only JSON
records certifying that a Mojarra release candidate passed the Jakarta Faces TCK.
Do not hand-edit it and do not merge it into a code branch.

## Why it exists

Since Faces 5.0, the standalone `jakarta.faces-api` is released from
[jakartaee/faces](https://github.com/jakartaee/faces) on a separate CI, but the
full Faces TCK only runs in **Mojarra's** release pipeline. So the API must not be
published to Maven Central unless Mojarra's TCK has passed against that exact API
version and source commit.

The two pipelines run on separate Eclipse CI controllers with no shared auth, so
Mojarra publishes a record here on every green TCK, and the Faces API release job
reads it over the public raw URL as a release gate.

## What's here

One file per validated API version:

    tck-validation-<apiVersion>.json      e.g. tck-validation-5.0.0-M3.json

- **Written** by [Mojarra's release pipeline](https://github.com/eclipse-ee4j/mojarra/blob/master/Jenkinsfile)
  (TCK stage) on a successful run (5.0+ lines only).
- **Read** by the [Faces API release pipeline](https://github.com/jakartaee/faces/blob/5.0/api/Jenkinsfile)
  (TCK gate) at:

      https://raw.githubusercontent.com/eclipse-ee4j/mojarra/tck-status/tck-validation-<version>.json

## Record format

| field        | meaning                                              |
|--------------|------------------------------------------------------|
| `line`       | release line (e.g. `5.0`)                            |
| `apiVersion` | the `jakarta.faces-api` version validated            |
| `facesSha`   | jakartaee/faces commit the TCK ran against           |
| `mojarraSha` | Mojarra commit that ran the TCK                      |
| `dryRun`     | whether the Mojarra run was a dry-run                |
| `runTck`     | always `true` (only written on a TCK run)            |
| `tckVersion` | Faces TCK version used                               |
| `glassfish`  | GlassFish version the TCK ran on                     |
| `passed`     | passed test count                                    |
| `result`     | always `SUCCESS` (only written on a green run)       |
| `buildUrl`   | Mojarra CI build that produced the record            |
| `timestamp`  | UTC build time                                       |

The gate requires `result == SUCCESS`, `runTck == true`, and matching `line`,
`apiVersion`, and `facesSha`.
