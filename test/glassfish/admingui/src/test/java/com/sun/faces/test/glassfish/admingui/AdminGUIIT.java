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

package com.sun.faces.test.glassfish.admingui;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.util.Cookie;
import java.util.Iterator;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class AdminGUIIT {

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
    public void testAppHasDeployForm() throws Exception {
        HtmlPage page = null;
        HtmlSubmitInput button;

        CookieManager cm = webClient.getCookieManager();
        cm.clearCookies();

        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setTimeout(6000000);

        String url = "http://localhost:4848/";

        for (int i = 0; i < 10; i++) {
            try {
                page = webClient.getPage(url + "common/index.jsf?bare=true");
                System.out.println("line 89");
                System.out.println(page.asXml());
            } catch (Exception e) {
                try {
                    page = webClient.getPage(url + "common/index.jsf");
                    System.out.println("line 93");
                    System.out.println(page.asXml());
                } catch (Exception e2) {
                    page = null;
                }
            }

            if (page != null) {
                HtmlElement element = page.getHtmlElementById("Login.username");
                if (element != null) {
                    break;
                }
            }
            Thread.sleep(30000);
        }

        /** 20150930-edburns
        page.getHtmlElementById("Login.username").type("admin");
        page.getHtmlElementById("Login.password").type("adminadmin");
        */
        page = page.getHtmlElementById("loginButton").click();

        Cookie jSessionID = cm.getCookie("JSESSIONID");
        Cookie c1 = new Cookie("", "_common_applications_uploadFrame.jsf", "left:0&top:0&badCookieChars:%28%2C%29%2C%3C%2C%3E%2C@%2C%2C%2C%3B%2C%3A%2C%5C%2C%22%2C/%2C%5B%2C%5D%2C%3F%2C%3D%2C%7B%2C%7D%2C%20%2C%09; treeForm_tree-hi=treeForm:tree:applications; JSESSIONID=" + jSessionID.getValue());
        cm.addCookie(c1);

        page = webClient.getPage(url + "common/applications/applications.jsf?bare=true");

        System.out.println(page.asXml());
        
        /***
        HtmlSubmitInput deployButton = page.getHtmlElementById("propertyForm:deployTable:topActionsGroup1:deployButton");
        page = deployButton.click();
        
        DomNodeList<DomElement> forms = page.getElementsByTagName("form");
        Iterator<DomElement> formIter = forms.iterator();
        boolean foundUplaodForm = false;
        while (formIter.hasNext()) {
            DomElement cur = formIter.next();
            String actionAttr = cur.getAttribute("action");
            if (null != actionAttr && actionAttr.contains("upload")) {
                foundUplaodForm = true;
            }
        }
        assertTrue(foundUplaodForm);
        ***/
    }
}
