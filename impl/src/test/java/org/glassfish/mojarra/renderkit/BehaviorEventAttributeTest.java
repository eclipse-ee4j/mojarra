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

package org.glassfish.mojarra.renderkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.html.HtmlBody;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.context.ResponseWriter;

import org.glassfish.mojarra.renderkit.AttributeManager.Key;
import org.glassfish.mojarra.renderkit.html_basic.TestResponseWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * An attribute whose name starts with "on" becomes a client event listener only when the component names the event after "on" in its
 * <code>ClientBehaviorHolder#getEventNames()</code>, so that a page author writing an event the component cannot chain behaviors on does not silently get a
 * listener for it. A component which is no <code>ClientBehaviorHolder</code> has no such list to check against, so all of its event attributes are honored.
 */
class BehaviorEventAttributeTest {

    private FacesContext facesContext;
    private ResponseWriter writer;
    private List<String> scripts;

    @BeforeEach
    void setUpCurrentFacesContext() throws Exception {
        ExternalContext externalContext = mock(ExternalContext.class);
        when(externalContext.getApplicationMap()).thenReturn(new HashMap<>());

        scripts = new ArrayList<>();
        PartialViewContext partialViewContext = mock(PartialViewContext.class);
        when(partialViewContext.isAjaxRequest()).thenReturn(true);
        when(partialViewContext.getEvalScripts()).thenReturn(scripts);

        facesContext = mock(FacesContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(facesContext.getPartialViewContext()).thenReturn(partialViewContext);

        writer = new TestResponseWriter(new StringWriter());
        setCurrentInstance(facesContext);
    }

    @AfterEach
    void tearDownCurrentFacesContext() throws Exception {
        setCurrentInstance(null);
    }

    @Test
    void aSupportedEventAttributeBecomesAListener() throws Exception {
        assertTrue(render(new HtmlPanelGroup(), Key.PANELGROUP, "oninput").contains("'input',['handle()']"));
    }

    @Test
    void anUnsupportedEventAttributeIsIgnored() throws Exception {
        assertEquals("", render(new HtmlPanelGroup(), Key.PANELGROUP, "onclck"));
    }

    @Test
    void aWindowEventAttributeIsSupportedByTheBodyOnly() throws Exception {
        assertTrue(render(new HtmlBody(), Key.OUTPUTBODY, "onpagehide").contains("'pagehide',['handle()']"));
        assertEquals("", render(new HtmlPanelGroup(), Key.PANELGROUP, "onpagehide"));
    }

    @Test
    void anEventAttributeOfANonBehaviorHolderBecomesAListener() throws Exception {
        assertTrue(render(new HtmlOutputText(), Key.OUTPUTTEXT, "onclick").contains("'click',['handle()']"));
    }

    private String render(UIComponent component, Key key, String name) throws Exception {
        component.getAttributes().put(name, "handle()");
        scripts.clear();

        RenderKitUtils.renderPassThruAttributes(facesContext, writer, component, AttributeManager.getAttributes(key));
        RenderKitUtils.flushPendingBehaviorEventListeners(facesContext, component, "j_id1");

        return String.join(";", scripts);
    }

    private static void setCurrentInstance(FacesContext facesContext) throws Exception {
        Method setCurrentInstance = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
        setCurrentInstance.setAccessible(true);
        setCurrentInstance.invoke(null, facesContext);
    }

}
