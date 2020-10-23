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

package com.sun.faces.test.servlet30.contractExtended;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

public class Issue3137IT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testInitialContractIsJarbase() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");

        // start with initial jarbase contract
        String text = page.asText();
        assertTrue(text.contains("\"jarbase\" template header"));
        assertTrue(text.contains("from \"jarbase\" subtemplate.xhtml"));
        assertTrue(text.contains("resolved contracts: [jarbase]"));
        assertContractForCss(page, "contract.css", "jarbase");
        assertContractForCss(page, "cssLayout.css", "jarbase");

    }

    @Test
    public void testWarbaseContract() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");

        // switch to warbase contract
        HtmlSelect selectOne = (HtmlSelect) page.getElementById("selectOne");
        selectOne.setSelectedAttribute("warbase", true);
        HtmlSubmitInput apply = (HtmlSubmitInput) page.getElementById("apply");
        page = apply.click();

        String text = page.asText();
        assertTrue(text.contains("\"warbase\" template header"));
        assertTrue(text.contains("from \"warbase\" subtemplate.xhtml"));
        assertTrue(text.contains("resolved contracts: [warbase]"));
        assertContractForCss(page, "contract.css", "warbase");
        assertContractForCss(page, "cssLayout.css", "warbase");

    }

    @Test
    public void testRedExtendsWarbaseOrJarBaseContract() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");

        // switch to red,warbase contract
        HtmlSelect selectOne = (HtmlSelect) page.getElementById("selectOne");
        selectOne.setSelectedAttribute("red,warbase", true);
        HtmlSubmitInput apply = (HtmlSubmitInput) page.getElementById("apply");
        page = apply.click();

        String text = page.asText();
        assertTrue(text.contains("\"red\" template header"));
        assertTrue(text.contains("from \"warbase\" subtemplate.xhtml"));
        assertTrue(text.contains("resolved contracts: [red, warbase]"));
        assertContractForCss(page, "contract.css", "red");
        assertContractForCss(page, "cssLayout.css", "warbase");

        // switch to red,jarbase contract
        selectOne = (HtmlSelect) page.getElementById("selectOne");
        selectOne.setSelectedAttribute("red,jarbase", true);
        apply = (HtmlSubmitInput) page.getElementById("apply");
        page = apply.click();

        text = page.asText();
        assertTrue(text.contains("\"red\" template header"));
        assertTrue(text.contains("from \"jarbase\" subtemplate.xhtml"));
        assertTrue(text.contains("resolved contracts: [red, jarbase]"));
        assertContractForCss(page, "contract.css", "red");
        assertContractForCss(page, "cssLayout.css", "jarbase");

    }

    @Test
    public void testBlueExtendsWarbaseOrJarBaseContract() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");

        // switch to blue,warbase contract
        HtmlSelect selectOne = (HtmlSelect) page.getElementById("selectOne");
        selectOne.setSelectedAttribute("blue,warbase", true);
        HtmlSubmitInput apply = (HtmlSubmitInput) page.getElementById("apply");
        page = apply.click();

        String text = page.asText();
        assertTrue(text.contains("\"blue\" template header"));
        assertTrue(text.contains("from \"warbase\" subtemplate.xhtml"));
        assertTrue(text.contains("resolved contracts: [blue, warbase]"));
        assertContractForCss(page, "contract.css", "blue");
        assertContractForCss(page, "cssLayout.css", "warbase");

        // switch to blue,jarbase contract
        selectOne = (HtmlSelect) page.getElementById("selectOne");
        selectOne.setSelectedAttribute("blue,jarbase", true);
        apply = (HtmlSubmitInput) page.getElementById("apply");
        page = apply.click();

        text = page.asText();
        assertTrue(text.contains("\"blue\" template header"));
        assertTrue(text.contains("from \"jarbase\" subtemplate.xhtml"));
        assertTrue(text.contains("resolved contracts: [blue, jarbase]"));
        assertContractForCss(page, "contract.css", "blue");
        assertContractForCss(page, "cssLayout.css", "jarbase");

    }

    @Test
    public void testGreenExtendsWarbaseOrJarBaseContract() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");

        // switch to green,warbase contract
        HtmlSelect selectOne = (HtmlSelect) page.getElementById("selectOne");
        selectOne.setSelectedAttribute("green,warbase", true);
        HtmlSubmitInput apply = (HtmlSubmitInput) page.getElementById("apply");
        page = apply.click();

        String text = page.asText();
        assertTrue(text.contains("\"green\" template header"));
        assertTrue(text.contains("from \"warbase\" subtemplate.xhtml"));
        assertTrue(text.contains("resolved contracts: [green, warbase]"));
        assertContractForCss(page, "contract.css", "green");
        assertContractForCss(page, "cssLayout.css", "warbase");

        // switch to green,jarbase contract
        selectOne = (HtmlSelect) page.getElementById("selectOne");
        selectOne.setSelectedAttribute("green,jarbase", true);
        apply = (HtmlSubmitInput) page.getElementById("apply");
        page = apply.click();

        text = page.asText();
        assertTrue(text.contains("\"green\" template header"));
        assertTrue(text.contains("from \"jarbase\" subtemplate.xhtml"));
        assertTrue(text.contains("resolved contracts: [green, jarbase]"));
        assertContractForCss(page, "contract.css", "green");
        assertContractForCss(page, "cssLayout.css", "jarbase");

    }

    @Test
    public void testUserPathCalculatedContracts() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/user/index.xhtml");

        // expect contracts blue,jarbase
        String text = page.asText();
        assertTrue(text.contains("\"blue\" template header"));
        assertTrue(text.contains("from \"jarbase\" subtemplate.xhtml"));
        assertTrue(text.contains("resolved contracts: [blue, jarbase]"));
        assertContractForCss(page, "contract.css", "blue");
        assertContractForCss(page, "cssLayout.css", "jarbase");

    }

    @Test
    public void testRedPathCalculatedContracts() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/red/index.xhtml");

        // expect contracts red,jarbase
        String text = page.asText();
        assertTrue(text.contains("\"red\" template header"));
        assertTrue(text.contains("from \"jarbase\" subtemplate.xhtml"));
        assertTrue(text.contains("resolved contracts: [red, jarbase]"));
        assertContractForCss(page, "contract.css", "red");
        assertContractForCss(page, "cssLayout.css", "jarbase");

    }

    @Test
    public void testGreenPathCalculatedContracts() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/green/index.xhtml");

        // expect contracts green,jarbase
        String text = page.asText();
        assertTrue(text.contains("\"green\" template header"));
        assertTrue(text.contains("from \"jarbase\" subtemplate.xhtml"));
        assertTrue(text.contains("resolved contracts: [green, jarbase]"));
        assertContractForCss(page, "contract.css", "green");
        assertContractForCss(page, "cssLayout.css", "jarbase");

    }

    private void assertContractForCss(HtmlPage page, String resourceName,
            String expectedContract) {
        DomNodeList<DomElement> links = page.getElementsByTagName("link");
        for (DomElement cur : links) {
            HtmlLink link = (HtmlLink) cur;
            String href = link.getHrefAttribute();
            if (href.contains(resourceName)) {
                String query = href.substring(href.indexOf("?") + 1);
                String[] parts = query.split("&");
                for (String part : parts) {
                    String[] kv = part.split("=");
                    if ("con".equals(kv[0])) {
                        assertEquals("examined link href=" + href,
                                expectedContract, kv[1]);
                        return;
                    }
                }
            }
        }
        fail("Could not find link for resource '" + resourceName + "'!");
    }

}
