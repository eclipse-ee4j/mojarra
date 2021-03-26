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

package com.sun.faces.test.servlet40.getviews;

import static java.lang.System.getProperty;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertFalse;
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
public class Spec1435IT {

    @ArquillianResource
    private URL webUrl;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return create(ZipImporter.class, getProperty("finalName") + ".war")
                .importFrom(new File("target/" + getProperty("finalName") + ".war"))
                .as(WebArchive.class);
    }

    private WebClient webClient;

    @Before
    public void setUp() {
        webClient = new WebClient();
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    // ### ViewHandler based tests

    @Test
    public void testGetAllViews() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViews.jsf");
        String content = page.asXml();

        assertTrue(content.contains("/getViews.xhtml"));
        assertTrue(content.contains("view: /foo.xhtml")); // include marker since is also subset of "/level2/foo.xhtml" etc
        assertTrue(content.contains("/level2/bar.xhtml"));
        assertTrue(content.contains("/level2/foo.xhtml"));
        assertTrue(content.contains("/level2/level3/foo.xhtml"));
        assertTrue(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
        assertFalse(content.contains("include.xtml"));
    }

    @Test
    public void testGetViewsForPath() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViews.jsf?path=%2Flevel2%2F");
        String content = page.asXml();

        assertFalse(content.contains("/getViews.xhtml"));
        assertFalse(content.contains("view: /foo.xhtml"));

        assertTrue(content.contains("/level2/bar.xhtml"));
        assertTrue(content.contains("/level2/foo.xhtml"));
        assertTrue(content.contains("/level2/level3/foo.xhtml"));
        assertTrue(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
        assertFalse(content.contains("include.xtml"));
    }

    @Test
    public void testGetAllViewsAsImplicit() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViews.jsf?implicit=true");
        String content = page.asXml();

        assertTrue(content.contains("/getViews"));
        assertTrue(content.contains("view: /foo"));
        assertTrue(content.contains("/level2/bar"));
        assertTrue(content.contains("/level2/foo"));
        assertTrue(content.contains("/level2/level3/foo"));
        assertTrue(content.contains("/level2/level3/level4/foo"));

        assertFalse(content.contains("/getViews.xhtml"));
        assertFalse(content.contains("view: /foo.xhtml"));
        assertFalse(content.contains("/level2/bar.xhtml"));
        assertFalse(content.contains("/level2/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
        assertFalse(content.contains("include.xtml"));

        assertFalse(content.contains("/some_file"));
        assertFalse(content.contains("include"));
    }

    @Test
    public void testGetAllViewsWithLimit2() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViews.jsf?maxDepth=2");
        String content = page.asXml();

        assertTrue(content.contains("/getViews.xhtml"));
        assertTrue(content.contains("view: /foo.xhtml")); // include marker since is also subset of "/level2/foo.xhtml" etc
        assertTrue(content.contains("/level2/bar.xhtml"));
        assertTrue(content.contains("/level2/foo.xhtml"));

        assertFalse(content.contains("/level2/level3/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
        assertFalse(content.contains("include.xtml"));
    }

    @Test
    public void testGetViewsForPathImplicit() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViews.jsf?path=%2Flevel2%2F&implicit=true");
        String content = page.asXml();

        assertFalse(content.contains("/getViews"));
        assertFalse(content.contains("view: /foo"));

        assertTrue(content.contains("/level2/bar"));
        assertTrue(content.contains("/level2/foo"));
        assertTrue(content.contains("/level2/level3/foo"));
        assertTrue(content.contains("/level2/level3/level4/foo"));

        assertFalse(content.contains("/level2/bar.xhtml"));
        assertFalse(content.contains("/level2/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
        assertFalse(content.contains("include.xtml"));

        assertFalse(content.contains("/some_file"));
        assertFalse(content.contains("include"));
    }

    @Test
    public void testGetViewsForPathImplicitWithLimit2() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViews.jsf?path=%2Flevel2%2F&implicit=true&maxDepth=2");
        String content = page.asXml();

        assertFalse(content.contains("/getViews"));
        assertFalse(content.contains("view: /foo"));

        // Contains only the views up to level 2, not those of /level2/ + 2
        assertTrue(content.contains("/level2/bar"));
        assertTrue(content.contains("/level2/foo"));

        assertFalse(content.contains("/level2/level3/foo"));
        assertFalse(content.contains("/level2/level3/level4/foo"));

        assertFalse(content.contains("/level2/bar.xhtml"));
        assertFalse(content.contains("/level2/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
        assertFalse(content.contains("include.xtml"));

        assertFalse(content.contains("/some_file"));
        assertFalse(content.contains("include"));
    }

    @Test
    public void testGetViewsForPathImplicitWithLimit3() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViews.jsf?path=%2Flevel2%2F&implicit=true&maxDepth=3");
        String content = page.asXml();

        assertFalse(content.contains("/getViews"));
        assertFalse(content.contains("view: /foo"));

        // Contains only the views up to level 3, not those of /level2/ + 2
        assertTrue(content.contains("/level2/bar"));
        assertTrue(content.contains("/level2/foo"));
        assertTrue(content.contains("/level2/level3/foo"));

        assertFalse(content.contains("/level2/level3/level4/foo"));

        assertFalse(content.contains("/level2/bar.xhtml"));
        assertFalse(content.contains("/level2/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
        assertFalse(content.contains("include.xtml"));

        assertFalse(content.contains("/some_file"));
        assertFalse(content.contains("include"));
    }

    @Test
    public void testGetViewsForPathImplicitWithLimit0() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViews.jsf?path=%2Flevel2%2F&implicit=true&maxDepth=0");
        String content = page.asXml();

        assertFalse(content.contains("/getViews"));
        assertFalse(content.contains("view: /foo"));

        // Special case, maxDepth lower than level of requested path - views from requested path are returned
        // but no other paths are traversed
        assertTrue(content.contains("/level2/bar"));
        assertTrue(content.contains("/level2/foo"));

        assertFalse(content.contains("/level2/level3/foo"));
        assertFalse(content.contains("/level2/level3/level4/foo"));

        assertFalse(content.contains("/level2/bar.xhtml"));
        assertFalse(content.contains("/level2/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
        assertFalse(content.contains("include.xtml"));

        assertFalse(content.contains("/some_file"));
        assertFalse(content.contains("include"));
    }


    // ### ViewDeclarationLanguage based tests


    @Test
    public void testGetAllViewsVDL() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViews.jsf?fromVDL=true");
        String content = page.asXml();

        assertTrue(content.contains("/getViews.xhtml"));
        assertTrue(content.contains("view: /foo.xhtml")); // include marker since is also subset of "/level2/foo.xhtml" etc
        assertTrue(content.contains("/level2/bar.xhtml"));
        assertTrue(content.contains("/level2/foo.xhtml"));
        assertTrue(content.contains("/level2/level3/foo.xhtml"));
        assertTrue(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
        assertFalse(content.contains("include.xtml"));
    }

    @Test
    public void testGetViewsForPathVDL() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViews.jsf?path=%2Flevel2%2F&fromVDL=true");
        String content = page.asXml();

        assertFalse(content.contains("/getViews.xhtml"));
        assertFalse(content.contains("view: /foo.xhtml"));

        assertTrue(content.contains("/level2/bar.xhtml"));
        assertTrue(content.contains("/level2/foo.xhtml"));
        assertTrue(content.contains("/level2/level3/foo.xhtml"));
        assertTrue(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
        assertFalse(content.contains("include.xtml"));
    }

    @Test
    public void testGetAllViewsAsImplicitVDL() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViews.jsf?implicit=true&fromVDL=true");
        String content = page.asXml();

        assertTrue(content.contains("/getViews"));
        assertTrue(content.contains("view: /foo"));
        assertTrue(content.contains("/level2/bar"));
        assertTrue(content.contains("/level2/foo"));
        assertTrue(content.contains("/level2/level3/foo"));
        assertTrue(content.contains("/level2/level3/level4/foo"));

        assertFalse(content.contains("/getViews.xhtml"));
        assertFalse(content.contains("view: /foo.xhtml"));
        assertFalse(content.contains("/level2/bar.xhtml"));
        assertFalse(content.contains("/level2/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
        assertFalse(content.contains("include.xtml"));

        assertFalse(content.contains("/some_file"));
        assertFalse(content.contains("include"));
    }

    @Test
    public void testGetAllViewsWithLimit2VDL() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViews.jsf?maxDepth=2&fromVDL=true");
        String content = page.asXml();

        assertTrue(content.contains("/getViews.xhtml"));
        assertTrue(content.contains("view: /foo.xhtml")); // include marker since is also subset of "/level2/foo.xhtml" etc
        assertTrue(content.contains("/level2/bar.xhtml"));
        assertTrue(content.contains("/level2/foo.xhtml"));

        assertFalse(content.contains("/level2/level3/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
        assertFalse(content.contains("include.xtml"));
    }

    @Test
    public void testGetViewsForPathImplicitVDL() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViews.jsf?path=%2Flevel2%2F&implicit=true&fromVDL=true");
        String content = page.asXml();

        assertFalse(content.contains("/getViews"));
        assertFalse(content.contains("view: /foo"));

        assertTrue(content.contains("/level2/bar"));
        assertTrue(content.contains("/level2/foo"));
        assertTrue(content.contains("/level2/level3/foo"));
        assertTrue(content.contains("/level2/level3/level4/foo"));

        assertFalse(content.contains("/level2/bar.xhtml"));
        assertFalse(content.contains("/level2/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
        assertFalse(content.contains("include.xtml"));

        assertFalse(content.contains("/some_file"));
        assertFalse(content.contains("include"));
    }

    @Test
    public void testGetViewsForPathImplicitWithLimit2VDL() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViews.jsf?path=%2Flevel2%2F&implicit=true&maxDepth=2&fromVDL=true");
        String content = page.asXml();

        assertFalse(content.contains("/getViews"));
        assertFalse(content.contains("view: /foo"));

        // Contains only the views up to level 2, not those of /level2/ + 2
        assertTrue(content.contains("/level2/bar"));
        assertTrue(content.contains("/level2/foo"));

        assertFalse(content.contains("/level2/level3/foo"));
        assertFalse(content.contains("/level2/level3/level4/foo"));

        assertFalse(content.contains("/level2/bar.xhtml"));
        assertFalse(content.contains("/level2/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
        assertFalse(content.contains("include.xtml"));

        assertFalse(content.contains("/some_file"));
        assertFalse(content.contains("include"));
    }

    @Test
    public void testGetViewsForPathImplicitWithLimit3VDL() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViews.jsf?path=%2Flevel2%2F&implicit=true&maxDepth=3&fromVDL=true");
        String content = page.asXml();

        assertFalse(content.contains("/getViews"));
        assertFalse(content.contains("view: /foo"));

        // Contains only the views up to level 3, not those of /level2/ + 2
        assertTrue(content.contains("/level2/bar"));
        assertTrue(content.contains("/level2/foo"));
        assertTrue(content.contains("/level2/level3/foo"));

        assertFalse(content.contains("/level2/level3/level4/foo"));

        assertFalse(content.contains("/level2/bar.xhtml"));
        assertFalse(content.contains("/level2/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
        assertFalse(content.contains("include.xtml"));

        assertFalse(content.contains("/some_file"));
        assertFalse(content.contains("include"));
    }

    @Test
    public void testGetViewsForPathImplicitWithLimit0VDL() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViews.jsf?path=%2Flevel2%2F&implicit=true&maxDepth=0&fromVDL=true");
        String content = page.asXml();

        assertFalse(content.contains("/getViews"));
        assertFalse(content.contains("view: /foo"));

        // Special case, maxDepth lower than level of requested path - views from requested path are returned
        // but no other paths are traversed
        assertTrue(content.contains("/level2/bar"));
        assertTrue(content.contains("/level2/foo"));

        assertFalse(content.contains("/level2/level3/foo"));
        assertFalse(content.contains("/level2/level3/level4/foo"));

        assertFalse(content.contains("/level2/bar.xhtml"));
        assertFalse(content.contains("/level2/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
        assertFalse(content.contains("include.xtml"));

        assertFalse(content.contains("/some_file"));
        assertFalse(content.contains("include"));
    }


    // ### ViewDeclarationLanguage based tests


    @Test
    public void testGetAllViewResources() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViewResources.jsf");
        String content = page.asXml();

        assertTrue(content.contains("/getViews.xhtml"));
        assertTrue(content.contains("resource name: /foo.xhtml")); // include marker since is also subset of "/level2/foo.xhtml" etc
        assertTrue(content.contains("/level2/bar.xhtml"));
        assertTrue(content.contains("/level2/foo.xhtml"));
        assertTrue(content.contains("/WEB-INF/include.xhtml"));
        assertTrue(content.contains("/level2/level3/foo.xhtml"));
        assertTrue(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
    }

    @Test
    public void testGetViewResourcesForPath() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViewResources.jsf?path=%2Flevel2%2F");
        String content = page.asXml();

        assertFalse(content.contains("/getViews.xhtml"));
        assertFalse(content.contains("view: /foo.xhtml"));
        assertFalse(content.contains("/WEB-INF/include.xhtml"));

        assertTrue(content.contains("/level2/bar.xhtml"));
        assertTrue(content.contains("/level2/foo.xhtml"));
        assertTrue(content.contains("/level2/level3/foo.xhtml"));
        assertTrue(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
    }

    @Test
    public void testGetAllViewResourcesTopLevel() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViewResources.jsf?topLevel=true");
        String content = page.asXml();

        assertTrue(content.contains("/getViews.xhtml"));
        assertTrue(content.contains("resource name: /foo.xhtml")); // include marker since is also subset of "/level2/foo.xhtml" etc
        assertTrue(content.contains("/level2/bar.xhtml"));
        assertTrue(content.contains("/level2/foo.xhtml"));
        assertTrue(content.contains("/level2/level3/foo.xhtml"));
        assertTrue(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/WEB-INF/include.xhtml"));
        assertFalse(content.contains("/some_file.txt"));
    }

    @Test
    public void testGetAllViewResourcesWithLimit2() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViewResources.jsf?maxDepth=2");
        String content = page.asXml();

        assertTrue(content.contains("/getViews.xhtml"));
        assertTrue(content.contains("resource name: /foo.xhtml")); // include marker since is also subset of "/level2/foo.xhtml" etc

        assertTrue(content.contains("/WEB-INF/include.xhtml"));
        assertTrue(content.contains("/level2/bar.xhtml"));
        assertTrue(content.contains("/level2/foo.xhtml"));

        assertFalse(content.contains("/level2/level3/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
    }

    @Test
    public void testGetViewResourcesForPathWithLimit3() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViewResources.jsf?path=%2Flevel2%2F&maxDepth=3");
        String content = page.asXml();

        assertFalse(content.contains("/getViews.xhtml"));
        assertFalse(content.contains("resource name: /foo.xhtml"));
        assertFalse(content.contains("include.xtml"));

        // Contains only the view resources up to level 3, not those of /level2/ + 2
        assertTrue(content.contains("/level2/bar.xhtml"));
        assertTrue(content.contains("/level2/foo.xhtml"));
        assertTrue(content.contains("/level2/level3/foo.xhtml"));

        assertFalse(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
    }

    @Test
    public void testGetAllViewResourcesForPathWithLimit0() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "getViewResources.jsf?path=%2Flevel2%2F&maxDepth=0");
        String content = page.asXml();

        assertFalse(content.contains("/getViews.xhtml"));
        assertFalse(content.contains("resource name: /foo.xhtml")); // include marker since is also subset of "/level2/foo.xhtml" etc
        assertFalse(content.contains("/WEB-INF/include.xhtml"));

        assertTrue(content.contains("/level2/bar.xhtml"));
        assertTrue(content.contains("/level2/foo.xhtml"));

        assertFalse(content.contains("/level2/level3/foo.xhtml"));
        assertFalse(content.contains("/level2/level3/level4/foo.xhtml"));

        assertFalse(content.contains("/some_file.txt"));
        assertFalse(content.contains("include.xtml"));
    }

}
