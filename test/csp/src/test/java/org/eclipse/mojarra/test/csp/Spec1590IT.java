/*
 * Copyright (c) Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GPL-2.0 with Classpath-exception-2.0 which
 * is available at https://openjdk.java.net/legal/gplv2+ce.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 or Apache-2.0
 */
package org.eclipse.mojarra.test.csp;

import static java.net.URI.create;
import static java.net.http.HttpClient.newHttpClient;
import static java.net.http.HttpRequest.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.faces.application.resource.ResourceHandlerImpl;
import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.FindBy;

class Spec1590IT extends BaseIT {

    @FindBy(id = "form1:commandLink")
    private WebElement commandLink;

    @FindBy(id = "form1:commandLinkExecuted")
    private WebElement commandLinkExecuted;

    @FindBy(id = "form2:ajaxInput")
    private WebElement ajaxInput;

    @FindBy(id = "form2:ajaxButton")
    private WebElement ajaxButton;

    @FindBy(id = "form2:ajaxOutput")
    private WebElement ajaxOutput;

    @FindBy(id = "form3:commandScript")
    private WebElement commandScript;

    @FindBy(id = "form3:commandScriptExecuted")
    private WebElement commandScriptExecuted;

    @FindBy(id = "form4:facesUtilChain")
    private WebElement facesUtilChain;

    @FindBy(id = "form4:facesUtilChainExecuted")
    private WebElement facesUtilChainExecuted;

    @FindBy(id = "form5:refreshButton")
    private WebElement refreshButton;

    /**
     * @see ResourceHandlerImpl#ENABLE_CSP_NONCE_PARAM_NAME
     * @see <a href="https://github.com/jakartaee/faces/issues/1590">https://github.com/jakartaee/faces/issues/1590</a>
     */
    @Test
    public void testCommandLinkWithoutAjax() {
        open("spec1590.xhtml");
        var nonce = getNonce();
        assertNotNull(nonce);
        assertEquals(nonce, getBehaviorScriptElement(commandLink).getAttribute("nonce"));
        assertEquals("false", commandLinkExecuted.getText());
        guardHttp(commandLink::click);
        assertEquals("true", commandLinkExecuted.getText());
        // Non-ajax postback must generate a new nonce.
        assertNotEquals(nonce, getNonce());
    }

    /**
     * @see ResourceHandlerImpl#ENABLE_CSP_NONCE_PARAM_NAME
     * @see <a href="https://github.com/jakartaee/faces/issues/1590">https://github.com/jakartaee/faces/issues/1590</a>
     */
    @Test
    public void testAjaxInputAndButton() {
        open("spec1590.xhtml");
        var nonce = getNonce();
        assertNotNull(nonce);
        assertEquals(nonce, getBehaviorScriptElement(ajaxInput).getAttribute("nonce"));
        assertEquals(nonce, getBehaviorScriptElement(ajaxButton).getAttribute("nonce"));
        assertEquals("", ajaxOutput.getText());
        ajaxInput.sendKeys("first");
        guardAjax(ajaxButton::click);
        assertEquals("first", ajaxOutput.getText());
        ajaxInput.clear();
        ajaxInput.sendKeys("second");
        guardAjax(ajaxButton::click);
        assertEquals("second", ajaxOutput.getText());
    }

    /**
     * @see ResourceHandlerImpl#ENABLE_CSP_NONCE_PARAM_NAME
     * @see <a href="https://github.com/jakartaee/faces/issues/1590">https://github.com/jakartaee/faces/issues/1590</a>
     */
    @Test
    public void testCommandScript() {
        open("spec1590.xhtml");
        var nonce = getNonce();
        assertNotNull(nonce);
        assertEquals(nonce, getBehaviorScriptElement(commandScript).getAttribute("nonce"));
        assertEquals("false", commandScriptExecuted.getText());
        guardAjax(() -> ((ChromeDriver)browser).executeScript("commandScript()"));
        assertEquals("true", commandScriptExecuted.getText());
    }

    /**
     * @see ResourceHandlerImpl#ENABLE_CSP_NONCE_PARAM_NAME
     * @see <a href="https://github.com/jakartaee/faces/issues/1590">https://github.com/jakartaee/faces/issues/1590</a>
     */
    @Test
    public void testFacesUtilChain() {
        open("spec1590.xhtml");
        var nonce = getNonce();
        assertNotNull(nonce);
        assertEquals(nonce, getBehaviorScriptElement(facesUtilChain).getAttribute("nonce"));
        assertEquals("false", facesUtilChainExecuted.getText());
        guardAjax(facesUtilChain::click);
        assertEquals("true", facesUtilChainExecuted.getText());
    }

    /**
     * @see ResourceHandlerImpl#ENABLE_CSP_NONCE_PARAM_NAME
     * @see <a href="https://github.com/jakartaee/faces/issues/1590">https://github.com/jakartaee/faces/issues/1590</a>
     */
    @Test
    public void testRefresh() {
        open("spec1590.xhtml");
        var nonce = getNonce();
        assertNotNull(nonce);
        assertEquals(nonce, getBehaviorScriptElement(refreshButton).getAttribute("nonce"));
        guardHttp(refreshButton::click);
        assertNotEquals(nonce, getBehaviorScriptElement(refreshButton).getAttribute("nonce"));

        var nonceAfterRefresh = getNonce();
        assertEquals(nonceAfterRefresh, getBehaviorScriptElement(commandLink).getAttribute("nonce"));
        assertEquals(nonceAfterRefresh, getBehaviorScriptElement(ajaxInput).getAttribute("nonce"));
        assertEquals(nonceAfterRefresh, getBehaviorScriptElement(ajaxButton).getAttribute("nonce"));
        assertEquals(nonceAfterRefresh, getBehaviorScriptElement(commandScript).getAttribute("nonce"));
        assertEquals(nonceAfterRefresh, getBehaviorScriptElement(facesUtilChain).getAttribute("nonce"));
        assertEquals(nonceAfterRefresh, getBehaviorScriptElement(refreshButton).getAttribute("nonce"));
    }

    /**
     * @see ResourceHandlerImpl#CSP_POLICY_PARAM_NAME
     * @see <a href="https://github.com/jakartaee/faces/issues/1590">https://github.com/jakartaee/faces/issues/1590</a>
     */
    @Test
    public void testCspResponseHeader() throws Exception {
        var response = newHttpClient().send(newBuilder(create(baseURL + "spec1590.xhtml"))
                .build(), ofString());
        var cspHeader = response.headers().firstValue("Content-Security-Policy");
        assertTrue(cspHeader.isPresent(), "Content-Security-Policy response header must be present");
        assertTrue(cspHeader.get().contains("script-src"), "Content-Security-Policy response header must contain script-src directive");
        assertTrue(cspHeader.get().contains("'nonce-"), "Content-Security-Policy response header must contain nonce");
    }

    /**
     * @see ResourceHandlerImpl#getCurrentNonce
     * @see <a href="https://github.com/jakartaee/faces/issues/1590">https://github.com/jakartaee/faces/issues/1590</a>
     */
    @Test
    public void testNonceConsistentDuringAjaxPostback() {
        open("spec1590.xhtml");
        var nonce = getNonce();
        assertNotNull(nonce);
        ajaxInput.sendKeys("first");
        guardAjax(ajaxButton::click);
        // Ajax postback must retain the same nonce on faces.js.
        assertEquals(nonce, getNonce());
        // Verify behavior scripts were successfully eval'd during ajax by performing another round-trip.
        // If the event listeners weren't properly re-attached, this would fail.
        ajaxInput.clear();
        ajaxInput.sendKeys("second");
        guardAjax(ajaxButton::click);
        assertEquals("second", ajaxOutput.getText());
        assertEquals(nonce, getNonce());
    }

}
