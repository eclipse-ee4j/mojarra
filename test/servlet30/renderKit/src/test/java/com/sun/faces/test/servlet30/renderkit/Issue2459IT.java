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

package com.sun.faces.test.servlet30.renderkit;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Issue2459IT {

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
    public void testInputFileRender() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue2459.xhtml");
        HtmlForm form = (HtmlForm) page.getElementById("form1");
        String attrValue = form.getAttribute("foo");
        assertEquals("bar", attrValue);
        
        attrValue = form.getAttribute("foo");
        assertEquals("bar", attrValue);
        
        attrValue = form.getAttribute("accept");
        assertEquals("text/html", attrValue);
        
        
        attrValue = form.getAttribute("dir");
        assertEquals("LTR", attrValue);
        
        
        attrValue = form.getAttribute("enctype");
        assertEquals("noneDefault", attrValue);
        
        
        attrValue = form.getAttribute("lang");
        assertEquals("en", attrValue);
        
        
        attrValue = form.getAttribute("onclick");
        assertEquals("js1", attrValue);
        
        
        attrValue = form.getAttribute("ondblclick");
        assertEquals("js2", attrValue);
        
        
        attrValue = form.getAttribute("onkeydown");
        assertEquals("js3", attrValue);
        
        
        attrValue = form.getAttribute("onkeypress");
        assertEquals("js4", attrValue);
        
        
        attrValue = form.getAttribute("onkeyup");
        assertEquals("js5", attrValue);
        
        
        attrValue = form.getAttribute("onmousedown");
        assertEquals("js6", attrValue);
        
        
        attrValue = form.getAttribute("onmousemove");
        assertEquals("js7", attrValue);
        
        
        attrValue = form.getAttribute("onmouseout");
        assertEquals("js8", attrValue);
        

        attrValue = form.getAttribute("onmouseover");
        assertEquals("js9", attrValue);
        
        
        attrValue = form.getAttribute("onmouseup");
        assertEquals("js10", attrValue);
        
        
        attrValue = form.getAttribute("onreset");
        assertEquals("js11", attrValue);
        
        
        attrValue = form.getAttribute("onsubmit");
        assertEquals("js12", attrValue);
        
        
        attrValue = form.getAttribute("style");
        assertEquals("Color: red;", attrValue);
        
        
        attrValue = form.getAttribute("target");
        assertEquals("frame1", attrValue);
        
        
        attrValue = form.getAttribute("title");
        assertEquals("FormTitle", attrValue);
        
        
    }
}
