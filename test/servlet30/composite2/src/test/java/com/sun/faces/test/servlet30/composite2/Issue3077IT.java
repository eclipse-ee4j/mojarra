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

package com.sun.faces.test.servlet30.composite2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import static org.junit.Assert.*;

public class Issue3077IT {

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
    public void testCCAndInclude() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/ccAndInclude.xhtml");
        String pageText = page.asXml();
        
        String beforeText = pageText.substring(0, pageText.indexOf("cc is evaluated in a xhtml file included by the composite component"));
        
        assertTrue(beforeText.indexOf("#{cc['clientId']} evaluates to: external:internal") != -1);
        assertTrue(beforeText.indexOf("#{cc.clientId} evaluates to: external:internal") != -1);
        assertTrue(beforeText.indexOf("#{cc['attrs'].param} evaluates to: Test string") != -1);
        assertTrue(beforeText.indexOf("#{cc.attrs.param} evaluates to: Test string") != -1);
        
        String afterText = pageText.substring(pageText.indexOf("cc is evaluated in a xhtml file included by the composite component"));
        
        assertTrue(afterText.indexOf("#{cc['clientId']} evaluates to: external:internal") != -1);
        assertTrue(afterText.indexOf("#{cc.clientId} evaluates to: external:internal") != -1);
        assertTrue(afterText.indexOf("#{cc['attrs'].param} evaluates to: Test string") != -1);
        assertTrue(afterText.indexOf("#{cc.attrs.param} evaluates to: Test string") != -1);
    }
}
