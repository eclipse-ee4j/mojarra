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

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlFileInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.sun.faces.test.junit.JsfServerExclude;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import com.sun.faces.test.junit.JsfVersion;
import java.io.File;
import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.Test;

@RunWith(JsfTestRunner.class)
public class Spec802IT {

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
    @Test
    public void testFileUpload() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/inputFile.xhtml");
        HtmlTextInput text;

        String basedir = System.getProperty("basedir");
        HtmlFileInput fileInput = (HtmlFileInput) page.getElementById("file");
        fileInput.setValueAttribute(basedir + File.separator + "inputFileSuccess.txt");

        text = (HtmlTextInput) page.getElementById("text");
        String textValue = "" + System.currentTimeMillis();
        text.setText(textValue);

        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");

        page = button.click();

        String pageText = page.getBody().asText();
        assertTrue(pageText.contains("JSR-344"));

        pageText = page.getElementById("textOutput").getTextContent();
        assertTrue(pageText.contains(textValue));

        page = webClient.getPage(webUrl + "faces/inputFile.xhtml");

        fileInput = (HtmlFileInput) page.getElementById("file");
        fileInput.setValueAttribute(basedir + File.separator + "inputFileFailure.txt");
        button = (HtmlSubmitInput) page.getElementById("button");

        text = (HtmlTextInput) page.getElementById("text");
        textValue = "" + System.currentTimeMillis();
        text.setText(textValue);

        page = button.click();

        pageText = page.getBody().asText();
        assertFalse(pageText.contains("JSR-344"));
        assertTrue(pageText.contains("Invalid file"));

        pageText = page.getElementById("textOutput").getTextContent();
        assertTrue(!pageText.contains(textValue));
    }

    @JsfTest(value = JsfVersion.JSF_2_2_0, excludes = {JsfServerExclude.WEBLOGIC_12_1_3})
    @Test
    public void testFileUploadMultipleTimes() throws Exception {
        webClient = new WebClient(BrowserVersion.FIREFOX_45);
        HtmlPage page = webClient.getPage(webUrl + "faces/uploadMultipleTimes.xhtml");

        String basedir = System.getProperty("basedir");
        HtmlFileInput fileInput = (HtmlFileInput) page.getElementById("file");
        fileInput.setValueAttribute(basedir + File.separator + "inputFileSuccess.txt");

        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");

        page = button.click();

        String pageText = page.getBody().asXml();
        assertTrue(pageText.contains("JSR-344"));

        fileInput = (HtmlFileInput) page.getElementById("file");
        fileInput.setValueAttribute(basedir + File.separator + "inputFileSuccess2.txt");

        button = (HtmlSubmitInput) page.getElementById("button");

        page = button.click();

        pageText = page.getBody().asXml();
        assertTrue(pageText.contains("Additional bytes here."));

        fileInput = (HtmlFileInput) page.getElementById("file");
        fileInput.setValueAttribute(basedir + File.separator + "inputFileSuccess3.txt");

        button = (HtmlSubmitInput) page.getElementById("button");

        page = button.click();

        pageText = page.getBody().asXml();
        assertTrue(pageText.contains("Yet more bytes."));
    }

    @JsfTest(value = JsfVersion.JSF_2_2_0, excludes = {JsfServerExclude.WEBLOGIC_12_1_3})
    @Test
    public void testFileUploadNoEncType() throws Exception {
        webClient = new WebClient();
        HtmlPage page = webClient.getPage(webUrl + "faces/inputFileNoEncType.xhtml");
        if (page.asText().contains("ProjectStage.Development")) {
            assertTrue(page.asText().contains(
                    "File upload component requires a form with an enctype of multipart/form-data"));
        }
    }
}
