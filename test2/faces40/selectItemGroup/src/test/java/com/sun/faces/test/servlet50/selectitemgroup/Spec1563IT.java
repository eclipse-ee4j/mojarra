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

package com.sun.faces.test.servlet50.selectitemgroup;

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
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

@RunWith(Arquillian.class)
public class Spec1563IT {

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
        HtmlPage page = webClient.getPage(webUrl + "spec1563IT.xhtml");
        HtmlSelect select = page.getHtmlElementById("form:input");

        assertValidMarkup(select);

        select.setSelectedAttribute(select.getOptionByValue("5"), true);

        assertEquals("messages is empty before submit", "", page.getHtmlElementById("form:messages").asNormalizedText());
        assertEquals("output is empty before submit", "", page.getHtmlElementById("form:output").asNormalizedText());

        page = page.getHtmlElementById("form:submit").click();

        assertValidMarkup(select);
        assertEquals("messages is still empty after submit", "", page.getHtmlElementById("form:messages").asNormalizedText());
        assertEquals("output is '5' after submit", "5", page.getHtmlElementById("form:output").asNormalizedText());

        select = page.getHtmlElementById("form:input");
        select.setSelectedAttribute(select.getOptionByValue("2"), true);
        page = page.getHtmlElementById("form:submit").click();

        assertValidMarkup(select);
        assertEquals("messages is still empty after submit", "", page.getHtmlElementById("form:messages").asNormalizedText());
        assertEquals("output is '2' after submit", "2", page.getHtmlElementById("form:output").asNormalizedText());
    }

    private static void assertValidMarkup(HtmlSelect select) {
        assertEquals("select has 2 children", 2, select.getChildElementCount());

        for (DomElement child : select.getChildElements()) {
            assertEquals("child element is an optgroup", "optgroup", child.getNodeName());
            assertEquals("child has in turn 3 grandchildren", 3, child.getChildElementCount());

            for (DomElement grandchild : child.getChildElements()) {
                assertEquals("grandchild  element is an option", "option", grandchild.getNodeName());
            }
        }

        assertEquals("select element has 6 options", 6, select.getOptions().size());

        HtmlOption option2 = select.getOptionByValue("2");
        assertEquals("2nd option is 'Cat'", "Cat", option2.getText());

        HtmlOption option5 = select.getOptionByValue("5");
        assertEquals("5th option is 'Audi'", "Audi", option5.getText());
    }
}
