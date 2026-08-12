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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.faces.application.resource.ResourceHandlerImpl;
import jakarta.faces.event.PostConstructViewMapEvent;
import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * The webapp enables <code>com.sun.faces.enableCspNonce</code> and the view is declared
 * <code>&lt;f:view transient="true"&gt;</code>.
 */
class Issue5952IT extends BaseIT {

    @FindBy(id = "form:commandLink")
    private WebElement commandLink;

    @FindBy(id = "form:commandLinkExecuted")
    private WebElement commandLinkExecuted;

    /**
     * A stateless view has no view map to carry the nonce across requests, so obtaining the nonce must not create
     * one. Creating it would publish a {@link PostConstructViewMapEvent}, which acquires a session and registers
     * an unreachable view map in it, defeating the statelessness of the view.
     *
     * @see ResourceHandlerImpl#getCurrentNonce
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/5952">https://github.com/eclipse-ee4j/mojarra/issues/5952</a>
     */
    @Test
    public void testStatelessViewAcquiresNoSession() throws Exception {
        var response = newHttpClient().send(newBuilder(create(baseURL + "issue5952.xhtml"))
                .build(), ofString());
        assertTrue(response.body().contains("value=\"stateless\""), "View must be stateless");

        var cspHeader = response.headers().firstValue("Content-Security-Policy");
        assertTrue(cspHeader.isPresent(), "Content-Security-Policy response header must be present");
        assertTrue(cspHeader.get().contains("'nonce-"), "Content-Security-Policy response header must contain nonce");

        assertFalse(response.headers().allValues("Set-Cookie").stream().anyMatch(cookie -> cookie.contains("JSESSIONID")),
                "Stateless view must not acquire a session");
    }

    /**
     * A stateless view must still get a nonce on its behavior scripts, and a fresh one on every request.
     *
     * @see ResourceHandlerImpl#ENABLE_CSP_NONCE_PARAM_NAME
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/5952">https://github.com/eclipse-ee4j/mojarra/issues/5952</a>
     */
    @Test
    public void testNonceOnStatelessView() {
        open("issue5952.xhtml");
        var nonce = getBehaviorScriptElement(commandLink).getAttribute("nonce");
        assertNotNull(nonce);
        assertNotEquals("", nonce);
        assertEquals("false", commandLinkExecuted.getText());
        guardHttp(commandLink::click);
        assertEquals("true", commandLinkExecuted.getText());
        assertNotEquals(nonce, getBehaviorScriptElement(commandLink).getAttribute("nonce"));
    }

}
