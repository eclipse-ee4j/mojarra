/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.glassfish.mojarra.test.impl.issue5746;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Locale.ROOT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v139.network.Network;
import org.openqa.selenium.devtools.v139.network.model.ResponseReceivedEarlyHints;

import ee.jakarta.tck.faces.util.selenium.BaseITNG;

/**
 * The head resources of a Facelets page must be announced as {@code Link: <url>;rel=preload;as=<destination>} headers, so the browser can use the preload for
 * the stylesheet or script request which follows. Over HTTP/2 they must be sent in a 103 interim response ahead of the final 200 response. Over HTTP/1.1 no 103
 * must be sent, because an HTTP/1.1 client or intermediary may take it for the final response (RFC 8297, section 3), but the final response must still carry
 * the headers. The specification does not mandate Early Hints: whether to send a 103 at all, which resources to advertise, and how, is left entirely to the
 * implementation.
 */
class Issue5746IT extends BaseITNG {

    private static final String VIEW = "issue5746.xhtml";
    private static final int DEFAULT_HTTPS_PORT = 8181;
    private static final Map<String, String> HEAD_RESOURCES = Map.of(
        "style1.css", "style",
        "style2.css", "style",
        "script1.js", "script",
        "script2.js", "script",
        "script3.js", "script"
    );

    /**
     * Over HTTP/1.1 the server must not send a 103 interim response, but the final 200 response must still preload every head resource with its destination.
     * Asserted at the wire level because {@link java.net.http.HttpClient} silently discards 1xx interim responses.
     */
    @Test
    void noEarlyHintsOverHttp1ButFinalResponsePreloadsHeadResources() throws IOException {
        String response = rawHttpGet(Map.of());

        assertFalse(response.contains("HTTP/1.1 103"), () -> "No 103 expected over HTTP/1.1, but got:\n" + response);
        assertTrue(response.startsWith("HTTP/1.1 200"), () -> "Expected a '200' final response, but got:\n" + response);

        List<String> linkHeaders = linkHeaders(response);
        HEAD_RESOURCES.forEach(
            (resource, destination) -> assertTrue(
                linkHeaders.stream().anyMatch(link -> link.contains(resource) && link.contains("rel=preload;as=" + destination)),
                () -> "The final response must preload " + resource + " as " + destination + ":\n" + response
            )
        );
    }

    /**
     * A conditional (revalidating) request already has the resources cached, so the server must skip early hints.
     *
     * @see org.glassfish.mojarra.context.ExternalContextImpl#addEarlyHintIfPossibleAndNecessary
     */
    @Test
    void earlyHintsAreSkippedOnConditionalRequest() throws IOException {
        String response = rawHttpGet(Map.of("If-Modified-Since", "Wed, 21 Oct 2099 07:28:00 GMT"));

        assertTrue(linkHeaders(response).isEmpty(), () -> "No Link headers expected on a conditional request, but got:\n" + response);
    }

    /**
     * Over HTTP/2 a real browser must receive a 103 interim response which preloads every head resource with its destination, captured via the Chrome DevTools
     * {@code Network.responseReceivedEarlyHints} event. Chrome only acts on Early Hints over HTTP/2, hence this navigates to the TLS (HTTP/2) listener instead
     * of the plaintext HTTP/1.1 one used by the wire checks above.
     */
    @Test
    void browserReceivesEarlyHintsWithDestinations() throws IOException {
        DevTools devTools = ((ChromeDriver) getWebDriver().getDelegate()).getDevTools();
        List<ResponseReceivedEarlyHints> received = new CopyOnWriteArrayList<>();
        devTools.addListener(Network.responseReceivedEarlyHints(), hints -> received.add(hints));

        getWebDriver().get("https://" + webUrl.getHost() + ":" + httpsPort() + viewPath());
        waitUntil(() -> !received.isEmpty());

        assertFalse(received.isEmpty(), "The browser should have received a 103 Early Hints response");
        List<String> links = received.stream()
            .flatMap(hints -> hints.getHeaders().entrySet().stream())
            .filter(header -> "link".equalsIgnoreCase(header.getKey()))
            .flatMap(header -> String.valueOf(header.getValue()).lines())
            .toList();
        HEAD_RESOURCES.forEach(
            (resource, destination) -> assertTrue(
                links.stream().anyMatch(link -> link.contains(resource) && link.contains("rel=preload;as=" + destination)),
                () -> "The 103 must preload " + resource + " as " + destination + ", but got: " + links
            )
        );
    }

    private static int httpsPort() throws IOException {
        String slot = System.getProperty("gf.pool.slot");

        if (slot == null) {
            return DEFAULT_HTTPS_PORT;
        }

        Properties ports = new Properties();

        try (InputStream input = Files.newInputStream(Path.of(System.getProperty("gf.pool.dir"), "slot-" + slot, "ports.properties"))) {
            ports.load(input);
        }

        return Integer.parseInt(ports.getProperty("glassfish.httpsPort"));
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

    private static List<String> linkHeaders(String response) {
        int headersEnd = response.indexOf("\r\n\r\n");
        return response.substring(0, headersEnd == -1 ? response.length() : headersEnd)
            .lines()
            .map(line -> line.toLowerCase(ROOT))
            .filter(line -> line.startsWith("link:"))
            .toList();
    }

    private static void waitUntil(BooleanSupplier condition) {
        long deadline = System.currentTimeMillis() + 5_000;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

}
