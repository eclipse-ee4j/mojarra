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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.faces.renderkit.RenderKitUtils;
import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * The webapp enables <code>com.sun.faces.enableCspNonce</code>, so <code>h:button</code> wires its click handler via an inline
 * <code>mojarra.ael()</code> script instead of an <code>onclick</code> attribute. The view loads <code>faces.js</code> via a
 * component resource in the <code>body</code> target, which is rendered at the end of the body, after that inline script.
 */
class Issue6038IT extends BaseIT {

    @FindBy(id = "button")
    private WebElement button;

    @FindBy(id = "clicked")
    private WebElement clicked;

    /**
     * The inline <code>mojarra.ael()</code> script requires <code>faces.js</code> to be loaded at that point. A
     * <code>faces.js</code> component resource which is only rendered later in the body must not count as loaded, and
     * <code>faces.js</code> must still be rendered only once.
     *
     * @see RenderKitUtils#addEventListener
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6038">https://github.com/eclipse-ee4j/mojarra/issues/6038</a>
     */
    @Test
    void testButtonNavigatesWhenFacesJsIsInBodyTarget() {
        open("issue6038.xhtml");
        assertEquals(1, browser.findElements(By.cssSelector("script[src*='jakarta.faces.resource/faces.js']")).size());
        assertEquals("false", clicked.getText());
        guardHttp(button::click);
        assertEquals("true", clicked.getText());
    }

}
