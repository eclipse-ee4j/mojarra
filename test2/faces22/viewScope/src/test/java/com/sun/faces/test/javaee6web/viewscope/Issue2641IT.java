/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.faces.test.javaee6web.viewscope;

import static java.lang.System.getProperty;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

@RunWith(Arquillian.class)
public class Issue2641IT {

    @ArquillianResource
    private URL webUrl;
    private WebClient webClient;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return create(ZipImporter.class, getProperty("finalName") + ".war")
                .importFrom(new File("target/" + getProperty("finalName") + ".war"))
                .as(WebArchive.class);
    }

    @Before
    public void setUp() {
        webClient = new WebClient();
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testViewScope() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/viewScoped.xhtml");
        int previousCount = 0;
        int count = Integer.parseInt(page.getElementById("count").getTextContent());
        assertTrue(previousCount < count);
        previousCount = count;

        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("stay");
        page = button.click();
        count = Integer.parseInt(page.getElementById("count").getTextContent());
        assertEquals(previousCount, count);

        button = (HtmlSubmitInput) page.getElementById("stay");
        page = button.click();
        count = Integer.parseInt(page.getElementById("count").getTextContent());
        assertEquals(previousCount, count);

        button = (HtmlSubmitInput) page.getElementById("go");
        page = button.click();
        count = Integer.parseInt(page.getElementById("count").getTextContent());
        assertTrue(previousCount < count);
        previousCount = count;

        button = (HtmlSubmitInput) page.getElementById("stay");
        page = button.click();
        count = Integer.parseInt(page.getElementById("count").getTextContent());
        assertEquals(previousCount, count);

        button = (HtmlSubmitInput) page.getElementById("stay");
        page = button.click();
        count = Integer.parseInt(page.getElementById("count").getTextContent());
        assertEquals(previousCount, count);

        button = (HtmlSubmitInput) page.getElementById("go");
        page = button.click();
        count = Integer.parseInt(page.getElementById("count").getTextContent());
        assertTrue(previousCount < count);
        previousCount = count;

        button = (HtmlSubmitInput) page.getElementById("stay");
        page = button.click();
        count = Integer.parseInt(page.getElementById("count").getTextContent());
        assertEquals(previousCount, count);

        button = (HtmlSubmitInput) page.getElementById("stay");
        page = button.click();
        count = Integer.parseInt(page.getElementById("count").getTextContent());
        assertEquals(previousCount, count);
    }

    @Test
    public void testInvalidatedSession() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/invalidatedSession.xhtml");

        assertTrue(page.asXml().contains("This is from the @PostConstruct"));
        webClient.getPage(webUrl + "faces/invalidatedPerform.xhtml");
        page = webClient.getPage(webUrl + "faces/invalidatedVerify.xhtml");
        assertTrue(page.asXml().contains("true"));
    }

    @Test
    public void testViewScopedInput() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/viewScopedInput.xhtml");
        HtmlTextInput input = (HtmlTextInput) page.getElementById("input");
        String value = "" + System.currentTimeMillis();
        input.setValueAttribute(value);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("stay");
        page = button.click();
        DomElement output = page.getElementById("output");
        assertTrue(output.asNormalizedText().contains(value));
    }
}
