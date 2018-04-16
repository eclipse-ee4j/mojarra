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

package com.sun.faces.test.servlet30.contractusinghostheader;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import org.junit.Ignore;

public class Issue2679IT {

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
    public void testDummy() {
    }

    public void testRelative() throws Exception {
        webClient.removeRequestHeader("Host");
        HtmlPage page = webClient.getPage(webUrl + "faces/foo/index.xhtml");
        HtmlElement body = page.getBody();

        // the first child is the text node
        DomNode firstChild = body.getChildren().iterator().next();
        String textContent = firstChild.getTextContent().trim();
        assertThat(textContent, is("foo/template.xhtml"));

        webClient.addRequestHeader("Host", "host5");
        page = webClient.getPage(webUrl + "faces/foo/index.xhtml");
        body = page.getBody();

        // the first child is the text node
        firstChild = body.getChildren().iterator().next();
        textContent = firstChild.getTextContent().trim();
        assertThat(textContent, is("host5: foo/template.xhtml"));
    }
}
