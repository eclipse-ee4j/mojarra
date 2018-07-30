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

package com.sun.faces.test.servlet30.dynamic;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

public class Issue2373IT {

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
    public void testMoveComponent() throws Exception {

        /*
         * Make sure the Moveable HELLO is before the first panelBox11
         */
        HtmlPage page = webClient.getPage(webUrl + "faces/moveComponent.xhtml");
        assertTrue(page.asXml().indexOf("Moveable HELLO text") < page.asXml().indexOf("form1:panelBox11"));

        /**
         * After clicking make sure it is inside the first panelBox11
         */
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("form1:commandButton11");
        page = button.click();
        assertTrue(page.asXml().indexOf("Moveable HELLO text") > page.asXml().indexOf("form1:panelBox11"));
        assertTrue(page.asXml().indexOf("Moveable HELLO text") < page.asXml().indexOf("form1:commandButton11"));

        /**
         * After clicking the same button make sure it is still there.
         */
        button = (HtmlSubmitInput) page.getElementById("form1:commandButton11");
        page = button.click();
        assertTrue(page.asXml().indexOf("Moveable HELLO text") > page.asXml().indexOf("form1:panelBox11"));
        assertTrue(page.asXml().indexOf("Moveable HELLO text") < page.asXml().indexOf("form1:commandButton11"));
    }

    @Test
    public void testMoveComponent2() throws Exception {

        /*
         * Make sure the Moveable HELLO is before the first panelBox11
         */
        HtmlPage page = webClient.getPage(webUrl + "faces/moveComponent.xhtml");
        assertTrue(page.asXml().indexOf("Moveable HELLO text") < page.asXml().indexOf("form1:subview1:panelBox12"));

        /**
         * After clicking make sure it is inside the first panelBox11
         */
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("form1:subview1:commandButton12");
        page = button.click();
        assertTrue(page.asXml().indexOf("Moveable HELLO text") > page.asXml().indexOf("form1:subview1:panelBox12"));
        assertTrue(page.asXml().indexOf("Moveable HELLO text") < page.asXml().indexOf("form1:subview1:commandButton12"));

        /**
         * And now move it to the first panel box.
         */
        button = (HtmlSubmitInput) page.getElementById("form1:commandButton11");
        page = button.click();
        assertTrue(page.asXml().indexOf("Moveable HELLO text") > page.asXml().indexOf("form1:panelBox11"));
        assertTrue(page.asXml().indexOf("Moveable HELLO text") < page.asXml().indexOf("form1:commandButton11"));
    }

    @Test
    public void testMoveComponent3() throws Exception {
        /*
         * Make sure the Moveable HELLO is before the first panelBox11
         */
        HtmlPage page = webClient.getPage(webUrl + "faces/moveComponent.xhtml");
        assertTrue(page.asXml().indexOf("Moveable HELLO text") < page.asXml().indexOf("form1:subview1:panelBox12"));

        for (int i = 0; i < 10; i++) {
            /**
             * After clicking make sure it is inside the first panelBox11
             */
            HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("form1:subview1:commandButton12");
            page = button.click();
            assertTrue(page.asXml().indexOf("Moveable HELLO text") > page.asXml().indexOf("form1:subview1:panelBox12"));
            assertTrue(page.asXml().indexOf("Moveable HELLO text") < page.asXml().indexOf("form1:subview1:commandButton12"));

            /**
             * And now move it to the first panel box.
             */
            button = (HtmlSubmitInput) page.getElementById("form1:commandButton11");
            page = button.click();
            assertTrue(page.asXml().indexOf("Moveable HELLO text") > page.asXml().indexOf("form1:panelBox11"));
            assertTrue(page.asXml().indexOf("Moveable HELLO text") < page.asXml().indexOf("form1:commandButton11"));

            /**
             * And now move it to the third panel box.
             */
            button = (HtmlSubmitInput) page.getElementById("form1:subview2:commandButton13");
            page = button.click();
            assertTrue(page.asXml().indexOf("Moveable HELLO text") > page.asXml().indexOf("form1:subview2:panelBox11"));
            assertTrue(page.asXml().indexOf("Moveable HELLO text") < page.asXml().indexOf("form1:subview2:commandButton13"));

            /**
             * And now move it to the fourth panel box.
             */
            button = (HtmlSubmitInput) page.getElementById("form1:subview2:subview2b:commandButton14");
            page = button.click();
            assertTrue(page.asXml().indexOf("Moveable HELLO text") > page.asXml().indexOf("form1:subview2:subview2b:panelBox4"));
            assertTrue(page.asXml().indexOf("Moveable HELLO text") < page.asXml().indexOf("form1:subview2:subview2b:commandButton14"));
        }
    }

    @Test
    public void testToggle1() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/toggle.xhtml");
        for (int i = 0; i < 10; i++) {
            String text = page.asXml();
            assertTrue(text.indexOf("Manually added child2") < text.indexOf("Manually added child1"));
            HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("form1:button");
            page = button.click();
            text = page.asXml();
            assertTrue(text.indexOf("Manually added child1") < text.indexOf("Manually added child2"));
            button = (HtmlSubmitInput) page.getElementById("form1:button");
            page = button.click();
        }
    }
}
