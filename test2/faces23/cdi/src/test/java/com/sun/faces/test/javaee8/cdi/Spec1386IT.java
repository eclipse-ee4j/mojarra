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

package com.sun.faces.test.javaee8.cdi;

import static java.lang.System.getProperty;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
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
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Tests the availability of the flow map via injection
 *
 */
@RunWith(Arquillian.class)
public class Spec1386IT {

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
    public void testInjectFlowMap() throws Exception {
        // Start on initial (non-flow) view
        HtmlPage page = webClient.getPage(webUrl + "injectFlowMap.xhtml");

        // Enter main flow
        page = page.getHtmlElementById("form:enter").click();

        // Put value in flow scope map
        page = page.getHtmlElementById("form:init").click();

        // Navigate to next page in flow
        page = page.getHtmlElementById("form:next").click();

        // Value should be available from flow map now
        assertTrue(page.asXml().contains("foo:bar"));

        // Enter nested flow
        page = page.getHtmlElementById("form:nested").click();

        // Put (different) value in flow map using same key
        page = page.getHtmlElementById("form:init").click();

        // Navigate to next page in nested flow
        page = page.getHtmlElementById("form:next").click();

        // Different value should be available from flow map now
        assertTrue(page.asXml().contains("foo:barx"));

        // Exit nested flow
        page = page.getHtmlElementById("form:exit").click();

        // Original value should be available from flow map again
        assertTrue(page.asXml().contains("foo:bar"));
    }

}
