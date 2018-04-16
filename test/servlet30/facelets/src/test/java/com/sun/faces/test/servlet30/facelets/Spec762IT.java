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

package com.sun.faces.test.servlet30.facelets;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Spec762IT {

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
    public void testMetadataShortCircuit() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/metadata/metadataShortCircuit.xhtml");
        assertTrue(Pattern.matches("(?s).*beforePhase\\s+RESTORE_VIEW\\s+1\\s+beforePhase\\s+APPLY_REQUEST_VALUES\\s+2\\s+beforePhase\\s+PROCESS_VALIDATIONS\\s+3\\s+beforePhase\\s+UPDATE_MODEL_VALUES\\s+4\\s+beforePhase\\s+INVOKE_APPLICATION\\s+5\\s+beforePhase\\s+RENDER_RESPONSE\\s+6.*", page.asXml()));
    }
}
