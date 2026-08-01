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
import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openqa.selenium.WebDriver;

/**
 * Drives {@link BuildViewBenchServlet} so a JFR recording captures only the Facelets buildView loop
 * (no render, no state save/restore). Run on Mojarra and on MyFaces (via the {@code -*-myfaces}
 * profiles) and diff the recordings to see where Mojarra's buildView does more work.
 *
 * <p>Gated behind {@code -Dbuildview=true}. Iteration counts reuse {@code -Dperf.warmup}/{@code
 * -Dperf.runs} (defaults 50/2000); {@code -Dperf.scenarios=<comma-separated>} selects the views to
 * run in order (default composite-build), so one server start covers a whole sweep.
 * {@code -Dperf.split=true} additionally reports where one build's nanoseconds go, which is what
 * separates a fixed per-build cost from a per-component one.
 */
@EnabledIfSystemProperty(named = "buildview", matches = "true")
class BuildViewBenchIT extends BaseIT {

    private static final int WARMUP = getInteger("perf.warmup", 50);
    private static final int RUNS = getInteger("perf.runs", 2000);
    private static final boolean SPLIT = Boolean.getBoolean("perf.split");

    @Override
    public void setup() {
        // intentionally empty: browser is unused, the servlet drives buildView server-side
    }

    @Override
    public void teardown() {
        WebDriver driver = this.browser;
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void buildView() throws Exception {
        List<String> scenarios = Stream.of(System.getProperty("perf.scenarios", "composite-build").split(","))
                .map(String::trim)
                .filter(not(String::isEmpty))
                .toList();

        if (scenarios.isEmpty()) {
            scenarios = List.of("composite-build");
        }

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(ofSeconds(10))
                .build();

        System.out.println();

        for (String scenario : scenarios) {
            String url = baseURL + "buildview-bench?scenario=" + scenario + "&warmup=" + WARMUP + "&runs=" + RUNS
                    + "&split=" + SPLIT;
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(ofSeconds(600)).GET().build();
            HttpResponse<String> response = client.send(request, ofString(UTF_8));

            assertEquals(200, response.statusCode(), "buildview-bench " + scenario);
            System.out.print(response.body());
        }
    }
}
