/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

package com.sun.faces.test.servlet50.resources;

import static java.lang.System.getProperty;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertEquals;

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

@RunWith(Arquillian.class)
public class Spec1565IT {

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
    public void testHtml5() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1565IT-HTML5.xhtml");

        for (DomElement element : page.getElementsByTagName("script")) {
            assertEquals("Script element has no type attribute", "", element.getAttribute("type"));
        }

        for (DomElement element : page.getElementsByTagName("link")) {
            assertEquals("Link element has no type attribute", "", element.getAttribute("type"));
        }

        for (DomElement element : page.getElementsByTagName("style")) {
            assertEquals("Style element has no type attribute", "", element.getAttribute("type"));
        }
    }

    @Test
    public void testHtml4() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1565IT-HTML4.xhtml");

        for (DomElement element : page.getElementsByTagName("script")) {
            assertEquals("Script element has a type='text/javascript' attribute", "text/javascript", element.getAttribute("type"));
        }

        for (DomElement element : page.getElementsByTagName("link")) {
            assertEquals("Link element has a type='text/css' attribute", "text/css", element.getAttribute("type"));
        }

        for (DomElement element : page.getElementsByTagName("style")) {
            assertEquals("Style element has a type='text/css' attribute", "text/css", element.getAttribute("type"));
        }
    }

}
