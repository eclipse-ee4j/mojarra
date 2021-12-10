/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation.
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

package com.sun.faces.test.servlet50.namespaces;

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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@RunWith(Arquillian.class)
public class Spec1553IT {

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
    public void test() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1553IT.xhtml");

        assertEquals("jakarta.faces.html h:head works", "Spec1553IT", page.getTitleText());

        assertEquals("http://java.sun.com/jsf/facelets works", "value", getValue(page.getElementById("ui_sun")));
        assertEquals("http://java.sun.com/jsf/core works", "value", getValue(page.getElementById("f_sun")));
        assertEquals("http://java.sun.com/jsf/html works", "value", getValue(page.getElementById("h_sun")));
        assertEquals("http://java.sun.com/jsf/composite works", "value", getValue(page.getElementById("cc_sun")));
        assertEquals("http://java.sun.com/jsp/jstl/core works", "value", getValue(page.getElementById("c_sun")));
        assertEquals("http://java.sun.com/jsp/jstl/functions works", "value", getValue(page.getElementById("fn_sun")));

        assertEquals("http://xmlns.jcp.org/jsf works", "id_jcp", page.getElementById("jsf_jcp").getChildElements().iterator().next().getAttribute("id"));
        assertEquals("http://xmlns.jcp.org/jsf/facelets works", "value", getValue(page.getElementById("ui_jcp")));
        assertEquals("http://xmlns.jcp.org/jsf/core works", "value", getValue(page.getElementById("f_jcp")));
        assertEquals("http://xmlns.jcp.org/jsf/html works", "value", getValue(page.getElementById("h_jcp")));
        assertEquals("http://xmlns.jcp.org/jsf/passthrough works", "email", page.getElementById("p_jcp").getChildElements().iterator().next().getAttribute("type"));
        assertEquals("http://xmlns.jcp.org/jsf/composite works", "value", getValue(page.getElementById("cc_jcp")));
//        assertEquals("http://xmlns.jcp.org/jsf/component works", "value", getValue(page.getElementById("comp_jcp")));
        assertEquals("http://xmlns.jcp.org/jsp/jstl/core works", "value", getValue(page.getElementById("c_jcp")));
        assertEquals("http://xmlns.jcp.org/jsp/jstl/functions works", "value", getValue(page.getElementById("fn_jcp")));

        assertEquals("jakarta.faces works", "id_jakarta", page.getElementById("faces_jakarta").getChildElements().iterator().next().getAttribute("id"));
        assertEquals("jakarta.faces.facelets works", "value", getValue(page.getElementById("ui_jakarta")));
        assertEquals("jakarta.faces.core works", "value", getValue(page.getElementById("f_jakarta")));
        assertEquals("jakarta.faces.html works", "value", getValue(page.getElementById("h_jakarta")));
        assertEquals("jakarta.faces.passthrough works", "email", page.getElementById("p_jakarta").getChildElements().iterator().next().getAttribute("type"));
        assertEquals("jakarta.faces.composite works", "value", getValue(page.getElementById("cc_jakarta")));
//        assertEquals("jakarta.faces.component works", "value", getValue(page.getElementById("comp_jakarta")));
        assertEquals("jakarta.tags.core works", "value", getValue(page.getElementById("c_jakarta")));
        assertEquals("jakarta.tags.functions works", "value", getValue(page.getElementById("fn_jakarta")));

    }

    @Test
    @Ignore // Fails due to FacesInitializer#onStartup(classes) being empty in current GlassFish version -- TODO: remove once GlassFish is fixed, see also outcomments above
    public void testFacesComponent() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1553IT.xhtml");

        assertEquals("http://xmlns.jcp.org/jsf/component works", "value", getValue(page.getElementById("comp_jcp")));
        assertEquals("jakarta.faces.component works", "value", getValue(page.getElementById("comp_jakarta")));
    }

    private static String getValue(DomElement element) {
        assertEquals("This element has no children", 0, element.getChildElementCount());
        return element.asNormalizedText();
    }
}
