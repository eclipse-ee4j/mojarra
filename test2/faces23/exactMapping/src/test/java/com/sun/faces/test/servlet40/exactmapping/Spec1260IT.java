/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Contributors to Eclipse Foundation.
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

package com.sun.faces.test.servlet40.exactmapping;

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

@RunWith(Arquillian.class)
public class Spec1260IT {

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
    public void testExactMappedViewLoads() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "foo");
        String content = page.asXml();

        // Basic test that if the FacesServlet is mapped to /foo, the right view "foo.xhtml" is loaded.
        assertTrue(content.contains("This is page foo"));
    }

    @Test
    public void testPostBackToExactMappedView() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "foo");

        page = page.getHtmlElementById("form:commandButton").click();

        String content = page.asXml();

        assertTrue(content.contains("foo method invoked"));

        // If page /foo postbacks to itself, the new URL should be /foo again
        assertTrue(page.getUrl().getPath().endsWith("/foo"));
    }

    @Test
    public void testLinkToNonExactMappedView() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "foo");

        assertTrue(page.asXml().contains("This is page foo"));

        page = page.getHtmlElementById("form:button").click();

        String content = page.asXml();

        assertTrue(content.contains("This is page bar"));

        // view "bar" is not exact mapped, so should be loaded via the suffix
        // or prefix the FacesServlet is mapped to when coming from /foo

        String path = page.getUrl().getPath();

        assertTrue(path.endsWith("/bar.jsf") || path.endsWith("/faces/bar"));
    }

    @Test
    public void testPostBackOnLinkedNonExactMappedView() throws Exception {

        // Navigate from /foo to /bar.jsf
        HtmlPage page = webClient.getPage(webUrl + "foo");
        page = page.getHtmlElementById("form:button").click();

        // After navigating to a non-exact mapped view, a postback should stil work
        page = page.getHtmlElementById("form:commandButton").click();
        assertTrue(page.asXml().contains("foo method invoked"));

        // Check we're indeed on bar.jsf or faces/bar
        String path = page.getUrl().getPath();
        assertTrue(path.endsWith("/bar.jsf") || path.endsWith("/faces/bar"));
    }


    @Test
    public void testResourceReferenceFromExactMappedView() throws Exception {

        HtmlPage page = webClient.getPage(webUrl + "foo");

        String content = page.asXml();

        // Runtime must have found out the mappings of the FacesServlet and used one of the prefix or suffix
        // mappings to render the reference to "faces.js", which is not exactly mapped.
        assertTrue(content.contains("jakarta.faces.resource/faces.js.jsf") || content.contains("jakarta.faces.resource/faces/faces.js") );
    }

    @Test
    public void testAjaxFromExactMappedView() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "foo");

        page = page.getHtmlElementById("form:commandButtonAjax").click();
        webClient.waitForBackgroundJavaScript(6000);

        String content = page.asXml();

        // AJAX from an exact-mapped view should work
        assertTrue(content.contains("partial request = true"));

        // Part of page not updated via AJAX so should not show
        assertTrue(!content.contains("should not see this"));
    }



}
