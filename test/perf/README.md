# test/perf — Mojarra request-pipeline microbenchmark

A WAR with a representative spread of Facelets pages (forms, tables, ui:repeat,
composites, nested variants, readonly+input flavors) plus CDI-managed converters
and validators, driven by a single integration test that loops thousands of
GETs and postbacks against a managed app server (GlassFish by default, optionally
WildFly, TomEE, Payara, OpenLiberty or Tomcat) and dumps per-phase server-side
timings recorded by `PhaseTimingListener` into `/perf-stats`.

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
mvn clean verify -Dperf=true             # GlassFish (default)
mvn clean verify -Dperf=true -Pwildfly   # WildFly
mvn clean verify -Dperf=true -Ptomee     # TomEE Plume
mvn clean verify -Dperf=true -Ppayara    # Payara
mvn clean verify -Dperf=true -Pliberty   # OpenLiberty (via facesContainer)
mvn clean verify -Dperf=true -Ptomcat    # Tomcat (+ Weld + Hibernate Validator)

# Every server also has a -myfaces twin that runs Apache MyFaces instead of Eclipse Mojarra:
mvn clean verify -Dperf=true -Pglassfish-myfaces   # GlassFish (with patched jakarta.faces.jar)
mvn clean verify -Dperf=true -Pwildfly-myfaces     # WildFly (via WAR_BUNDLES_JSF_IMPL)
mvn clean verify -Dperf=true -Ptomee-myfaces       # TomEE WebProfile
mvn clean verify -Dperf=true -Ppayara-myfaces      # Payara (with patched jakarta.faces.jar)
mvn clean verify -Dperf=true -Pliberty-myfaces     # OpenLiberty
mvn clean verify -Dperf=true -Ptomcat-myfaces      # Tomcat (+ Weld + Hibernate Validator)

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
provisions its own server under `target/` and supplies the chosen Mojarra jar —
either overlaid into the server's modules (GlassFish, Payara, WildFly, TomEE) or,
for the bare servlet containers that ship no Faces implementation, bundled into
the WAR's `WEB-INF/lib` (OpenLiberty, Tomcat).

| profile         | server          | provisioned into                | version property                  |
|-----------------|-----------------|----------------------------------|-----------------------------------|
| *(default)*     | GlassFish       | `target/glassfish8`             | `-Dglassfish.version` (8.0.3)      |
| `-Pwildfly`     | WildFly         | `target/wildfly`                | `-Dwildfly.version` (40.0.0.Final) |
| `-Ptomee`       | TomEE Plume     | `target/apache-tomee-plume-*`   | `-Dtomee.version` (10.1.5)        |
| `-Ppayara`      | Payara          | `target/payara7`                | `-Dpayara.version` (7.2026.5)     |
| `-Pliberty`     | OpenLiberty     | `target/wlp`                    | `-Dliberty.version` (26.0.0.5-beta) |
| `-Ptomcat`      | Tomcat          | `target/apache-tomcat-*`        | `-Dtomcat.version` (11.0.22)      |

- **WildFly**: pass extra JVM args to the managed process with
  `-Djboss.options="-Xmx8g"`.
- **TomEE**: Plume 10.1.5 is Jakarta EE 10, but Mojarra 4.x (Faces 4.1) is EE 11,
  so the profile overlays the EE 11 API jars Faces references (EL 6.0, CDI 4.1)
  alongside the Mojarra jar. The same shim is applied to every measured version,
  so the per-version delta stays apples-to-apples.
- **Payara**: GlassFish-derived, so Mojarra is overlaid into
  `payara7/glassfish/modules` exactly as for GlassFish.
- **OpenLiberty**: ships MyFaces. The profile enables the `facesContainer-4.1`
  feature (instead of `faces-4.1`) so the WAR-bundled Mojarra is used as the Faces
  implementation; CDI, EL and Bean Validation come from the server. The bench
  driver is pinned to HTTP/1.1, which this server's endpoint would otherwise
  upgrade to HTTP/2.
- **Tomcat**: a bare servlet container, so the profile bundles Mojarra plus Weld 6
  (`-Dweld.version`, CDI) and Hibernate Validator 9 (`-DhibernateValidator.version`,
  Bean Validation) into the WAR's `WEB-INF/lib`.

Every profile launches its server with the same max heap (`-Xmx1g`) so the
cross-server comparison is apples-to-apples; change it for all servers at once with
`-Dperf.heapSize=2g`.

### MyFaces variants

Each server has a `-myfaces` twin (`-Pglassfish-myfaces`, `-Pwildfly-myfaces`,
`-Ptomee-myfaces`, `-Ppayara-myfaces`, `-Pliberty-myfaces`, `-Ptomcat-myfaces`)
that runs **Apache MyFaces** (`-Dmyfaces.version`, default `4.1.4-SNAPSHOT`) instead of
Mojarra, for cross-implementation comparison. How MyFaces is hosted depends on the server:

- **Tomcat / OpenLiberty**: bundle `myfaces-api`+`myfaces-impl` in the WAR instead of the
  Mojarra jar (OpenLiberty still via `facesContainer-4.1`).
- **TomEE**: uses the **WebProfile** distribution (the MyFaces flavour) and overlays
  `${myfaces.version}` into its `lib/`.
- **WildFly**: bundles MyFaces and tells WildFly to step aside via
  `WEB-INF/jboss-deployment-structure.xml` (excluding `org.jboss.as.jsf` etc.) plus
  `org.jboss.jbossfaces.WAR_BUNDLES_JSF_IMPL=true`.
- **GlassFish / Payara**: keep the Mojarra module (the Weld integration needs it) but make
  the WAR use bundled MyFaces via `WEB-INF/glassfish-web.xml` (`delegate=false` +
  `useBundledJsf=true`), and strip Mojarra's CDI-extension and `ServletContainerInitializer`
  service entries from the container `jakarta.faces.jar` to avoid clashes.

## Picking the Mojarra version

The injected jar is chosen by `-Dmojarra.version`; the default is the current
`-SNAPSHOT` — i.e. whatever you just installed in [Setup](#setup-once-and-again-whenever-you-change-impl).
To measure an already-released build instead (no impl install needed):

```
mvn clean verify -Dperf=true -Dmojarra.version=4.1.9
```

This works on every server profile. The `-myfaces` profiles take `-Dmyfaces.version`
(default `4.1.4-SNAPSHOT`) the same way:

```
mvn clean verify -Dperf=true -Ptomcat-myfaces -Dmyfaces.version=4.1.3
```

## Comparing two versions

Same checkout, two released versions, no rebuild — just swap the injected jar:

```
cd test/perf
mvn clean verify -Dperf=true -Dmojarra.version=4.1.8 && cp target/perf-stats-*.txt /tmp/a.txt
mvn clean verify -Dperf=true -Dmojarra.version=4.1.9 && cp target/perf-stats-*.txt /tmp/b.txt
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

## Reporting a comparison

`perfreport.py` turns two dumps into a markdown scenario × phase report — a delta
matrix and a timings matrix (`A / B` average µs per invocation). Each closes with a
`TOTAL` row (the suite for that phase) and a `TOTAL` column (one request through
every phase):

```
python3 perfreport.py /tmp/a.txt /tmp/b.txt \
    --a-label Mojarra --b-label MyFaces --title "Tomcat" --out /tmp/report.md
```

Negative delta means A is faster. A phase whose per-invocation average is under
100µs on both sides prints `noise` instead of a percentage; `-` means the scenario
does not exercise that phase.

Both dumps must come from the same bench session on the same host. Scoring a run
against a dump from an earlier session measures the host, not the code.

`--fail-on-regression` exits non-zero when a phase's suite total is slower in A than
in B, and `--fail-over PERCENT` widens the tolerance. That is how the CI job gates.

## CI perf report

`Jenkinsfile` in this directory drives the `mojarra-perf-report` Eclipse CI job,
which runs nightly. It runs both
arms — freshly built Mojarra, then MyFaces — back-to-back against Tomcat on one agent
and archives the `perfreport.py` output as `target/perf/report.md`. Nothing picks a
non-root `Jenkinsfile` up by itself: it needs a Jiro job configured with this path
as its pipeline script.

The build fails when a phase's suite total is slower than MyFaces. Only suite totals
gate: CI agents are shared containers with no CPU-governor control, so absolute
timings drift between nights and per-scenario cells are far too noisy to assert on.
A single scenario going red is a lead to chase, not a broken build. A phase sitting
near parity can still flip on agent noise alone — raise `FAIL_OVER` rather than
letting the job cry wolf.

Tomcat is the only server: no application-server provisioning keeps
the wall-clock sane and keeps the measured stack down to Faces + Weld + container.
`MYFACES_VERSION` defaults to a released version rather than the pom's `-SNAPSHOT`
default: a moving baseline makes a trend unreadable, because a swing could be MyFaces
trunk rather than Mojarra, and Apache purges old snapshot timestamps so an old build's
numbers can never be reproduced. A `-SNAPSHOT` value does work — the pipeline passes
Maven a `-gs` settings file adding the Apache snapshot repo, which merges with the
agent's own settings instead of replacing it — but prefer a release.

## Tuning state saving

The state-saving context parameters are filtered into the WAR's `web.xml`
at package time, so they can be tuned per run without editing anything. Defaults
match the implementation defaults; each knob sets the Mojarra parameter and, where
applicable, its MyFaces equivalent (the other implementation ignores the foreign one):

| property | default | Faces / Mojarra param | MyFaces equivalent |
|----------|---------|-----------------------|--------------------|
| `-Dwebapp.stateSavingMethod`     | `server` | `jakarta.faces.STATE_SAVING_METHOD`    | *(same)* |
| `-Dwebapp.serializeServerState`  | `false`  | `jakarta.faces.SERIALIZE_SERVER_STATE` | *(same)* |
| `-Dwebapp.compressViewState`     | `true`   | `com.sun.faces.compressViewState`      | `org.apache.myfaces.COMPRESS_STATE_IN_SESSION` |

```
mvn clean verify -Dperf=true -Dwebapp.stateSavingMethod=client -Dwebapp.serializeServerState=true
```

For any parameter without a dedicated knob, pass raw `<context-param>` XML through the
`webapp.additionalContextParams` escape hatch (the value is shell-single-quoted, so a literal
`'` inside a param value must be written as `'\''` — close the quote, an escaped quote, reopen):

```
mvn clean verify -Dperf=true \
  -Dwebapp.additionalContextParams='<context-param><param-name>com.sun.faces.disableIdUniquenessCheck</param-name><param-value>true</param-value></context-param>'
```

(`jakarta.faces.PROJECT_STAGE` is fixed at `Production` — development-stage performance is not of interest.)

## Scenarios

Row data for the table/repeat/composite/foreach scenarios comes from one shared `DataBean`, sized by four
constants — one per tier — so a whole tier is tuned by editing a single number: `READONLY_ROWS` (200),
`INPUT_ROWS` (35), `FOREACH_ROWS` (100) and nested `GROUPS`×`GROUP_ROWS` (5×10). The rows themselves are
realistic `Row` records (typed fields, a non-ASCII/HTML-metachar description exercising the slow escaping path).
The two `dynamic-*` scenarios are sized by the `FIELD_COUNT` constant of their backing bean, and the flat forms
by the shared `/WEB-INF/includes/form-fields.xhtml` field body. `index` and `viewparam-get` are intentionally
left small.

### Component-family matrix

Four component families each span up to six variants. The family fixes the *structure*; the variant fixes the
*request shape* and whether inputs are present:

| family | iterator | readonly (GET) | inputs | nested | build | inputs-ajax | nested-ajax |
|---|---|:-:|:-:|:-:|:-:|:-:|:-:|
| **table** | `h:dataTable` | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| **repeat** | `ui:repeat` | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| **composite** | composite components | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| **foreach** | `c:forEach items` (build-time unrolled) | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |

- **readonly** — GET render, outputs only, no form: isolates fresh `buildView` + encode (no state restore). Sized by `READONLY_ROWS` (200); `foreach-readonly` by `FOREACH_ROWS` (100).
- **inputs** — full postback, per-row inputs + managed converters/validators (`INPUT_ROWS`, 35): full lifecycle over a flat iteration.
- **nested** — full postback, the iterator inside itself two levels deep with per-row inputs (`GROUPS`×`GROUP_ROWS`, 5×10): isolates per-row child-state save/restore.
- **build** — full postback of a **readonly** (no-input) tree: empty ARV/PV/UMV, so it isolates the state-**restore** path + encode from any input processing — the representative postback cost for readonly content. `table-build`/`repeat-build` re-post the standard readonly tree (`READONLY_ROWS`); `composite-build` (the #4811 all-NamingContainer case) and `foreach-build` (flat unrolled outputs) are the `c:forEach`-built trees (`FOREACH_ROWS`, delta-free restore).
- **inputs-ajax** / **nested-ajax** — ajax twins of `inputs`/`nested`, submitting `<f:ajax execute="@form" render="…">`; the driver sends a partial-ajax POST and refreshes `ViewState` from the XML response.

The GET `readonly` and postback `build` variants of the same family render the same tree, giving a clean
`buildView` (GET) vs `restore` (postback) A/B. Everything else is a full postback (all six phases on POST).

Scenarios outside the matrix:

- `index` — landing page (smallest baseline); also relocates a stylesheet + script via `h:outputStylesheet`/`h:outputScript`
- `form-inputs` — flat multi-section form (product/contact/address/company/payment/preferences) sharing `/WEB-INF/includes/form-fields.xhtml`: text/textarea/select/checkbox/radio inputs over managed converters+validators and Bean Validation; `country` uses a `Map`-valued `f:selectItems` (label→code). The hero `name`/`quantity`/`price` fields stay flat; the bulk sit in nested typed section view-models on `FormBean` (two-level `BeanELResolver` path)
- `form-inputs-ajax` — ajax twin of `form-inputs` (same shared field body): submits via `<f:ajax execute="@form" render="@form messages">`; additionally the `name` field carries **two** `f:ajax` on distinct events (keyup+blur), which flips the client-behavior chain and Mojarra's *unoptimized* pass-through-attribute renderer path (enabled via the fragment's `ajax` param)
- `form-invalid` — same shared field body as `form-inputs`, but the driver posts values that fail conversion/validation (`name=forbidden` trips the CDI prohibited-words validator; non-numeric quantity/price trip `convertNumber`) so every run exercises `FacesMessage` creation, `UIInput` invalid-marking, the **skipped** Update Model/Invoke phases, redisplay of the rejected values and `h:messages` rendering with content
- `form-invalid-ajax` — ajax twin of `form-invalid`: the same rejected values, but submitted via `<f:ajax>`, so the unhappy path (message creation, invalid-marking, skipped Update Model/Invoke, redisplay of rejected values) runs through the **partial** response render rather than a full postback
- `dynamic-form-ajax` — the idiomatic **dynamic components** pattern: a request-scoped bean holds the container via `binding`, and an `f:event type="postAddToView"` listener (`DynamicFormBean#build`) **programmatically** builds `FIELD_COUNT` labelled inputs each time the view is (re)built, rather than declaring them in the facelet. The tree structure is identical every request, so the ajax postback runs the full lifecycle (decode/validate/update + partial render) over the dynamically-built inputs exactly like `form-inputs-ajax`. Unlike `dynamic-toggle-ajax` it does **not** touch the dynamic add/remove machinery (the components are built during the normal view (re)build, not after it) — it isolates the cost of `binding` resolution plus programmatic component construction, and is portable across implementations.
- `dynamic-toggle-ajax` — each ajax toggle adds or removes a `FIELD_COUNT`-input subtree under the in-view `container` via `getChildren().add()/clear()` in the action. Because the mutation happens **after** `markInitialState`, this is the scenario that drives Mojarra's dynamic add/remove path — `StateContext` dynamic-action tracking, the `DYNAMIC_COMPONENT` marker, and full-state-save/restore of the dynamic subtree — which no structurally-static scenario reaches. It is the one for benchmarking that path. On Mojarra the dynamic subtree is replayed on restore, so toggles alternate add↔remove; MyFaces re-adds it each request rather than persisting it across the postback.
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

This works for **Tomcat, WildFly and TomEE** (where `perf.jvmArguments` feeds the forked VM
directly). **OpenLiberty** has no such hook — put the same flags in
`wlp/usr/servers/defaultServer/jvm.options`. **GlassFish and Payara** can't be JFR-injected
through the harness at all: the `arquillian-glassfish-server-managed` adapter only exposes
`maxHeapSize`, with no arbitrary-JVM-args property — so profile on Tomcat or WildFly instead.

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
`PartialViewContextImpl`). Filter to the request worker threads (`http-nio-*-exec-*` on
Tomcat, `http-thread-pool-*` on GlassFish) to drop server-housekeeping samples.

### Gotchas

- Cancelling a bench mid-flight can leave an already-deployed app in `target/glassfish8/glassfish/domains/domain1/applications/`; the next run will then error with `Application with name perf-<version> is already registered`. Fix with `rm -rf target/glassfish8` followed by `mvn clean package -DskipTests`.
- A killed bench can also leave an orphan `ASMain` JVM running, which trips the next run with `The server is already running!`. Check `jps`, identify the specific PID, kill it by ID — never blanket `pkill java` on a shared host.

## Profiling a non-Mojarra layer (e.g. GlassFish, Weld)

Because the bench drives the *whole* request stack, the JFR profile picks up CPU and allocation from every layer — Mojarra, EL, Weld/CDI, Grizzly, and the JDK. To swap one of those layers for a local build:

1. Build the layer in its own checkout (e.g. `mvn clean install` in a GlassFish module) so a `<version>-SNAPSHOT` artifact lands in `~/.m2`.
2. Either re-run `mvn clean package -DskipTests -Dglassfish.version=<version>-SNAPSHOT` (forces a fresh GlassFish unpack with the SNAPSHOT zip), or — for a single-jar swap — replace the matching `target/glassfish8/glassfish/modules/<artifact>.jar` in place.
3. Enable JFR via `-Dperf.jvmArguments` (see above) and run the bench. Since that hook is unavailable on GlassFish/Payara, profile a server-layer swap on a JFR-injectable server (Tomcat or WildFly) where possible.

The bench couldn't care less which version of which jar is in the server's modules directory; whatever's on disk at startup is what gets profiled.
