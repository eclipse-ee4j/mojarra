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

package com.sun.faces.test.glassfish.undeploy.undeploy2;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.sun.faces.test.junit.JsfServerExclude;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import com.sun.faces.test.junit.JsfVersion;
import java.net.URL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class Issue2398IT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(60000);
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    /**
     * Test for issue #2398.
     *
     * <p>
     * 1. Test if the undeploy #1 application is active. 2. Test if the undeploy
     * #2 application is active. 3. Get the number of active InitFacesContexts.
     * 4. Undeploy 'undeploy #1' 5. Verify the number of active
     * InitFacesContexts stayed the same.
     * </p>
     * 
     * <p>
     *  The test has been turned off for automatic running against Glassfish 4.1 
     *  as this would require enabling secure admin (which is not on by default).
     * </p>
     *
     * @throws Exception when a serious error occurs.
     */
    @JsfTest(value = JsfVersion.JSF_2_2_0_M02, excludes = {JsfServerExclude.GLASSFISH_4_1})
    @Test
    public void testIssue2398() throws Exception {
        HtmlPage page = webClient.getPage(webUrl.substring(0, webUrl.length() - 2) + "2/faces/index.xhtml");

        if (!(page.getWebResponse().getResponseHeaderValue("Server").equals("GlassFish Server Open Source Edition  4.0"))) {
            page = webClient.getPage(webUrl + "faces/index.xhtml");

            page = webClient.getPage(webUrl.substring(0, webUrl.length() - 2) + "1/faces/index.xhtml");
            assertTrue(page.asText().indexOf("Undeploy #1 is active!") != -1);

            page = webClient.getPage(webUrl + "faces/index.xhtml");
            assertTrue(page.asText().indexOf("Undeploy #2 is active!") != -1);

            page = webClient.getPage(webUrl + "faces/count.xhtml");
            Integer count = new Integer(page.asText().trim());

            WebRequest webRequest = new WebRequest(
                    new URL("http://localhost:4848/management/domain/applications/application/test-glassfish-undeploy-undeploy1"),
                    HttpMethod.DELETE);
            webRequest.setAdditionalHeader("X-Requested-By", "127.0.0.1");
            
            DefaultCredentialsProvider credentialsProvider = new DefaultCredentialsProvider();
            credentialsProvider.addCredentials("admin", "adminadmin");
            webClient.setCredentialsProvider(credentialsProvider);
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getPage(webRequest);

            try {
                webClient.getOptions().setPrintContentOnFailingStatusCode(false);
                webClient.getPage(webUrl.substring(0, webUrl.length() - 2) + "1/faces/index.xhtml");
                fail("Undeploy #1 is active!");
            } catch (FailingHttpStatusCodeException exception) {
                assertEquals(404, exception.getStatusCode());
                webClient.getOptions().setPrintContentOnFailingStatusCode(true);
            }

            page = webClient.getPage(webUrl + "faces/count.xhtml");
            Integer newCount = new Integer(page.asText().trim());
            assertTrue(count.intValue() >= newCount.intValue());
        }
    }
}
