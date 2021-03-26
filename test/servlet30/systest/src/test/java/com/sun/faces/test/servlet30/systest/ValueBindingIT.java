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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ValueBindingIT {

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
    
    /*
     * ValueBinding Test #1 (Simple Bean Getter)
     */
    @Test
    public void testValueBinding1() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/valueBinding01.jsp");
        assertTrue(Pattern.matches("(?s).*/valueBinding01.jsp PASSED.*", page.asXml()));
    }

    /*
     * ValueBinding Test #2 (Simple String Property Getter)
     */
    @Test
    public void testValueBinding2() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/valueBinding02.jsp");
        assertTrue(Pattern.matches("(?s).*/valueBinding02.jsp PASSED.*", page.asXml()));
    }
    
    /*
     * ValueBinding Test #3 (Simple Integer Property Getter) 
     */
    @Test
    public void testValueBinding3() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/valueBinding03.jsp");
        assertTrue(Pattern.matches("(?s).*/valueBinding03.jsp PASSED.*", page.asXml()));
    }
    
    /*
     * ValueBinding Test #4 (Simple Boolean Property Getter)
     */
    @Test
    public void testValueBinding4() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/valueBinding04.jsp");
        assertTrue(Pattern.matches("(?s).*/valueBinding04.jsp PASSED.*", page.asXml()));
    }
    
    /*
     * ValueBinding Test #5 (Simple Integer Expression Getter)
     */
    @Test
    public void testValueBinding5() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/valueBinding05.jsp");
        assertTrue(Pattern.matches("(?s).*/valueBinding05.jsp PASSED.*", page.asXml()));
    }
    
    /*
     * ValueBinding Test #6 (Simple Boolean Expression Getter)
     */
    @Test
    public void testValueBinding6() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/valueBinding06.jsp");
        assertTrue(Pattern.matches("(?s).*/valueBinding06.jsp PASSED.*", page.asXml()));
    }
    
    /*
     * ValueBinding Test #7 (Mixed Literal and Expression Getter)
     */
    @Test
    public void testValueBinding7() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/valueBinding07.jsp");
        assertTrue(Pattern.matches("(?s).*/valueBinding07.jsp PASSED.*", page.asXml()));
    }
}
