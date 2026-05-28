# test/perf — Mojarra request-pipeline microbenchmark

A WAR with a representative spread of Facelets pages (forms, tables, ui:repeat,
composites, nested variants, readonly+input flavors) plus CDI-managed converters
and validators, driven by a single integration test that loops thousands of
GETs and postbacks against a managed GlassFish and dumps per-phase server-side
timings recorded by `PhaseTimingListener` into `/perf-stats`.

## Run

```
# Install the mojarra impl jar of the branch you want to measure:
. java21 && mvn -pl impl -am install -DskipTests

# Build & run the bench (warmup=50, runs=500 by default).
# The IT is gated behind -Dperf=true (same convention as ComponentTreePerfHarness):
mvn -pl test/perf -am verify -Dperf=true

# Or tune iteration counts:
mvn -pl test/perf -am verify -Dperf=true -Dperf.warmup=200 -Dperf.runs=2000
```

Without `-Dperf=true`, `PerfBenchIT` is disabled by JUnit's
`@EnabledIfSystemProperty`, so a normal `mvn clean install` does not execute the
benchmark.

After the run, `target/perf-stats-<timestamp>.txt` contains the per-scenario,
per-phase table (count / total_us / avg_us / min_us / max_us) plus an aggregate
`<TOTAL>` row per phase. The same text is printed to stdout.

## Comparing two branches

```
# Branch A:
git checkout <branch-a>
mvn -pl impl -am install -DskipTests
mvn -pl test/perf -am verify
cp test/perf/target/perf-stats-*.txt /tmp/branch-a.txt

# Branch B:
git checkout <branch-b>
mvn -pl impl -am install -DskipTests
mvn -pl test/perf -am verify
cp test/perf/target/perf-stats-*.txt /tmp/branch-b.txt

diff /tmp/branch-a.txt /tmp/branch-b.txt
```

Note that the test/perf module itself only needs to exist on one branch; the
mojarra jar gets injected into GlassFish at `process-test-classes`, so as long
as you `install` the impl from the branch you want to measure first, the bench
exercises that jar.

## Scenarios

- `index` — landing page (smallest baseline)
- `form-inputs` — single h:form with text/textarea/select/checkbox/radio, managed converters+validators, BV
- `table-readonly` — h:dataTable, 200 rows, outputs only
- `table-inputs` — h:dataTable, 50 rows, per-row inputs + converters/validators
- `repeat-readonly` — ui:repeat, 200 rows, outputs only
- `repeat-inputs` — ui:repeat, 40 rows, per-row inputs
- `repeat-nested` — ui:repeat ∋ ui:repeat (5×10 rows, per-row inputs)
- `composite-readonly` — readonly composite component, 200 instances
- `composite-inputs` — input composite component, 40 instances
- `composite-nested` — composite component nested inside ui:repeat (5×10)
- `form-inputs-ajax`, `table-inputs-ajax`, `repeat-inputs-ajax` — same as their non-ajax twins but submit via `<f:ajax execute="@form" render="@form messages">`; driver sends a partial-ajax POST and refreshes `ViewState` from the XML response.
- `viewparam-get` — GET with `<f:metadata><f:viewParam><f:viewAction></f:metadata>` so the GET runs the **entire** lifecycle (Apply Request Values → Render Response), not just Restore View + Render Response.

GET-only scenarios fire RESTORE_VIEW + RENDER_RESPONSE; the `viewparam-get`
scenario fires all six on GET because of the `<f:viewParam>`/`<f:viewAction>`;
postback and ajax-postback scenarios fire all six phases on POST.

## Endpoints

- `GET /perf-stats` — plain-text dump
- `GET /perf-stats?format=json` — JSON dump (easier diff)
- `GET /perf-stats?reset=1` — clear all accumulators
