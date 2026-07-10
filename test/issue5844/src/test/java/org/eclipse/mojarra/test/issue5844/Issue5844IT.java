/*
 * Copyright (c) Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GPL-2.0 with Classpath-exception-2.0 which
 * is available at https://openjdk.java.net/legal/gplv2+ce.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 or Apache-2.0
 */
package org.eclipse.mojarra.test.issue5844;

import static java.net.URI.create;
import static java.net.http.HttpRequest.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.eclipse.mojarra.test.issue5844.Issue5844Latch.REQUESTS_PER_WAVE;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.CookieManager;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;

class Issue5844IT extends BaseIT {

    private static final String WAVE_A = "wavea.xhtml";
    private static final String WAVE_B = "waveb.xhtml";
    private static final String PARAM_VALUE = "foo";
    private static final String EXPECTED_OUTPUT = "<span id=\"param\">" + PARAM_VALUE + "</span>";

    /**
     * When more requests are concurrently in flight within the same session than com.sun.faces.numberOfActiveViewMaps,
     * then the LRU eviction of the eldest view map must not destroy its beans while the request owning it has not
     * finished yet. That request keeps holding the very same view map, so when it were emptied, then it would during
     * render silently re-create its @ViewScoped bean and thereby lose the value which f:viewParam had put in it.
     * <p>
     * Wave A is in flight and holds its value. Wave B then registers its own view maps, which overflows the LRU map of
     * active view maps and evicts wave A. Wave A is released and renders. It must still hold its value.
     *
     * https://github.com/eclipse-ee4j/mojarra/issues/5844
     */
    @Test
    void concurrentRequestsMustNotEvictEachOthersViewMap() throws Exception {
        HttpClient client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build(); // Shares the JSESSIONID.
        getResponseBody(client, WAVE_A); // Creates the session. Deliberately without param, so it doesn't await.

        ExecutorService executor = newFixedThreadPool(2 * REQUESTS_PER_WAVE);

        try {
            List<Future<String>> waveA = submit(executor, client, WAVE_A + "?param=" + PARAM_VALUE);
            List<Future<String>> waveB = submit(executor, client, WAVE_B);

            for (Future<String> response : waveB) {
                response.get(); // Wave B only needs to register its view maps.
            }

            for (Future<String> response : waveA) {
                String body = response.get();
                assertTrue(body.contains(EXPECTED_OUTPUT), "@ViewScoped bean of wave A must still hold the view param, but rendered:\n" + body);
            }
        }
        finally {
            executor.shutdownNow();
        }
    }

    private List<Future<String>> submit(ExecutorService executor, HttpClient client, String resource) {
        List<Future<String>> responses = new ArrayList<>();

        for (int i = 0; i < REQUESTS_PER_WAVE; i++) {
            responses.add(executor.submit(() -> getResponseBody(client, resource)));
        }

        return responses;
    }

    private String getResponseBody(HttpClient client, String resource) throws Exception {
        return client.send(newBuilder(create(baseURL + resource)).build(), ofString()).body();
    }
}
