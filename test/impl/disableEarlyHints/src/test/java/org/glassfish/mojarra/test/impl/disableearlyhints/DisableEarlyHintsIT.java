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
package org.glassfish.mojarra.test.impl.disableearlyhints;

import static java.net.http.HttpResponse.BodyHandlers.discarding;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import ee.jakarta.tck.faces.util.selenium.BaseITNG;

/**
 * With {@code org.glassfish.mojarra.disableEarlyHints} set to {@code true}, the head resources of a Facelets page must not be announced as {@code Link} preload
 * headers. Without them there is nothing to send in a 103 interim response either.
 */
class DisableEarlyHintsIT extends BaseITNG {

    private static final String VIEW = "disableEarlyHints.xhtml";
    private static final int OK = 200;
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    @Test
    void finalResponseCarriesNoLinkHeaders() throws IOException, InterruptedException {
        HttpResponse<Void> response = HTTP.send(HttpRequest.newBuilder(URI.create(webUrl + VIEW)).build(), discarding());

        assertEquals(OK, response.statusCode());
        assertTrue(response.headers().allValues("Link").isEmpty(), () -> "No Link headers expected, but got: " + response.headers().allValues("Link"));
    }

}
