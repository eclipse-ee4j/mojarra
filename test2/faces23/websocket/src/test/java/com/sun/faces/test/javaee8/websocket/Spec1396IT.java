/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation.
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
package com.sun.faces.test.javaee8.websocket;

import static java.lang.System.getProperty;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;
import java.util.function.Predicate;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

@RunWith(Arquillian.class)
public class Spec1396IT {

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

    @Test
    public void testEnableWebsocketEndpoint() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1396EnableWebsocketEndpoint.xhtml");
        assertTrue(page.getHtmlElementById("param").asNormalizedText().equals("true"));
    }

    @Test
    public void testDefaultWebsocket() throws Exception {
        webClient.setIncorrectnessListener((o, i) -> {}); // Suppress false JS errors on websocket URL.
        HtmlPage page = webClient.getPage(webUrl + "spec1396DefaultWebsocket.xhtml");

        String pageSource = page.getWebResponse().getContentAsString();
        assertTrue(pageSource.contains(">faces.push.init("));
        assertTrue(pageSource.contains("/jakarta.faces.push/push?"));

        waitUntilWebsocketIsOpened(page);

        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("form:button");
        page = button.click();

        waitUntilWebsocketIsPushed(page);
        webClient.close(); // This will explicitly close websocket as well. HtmlUnit doesn't seem to like to leave it open before loading next page.
    }

    @Test
    public void testUserScopedWebsocket() throws Exception {
        webClient.setIncorrectnessListener((o, i) -> {}); // Suppress false JS errors on websocket URL.
        HtmlPage page = webClient.getPage(webUrl + "spec1396UserScopedWebsocket.xhtml");

        String pageSource = page.getWebResponse().getContentAsString();
        assertTrue(pageSource.contains(">faces.push.init("));
        assertTrue(pageSource.contains("/jakarta.faces.push/user?"));

        waitUntilWebsocketIsOpened(page);

        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("form:button");
        page = button.click();

        waitUntilWebsocketIsPushed(page);
        webClient.close(); // This will explicitly close websocket as well. HtmlUnit doesn't seem to like to leave it open before loading next page.
    }

    @Test
    public void testViewScopedWebsocket() throws Exception {
        webClient.setIncorrectnessListener((o, i) -> {}); // Suppress false JS errors on websocket URL.
        HtmlPage page = webClient.getPage(webUrl + "spec1396ViewScopedWebsocket.xhtml");

        String pageSource = page.getWebResponse().getContentAsString();
        assertTrue(pageSource.contains(">faces.push.init("));
        assertTrue(pageSource.contains("/jakarta.faces.push/view?"));

        waitUntilWebsocketIsOpened(page);

        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("form:button");
        page = button.click();

        waitUntilWebsocketIsPushed(page);
        webClient.close(); // This will explicitly close websocket as well. HtmlUnit doesn't seem to like to leave it open before loading next page.
    }

    /**
     * HtmlUnit is not capable of waiting until WS is opened. Hence this work around.
     */
    static void waitUntilWebsocketIsOpened(HtmlPage page) throws Exception {
        Predicate<HtmlPage> isWebsocketOpened = p -> "yes".equals(page.getElementById("opened").asNormalizedText());
        int retries = 10;

        while (!isWebsocketOpened.test(page) && retries --> 0) {
            Thread.sleep(300);
        }

        if (!isWebsocketOpened.test(page)) {
            fail("Failed to establish connection with websocket within 3 seconds.");
        }
    }

    /**
     * HtmlUnit is not capable of waiting until WS is pushed. Hence this work around.
     */
    static void waitUntilWebsocketIsPushed(HtmlPage page) throws Exception {
        Predicate<HtmlPage> isWebsocketPushed = p -> "yes".equals(page.getElementById("opened").asNormalizedText());
        int retries = 10;

        while (!isWebsocketPushed.test(page) && retries --> 0) {
            Thread.sleep(300);
        }

        if (!isWebsocketPushed.test(page)) {
            fail("Failed to retrieve push message from websocket within 3 seconds.");
        }
    }

    @After
    public void tearDown() {
        webClient.close();
    }

}
