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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openqa.selenium.WebDriver;

/**
 * Drives {@link RenderResponseBenchServlet} so a JFR recording captures only the encode walk (no
 * buildView, no state save/restore). Run on Mojarra and on MyFaces (via the {@code -*-myfaces}
 * profiles) and diff the recordings to see where Mojarra's render does more work.
 *
 * <p>Gated behind {@code -Drender=true}. Iteration counts reuse {@code -Dperf.warmup}/{@code
 * -Dperf.runs} (defaults 50/2000); {@code -Dperf.scenarios=<one>} selects the view (default
 * composite-heavy).
 */
@EnabledIfSystemProperty(named = "render", matches = "true")
class RenderResponseBenchIT extends BaseIT {

    private static final int WARMUP = getInteger("perf.warmup", 50);
    private static final int RUNS = getInteger("perf.runs", 2000);

    @Override
    public void setup() {
        // intentionally empty: browser is unused, the servlet drives encode server-side
    }

    @Override
    public void teardown() {
        WebDriver driver = this.browser;
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void renderResponse() throws Exception {
        String scenario = System.getProperty("perf.scenarios", "composite-heavy").trim();
        if (scenario.isEmpty() || scenario.contains(",")) {
            scenario = "composite-heavy";
        }

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(ofSeconds(10))
                .build();

        String url = baseURL + "renderresponse-bench?scenario=" + scenario + "&warmup=" + WARMUP + "&runs=" + RUNS;
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(ofSeconds(600)).GET().build();
        HttpResponse<String> response = client.send(request, ofString(UTF_8));

        assertEquals(200, response.statusCode(), "renderresponse-bench");
        System.out.println();
        System.out.println(response.body());
    }
}
