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

package com.sun.faces.test.servlet50.selectmanycheckbox;

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
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@RunWith(Arquillian.class)
public class Spec1574IT {

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
    public void testSelectManyCheckboxDefaultMarkup() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1574IT.xhtml");
        HtmlElement selectManyCheckbox = page.getHtmlElementById("form:input");
        assertValidMarkup(selectManyCheckbox, true, false);
    }

    @Test
    public void testSelectManyCheckboxLineDirectionMarkup() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1574IT.xhtml?layout=lineDirection");
        HtmlElement selectManyCheckbox = page.getHtmlElementById("form:input");
        assertValidMarkup(selectManyCheckbox, true, false);
    }

    @Test
    public void testSelectManyCheckboxPageDirectionMarkup() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1574IT.xhtml?layout=pageDirection");
        HtmlElement selectManyCheckbox = page.getHtmlElementById("form:input");
        assertValidMarkup(selectManyCheckbox, true, true);
    }

    @Test
    public void testSelectManyCheckboxListMarkup() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1574IT.xhtml?layout=list");
        HtmlElement selectManyCheckbox = page.getHtmlElementById("form:input");
        assertValidMarkup(selectManyCheckbox, false, true);
    }

    @Test
    public void testSelectOneRadioDefaultMarkup() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1574IT.xhtml?radio=true");
        HtmlElement selectOneRadio = page.getHtmlElementById("form:input");
        assertValidMarkup(selectOneRadio, true, false);
    }

    @Test
    public void testSelectOneRadioLineDirectionMarkup() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1574IT.xhtml?radio=true&layout=lineDirection");
        HtmlElement selectOneRadio = page.getHtmlElementById("form:input");
        assertValidMarkup(selectOneRadio, true, false);
    }

    @Test
    public void testSelectOneRadioPageDirectionMarkup() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1574IT.xhtml?radio=true&layout=pageDirection");
        HtmlElement selectOneRadio = page.getHtmlElementById("form:input");
        assertValidMarkup(selectOneRadio, true, true);
    }

    @Test
    public void testSelectOneRadioListMarkup() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1574IT.xhtml?radio=true&layout=list");
        HtmlElement selectOneRadio = page.getHtmlElementById("form:input");
        assertValidMarkup(selectOneRadio, false, true);
    }

    private static void assertValidMarkup(HtmlElement element, boolean table, boolean vertical) {
        int inputFields = 0;

        if (table) {
            assertEquals("element is table", "table", element.getNodeName());
            assertEquals("table has 1 child", 1, element.getChildElementCount());
            assertEquals("table child is tbody", "tbody", element.getFirstElementChild().getNodeName());
            int tbodyChildCount = vertical ? 4 : 1;
            assertEquals("tbody has " + tbodyChildCount + " rows", tbodyChildCount, element.getFirstElementChild().getChildElementCount());
            int i = 1;

            for (DomElement row : element.getFirstElementChild().getChildElements()) {
                assertEquals("row is tr", "tr", row.getNodeName());
                int trChildCount = vertical ? 1 : 4;
                assertEquals("tr has " + trChildCount + " cells", trChildCount, row.getChildElementCount());

                for (DomElement cell : row.getChildElements()) {
                    assertEquals("cell is td", "td", cell.getNodeName());

                    if (i % 2 == 0) {
                        assertEquals("cell has 1 child", 1, cell.getChildElementCount());
                        assertEquals("cell child is table", "table", cell.getFirstElementChild().getNodeName());
                        assertEquals("child table has 1 child", 1, cell.getFirstElementChild().getChildElementCount());
                        assertEquals("group is tbody", "tbody", cell.getFirstElementChild().getFirstElementChild().getNodeName());
                        int groupChildCount = vertical ? 3 : 1;
                        assertEquals("group has " + groupChildCount + " rows", groupChildCount, cell.getFirstElementChild().getFirstElementChild().getChildElementCount());

                        for (DomElement group : cell.getFirstElementChild().getFirstElementChild().getChildElements()) {
                            assertEquals("group is tr", "tr", group.getNodeName());

                            for (DomElement item : group.getChildElements()) {
                                assertEquals("item is td", "td", item.getNodeName());
                                assertEquals("td has 2 children", 2, item.getChildElementCount());
                                assertEquals("first child is input", "input", item.getFirstElementChild().getNodeName());
                                assertEquals("last child is label", "label", item.getLastElementChild().getNodeName());
                                inputFields++;
                            }
                        }
                    }
                    else {
                        assertEquals("cell has no children", 0, cell.getChildElementCount());
                    }
                    
                    i++;
                }
            }
        }
        else {
            assertEquals("element is ul", "ul", element.getNodeName());
            assertEquals("ul has 2 children", 2, element.getChildElementCount());

            for (DomElement group : element.getChildElements()) {
                assertEquals("group is li", "li", group.getNodeName());
                assertEquals("li has 1 child", 1, group.getChildElementCount());
                assertEquals("child is ul", "ul", group.getFirstElementChild().getNodeName());

                for (DomElement item : group.getFirstElementChild().getChildElements()) {
                    assertEquals("item is li", "li", item.getNodeName());
                    assertEquals("li has 2 children", 2, item.getChildElementCount());
                    assertEquals("first child is input", "input", item.getFirstElementChild().getNodeName());
                    assertEquals("last child is label", "label", item.getLastElementChild().getNodeName());
                    inputFields++;
                }
            }
        }
        
        assertEquals("there were 6 input fields", 6, inputFields);
    }
}
