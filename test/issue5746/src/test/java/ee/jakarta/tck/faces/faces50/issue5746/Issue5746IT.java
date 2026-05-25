/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package ee.jakarta.tck.faces.faces50.issue5746;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Locale.ROOT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v139.network.Network;
import org.openqa.selenium.devtools.v139.network.model.ResponseReceivedEarlyHints;

import ee.jakarta.tck.faces.util.selenium.BaseITNG;

/**
 * Verifies the HTTP 103 (Early Hints) support: the head resources of a Facelets page must be announced as
 * {@code Link: <url>;rel=preload} headers in a 103 interim response sent before the final 200 response.
 * <p>
 * This is a Mojarra-specific integration test and deliberately does <em>not</em> live in the Jakarta Faces TCK: the
 * specification does not mandate Early Hints. Whether to send a 103 at all, which resources to advertise, and how, is
 * left entirely to the implementation &mdash; implementors are free to choose their own early-hints approach (or none).
 * Being implementation-specific rather than spec-mandated behaviour, it is not TCK-testable. The sources nonetheless
 * follow the TCK's package and naming conventions ({@code ee.jakarta.tck.faces.faces50.issue5746}) so they could be
 * dropped into the TCK unchanged should the spec ever standardise this.
 *
 * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/5746">Mojarra issue 5746</a>
 */
class Issue5746IT extends BaseITNG {

    private static final String VIEW = "issue5746.xhtml";
    private static final int HTTPS_PORT = 8181;
    private static final List<String> HEAD_RESOURCES = List.of("style1.css", "style2.css", "script1.js", "script2.js", "script3.js");

    /**
     * On a regular (non-conditional) request, the server must emit a 103 interim response that preloads every head
     * resource, ahead of the final 200 response. Asserted at the wire level because {@link java.net.http.HttpClient}
     * silently discards 1xx interim responses.
     */
    @Test
    void earlyHintsArePreloadedBeforeFinalResponse() throws IOException {
        String response = rawHttpGet(Map.of());

        int interimStatus = response.indexOf("HTTP/1.1 103");
        int finalStatus = response.indexOf("HTTP/1.1 200");
        assertTrue(interimStatus >= 0, () -> "Expected a '103 Early Hints' interim response, but got:\n" + response);
        assertTrue(finalStatus > interimStatus, () -> "The '200' final response must come after the '103' interim response:\n" + response);

        String interim = response.substring(interimStatus, finalStatus).toLowerCase(ROOT);
        assertTrue(interim.contains("link:"), () -> "The 103 block must carry Link headers:\n" + response);
        assertTrue(interim.contains("rel=preload"), () -> "The Link headers must use rel=preload:\n" + response);
        for (String resource : HEAD_RESOURCES) {
            assertTrue(interim.contains(resource), () -> "The 103 block must preload " + resource + ":\n" + response);
        }
    }

    /**
     * A conditional (revalidating) request already has the resources cached, so the server must skip early hints.
     *
     * @see org.glassfish.mojarra.context.ExternalContextImpl#addEarlyHintIfPossibleAndNecessary
     */
    @Test
    void earlyHintsAreSkippedOnConditionalRequest() throws IOException {
        String response = rawHttpGet(Map.of("If-Modified-Since", "Wed, 21 Oct 2099 07:28:00 GMT"));

        assertFalse(response.contains("HTTP/1.1 103"), () -> "No 103 expected on a conditional request, but got:\n" + response);
    }

    /**
     * Verifies that a real browser actually receives the 103 interim response, captured via the Chrome DevTools
     * {@code Network.responseReceivedEarlyHints} event. Chrome only acts on Early Hints over HTTP/2, hence this
     * navigates to the TLS (HTTP/2) listener instead of the plaintext HTTP/1.1 one used by the wire checks above.
     */
    @Test
    void browserReceivesEarlyHints() {
        DevTools devTools = ((ChromeDriver) getWebDriver().getDelegate()).getDevTools();
        List<ResponseReceivedEarlyHints> received = new CopyOnWriteArrayList<>();
        devTools.addListener(Network.responseReceivedEarlyHints(), hints -> received.add(hints));

        getWebDriver().get("https://" + webUrl.getHost() + ":" + HTTPS_PORT + viewPath());
        waitUntil(() -> !received.isEmpty());

        assertFalse(received.isEmpty(), "The browser should have received a 103 Early Hints response");
        boolean preloadsResources = received.stream()
                .flatMap(hints -> hints.getHeaders().entrySet().stream())
                .anyMatch(header -> "link".equalsIgnoreCase(header.getKey()) && String.valueOf(header.getValue()).contains("rel=preload"));
        assertTrue(preloadsResources, "The early hints received by the browser must contain Link rel=preload headers");
    }

    private String viewPath() {
        String base = webUrl.getPath();
        return base + (base.endsWith("/") ? "" : "/") + VIEW;
    }

    private String rawHttpGet(Map<String, String> headers) throws IOException {
        String host = webUrl.getHost();
        int port = webUrl.getPort() != -1 ? webUrl.getPort() : webUrl.getDefaultPort();

        StringBuilder request = new StringBuilder();
        request.append("GET ").append(viewPath()).append(" HTTP/1.1\r\n");
        request.append("Host: ").append(host).append(':').append(port).append("\r\n");
        headers.forEach((name, value) -> request.append(name).append(": ").append(value).append("\r\n"));
        request.append("Connection: close\r\n\r\n");

        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(15_000);
            socket.getOutputStream().write(request.toString().getBytes(US_ASCII));
            socket.getOutputStream().flush();
            return new String(socket.getInputStream().readAllBytes(), ISO_8859_1);
        }
    }

    private static void waitUntil(BooleanSupplier condition) {
        long deadline = System.currentTimeMillis() + 5_000;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
