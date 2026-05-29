# test/perf — Mojarra request-pipeline microbenchmark

A WAR with a representative spread of Facelets pages (forms, tables, ui:repeat,
composites, nested variants, readonly+input flavors) plus CDI-managed converters
and validators, driven by a single integration test that loops thousands of
GETs and postbacks against a managed app server (GlassFish by default, optionally
WildFly or TomEE) and dumps per-phase server-side timings recorded by
`PhaseTimingListener` into `/perf-stats`.

## Setup (once, and again whenever you change impl)

From the repo root, install the impl jar you want to measure plus the `base`
test helper into your local `~/.m2`:

```
. java21 && mvn -pl impl,test/base -am clean install -DskipTests
```

The bench injects this freshly-installed `jakarta.faces` jar into the server at
build time, so re-run this after every impl change you want reflected.

## Run

Everything else runs from inside the module — `cd test/perf` first, then:

```
mvn clean verify -Dperf=true                 # GlassFish (default)
mvn clean verify -Dperf=true -Pwildfly       # WildFly
mvn clean verify -Dperf=true -Ptomee         # TomEE (Plume)

# Tune iteration counts (warmup=50, runs=500 by default):
mvn clean verify -Dperf=true -Dperf.warmup=200 -Dperf.runs=2000
```

The IT is gated behind `-Dperf=true` (same convention as
`ComponentTreePerfHarness`): without it, `PerfBenchIT` is disabled by JUnit's
`@EnabledIfSystemProperty`, so a normal `mvn clean install` does not execute the
benchmark.

After the run, `target/perf-stats-<timestamp>.txt` contains the per-scenario,
per-phase table (count / total_us / avg_us / min_us / max_us) plus an aggregate
`<TOTAL>` row per phase. The same text is printed to stdout.

## Servers

The server is selected with `-P`; GlassFish is the default profile. Each profile
provisions its own server under `target/` and overlays the chosen Mojarra jar.

| profile     | server          | provisioned into                | version property                  |
|-------------|-----------------|----------------------------------|-----------------------------------|
| *(default)* | GlassFish       | `target/glassfish7`             | `-Dglassfish.version` (7.0.16)    |
| `-Pwildfly` | WildFly         | `target/wildfly`                | `-Dwildfly.feature-pack.version` (40.0.0.Final) |
| `-Ptomee`   | TomEE Plume     | `target/apache-tomee-plume-*`   | `-Dtomee.version` (10.1.5)        |

- **WildFly**: pass extra JVM args to the managed process with
  `-Djboss.options="-Xmx8g"`.
- **TomEE**: Plume 10.1.5 is Jakarta EE 10, but Mojarra 4.x (Faces 4.1) is EE 11,
  so the profile overlays the EE 11 API jars Faces references (EL 6.0, CDI 4.1)
  alongside the Mojarra jar. The same shim is applied to every measured version,
  so the per-version delta stays apples-to-apples. HTTP/stop ports are
  `-Dtomee.httpPort` (8080) / `-Dtomee.stopPort` (8005).

## Picking the Mojarra version

The injected jar is chosen by `-Dmojarra.version`; the default is the current
`-SNAPSHOT` — i.e. whatever you just installed in [Setup](#setup-once-and-again-whenever-you-change-impl).
To measure an already-released build instead (no impl install needed):

```
mvn clean verify -Dperf=true -Dmojarra.version=4.0.18
```

This works on every server profile.

## Comparing two versions

Same checkout, two released versions, no rebuild — just swap the injected jar:

```
cd test/perf
mvn clean verify -Dperf=true -Dmojarra.version=4.0.17 && cp target/perf-stats-*.txt /tmp/a.txt
mvn clean verify -Dperf=true -Dmojarra.version=4.0.18 && cp target/perf-stats-*.txt /tmp/b.txt
diff /tmp/a.txt /tmp/b.txt
```

Comparing two branches (e.g. a SNAPSHOT against itself before/after a change)
means rebuilding impl between runs:

```
git checkout <branch-a> && mvn -pl impl,test/base -am clean install -DskipTests
( cd test/perf && mvn clean verify -Dperf=true && cp target/perf-stats-*.txt /tmp/a.txt )

git checkout <branch-b> && mvn -pl impl,test/base -am clean install -DskipTests
( cd test/perf && mvn clean verify -Dperf=true && cp target/perf-stats-*.txt /tmp/b.txt )

diff /tmp/a.txt /tmp/b.txt
```

Run both sides on the same server profile so the comparison is fair.

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

## Profiling with JFR

The bench is also useful as a JFR driver for figuring out *where* the time is going. `PerfStats` tells you per-(scenario, phase) wall time; JFR layered on top tells you which methods are burning the CPU and which call sites are allocating.

Enable JFR by adding two `<jvm-options>` to the GlassFish JVM at the top of `<java-config>` inside `server-config`:

```
target/glassfish7/glassfish/domains/domain1/config/domain.xml
```

```xml
<jvm-options>-XX:StartFlightRecording=duration=900s,filename=/tmp/perf-bench.jfr,settings=profile,name=perf</jvm-options>
<jvm-options>-XX:FlightRecorderOptions=stackdepth=128</jvm-options>
```

(For WildFly/TomEE the same JVM options go into their respective config — WildFly
`standalone.conf`, TomEE `bin/setenv.sh` / `-Dtomee.catalina_opts` — rather than
`domain.xml`.)

Then run the bench at a tighter iteration count — JFR samples on a 20 ms cadence by default, so ~7,500 samples (≈150 s of bench time) is plenty for a clear hot-method picture without producing a huge file:

```
mvn failsafe:integration-test failsafe:verify -Dperf=true -Dperf.warmup=20 -Dperf.runs=200
```

Analyze with the JDK-bundled `jfr` tool:

```
jfr summary                          /tmp/perf-bench.jfr
jfr view --width 200 hot-methods         /tmp/perf-bench.jfr
jfr view --width 200 allocation-by-class /tmp/perf-bench.jfr
jfr view --width 200 allocation-by-site  /tmp/perf-bench.jfr
```

For deeper aggregation (e.g. grouping hot leaves by their Mojarra-side caller, filtering by thread, attributing samples to scenarios via the timeline) export the execution samples to JSON and process them with a small script:

```
jfr print --json --events jdk.ExecutionSample /tmp/perf-bench.jfr > /tmp/samples.json
```

Each event lives at `event['values']['stackTrace']['frames']`; class names use slash form (`com/sun/faces/...`).

### Gotchas

- `domain.xml` is regenerated by `mvn clean package`, so reapply the JFR `<jvm-options>` after every WAR rebuild.
- Cancelling a bench mid-flight can leave an already-deployed app in `target/glassfish7/glassfish/domains/domain1/applications/`; the next run will then error with `Application with name perf-<version> is already registered`. Fix with `rm -rf target/glassfish7` followed by `mvn clean package -DskipTests` (then reapply the JFR edit).
- A killed bench can also leave an orphan `ASMain` JVM running, which trips the next run with `The server is already running!`. Check `jps`, identify the specific PID, kill it by ID — never blanket `pkill java` on a shared host.

## Profiling a non-Mojarra layer (e.g. GlassFish, Weld)

Because the bench drives the *whole* request stack, the JFR profile picks up CPU and allocation from every layer — Mojarra, EL, Weld/CDI, Grizzly, and the JDK. To swap one of those layers for a local build:

1. Build the layer in its own checkout (e.g. `mvn clean install` in a GlassFish module) so a `<version>-SNAPSHOT` artifact lands in `~/.m2`.
2. Either re-run `mvn clean package -DskipTests -Dglassfish.version=<version>-SNAPSHOT` (forces a fresh GlassFish unpack with the SNAPSHOT zip), or — for a single-jar swap — replace the matching `target/glassfish7/glassfish/modules/<artifact>.jar` in place.
3. Reapply the JFR `<jvm-options>` and run the bench.

The bench couldn't care less which version of which jar is in the server's modules directory; whatever's on disk at startup is what gets profiled.
