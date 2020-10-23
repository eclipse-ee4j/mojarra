/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.servlet30.requestcharencodingnosession;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class NoSessionCharEncIT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testCharEnc() throws Exception {

        HtmlPage page = webClient.getPage(webUrl + "faces/utf8.xhtml");

        // force creation of the session
        webClient.getPage(webUrl + "faces/utf8.xhtml?makeSession=true");

        // try again, this time make sure the session shows the encoding
        page = webClient.getPage(webUrl + "faces/utf8.xhtml");
        assertTrue("Incorrect encoding.  extContextCharEnc: UTF-8 hasSession: true sessionCharEnc: UTF-8\n"
                + "\nactual: " + page.asText(),
                page.asText().matches("(?s).*extContextCharEnc:.*UTF-8.*hasSession:.*true.*sessionCharEnc:.*UTF-8.*"));

        page = webClient.getPage(webUrl + "faces/ascii.xhtml");
        assertTrue("Incorrect encoding.  extContextCharEnc: UTF-8 hasSession: true sessionCharEnc: US-ASCII",
                page.asText().matches("(?s).*extContextCharEnc:.*US-ASCII.*hasSession:.*true.*sessionCharEnc:.*US-ASCII.*"));

        // force invalidation of the session
        webClient.getPage(webUrl + "faces/utf8.xhtml?invalidateSession=true");
    }
}
