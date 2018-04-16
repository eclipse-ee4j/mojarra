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

package com.sun.faces.test.servlet31.facelets;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlFileInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.sun.faces.test.junit.JsfServerExclude;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import com.sun.faces.test.junit.JsfVersion;
import java.io.File;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class Issue2326IT {

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

    @JsfTest(value = JsfVersion.JSF_2_2_0, excludes = {JsfServerExclude.WEBLOGIC_12_1_3})
    public void testFileException() throws Exception {
        webClient = new WebClient();
        HtmlPage page = webClient.getPage(webUrl + "faces/inputFileNegative.xhtml");

        String basedir = System.getProperty("basedir");
        HtmlFileInput fileInput = (HtmlFileInput) page.getElementById("file");
        fileInput.setValueAttribute(basedir + File.separator + "inputFileSuccess.txt");

        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");

        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        page = button.click();

        String pageText = page.getBody().asText();
        assertTrue(pageText.contains("Negative test, intentional failure"));
    }
}
