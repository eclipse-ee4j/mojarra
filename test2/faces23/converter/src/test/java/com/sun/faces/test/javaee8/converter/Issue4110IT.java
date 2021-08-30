/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.javaee8.converter;

import static java.lang.System.getProperty;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.Locale;

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
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

@RunWith(Arquillian.class)
public class Issue4110IT {

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
        webClient.addRequestHeader("Accept-Language", "en-US");
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testJavaTimeTypes() throws Exception {
        doTestJavaTimeTypes("30 sep. 2015", "localDate", "2015-09-30");
        doTestJavaTimeTypes("16:52:56", "localTime", "16:52:56");
        doTestJavaTimeTypes("30 sep. 2015 16:14:43", "localDateTime", "2015-09-30T16:14:43");
    }

    private void doTestJavaTimeTypes(String value, String type, String expected) throws Exception {
        Locale.setDefault(Locale.US);
        HtmlPage page = webClient.getPage(webUrl + "faces/issue4110.xhtml");

        try {
            HtmlTextInput input = page.getHtmlElementById("form:" + type + "Input");
            input.setValueAttribute(value);
            HtmlSubmitInput submit = page.getHtmlElementById("form:submit");
            page = submit.click();

            HtmlSpan output = page.getHtmlElementById("form:" + type + "Output");
            assertEquals(expected, output.getTextContent());
        } catch (AssertionError e) {
            if (page != null) {
                System.out.println(page.asXml());
            }
            throw e;
        }
    }

}
