/*
 * Copyright (c) Contributors to Eclipse Foundation.
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

package org.glassfish.mojarra.renderkit.html_basic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.HashMap;

import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A behavior event attribute is wired by <code>mojarra.ael()</code>, which locates its element by the component's client id, so a component carrying one gets
 * that id rendered even when the id was auto generated and would otherwise have been left out.
 */
class HtmlBasicRendererIdTest {

    private final GroupRenderer renderer = new GroupRenderer();

    @BeforeEach
    void setUpCurrentFacesContext() throws Exception {
        // The supported event names of a component are resolved through the application map.
        ExternalContext externalContext = mock(ExternalContext.class);
        when(externalContext.getApplicationMap()).thenReturn(new HashMap<>());

        FacesContext facesContext = mock(FacesContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        setCurrentInstance(facesContext);
    }

    @AfterEach
    void tearDownCurrentFacesContext() throws Exception {
        setCurrentInstance(null);
    }

    @Test
    void anAutoIdIsRenderedForARendererSpecificEventAttribute() {
        HtmlPanelGroup component = autoIdComponent();
        component.setOnclick("handle()");

        assertTrue(renderer.shouldWriteIdAttribute(component));
    }

    @Test
    void anAutoIdIsRenderedForAPassThroughEventAttribute() {
        HtmlPanelGroup component = autoIdComponent();
        component.getPassThroughAttributes(true).put("onerror", "fallback()");

        assertTrue(renderer.shouldWriteIdAttribute(component));
    }

    /**
     * A component whose id was never set has none to leave out, so the event attribute is what makes one be generated.
     */
    @Test
    void anAbsentIdIsGeneratedForAnEventAttribute() {
        HtmlPanelGroup component = new HtmlPanelGroup();
        component.getPassThroughAttributes(true).put("onerror", "fallback()");

        assertTrue(renderer.shouldWriteIdAttribute(component));
    }

    /**
     * A delegated handler travels in its own attribute and needs no id, but the renderer cannot tell which element it is about to write, so it forces the id
     * for that case too.
     */
    @Test
    void anAutoIdIsRenderedForADelegatedEventAttributeAsWell() {
        HtmlPanelGroup component = autoIdComponent();
        component.getPassThroughAttributes(true).put("onload", "init()");

        assertTrue(renderer.shouldWriteIdAttribute(component));
    }

    @Test
    void anAutoIdIsNotRenderedForAnAttributeWhichIsNoEventAttribute() {
        HtmlPanelGroup component = autoIdComponent();
        component.getPassThroughAttributes(true).put("data-x", "1");

        assertFalse(renderer.shouldWriteIdAttribute(component));
    }

    @Test
    void anAbsentIdIsNotGeneratedWithoutAnEventAttribute() {
        assertFalse(renderer.shouldWriteIdAttribute(new HtmlPanelGroup()));
    }

    private static void setCurrentInstance(FacesContext facesContext) throws Exception {
        Method setCurrentInstance = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
        setCurrentInstance.setAccessible(true);
        setCurrentInstance.invoke(null, facesContext);
    }

    private static HtmlPanelGroup autoIdComponent() {
        HtmlPanelGroup component = new HtmlPanelGroup();
        component.setId("j_id1");
        return component;
    }

}
