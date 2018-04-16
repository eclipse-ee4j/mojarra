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

package test.java.com.sun.faces.test.javaee8.localizedResources;

import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Issue4082IT {

    private String webUrl;
    private WebClient webClient;

    private static final ArrayList<String> expectedAlerts = new ArrayList<String>() {
        {
            add("J'ai gagné Roland Garros ...(resources/fr/1_0/css/js/rafa.js/1_2.js)");
            add("I won Wimbledon ...(resources/en/1_0/css/js/rafa.js/1_1.js)");
            add("I won US Open ...(resources/us/1_0/css/js/rafa.js/1_0.js)");
            add("I won Australian Open ...(resources/au/1_0/css/js/rafa.js/1_0.js)");
        }
    };

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    public Issue4082IT() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void testLocalizedResources() throws Exception {

        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(true);

        // test locale="en" (default)
        final List collectedAlertsEN = new ArrayList();
        webClient.setAlertHandler(new CollectingAlertHandler(collectedAlertsEN));

        HtmlPage pageEN = webClient.getPage(webUrl + "faces/Issue4082.xhtml");

        Object alertEN = collectedAlertsEN.get(0);
        validateAlert(alertEN, 1);

        // test locale="fr"
        final List collectedAlertsFR = new ArrayList();
        webClient.setAlertHandler(new CollectingAlertHandler(collectedAlertsFR));

        HtmlSubmitInput submitFR = (HtmlSubmitInput) pageEN.getHtmlElementById("atp:rg");
        HtmlPage pageFR = submitFR.click();

        Object alertFR = collectedAlertsFR.get(0);
        validateAlert(alertFR, 0);

        // test locale="au"
        final List collectedAlertsAU = new ArrayList();
        webClient.setAlertHandler(new CollectingAlertHandler(collectedAlertsAU));

        HtmlSubmitInput submitAU = (HtmlSubmitInput) pageFR.getHtmlElementById("atp:au");
        HtmlPage pageAU = submitAU.click();

        Object alertAU = collectedAlertsAU.get(0);
        validateAlert(alertAU, 3);

        // test locale="us"
        final List collectedAlertsUS = new ArrayList();
        webClient.setAlertHandler(new CollectingAlertHandler(collectedAlertsUS));

        HtmlSubmitInput submitUS = (HtmlSubmitInput) pageAU.getHtmlElementById("atp:us");
        HtmlPage pageUS = submitUS.click();

        Object alertUS = collectedAlertsUS.get(0);
        validateAlert(alertUS, 2);

        // test locale="en" from submit
        final List collectedAlertsEN2 = new ArrayList();
        webClient.setAlertHandler(new CollectingAlertHandler(collectedAlertsEN2));

        HtmlSubmitInput submitEN2 = (HtmlSubmitInput) pageUS.getHtmlElementById("atp:wo");
        submitEN2.click();

        Object alertEN2 = collectedAlertsEN2.get(0);
        validateAlert(alertEN2, 1);
    }

    private static void validateAlert(Object alert, int pos) {
        if (alert != null) {
            assertEquals(alert, expectedAlerts.get(pos));
        } else {
            fail("No alerts detected, so no JavaScript resource was loaded");
        }
    }
}
