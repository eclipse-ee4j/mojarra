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

package com.sun.faces.test.webprofile.flow.issue4279getNavigationCaseNotIdempotent;

import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.IOException;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Assert;
import static org.junit.Assert.fail;

public class Issue4279GetNavigationCaseNotIdempotentIT {
    /**
     * Stores the web URL.
     */
    private String webUrl;
    /**
     * Stores the web client.
     */
    private WebClient webClient;

    /**
     * Setup before testing.
     * 
     * @throws Exception when a serious error occurs.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    /**
     * Cleanup after testing.
     * 
     * @throws Exception when a serious error occurs.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Setup before testing.
     */
    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
System.err.println("setUp: webUrl = " + webUrl);
        webClient = new WebClient();
    }

    /**
     * Tear down after testing.
     */
    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testIssue4279GetNavigationCaseNotIdempotent() throws Exception {

		doTestIssue4279GetNavigationCaseNotIdempotent("call_getNavigationCase_and_returnFromFlow2");
		doTestIssue4279GetNavigationCaseNotIdempotent("returnFromFlow2");
	}

	private void doTestIssue4279GetNavigationCaseNotIdempotent(String returnFromFlow2ButtonId) throws IOException {

		HtmlPage page = webClient.getPage(webUrl);
		assertNotNull(page.getElementById("flow1"));
		HtmlInput flow1Button = (HtmlInput) page.getElementById("flow1");
		page = flow1Button.click();
		assertNotNull(page.getElementById("callFlow2"));
		HtmlInput callFlow2Button = (HtmlInput) page.getElementById("callFlow2");
		page = callFlow2Button.click();
		assertNotNull(page.getElementById("returnFromFlow2"));
		HtmlInput returnFromFlow2Button = (HtmlInput) page.getElementById(returnFromFlow2ButtonId);
		try {
			page = returnFromFlow2Button.click();
		} catch(FailingHttpStatusCodeException exception) {
			throw new AssertionError("Failed to exit flow2 and return to flow1 after clicking " + returnFromFlow2ButtonId, exception);
		}
		assertNotNull(page.getElementById("returnFromFlow1"));
		HtmlInput returnFromFlow1Button = (HtmlInput) page.getElementById("returnFromFlow1");
		page = returnFromFlow1Button.click();
		assertNotNull(page.getElementById("flow1"));
	}
}
