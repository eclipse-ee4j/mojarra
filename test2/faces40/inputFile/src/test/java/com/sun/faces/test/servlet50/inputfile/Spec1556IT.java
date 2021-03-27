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

package com.sun.faces.test.servlet50.inputfile;

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
import com.gargoylesoftware.htmlunit.html.HtmlFileInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

@RunWith(Arquillian.class)
public class Spec1556IT {

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
    public void testRenderingOfAcceptAttribute(String form) throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1556IT.xhtml");

        HtmlFileInput inputFileWithoutAccept = page.getHtmlElementById("form:inputFileWithoutAccept");
        assertEquals("Unspecified 'accept' attribute on h:inputFile is NOT rendered", "", inputFileWithoutAccept.getAttribute("accept"));

        HtmlFileInput inputFileWithAccept = page.getHtmlElementById("form:inputFileWithAccept");
        assertEquals("Specified 'accept' attribute on h:inputFile is rendered", "image/*", inputFileWithAccept.getAttribute("accept"));

        // It's for Mojarra also explicitly tested on h:inputText because they share the same renderer.
        HtmlTextInput inputTextWithoutAccept = page.getHtmlElementById("form:inputTextWithoutAccept");
        assertEquals("Unspecified 'accept' attribute on h:inputText is NOT rendered", "", inputTextWithoutAccept.getAttribute("accept"));

        HtmlTextInput inputTextWithAccept = page.getHtmlElementById("form:inputTextWithAccept");
        assertEquals("Specified 'accept' attribute on h:inputText is NOT rendered", "", inputTextWithAccept.getAttribute("accept"));

        // NOTE: HtmlUnit doesn't support filtering files by accept attribute. So the upload part is not tested to keep it simple (it's nonetheless already tested in Spec1555IT).
    }

}
