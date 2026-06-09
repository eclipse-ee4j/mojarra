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

## Tuning state saving / view pooling

The state and view-pooling context parameters are filtered into the WAR's `web.xml`
at package time, so they can be tuned per run without editing anything. Defaults
match the implementation defaults; each knob sets the Mojarra parameter and, where
applicable, its MyFaces equivalent (the other implementation ignores the foreign one):

| property | default | Faces / Mojarra param | MyFaces equivalent |
|----------|---------|-----------------------|--------------------|
| `-Dwebapp.stateSavingMethod`     | `server` | `jakarta.faces.STATE_SAVING_METHOD`    | *(same)* |
| `-Dwebapp.serializeServerState`  | `false`  | `jakarta.faces.SERIALIZE_SERVER_STATE` | *(same)* |
| `-Dwebapp.compressViewState`     | `true`   | `com.sun.faces.compressViewState`      | `org.apache.myfaces.COMPRESS_STATE_IN_SESSION` |
| `-Dwebapp.numberOfViewsInSession`| `15`     | `com.sun.faces.numberOfLogicalViews`   | `org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION` |

```
mvn clean verify -Dperf=true -Dwebapp.stateSavingMethod=client -Dwebapp.numberOfViewsInSession=10
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
- `dynamic-form-ajax` — the idiomatic **dynamic components** pattern: a request-scoped bean holds the container via `binding`, and an `f:event type="postAddToView"` listener (`DynamicFormBean#build`) **programmatically** builds its 25 labelled inputs each time the view is (re)built, rather than declaring them in the facelet. The tree structure is identical every request, so the ajax postback runs the full lifecycle (decode/validate/update + partial render) over the dynamically-built inputs exactly like `form-inputs-ajax`. Unlike `dynamic-toggle-ajax` it does **not** touch the dynamic add/remove machinery (the components are built during the normal view (re)build, not after it) — it isolates the cost of `binding` resolution plus programmatic component construction, and is portable across implementations. The driver's ajax POST mirrors faces.js for a `commandButton` `f:ajax` (`jakarta.faces.behavior.event=action` plus the source clientId prepended to `partial.execute`) so the submit actually fires — a bare `@form` execute decodes the form but not the behavior.
- `dynamic-toggle-ajax` — each ajax toggle adds or removes a 25-input subtree under the in-view `container` via `getChildren().add()/clear()` in the action. Because the mutation happens **after** `markInitialState`, this is the scenario that drives Mojarra's dynamic add/remove path — `StateContext` dynamic-action tracking, the `DYNAMIC_COMPONENT` marker, and full-state-save/restore of the dynamic subtree — which no structurally-static scenario reaches. It is the one for benchmarking that path. The branch is chosen from the live tree (not a flag): on Mojarra the dynamic subtree is replayed on restore so toggles alternate add↔remove; MyFaces does not persist it across postback the same way and re-adds each request — both run cleanly, and the divergence is itself the cross-impl observation.
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
target/glassfish8/glassfish/domains/domain1/config/domain.xml
```

```xml
<jvm-options>-XX:StartFlightRecording=duration=900s,filename=/tmp/perf-bench.jfr,settings=profile,name=perf</jvm-options>
<jvm-options>-XX:FlightRecorderOptions=stackdepth=128</jvm-options>
```

(For the other servers the same JVM options go into their respective config rather
than `domain.xml`: Payara `payara7/glassfish/domains/domain1/config/domain.xml`,
WildFly `standalone.conf`, TomEE `bin/setenv.sh` / `-Dtomee.catalina_opts`, Tomcat
`bin/setenv.sh`, OpenLiberty `wlp/usr/servers/defaultServer/jvm.options`.)

Then run the bench at a tighter iteration count — JFR samples on a 20 ms cadence by default, so ~7,500 samples (≈150 s of bench time) is plenty for a clear hot-method picture without producing a huge file:

```
mvn failsafe:integration-test failsafe:verify -Dperf=true -Dperf.warmup=20 -Dperf.runs=200
```

Analyze with the JDK-bundled `jfr` tool:

```
jfr summary /tmp/perf-bench.jfr
jfr view --width 200 hot-methods /tmp/perf-bench.jfr
jfr view --width 200 allocation-by-class /tmp/perf-bench.jfr
jfr view --width 200 allocation-by-site /tmp/perf-bench.jfr
```

For deeper aggregation (e.g. grouping hot leaves by their Mojarra-side caller, filtering by thread, attributing samples to scenarios via the timeline) export the execution samples to JSON and process them with a small script:

```
jfr print --json --events jdk.ExecutionSample /tmp/perf-bench.jfr > /tmp/samples.json
```

Each event lives at `event['values']['stackTrace']['frames']`; class names use slash form (`com/sun/faces/...`).

### Gotchas

- `domain.xml` is regenerated by `mvn clean package`, so reapply the JFR `<jvm-options>` after every WAR rebuild.
- Cancelling a bench mid-flight can leave an already-deployed app in `target/glassfish8/glassfish/domains/domain1/applications/`; the next run will then error with `Application with name perf-<version> is already registered`. Fix with `rm -rf target/glassfish8` followed by `mvn clean package -DskipTests` (then reapply the JFR edit).
- A killed bench can also leave an orphan `ASMain` JVM running, which trips the next run with `The server is already running!`. Check `jps`, identify the specific PID, kill it by ID — never blanket `pkill java` on a shared host.

## Profiling a non-Mojarra layer (e.g. GlassFish, Weld)

Because the bench drives the *whole* request stack, the JFR profile picks up CPU and allocation from every layer — Mojarra, EL, Weld/CDI, Grizzly, and the JDK. To swap one of those layers for a local build:

1. Build the layer in its own checkout (e.g. `mvn clean install` in a GlassFish module) so a `<version>-SNAPSHOT` artifact lands in `~/.m2`.
2. Either re-run `mvn clean package -DskipTests -Dglassfish.version=<version>-SNAPSHOT` (forces a fresh GlassFish unpack with the SNAPSHOT zip), or — for a single-jar swap — replace the matching `target/glassfish8/glassfish/modules/<artifact>.jar` in place.
3. Reapply the JFR `<jvm-options>` and run the bench.

The bench couldn't care less which version of which jar is in the server's modules directory; whatever's on disk at startup is what gets profiled.
