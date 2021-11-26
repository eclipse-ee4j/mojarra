/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation.
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

package com.sun.faces.test.javaee7.multiFieldValidation;

import static java.lang.System.getProperty;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlParagraph;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

@RunWith(Arquillian.class)
public class Spec1IT {

    @ArquillianResource
    private URL webUrl;
    private WebClient webClient;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return create(ZipImporter.class, getProperty("finalName") + ".war")
                .importFrom(new File("target/" + getProperty("finalName") + ".war"))
                .as(WebArchive.class);
    }

    @Before
    public void setUp() {
        webClient = new WebClient();
    }

    @After
    public void tearDown() {
        webClient.close();
    }
    
    @Test
    public void testSimpleInvalidField() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        
        HtmlTextInput password1 = page.getHtmlElementById("password1");
        password1.setValueAttribute("foofoofoo");

        HtmlTextInput password2 = page.getHtmlElementById("password2");
        password2.setValueAttribute("bar");
        
        HtmlSubmitInput button = page.getHtmlElementById("submit");
        
        page = button.click();
        
        String pageText = page.asXml();
        assertTrue(!pageText.contains("[foofoofoo]"));
        assertTrue(pageText.contains("[bar]"));
        
        assertTrue(!pageText.contains("Password fields must match"));
        
        HtmlParagraph password1Value = page.getHtmlElementById("password1Value");
        assertTrue(password1Value.asNormalizedText().isEmpty());
        
        HtmlParagraph password2Value = page.getHtmlElementById("password2Value");
        assertTrue(password2Value.asNormalizedText().isEmpty());
    }
    
    @Test
    public void testSimpleInvalidFields() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        
        HtmlTextInput password1 = page.getHtmlElementById("password1");
        password1.setValueAttribute("foo");

        HtmlTextInput password2 = page.getHtmlElementById("password2");
        password2.setValueAttribute("bar");
        
        HtmlSubmitInput button = page.getHtmlElementById("submit");
        
        page = button.click();
        
        String pageText = page.asXml();
        assertTrue(pageText.contains("[foo]"));
        assertTrue(pageText.contains("[bar]"));
        
        assertTrue(!pageText.contains("Password fields must match"));
        
        HtmlParagraph password1Value = page.getHtmlElementById("password1Value");
        assertTrue(password1Value.asNormalizedText().isEmpty());
        
        HtmlParagraph password2Value = page.getHtmlElementById("password2Value");
        assertTrue(password2Value.asNormalizedText().isEmpty());
    }
    
    @Test
    public void testSimpleValidFieldsInvalidBean() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        
        HtmlTextInput password1 = page.getHtmlElementById("password1");
        password1.setValueAttribute("foofoofoo");

        HtmlTextInput password2 = page.getHtmlElementById("password2");
        password2.setValueAttribute("barbarbar");
        
        HtmlSubmitInput button = page.getHtmlElementById("submit");
        
        page = button.click();
        
        String pageText = page.asXml();
        assertTrue(!pageText.contains("[foofoofoo]"));
        assertTrue(!pageText.contains("[barbarbar]"));
        
        assertTrue(pageText.contains("Password fields must match"));

        HtmlParagraph password1Value = page.getHtmlElementById("password1Value");
        assertTrue(password1Value.asNormalizedText().isEmpty());
        
        HtmlParagraph password2Value = page.getHtmlElementById("password2Value");
        assertTrue(password2Value.asNormalizedText().isEmpty());
        
    }
    
    @Test
    public void testSimpleValidFieldsValidBean() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        
        HtmlTextInput password1 = page.getHtmlElementById("password1");
        password1.setValueAttribute("foofoofoo");

        HtmlTextInput password2 = page.getHtmlElementById("password2");
        password2.setValueAttribute("foofoofoo");
        
        HtmlSubmitInput button = page.getHtmlElementById("submit");
        
        page = button.click();
        
        String pageText = page.asXml();
        assertTrue(!pageText.contains("[foofoofoo]"));
        assertTrue(!pageText.contains("[barbarbar]"));
        
        assertTrue(!pageText.contains("Password fields must match"));

        HtmlParagraph password1Value = page.getHtmlElementById("password1Value");
        assertTrue(password1Value.asNormalizedText().contains("foofoofoo"));
        
        HtmlParagraph password2Value = page.getHtmlElementById("password2Value");
        assertTrue(password2Value.asNormalizedText().contains("foofoofoo"));
        
    }
    
    @Test
    public void testFailingPreconditionsNotAfterAllInputComponents() throws Exception {
    	try {
    		// In this test f:validateWholeBean is misplaced (does not appear after
    		// all input components), which should result in an exception    		
    		webClient.getPage(webUrl + "faces/failingDevTimePreconditions.xhtml");
    		fail("Exception should have been thrown resulting in a 500 http status code");
    	} catch (FailingHttpStatusCodeException e) {
    		assertEquals(500, e.getStatusCode());
    	}
    }
    
    
}
