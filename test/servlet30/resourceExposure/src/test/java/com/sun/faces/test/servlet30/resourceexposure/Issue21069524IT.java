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

package com.sun.faces.test.servlet30.resourceexposure;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.TextPage;
import org.junit.After;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class Issue21069524IT {

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
    public void testBugDB21069524() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        TextPage page = webClient.getPage(webUrl + "javax.faces.resource/issue21069579.xhtml/.faces?ln=showcase");
        String pageXml = page.getContent();
        assertTrue(!pageXml.contains("<h:form"));
        assertTrue(!pageXml.contains("<h:body"));
        int status = page.getWebResponse().getStatusCode();
        assertNotEquals(200L, status);
    }
}
