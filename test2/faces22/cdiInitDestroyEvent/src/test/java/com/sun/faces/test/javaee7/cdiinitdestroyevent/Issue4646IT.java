/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2019 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.javaee7.cdiinitdestroyevent;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertEquals;

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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

@RunWith(Arquillian.class)
public class Issue4646IT {
    
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
    public void testPreDestroyEventIssue4646() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue4646.xhtml");
        HtmlElement counterElement = (HtmlElement) page.getElementById("counterMessage");
        int currentCount = parseInt(counterElement.asNormalizedText());
        
        // +1
        page = webClient.getPage(webUrl + "faces/issue4646.xhtml");
        counterElement = (HtmlElement) page.getElementById("counterMessage");
        assertEquals("+1 should be the objects created", currentCount + 1, parseInt(counterElement.asNormalizedText()));
        
        // +2
        page = webClient.getPage(webUrl + "faces/issue4646.xhtml");
        counterElement = (HtmlElement) page.getElementById("counterMessage");
        assertEquals("+2 should be the objects created", currentCount + 2, parseInt(counterElement.asNormalizedText()));
        
        // invalidate
        HtmlSubmitInput invalidateButton = (HtmlSubmitInput) page.getElementById("invalidateSession");
        invalidateButton.click();
        
        // should be the initial count
        page = webClient.getPage(webUrl + "faces/issue4646.xhtml");
        counterElement = (HtmlElement) page.getElementById("counterMessage");
        assertEquals("The initial count should be again", currentCount, parseInt(counterElement.asNormalizedText()));
        
        // invalidate again
        invalidateButton = (HtmlSubmitInput) page.getElementById("invalidateSession");
        invalidateButton.click();
    }
}
