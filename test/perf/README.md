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
mvn -pl impl,test/base -am clean install -DskipTests
```

The bench injects this freshly-installed `jakarta.faces` jar into the server at
build time, so re-run this after every impl change you want reflected.

## Run

Everything else runs from inside the module — `cd test/perf` first, then:

```
mvn clean verify -Dperf=true                 # GlassFish (default)
mvn clean verify -Dperf=true -Pwildfly       # WildFly
mvn clean verify -Dperf=true -Ptomee         # TomEE (Plume)

# Tune iteration counts (warmup=50, runs=1000 by default):
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

Row data for the table/repeat/composite scenarios comes from one shared `DataBean`. The counts are
calibrated so each scenario costs roughly ~5 ms per postback on tomcat-myfaces, keeping per-scenario times
comparable: `readonlyRows` (200) and `inputRows` (50) for the cheap table/repeat rows, `compositeRows` (100)
for the heavier per-row composites, `ajaxRows` (200) for the restore-dominated ajax-inputs scenarios (kept
under the container's default `maxParameterCount`), and `groups` (5×10) for the nested scenarios. The
`*-unrolled` scenarios size their `c:forEach` directly for the same ~5 ms target.

- `index` — landing page (smallest baseline)
- `form-inputs` — single h:form with text/textarea/select/checkbox/radio, managed converters+validators, BV
- `table-readonly` — h:dataTable, 200 rows, outputs only
- `table-inputs` — h:dataTable, 50 rows, per-row inputs + converters/validators
- `table-nested` — h:dataTable ∋ h:dataTable with a per-row input composite (5×10); UIData twin of `composite-nested`, isolating UIData's per-row child-state save/restore against ui:repeat's
- `repeat-readonly` — ui:repeat, 200 rows, outputs only
- `repeat-inputs` — ui:repeat, 50 rows, per-row inputs
- `repeat-nested` — ui:repeat ∋ ui:repeat (5×10 rows, per-row inputs)
- `composite-readonly` — readonly composite component, 100 instances
- `composite-inputs` — input composite component, 50 instances
- `composite-nested` — composite component nested inside ui:repeat (5×10)
- `composite-unrolled` — *static* tree of composite components built by `c:forEach` (~100 composites, all NamingContainers — the issue #4811 pattern); a postback rebuilds and re-renders the whole composite tree at scale (its restore is delta-free, so the state-restore walk is skipped)
- `view-unrolled` — flat *static* tree built by `c:forEach` (~200 panelGroups, no inputs); delta-free full postback exercising `restoreViewRootOnly` + the descendant mark-id cache + full render at scale
- `form-inputs-ajax`, `table-inputs-ajax`, `repeat-inputs-ajax` — same as their non-ajax twins but submit via `<f:ajax execute="@form" render="@form messages">`; driver sends a partial-ajax POST and refreshes `ViewState` from the XML response.
- `view-unrolled-ajax` — ajax twin of `view-unrolled`: same flat static tree, `@form` ajax postback (`restoreViewRootOnly` + partial-response render at scale)
- `dynamic-form-ajax` — the idiomatic **dynamic components** pattern: a request-scoped bean holds the container via `binding`, and an `f:event type="postAddToView"` listener (`DynamicFormBean#build`) **programmatically** builds a large set of labelled inputs each time the view is (re)built, rather than declaring them in the facelet. The tree structure is identical every request, so the ajax postback runs the full lifecycle (decode/validate/update + partial render) over the dynamically-built inputs exactly like `form-inputs-ajax`. Unlike `dynamic-toggle-ajax` it does **not** touch the dynamic add/remove machinery (the components are built during the normal view (re)build, not after it) — it isolates the cost of `binding` resolution plus programmatic component construction, and is portable across implementations.
- `dynamic-toggle-ajax` — each ajax toggle adds or removes a large input subtree under the in-view `container` via `getChildren().add()/clear()` in the action. Because the mutation happens **after** `markInitialState`, this is the scenario that drives Mojarra's dynamic add/remove path — `StateContext` dynamic-action tracking, the `DYNAMIC_COMPONENT` marker, and full-state-save/restore of the dynamic subtree — which no structurally-static scenario reaches. It is the one for benchmarking that path. On Mojarra the dynamic subtree is replayed on restore, so toggles alternate add↔remove; MyFaces re-adds it each request rather than persisting it across the postback.
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

Enable JFR by appending the flags to `-Dperf.jvmArguments` — this is filtered straight into
the forked server VM, so there is nothing to edit in server config (and nothing to lose when
`mvn clean` regenerates that config). Keep the heap flag the property defaults to:

```
-Dperf.jvmArguments="-Xmx1g -XX:StartFlightRecording=filename=/tmp/perf-bench.jfr,settings=profile,name=perf,dumponexit=true -XX:FlightRecorderOptions=stackdepth=256"
```

`dumponexit=true` writes the recording when Arquillian shuts the server down at the end of
the run, so no `duration=` guesswork is needed.

This works for **WildFly and TomEE** (where `perf.jvmArguments` feeds the forked VM
directly). **GlassFish** can't be JFR-injected through the harness at all: the
`arquillian-glassfish-server-managed` adapter only exposes `maxHeapSize`, with no
arbitrary-JVM-args property — so profile on WildFly or TomEE instead.

Then run the bench at a tighter iteration count — JFR samples on a 20 ms cadence by default, so ~7,500 samples (≈150 s of bench time) is plenty for a clear hot-method picture without producing a huge file:

```
mvn failsafe:integration-test failsafe:verify -Dperf=true -Dperf.warmup=20 -Dperf.runs=200
```

**Isolating one scenario / one phase.** To profile a single scenario, restrict the run
with `-Dperf.scenarios=<name>` (comma-separated; the IT filters to just those). But note
the sampling-budget trap: JFR fires one sample per thread per ~10–20 ms, so a *thin* phase
gets very few samples even over a long run. At the suite default of 1000 runs, a phase
that costs ~0.3 ms/req accumulates only ~0.3 s of wall time → **~20 samples**, far too few
to rank methods. Scale `-Dperf.runs` up (e.g. `-Dperf.runs=20000`) until the *thinnest*
phase you care about clears a few hundred samples — a single-scenario run is cheap enough
to afford it. Check the per-phase `total_us` in the stats dump: divide by ~15000 µs to
estimate the sample count before trusting any ranking.

Analyze with the JDK-bundled `jfr` tool:

```
jfr summary /tmp/perf-bench.jfr
jfr view --width 200 hot-methods /tmp/perf-bench.jfr
jfr view --width 200 allocation-by-class /tmp/perf-bench.jfr
jfr view --width 200 allocation-by-site /tmp/perf-bench.jfr
```

For deeper aggregation (e.g. grouping hot leaves by their Mojarra-side caller, filtering by thread, attributing samples to scenarios via the timeline) export the execution samples to JSON and process them with a small script:

```
jfr print --json --stack-depth 256 --events jdk.ExecutionSample /tmp/perf-bench.jfr > /tmp/samples.json
```

**`--stack-depth` is mandatory here:** `jfr print` truncates each stack to **5 frames by
default**, which silently drops every frame below the JDK leaf — your aggregation then finds
no Faces/Mojarra frames at all (the recording itself is fine; only the print is truncated).
Pass a depth that comfortably exceeds the deepest request stack (256 is safe).

Each event lives at `event['values']['stackTrace']['frames']`, ordered **root → leaf**
(so `frames[0]` is the outermost caller and `frames[-1]` is the sampled leaf); class names
use slash form (`com/sun/faces/...`). To attribute a sample to a lifecycle phase, scan its
frames for the phase class — `RestoreViewPhase`, `ApplyRequestValuesPhase`,
`ProcessValidationsPhase`, `UpdateModelValuesPhase`, `InvokeApplicationPhase`,
`RenderResponsePhase` (for ajax, Restore View and Render Response also run through
`PartialViewContextImpl`). Filter to the request worker threads (`http-thread-pool-*` on
GlassFish) to drop server-housekeeping samples.

### Gotchas

- Cancelling a bench mid-flight can leave an already-deployed app in `target/glassfish7/glassfish/domains/domain1/applications/`; the next run will then error with `Application with name perf-<version> is already registered`. Fix with `rm -rf target/glassfish7` followed by `mvn clean package -DskipTests`.
- A killed bench can also leave an orphan `ASMain` JVM running, which trips the next run with `The server is already running!`. Check `jps`, identify the specific PID, kill it by ID — never blanket `pkill java` on a shared host.

## Profiling a non-Mojarra layer (e.g. GlassFish, Weld)

Because the bench drives the *whole* request stack, the JFR profile picks up CPU and allocation from every layer — Mojarra, EL, Weld/CDI, Grizzly, and the JDK. To swap one of those layers for a local build:

1. Build the layer in its own checkout (e.g. `mvn clean install` in a GlassFish module) so a `<version>-SNAPSHOT` artifact lands in `~/.m2`.
2. Either re-run `mvn clean package -DskipTests -Dglassfish.version=<version>-SNAPSHOT` (forces a fresh GlassFish unpack with the SNAPSHOT zip), or — for a single-jar swap — replace the matching `target/glassfish7/glassfish/modules/<artifact>.jar` in place.
3. Enable JFR via `-Dperf.jvmArguments` (see above) and run the bench. Since that hook is unavailable on GlassFish, profile a server-layer swap on a JFR-injectable server (WildFly or TomEE) where possible.

The bench couldn't care less which version of which jar is in the server's modules directory; whatever's on disk at startup is what gets profiled.
