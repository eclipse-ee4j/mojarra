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

package com.sun.faces.test.cluster.flash.basic;

import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import java.io.IOException;


import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertTrue;
import org.junit.Ignore;

public class KeepMessagesIT {

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


    // ------------------------------------------------------------ Test Methods

    @Test
    @Ignore
    public void testMessagesAreKeptAfterRedirect() throws Exception {

        HtmlPage page = webClient.getPage(webUrl + "/faces/keepMessages.xhtml") ;
        HtmlSubmitInput button = (HtmlSubmitInput) page.getByXPath("//input[contains(@id, 'submit')]").get(0);
        page = button.click();

        assertTrue(-1 != page.asText().indexOf("This is a global message"));

        // submit the page again to make sure messages aren't re-displayed since
        // keep messages isn't set to true on this view
        button = (HtmlSubmitInput) page.getByXPath("//input[contains(@id, 'button')]").get(0);
        page = button.click();

        assertTrue(page.asText().indexOf("This is a global message") == -1);
    }
    
    @Test
    @Ignore
    public void testMessagesAreKeptAfterRedirectAfterDoubleValidationError() throws Exception {
    	
    	HtmlPage page = webClient.getPage(webUrl + "/faces/keepMessages.xhtml") ;
		page = submitRequiredForm(page);
    	assertOnPage(page, "first page");
    	
    	page = submitRequiredForm(page);
    	assertOnPage(page, "first page");
    	
    	HtmlInput requiredInput = (HtmlInput) page.getByXPath("//input[contains(@id, 'requiredInput')]").get(0);
    	requiredInput.setValueAttribute("a value");
    	
    	page = submitRequiredForm(page);
    	assertOnPage(page, "second page");
    	
    	assertTrue("FacesMessage should have survived redirect", page.asText().indexOf("This is a global message") != -1);
    }


	private void assertOnPage(HtmlPage page, String titleText) {
		assertTrue(-1 != page.getTitleText().indexOf(titleText));
	}


	private HtmlPage submitRequiredForm(HtmlPage page) throws IOException {
		HtmlSubmitInput button = (HtmlSubmitInput) page.getByXPath("//input[contains(@id, 'submitRequired')]").get(0);
    	return button.click();
	}

}
