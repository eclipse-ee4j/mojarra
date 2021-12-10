/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.javaee8.ajax;

import static java.lang.System.getProperty;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertTrue;

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
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.sun.faces.test.htmlunit.IgnoringIncorrectnessListener;

@RunWith(Arquillian.class)
public class Spec790IT {

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
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(120000);
    }

    @Test
    public void testSpec790() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());

        HtmlPage page = webClient.getPage(webUrl + "spec790.xhtml");
        HtmlForm form1 = (HtmlForm) page.getHtmlElementById("form1");
        HtmlInput form1ViewState = form1.getInputByName("jakarta.faces.ViewState");
        HtmlForm form2 = (HtmlForm) page.getHtmlElementById("form2");
        HtmlInput form2ViewState = form2.getInputByName("jakarta.faces.ViewState");
        HtmlForm form3 = (HtmlForm) page.getHtmlElementById("form3");
        HtmlInput form3ViewState = form3.getInputByName("jakarta.faces.ViewState");
        assertTrue(!form1ViewState.getValueAttribute().isEmpty());
        assertTrue(!form2ViewState.getValueAttribute().isEmpty());
        assertTrue(!form3ViewState.getValueAttribute().isEmpty());

        HtmlSubmitInput form1Button = (HtmlSubmitInput) page.getHtmlElementById("form1:button");
        page = form1Button.click();
        webClient.waitForBackgroundJavaScript(60000);
        form1 = (HtmlForm) page.getHtmlElementById("form1");
        form1ViewState = form1.getInputByName("jakarta.faces.ViewState");
        form2 = (HtmlForm) page.getHtmlElementById("form2");
        form2ViewState = form2.getInputByName("jakarta.faces.ViewState");
        form3 = (HtmlForm) page.getHtmlElementById("form3");
        form3ViewState = form3.getInputByName("jakarta.faces.ViewState");
        assertTrue(!form1ViewState.getValueAttribute().isEmpty());
        assertTrue(!form2ViewState.getValueAttribute().isEmpty());
        assertTrue(!form3ViewState.getValueAttribute().isEmpty());

        HtmlAnchor form2Link = (HtmlAnchor) page.getHtmlElementById("form2:link");
        page = form2Link.click();
        webClient.waitForBackgroundJavaScript(60000);
        form1 = (HtmlForm) page.getHtmlElementById("form1");
        form1ViewState = form1.getInputByName("jakarta.faces.ViewState");
        form2 = (HtmlForm) page.getHtmlElementById("form2");
        form2ViewState = form2.getInputByName("jakarta.faces.ViewState");
        form3 = (HtmlForm) page.getHtmlElementById("form3");
        form3ViewState = form3.getInputByName("jakarta.faces.ViewState");
        assertTrue(!form1ViewState.getValueAttribute().isEmpty());
        assertTrue(!form2ViewState.getValueAttribute().isEmpty());
        assertTrue(!form3ViewState.getValueAttribute().isEmpty());

        HtmlAnchor form3Link = (HtmlAnchor) page.getHtmlElementById("form3:link");
        page = form3Link.click();
        webClient.waitForBackgroundJavaScript(60000);
        form1 = (HtmlForm) page.getHtmlElementById("form1");
        form1ViewState = form1.getInputByName("jakarta.faces.ViewState");
        form2 = (HtmlForm) page.getHtmlElementById("form2");
        form2ViewState = form2.getInputByName("jakarta.faces.ViewState");
        form3 = (HtmlForm) page.getHtmlElementById("form3");
        form3ViewState = form3.getInputByName("jakarta.faces.ViewState");
        assertTrue(!form1ViewState.getValueAttribute().isEmpty());
        assertTrue(!form2ViewState.getValueAttribute().isEmpty());
        assertTrue(!form3ViewState.getValueAttribute().isEmpty());
    }

    @Test
    public void testSpec790AjaxNavigation() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());

        HtmlPage page = webClient.getPage(webUrl + "spec790AjaxNavigation.xhtml");
        HtmlForm form = (HtmlForm) page.getHtmlElementById("form");
        HtmlInput formViewState = form.getInputByName("jakarta.faces.ViewState");
        assertTrue(!formViewState.getValueAttribute().isEmpty());

        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("form:button");
        page = button.click();
        webClient.waitForBackgroundJavaScript(60000);
        HtmlForm form1 = (HtmlForm) page.getHtmlElementById("form1");
        HtmlInput form1ViewState = form1.getInputByName("jakarta.faces.ViewState");
        HtmlForm form2 = (HtmlForm) page.getHtmlElementById("form2");
        HtmlInput form2ViewState = form2.getInputByName("jakarta.faces.ViewState");
        assertTrue(!form1ViewState.getValueAttribute().isEmpty());
        assertTrue(!form2ViewState.getValueAttribute().isEmpty());
    }

    @After
    public void tearDown() {
        webClient.close();
    }

}
