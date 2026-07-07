/*
 * Copyright (c) Contributors to Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package org.eclipse.mojarra.test.perf;

import static java.lang.Integer.getInteger;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openqa.selenium.WebDriver;

/**
 * Drives every scenario page through GET and (where applicable) POST cycles,
 * lets the {@link PhaseTimingListener} accumulate per-phase timings server-side,
 * then dumps the table from {@code /perf-stats} to stdout and to
 * {@code target/perf-stats-<timestamp>.txt}.
 *
 * <p>Iteration counts are tunable:
 * <ul>
 *   <li>{@code -Dperf.warmup=N} (default 50)</li>
 *   <li>{@code -Dperf.runs=N}   (default 1000)</li>
 * </ul>
 *
 * <p>Gated behind {@code -Dperf=true} so a normal {@code mvn install} does not run it.
 */
@EnabledIfSystemProperty(named = "perf", matches = "true")
class PerfBenchIT extends BaseIT {

    private static final int WARMUP = getInteger("perf.warmup", 50);
    private static final int RUNS = getInteger("perf.runs", 1000);

    /** Skip the BaseIT ChromeDriver bootstrap — this bench drives the server with HttpClient. */
    @Override
    public void setup() {
        // intentionally empty: browser is unused
    }

    /** Pair with overridden {@link #setup()}: the parent's teardown would NPE on a null driver. */
    @Override
    public void teardown() {
        WebDriver driver = this.browser;
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Optional scenario filter for diagnostics: {@code -Dperf.scenarios=a,b,c} restricts the run to
     * the named scenarios (empty = all). Lets you isolate e.g. large-output renders from the rest.
     */
    private static final Set<String> ONLY = parseScenarioFilter();

    private static Set<String> parseScenarioFilter() {
        String value = System.getProperty("perf.scenarios", "").trim();
        if (value.isEmpty()) {
            return Set.of();
        }
        Set<String> only = new LinkedHashSet<>();
        for (String name : value.split(",")) {
            if (!name.isBlank()) {
                only.add(name.trim());
            }
        }
        return only;
    }

    private static Map<String, String> only(Map<String, String> full) {
        if (ONLY.isEmpty()) {
            return full;
        }
        Map<String, String> filtered = new LinkedHashMap<>();
        full.forEach((scenario, url) -> {
            if (ONLY.contains(scenario)) {
                filtered.put(scenario, url);
            }
        });
        return filtered;
    }

    private static List<String> only(List<String> full) {
        return ONLY.isEmpty() ? full : full.stream().filter(ONLY::contains).toList();
    }

    /** Plain GETs. Most fire RESTORE_VIEW + RENDER_RESPONSE; {@code viewparam-get} fires all 6. */
    private static final Map<String, String> GET_ONLY = only(Map.ofEntries(
            Map.entry("index", "index.xhtml"),
            Map.entry("table-readonly", "table-readonly.xhtml"),
            Map.entry("repeat-readonly", "repeat-readonly.xhtml"),
            Map.entry("composite-readonly", "composite-readonly.xhtml"),
            Map.entry("foreach-readonly", "foreach-readonly.xhtml"),
            Map.entry("viewparam-get", "viewparam-get.xhtml?id=42")));

    /** Full (non-ajax) form postbacks. The {@code *-build} scenarios are readonly (no input fields), so their
     *  postback isolates state restore + encode from any input-processing cost. */
    private static final List<String> POSTBACK = only(List.of(
            "form-inputs",
            "form-invalid",
            "table-inputs",
            "repeat-inputs",
            "composite-inputs",
            "foreach-inputs",
            "table-nested",
            "repeat-nested",
            "composite-nested",
            "foreach-nested",
            "table-build",
            "repeat-build",
            "composite-build",
            "foreach-build"));

    /** Ajax-partial postbacks. Same body fields as their non-ajax twin plus the
     *  {@code jakarta.faces.partial.*} markers and the {@code Faces-Request} header. */
    private static final List<String> POSTBACK_AJAX = only(List.of(
            "form-inputs-ajax",
            "form-invalid-ajax",
            "table-inputs-ajax",
            "repeat-inputs-ajax",
            "composite-inputs-ajax",
            "foreach-inputs-ajax",
            "table-nested-ajax",
            "repeat-nested-ajax",
            "composite-nested-ajax",
            "foreach-nested-ajax",
            "dynamic-form-ajax",
            "dynamic-toggle-ajax"));

    private static final Pattern VIEW_STATE = Pattern.compile(
            "name=\"jakarta\\.faces\\.ViewState\"[^>]*value=\"([^\"]+)\"" +
            "|value=\"([^\"]+)\"[^>]*name=\"jakarta\\.faces\\.ViewState\"");
    private static final Pattern AJAX_VIEW_STATE = Pattern.compile(
            "<update\\s+id=\"[^\"]*ViewState[^\"]*\"><!\\[CDATA\\[(.*?)\\]\\]></update>", Pattern.DOTALL);
    private static final Pattern INPUT_TAG = Pattern.compile("<input\\b([^>]*)/?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern TEXTAREA_TAG = Pattern.compile("<textarea\\b([^>]*)>(.*?)</textarea>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SELECT_TAG = Pattern.compile("<select\\b([^>]*)>(.*?)</select>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern OPTION_TAG = Pattern.compile("<option\\b([^>]*)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern ATTR = Pattern.compile("\\b(\\w+)\\s*=\\s*\"([^\"]*)\"");
    private static final Pattern FORM_ID = Pattern.compile("<form\\b[^>]*\\bid=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    /**
     * The rendered ajax-behavior call, impl-agnostic: Mojarra emits {@code mojarra.ab(this,event,<event>,<execute>,
     * <render>)} and MyFaces {@code myfaces.ab(this,event,<event>,<execute>,<render>,{})}. Same arg positions; an
     * arg is a quoted string, {@code 0} (Mojarra "no execute") or {@code ''} (MyFaces "no execute").
     */
    private static final Pattern AJAX_AB = Pattern.compile(
            "(?:mojarra|myfaces)\\.ab\\(this,\\s*event,\\s*('[^']*'|0)\\s*,\\s*('[^']*'|0)\\s*,\\s*('[^']*'|0)\\s*[,)]");

    private static int totalScenarios() {
        return GET_ONLY.size() + POSTBACK.size() + POSTBACK_AJAX.size();
    }

    @Test
    void runBenchmark() throws Exception {
        // Pin HTTP/1.1: some servers (e.g. OpenLiberty) negotiate HTTP/2 (h2c) on the bench endpoint and
        // would otherwise send a GOAWAY to the default HTTP/2 client. The bench measures server-side Faces
        // phase timings, so the wire protocol is immaterial; forcing 1.1 keeps every server on equal footing.
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(new CookieManager())
                .connectTimeout(ofSeconds(10))
                .build();

        // bootstrap each postback scenario once (GET → extract ViewState + form fields)
        Map<String, FormSpec> forms = new LinkedHashMap<>();
        for (String scenario : POSTBACK) {
            String html = get(client, scenario + ".xhtml");
            forms.put(scenario, parseForm(html, scenario + ".xhtml", false));
        }
        Map<String, FormSpec> ajaxForms = new LinkedHashMap<>();
        for (String scenario : POSTBACK_AJAX) {
            String html = get(client, scenario + ".xhtml");
            ajaxForms.put(scenario, parseForm(html, scenario + ".xhtml", true));
        }

        // The unhappy path, for both the full-postback and ajax invalid forms (see injectInvalidValues).
        injectInvalidValues(forms.get("form-invalid"));
        injectInvalidValues(ajaxForms.get("form-invalid-ajax"));

        get(client, "perf-stats?reset=1");

        // warmup: lets JIT settle and primes any caches the perf branches add
        for (int i = 0; i < WARMUP; i++) {
            for (String url : GET_ONLY.values()) {
                get(client, url);
            }
            for (FormSpec form : forms.values()) {
                postAndRefresh(client, form);
            }
            for (FormSpec form : ajaxForms.values()) {
                ajaxPostAndRefresh(client, form);
            }
        }

        get(client, "perf-stats?reset=1");

        long startWall = System.nanoTime();
        // Block-wise measurement: run each scenario RUNS times back-to-back rather than round-robin, so its
        // code+data stay cache-resident within its block. This isolates intrinsic per-phase cost from the
        // cross-scenario cache interleaving. Warmup above stays round-robin so shared lifecycle code is still compiled against
        // every scenario's types (realistic mixed JIT state). Each form's ViewState went stale during the prior
        // block, so re-GET a live ViewState at the start of each block before posting (never submit an expired one).
        for (String url : GET_ONLY.values()) {
            for (int i = 0; i < RUNS; i++) {
                get(client, url);
            }
        }
        for (Map.Entry<String, FormSpec> entry : forms.entrySet()) {
            refreshViewState(client, entry.getKey(), entry.getValue());
            for (int i = 0; i < RUNS; i++) {
                postAndRefresh(client, entry.getValue());
            }
        }
        for (Map.Entry<String, FormSpec> entry : ajaxForms.entrySet()) {
            refreshViewState(client, entry.getKey(), entry.getValue());
            for (int i = 0; i < RUNS; i++) {
                ajaxPostAndRefresh(client, entry.getValue());
            }
        }
        long elapsedMs = (System.nanoTime() - startWall) / 1_000_000L;

        String stats = get(client, "perf-stats");
        String header = String.format(
                "# warmup=%d runs=%d scenarios=%d elapsed=%d ms%n",
                WARMUP, RUNS, totalScenarios(), elapsedMs);
        String report = header + stats;
        System.out.println();
        System.out.println(report);

        Path out = Path.of("target", "perf-stats-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".txt");
        Files.createDirectories(out.getParent());
        Files.writeString(out, report);
        System.out.println("Wrote " + out.toAbsolutePath());

        // Sanity: every scenario should have at least RUNS RENDER_RESPONSE samples.
        int minimumExpected = RUNS;
        for (String scenario : GET_ONLY.keySet()) {
            assertTrue(scenarioCount(stats, scenario, "RENDER_RESPONSE") >= minimumExpected,
                    scenario + " RENDER_RESPONSE count below " + minimumExpected);
        }
        for (String scenario : POSTBACK) {
            assertTrue(scenarioCount(stats, scenario, "RENDER_RESPONSE") >= minimumExpected,
                    scenario + " RENDER_RESPONSE count below " + minimumExpected);
        }
        for (String scenario : POSTBACK_AJAX) {
            assertTrue(scenarioCount(stats, scenario, "RENDER_RESPONSE") >= minimumExpected,
                    scenario + " RENDER_RESPONSE count below " + minimumExpected);
        }
    }

    /**
     * The "unhappy path": replace the happy-path field values with ones that fail conversion/validation, so every run
     * exercises FacesMessage creation, UIInput invalid-marking, the skipped UPDATE_MODEL/INVOKE phases, redisplay of
     * the submitted (rejected) value and h:messages rendering with content. "forbidden" trips the CDI prohibited-words
     * validator; the non-numeric quantity/price trip convertNumber. Injected once; reposted verbatim each run.
     */
    private static void injectInvalidValues(FormSpec form) {
        if (form != null) {
            form.fields.replaceAll((name, value) ->
                      name.endsWith(":name")     ? "forbidden"
                    : name.endsWith(":quantity") ? "not-a-number"
                    : name.endsWith(":price")    ? "xyz"
                    : value);
        }
    }

    private static long scenarioCount(String stats, String scenario, String phase) {
        Pattern p = Pattern.compile("^" + Pattern.quote(scenario) + "\\s+" + Pattern.quote(phase) + "\\s+(\\d+)", Pattern.MULTILINE);
        Matcher m = p.matcher(stats);
        return m.find() ? Long.parseLong(m.group(1)) : 0L;
    }

    private void postAndRefresh(HttpClient client, FormSpec form) throws IOException, InterruptedException {
        String body = post(client, form);
        Matcher vs = VIEW_STATE.matcher(body);
        if (vs.find()) {
            form.fields.put("jakarta.faces.ViewState", vs.group(1) != null ? vs.group(1) : vs.group(2));
        }
    }

    /**
     * Re-GET the scenario page to install a live ViewState into the form: in the block-wise measurement loop a
     * form's stored ViewState goes stale (and, under server-side state saving, gets evicted) while the previous
     * scenario's block runs, so the block's first post would otherwise submit an expired ViewState. Only the
     * ViewState is replaced; the form's input values (including injected invalids) are left intact.
     */
    private void refreshViewState(HttpClient client, String scenario, FormSpec form) throws IOException, InterruptedException {
        String html = get(client, scenario + ".xhtml");
        Matcher vs = VIEW_STATE.matcher(html);
        if (vs.find()) {
            form.fields.put("jakarta.faces.ViewState", vs.group(1) != null ? vs.group(1) : vs.group(2));
        }
    }

    private void ajaxPostAndRefresh(HttpClient client, FormSpec form) throws IOException, InterruptedException {
        String responseBody = ajaxPost(client, form);
        Matcher vs = AJAX_VIEW_STATE.matcher(responseBody);
        if (vs.find()) {
            form.fields.put("jakarta.faces.ViewState", vs.group(1));
        }
    }

    private String get(HttpClient client, String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseURL + path)).GET().build();
        HttpResponse<String> response = client.send(request, ofString(UTF_8));
        assertEquals(200, response.statusCode(), "GET " + path);
        return response.body();
    }

    private String post(HttpClient client, FormSpec form) throws IOException, InterruptedException {
        String body = encodeForm(form.fields);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseURL + form.action))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(BodyPublishers.ofString(body, UTF_8))
                .build();
        HttpResponse<String> response = client.send(request, ofString(UTF_8));
        assertEquals(200, response.statusCode(), "POST " + form.action);
        return response.body();
    }

    private String ajaxPost(HttpClient client, FormSpec form) throws IOException, InterruptedException {
        String body = encodeForm(form.fields);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseURL + form.action))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("Faces-Request", "partial/ajax")
                .header("Accept", "application/xml")
                .POST(BodyPublishers.ofString(body, UTF_8))
                .build();
        HttpResponse<String> response = client.send(request, ofString(UTF_8));
        assertEquals(200, response.statusCode(), "AJAX " + form.action);
        return response.body();
    }

    private static String encodeForm(Map<String, String> fields) {
        StringBuilder sb = new StringBuilder(fields.size() * 16);
        boolean first = true;
        for (Map.Entry<String, String> e : fields.entrySet()) {
            if (!first) {
                sb.append('&');
            }
            first = false;
            sb.append(URLEncoder.encode(e.getKey(), UTF_8));
            sb.append('=');
            sb.append(URLEncoder.encode(e.getValue() == null ? "" : e.getValue(), UTF_8));
        }
        return sb.toString();
    }

    /**
     * Pulls every named &lt;input&gt; tag (hidden, text, submit, etc.), &lt;textarea&gt;
     * (its text content) and &lt;select&gt; (its selected, else first, option) plus the
     * jakarta.faces.ViewState marker. Good enough for the deterministic markup
     * Mojarra emits for our perf pages.
     *
     * <p>When {@code ajax} is true, the submitted "button" is replaced by the
     * {@code jakarta.faces.partial.*} markers that {@code faces.js} would
     * normally add to an ajax POST body — so an ajax request gets the same
     * input population as a full postback would.
     */
    private static FormSpec parseForm(String html, String action, boolean ajax) {
        Map<String, String> fields = new LinkedHashMap<>();
        Matcher m = INPUT_TAG.matcher(html);
        boolean submitSeen = false;
        String submitName = null;
        String submitAttrs = null;
        while (m.find()) {
            String attrs = m.group(1);
            String name = attribute(attrs, "name");
            if (name == null) {
                continue;
            }
            String type = attribute(attrs, "type");
            String value = attribute(attrs, "value");
            if ("checkbox".equalsIgnoreCase(type) || "radio".equalsIgnoreCase(type)) {
                if (attrs.contains("checked")) {
                    fields.put(name, value == null ? "on" : value);
                }
                continue;
            }
            if ("submit".equalsIgnoreCase(type)) {
                if (!submitSeen) {
                    fields.put(name, value == null ? "" : value);
                    submitSeen = true;
                    submitName = name;
                    submitAttrs = attrs;
                }
                continue;
            }
            fields.put(name, value == null ? "" : value);
        }
        for (Matcher ta = TEXTAREA_TAG.matcher(html); ta.find(); ) {
            String name = attribute(ta.group(1), "name");
            if (name != null) {
                fields.put(name, ta.group(2).trim());
            }
        }
        for (Matcher se = SELECT_TAG.matcher(html); se.find(); ) {
            String name = attribute(se.group(1), "name");
            if (name != null) {
                fields.put(name, selectedOptionValue(se.group(2)));
            }
        }
        Matcher vs = VIEW_STATE.matcher(html);
        if (vs.find()) {
            String v = vs.group(1) != null ? vs.group(1) : vs.group(2);
            fields.put("jakarta.faces.ViewState", v);
        }
        if (ajax && submitName != null) {
            // Faces partial-ajax markers — exactly what faces.js emits for this commandButton's f:ajax submit.
            // The markers (execute/render targets, behavior event) are read from the rendered (mojarra|myfaces).ab(...)
            // call so each scenario drives the real execute/render its view declares, on either impl. CRUCIAL:
            // resolve @form/@this to concrete client ids the way faces.js does — the server only treats @all as a
            // keyword, so a literal @form would findComponent("@form") -> miss -> process/render nothing (silently).
            // Named targets are already absolute in the rendered call (Faces resolves them at render time).
            String eventName = "action";
            String executeRaw = "@form";
            String renderRaw = "@form";
            String onclick = submitAttrs == null ? null : attribute(submitAttrs, "onclick");
            if (onclick != null) {
                Matcher ab = AJAX_AB.matcher(onclick);
                if (ab.find()) {
                    eventName = unquote(ab.group(1));
                    executeRaw = ab.group(2);
                    renderRaw = ab.group(3);
                }
            }
            Matcher fm = FORM_ID.matcher(html);
            String formId = fm.find() ? fm.group(1) : null;
            String execute = resolveAjaxTargets(executeRaw, submitName, formId);
            // faces.js always executes the source so its behavior decodes; ensure it is present.
            if (execute.isEmpty()) {
                execute = submitName;
            } else if (!(" " + execute + " ").contains(" " + submitName + " ")) {
                execute = submitName + " " + execute;
            }
            fields.put("jakarta.faces.partial.ajax", "true");
            fields.put("jakarta.faces.source", submitName);
            fields.put("jakarta.faces.behavior.event", eventName);
            fields.put("jakarta.faces.partial.event", "click");
            fields.put("jakarta.faces.partial.execute", execute);
            fields.put("jakarta.faces.partial.render", resolveAjaxTargets(renderRaw, submitName, formId));
        }
        return new FormSpec(action, fields);
    }

    /**
     * Resolve a faces.js execute/render argument to the concrete client ids the server expects, mirroring
     * faces.js: {@code @form}/{@code @this} become the form/source client id, {@code @all}/{@code @none} stay
     * keywords, {@code 0} (mojarra.ab's "default, i.e. @this only") yields nothing extra, and any other token is
     * already an absolute client id (Faces resolves named targets at render time). Returns a space-separated list.
     */
    private static String resolveAjaxTargets(String raw, String source, String formId) {
        String value = unquote(raw);
        if (value.isEmpty() || "0".equals(value)) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (String token : value.split("\\s+")) {
            String resolved = switch (token) {
                case "@form" -> formId;
                case "@this" -> source;
                default -> token; // @all/@none keyword, or an already-absolute client id
            };
            if (resolved != null && !resolved.isEmpty()) {
                if (out.length() > 0) {
                    out.append(' ');
                }
                out.append(resolved);
            }
        }
        return out.toString();
    }

    private static String unquote(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("'") && trimmed.endsWith("'")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static String attribute(String attrs, String name) {
        Matcher m = ATTR.matcher(attrs);
        while (m.find()) {
            if (m.group(1).equalsIgnoreCase(name)) {
                return m.group(2);
            }
        }
        return null;
    }

    /** Value a {@code <select>} submits: the selected option, else the first option (browser default), else empty. */
    private static String selectedOptionValue(String optionsHtml) {
        String first = null;
        for (Matcher opt = OPTION_TAG.matcher(optionsHtml); opt.find(); ) {
            String attrs = opt.group(1);
            String value = attribute(attrs, "value");
            if (first == null) {
                first = value;
            }
            if (attrs.toLowerCase().contains("selected")) {
                return value == null ? "" : value;
            }
        }
        return first == null ? "" : first;
    }

    private static final class FormSpec {
        final String action;
        final Map<String, String> fields;
        FormSpec(String action, Map<String, String> fields) {
            this.action = action;
            this.fields = fields;
        }
    }
}
