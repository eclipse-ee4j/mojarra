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

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import static junit.framework.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Issue2025IT {

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
    public void testCompositionBadPath() throws Exception {
        try { 
            webClient.getPage(webUrl + "faces/compositionBadPath.xhtml");        
        } catch (FailingHttpStatusCodeException e) {
            assertTrue(e.getResponse().getContentAsString().contains("Invalid path : foobar"));
        }
    }
    
    @Test
    public void testCompositionEmptyPath() throws Exception {
        try {
            webClient.getPage(webUrl + "faces/compositionEmptyPath.xhtml");
        } catch (FailingHttpStatusCodeException e) {
            assertTrue(e.getResponse().getContentAsString().contains("Invalid path :"));
        }
    }
    
    @Test
    public void testDecorateBadPath() throws Exception {
        try {
            webClient.getPage(webUrl + "faces/decorateBadPath.xhtml");
        } catch (FailingHttpStatusCodeException e) {
            assertTrue(e.getResponse().getContentAsString().contains("Invalid path : foobar"));
        }
    }
    
    @Test
    public void testDecorateEmptyPath() throws Exception {
        try {
            webClient.getPage(webUrl + "faces/decorateEmptyPath.xhtml");
        } catch (FailingHttpStatusCodeException e) {
            assertTrue(e.getResponse().getContentAsString().contains("Invalid path :"));
        }        
    }
}
