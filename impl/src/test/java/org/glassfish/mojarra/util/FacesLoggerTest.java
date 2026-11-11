/*
 * Copyright (c) 2022, 2026 Contributors to the Eclipse Foundation.
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
package org.glassfish.mojarra.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FacesLoggerTest {

    private Locale previousDefaultLocale;

    @BeforeEach
    public void setUp() {
        previousDefaultLocale = Locale.getDefault();
    }

    @AfterEach
    public void tearDown() {
        Locale.setDefault(previousDefaultLocale);
    }

    @Test
    public void unresolvedNavigationUS() {
        FacesContext facesContext = facesContextWithViewLocale(Locale.US);
        final Object[] params = { "componentId1", "outcome.xhtml", "viewId.xhtml" };
        Locale.setDefault(new Locale("en", "US"));
        final String result = FacesLogger.RENDERKIT.interpolateMessage(facesContext, "faces.outcometarget.navigation.case.not.resolved", params);
        Assertions.assertEquals("JSF1090: Navigation case outcome.xhtml not resolved for component componentId1 in viewId viewId.xhtml", result);
    }

    @Test
    public void unresolvedNavigationGermany() {
        FacesContext facesContext = facesContextWithViewLocale(Locale.GERMANY);
        final Object[] params = { "componentId1", "outcome.xhtml", "viewId.xhtml" };
        Locale.setDefault(new Locale("de", "DE"));
        final String result = FacesLogger.RENDERKIT.interpolateMessage(facesContext, "faces.outcometarget.navigation.case.not.resolved", params);
        Assertions.assertEquals("JSF1090: Navigations-Fall wurde für Komponente componentId1 nicht aufgelöst.", result);
    }

    private static FacesContext facesContextWithViewLocale(Locale locale) {
        UIViewRoot root = mock(UIViewRoot.class);
        when(root.getLocale()).thenReturn(locale);
        FacesContext facesContext = mock(FacesContext.class);
        when(facesContext.getViewRoot()).thenReturn(root);
        return facesContext;
    }

}
