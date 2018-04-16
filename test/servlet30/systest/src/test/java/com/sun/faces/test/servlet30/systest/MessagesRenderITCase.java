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

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import junit.framework.Test;
import static junit.framework.TestCase.assertTrue;
import junit.framework.TestSuite;


public class MessagesRenderITCase extends HtmlUnitFacesITCase {

    public MessagesRenderITCase(String name) {
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
        return (new TestSuite(MessagesRenderITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    public void testMessagesToolTip() throws Exception {
        HtmlPage page = getPage("/faces/messages.xhtml");
        String pageXml = page.asXml().replaceAll("\n","");
        pageXml = pageXml.replaceAll("\t","");
        pageXml = pageXml.replaceAll("\r","");
        String case1 = "<!-- Case 1: Expected output: Both summary and detail rendered. -->      <ul>        <li>          This is the summary This is the detail        </li>        <li>          This is the summary This is the detail        </li>      </ul>";
        String case2 = "<!-- Case 2: Expected output: Both summary and detail rendered. Tooltip detail rendered. -->      <ul>        <li>          <span title=" + '"' + "This is the detail" + '"' + ">            This is the summary This is the detail          </span>        </li>        <li>          <span title=" + '"' + "This is the detail" + '"' + ">            This is the summary This is the detail          </span>        </li>      </ul>"; 
        String case3 = "<!-- Case 3: Expected output: Both summary and detail rendered. Tooltip detail rendered. -->      <ul>        <li>          <span title=" + '"' + "This is the detail" + '"' + ">            This is the summary This is the detail          </span>        </li>        <li>          <span title=" + '"' + "This is the detail" + '"' + ">            This is the summary This is the detail          </span>        </li>      </ul>";
        String case4 = "<!-- Case 4: Expected output: Summary rendered. Tooltip detail rendered. -->      <ul>        <li>          <span title=" + '"' + "This is the detail" + '"' + ">            This is the summary           </span>        </li>        <li>          <span title=" + '"' + "This is the detail" + '"' + ">            This is the summary           </span>        </li>      </ul>";
        String case5 = "<!-- Case 5: Expected output: Summary rendered. Tooltip detail rendered. -->      <ul>        <li>          <span title=" + '"' + "This is the detail" + '"' + ">            This is the summary           </span>        </li>        <li>          <span title=" + '"' + "This is the detail" + '"' + ">            This is the summary           </span>        </li>      </ul>";
        String case6 = "<!-- Case 6: Expected output: Summary rendered. Tooltip detail rendered. -->      <ul>        <li>          <span title=" + '"' + "This is the detail" + '"' + ">            This is the summary           </span>        </li>        <li>          <span title=" + '"' + "This is the detail" + '"' + ">            This is the summary           </span>        </li>      </ul>";
        assertTrue(-1 != pageXml.indexOf(case1));
        assertTrue(-1 != pageXml.indexOf(case2));
        assertTrue(-1 != pageXml.indexOf(case3));
        assertTrue(-1 != pageXml.indexOf(case4));
        assertTrue(-1 != pageXml.indexOf(case5));
        assertTrue(-1 != pageXml.indexOf(case6));

    }


}
