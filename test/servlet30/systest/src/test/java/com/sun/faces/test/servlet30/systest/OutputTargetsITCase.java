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

package com.sun.faces.test.servlet30.systest;

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test h:link and h:button.
 */
public class OutputTargetsITCase extends HtmlUnitFacesITCase {

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public OutputTargetsITCase(String name) {
        super(name);
    }


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        super.setUp();
    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return new TestSuite(OutputTargetsITCase.class);
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    // ------------------------------------------------------------ Test Methods


    public void testOutputTargetButton() throws Exception {
        HtmlPage page = getPage("/faces/standard/outcometarget01.xhtml");
        // get the page twice to avoid jsession ID encoding in the results
        page = getPage("/faces/standard/outcometarget01.xhtml");
        assertNotNull(page);

        List<HtmlInput> buttonList = new ArrayList<HtmlInput>(7);
        getAllElementsOfGivenClass(page, buttonList, HtmlInput.class);
        assertTrue(buttonList.size() == 7);

        HtmlInput button = buttonList.get(0);
        String onclick = button.getOnClickAttribute();
        assertEquals("button", button.getTypeAttribute());
        assertEquals("button1", button.getAttribute("id"));
        assertEquals("window.location.href='/jsf-systest/faces/standard/outcometarget01.xhtml'; return false;", onclick);
        page = button.click();
        assertEquals("outcometarget01", page.getTitleText());

        
        // ---------------------------------------------------------------------

        page = getPage("/faces/standard/outcometarget01.xhtml");
        button = getAllElementsOfGivenClass(page, HtmlInput.class).get(1);
        onclick = button.getOnClickAttribute();
        assertEquals("button", button.getTypeAttribute());
        assertEquals("button2", button.getAttribute("id"));
        assertEquals("window.location.href='/jsf-systest/faces/standard/nav1.xhtml'; return false;", onclick);
        
        page = button.click();
        assertEquals("nav1", page.getTitleText());
        
        page = getPage("/faces/standard/outcometarget01.xhtml");

        
        // ---------------------------------------------------------------------

        page = getPage("/faces/standard/outcometarget01.xhtml");
        button = getAllElementsOfGivenClass(page, HtmlInput.class).get(2);
        onclick = button.getOnClickAttribute();
        assertEquals("button", button.getTypeAttribute());
        assertEquals("button3", button.getAttribute("id"));
        assertEquals("window.location.href='/jsf-systest/faces/standard/outcometarget01.xhtml?id=page&id2=view#about'; return false;", onclick);
        
        page = button.click();
        assertEquals("outcometarget01", page.getTitleText());

        
        // ---------------------------------------------------------------------

        page = getPage("/faces/standard/outcometarget01.xhtml");
        button = getAllElementsOfGivenClass(page, HtmlInput.class).get(3);
        onclick = button.getOnClickAttribute();
        assertEquals("button", button.getTypeAttribute());
        assertEquals("button4", button.getAttribute("id"));
        assertEquals("alert('foo'); window.location.href='/jsf-systest/faces/standard/nav2.xhtml'; return false;", onclick);
        
        page = button.click();
        assertEquals("nav2", page.getTitleText());

        
        // ---------------------------------------------------------------------

        page = getPage("/faces/standard/outcometarget01.xhtml");
        System.out.println("**************************************" + page.asXml());
        button = getAllElementsOfGivenClass(page, HtmlInput.class).get(4);
        onclick = button.getOnClickAttribute();
        
        assertEquals("button", button.getTypeAttribute());
        assertEquals("button5", button.getAttribute("id"));
        assertEquals("window.location.href='/jsf-systest/faces/standard/outcometarget01.xhtml?id=page'; return false;", onclick);
        
        page = button.click();
        assertEquals("outcometarget01", page.getTitleText());

        
        // ---------------------------------------------------------------------

        page = getPage("/faces/standard/outcometarget01.xhtml");
        button = getAllElementsOfGivenClass(page, HtmlInput.class).get(5);
        onclick = button.getOnClickAttribute();
        
        assertEquals("button", button.getTypeAttribute());
        assertEquals("button6", button.getAttribute("id"));
        assertEquals("window.location.href='/jsf-systest/faces/standard/outcometarget01.xhtml?id=page&id2=view'; return false;", onclick);
        
        page = button.click();
        assertEquals("outcometarget01", page.getTitleText());

       
        // ---------------------------------------------------------------------

        page = getPage("/faces/standard/outcometarget01.xhtml");
        button = getAllElementsOfGivenClass(page, HtmlInput.class).get(6);
        onclick = button.getOnClickAttribute();
        assertEquals("button", button.getTypeAttribute());
        assertEquals("button7", button.getAttribute("id"));
        assertEquals("window.location.href='/jsf-systest/faces/standard/outcometarget01.xhtml?id=config&id2=view'; return false;", onclick);
        
        page = button.click();
        assertEquals("outcometarget01", page.getTitleText());
    }

    public void testOutputTargetLink() throws Exception {

        HtmlPage page = getPage("/faces/standard/outcometarget01.xhtml");
        // get the page twice to avoid jsession ID encoding in the results
        page = getPage("/faces/standard/outcometarget01.xhtml");
        assertNotNull(page);

        List<HtmlAnchor> linkList = new ArrayList<HtmlAnchor>(7);
        getAllElementsOfGivenClass(page, linkList, HtmlAnchor.class);
        assertTrue(linkList.size() == 7);

        HtmlAnchor link = getAllElementsOfGivenClass(page, HtmlAnchor.class).get(0);
        String onclick = link.getOnClickAttribute();
        assertEquals("link1", link.getAttribute("id"));
        assertEquals("", onclick);
        assertEquals("/jsf-systest/faces/standard/outcometarget01.xhtml", link.getHrefAttribute());
        page = link.click();
        assertEquals("outcometarget01", page.getTitleText());

        
        // ---------------------------------------------------------------------

        page = getPage("/faces/standard/outcometarget01.xhtml");
        link = getAllElementsOfGivenClass(page, HtmlAnchor.class).get(1);
        onclick = link.getOnClickAttribute();
        assertEquals("link2", link.getAttribute("id"));
        assertEquals("", onclick);
        assertEquals("/jsf-systest/faces/standard/nav1.xhtml", link.getHrefAttribute());
        page = link.click();
        assertEquals("nav1", page.getTitleText());

        
        // ---------------------------------------------------------------------

        page = getPage("/faces/standard/outcometarget01.xhtml");
        link = getAllElementsOfGivenClass(page, HtmlAnchor.class).get(2);
        onclick = link.getOnClickAttribute();
        assertEquals("link3", link.getAttribute("id"));
        assertEquals("", onclick);
        assertEquals("/jsf-systest/faces/standard/outcometarget01.xhtml?id=page&id2=view#about",
                     link.getHrefAttribute());
        page = link.click();
        assertEquals("outcometarget01", page.getTitleText());

        // ---------------------------------------------------------------------

        page = getPage("/faces/standard/outcometarget01.xhtml");
        link = getAllElementsOfGivenClass(page, HtmlAnchor.class).get(3);
        onclick = link.getOnClickAttribute();
        assertEquals("link4", link.getAttribute("id"));
        assertEquals("alert('foo');", onclick);
        assertEquals("/jsf-systest/faces/standard/nav2.xhtml", link.getHrefAttribute());
        page = link.click();
        assertEquals("nav2", page.getTitleText());
        

        // ---------------------------------------------------------------------

        page = getPage("/faces/standard/outcometarget01.xhtml");
        link = getAllElementsOfGivenClass(page, HtmlAnchor.class).get(4);
        onclick = link.getOnClickAttribute();
        assertEquals("link5", link.getAttribute("id"));
        assertEquals("/jsf-systest/faces/standard/outcometarget01.xhtml?id=page", link.getHrefAttribute());
        page = link.click();
        assertEquals("outcometarget01", page.getTitleText());
        

        // ---------------------------------------------------------------------

        page = getPage("/faces/standard/outcometarget01.xhtml");
        link = getAllElementsOfGivenClass(page, HtmlAnchor.class).get(5);
        onclick = link.getOnClickAttribute();
        assertEquals("link6", link.getAttribute("id"));
        assertEquals("/jsf-systest/faces/standard/outcometarget01.xhtml?id=page&id2=view", link.getHrefAttribute());
        page = link.click();
        assertEquals("outcometarget01", page.getTitleText());
        

        // ---------------------------------------------------------------------

        page = getPage("/faces/standard/outcometarget01.xhtml");
        link = getAllElementsOfGivenClass(page, HtmlAnchor.class).get(6);
        onclick = link.getOnClickAttribute();
        assertEquals("link7", link.getAttribute("id"));
        assertEquals("/jsf-systest/faces/standard/outcometarget01.xhtml?id=config&id2=view", link.getHrefAttribute());
        page = link.click();
        assertEquals("outcometarget01", page.getTitleText());
    }
}
