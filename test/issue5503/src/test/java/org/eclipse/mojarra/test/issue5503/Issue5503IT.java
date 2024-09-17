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
package org.eclipse.mojarra.test.issue5503;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * https://github.com/eclipse-ee4j/mojarra/issues/5503
 */
class Issue5503IT extends BaseIT {

    @FindBy(id = "protectedViewLink")
    private WebElement protectedViewLink;
    
    @FindBy(id = "unprotectedViewLink")
    private WebElement unprotectedViewLink;

    @Test
    void testOpeningProtectedViewWithXhtmlMapping() {
        open("issue5503-protected.xhtml");
        assertEquals("issue5503 - ProtectedViewException", getPageTitle());
    }

    @Test
    void testOpeningProtectedViewWithJsfMapping() {
        open("issue5503-protected.jsf");
        assertEquals("issue5503 - ProtectedViewException", getPageTitle());
    }

    @Test
    void testOpeningProtectedViewWithFacesMapping() {
        open("faces/issue5503-protected.xhtml");
        assertEquals("issue5503 - ProtectedViewException", getPageTitle());
    }

    @Test
    void testLinkingProtectedViewWithXhtmlMapping() {
        open("issue5503-unprotected.xhtml");
        assertEquals("issue5503 - unprotected view", getPageTitle());
        assertEquals("issue5503-unprotected.xhtml", getHrefURI(unprotectedViewLink));
        assertTrue(getHrefURI(protectedViewLink).startsWith("issue5503-protected.xhtml?jakarta.faces.Token="), "'" + getHrefURI(protectedViewLink) + "' starts with 'issue5503-protected.xhtml?jakarta.faces.Token='");

        guardHttp(protectedViewLink::click);
        assertEquals("issue5503 - protected view", getPageTitle());
        assertEquals("issue5503-unprotected.xhtml", getHrefURI(unprotectedViewLink));
        assertTrue(getHrefURI(protectedViewLink).startsWith("issue5503-protected.xhtml?jakarta.faces.Token="), "'" + getHrefURI(protectedViewLink) + "' starts with 'issue5503-protected.xhtml?jakarta.faces.Token='");
    }

    @Test
    void testLinkingProtectedViewWithJsfMapping() {
        open("issue5503-unprotected.jsf");
        assertEquals("issue5503 - unprotected view", getPageTitle());
        assertEquals("issue5503-unprotected.jsf", getHrefURI(unprotectedViewLink));
        assertTrue(getHrefURI(protectedViewLink).startsWith("issue5503-protected.jsf?jakarta.faces.Token="), "'" + getHrefURI(protectedViewLink) + "' starts with 'issue5503-protected.jsf?jakarta.faces.Token='");

        guardHttp(protectedViewLink::click);
        assertEquals("issue5503 - protected view", getPageTitle());
        assertEquals("issue5503-unprotected.jsf", getHrefURI(unprotectedViewLink));
        assertTrue(getHrefURI(protectedViewLink).startsWith("issue5503-protected.jsf?jakarta.faces.Token="), "'" + getHrefURI(protectedViewLink) + "' starts with 'issue5503-protected.jsf?jakarta.faces.Token='");
    }

    @Test
    void testLinkingProtectedViewWithFacesMapping() {
        open("faces/issue5503-unprotected.xhtml");
        assertEquals("issue5503 - unprotected view", getPageTitle());
        assertEquals("faces/issue5503-unprotected.xhtml", getHrefURI(unprotectedViewLink));
        assertTrue(getHrefURI(protectedViewLink).startsWith("faces/issue5503-protected.xhtml?jakarta.faces.Token="), "'" + getHrefURI(protectedViewLink) + "' starts with 'faces/issue5503-protected.xhtml?jakarta.faces.Token='");

        guardHttp(protectedViewLink::click);
        assertEquals("issue5503 - protected view", getPageTitle());
        assertEquals("faces/issue5503-unprotected.xhtml", getHrefURI(unprotectedViewLink));
        assertTrue(getHrefURI(protectedViewLink).startsWith("faces/issue5503-protected.xhtml?jakarta.faces.Token="), "'" + getHrefURI(protectedViewLink) + "' starts with 'faces/issue5503-protected.xhtml?jakarta.faces.Token='");
    }
}
