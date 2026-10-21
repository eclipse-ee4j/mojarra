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

package com.sun.faces.facelets.tag.faces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.sun.faces.facelets.tag.faces.core.FacetHandler;

import jakarta.faces.application.Application;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.component.html.HtmlPanelGrid;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.FaceletContext;

class ComponentSupportTest {

    @Test
    void toIdPartReturnsAValidNameItself() {
        String name = "footer-1_a";
        assertSame(name, ComponentSupport.toIdPart(name));
    }

    @Test
    void toIdPartReplacesEveryCharacterAnIdMayNotHold() {
        assertEquals("a_b_c_", ComponentSupport.toIdPart("a.b{c}"));
        assertEquals("_", ComponentSupport.toIdPart("é"));
    }

    @Test
    void createUniqueIdIsVendedByTheClosestUniqueIdVendor() {
        UIForm form = new UIForm();
        HtmlPanelGroup parent = new HtmlPanelGroup();
        parent.setParent(form);

        assertEquals("j_idseed", ComponentSupport.createUniqueId(faceletContext(null), parent, "seed"));
    }

    @Test
    void createUniqueIdFallsBackToTheViewRoot() {
        UIViewRoot root = mock(UIViewRoot.class);
        FaceletContext ctx = faceletContext(root);
        when(root.createUniqueId(ctx.getFacesContext(), "seed")).thenReturn("j_idseed");

        assertEquals("j_idseed", ComponentSupport.createUniqueId(ctx, new HtmlPanelGroup(), "seed"));
    }

    @Test
    void createUniqueIdIsNullWithoutUniqueIdVendorAndViewRoot() {
        assertNull(ComponentSupport.createUniqueId(faceletContext(null), new HtmlPanelGroup(), "seed"));
    }

    /**
     * The panel which holds the components of a facet with more than one of them takes an id derived from its parent
     * and the facet name when it is created, so that the counter of the {@link jakarta.faces.component.UniqueIdVendor}
     * never numbers it while the view is built.
     */
    @Test
    void implicitFacetPanelIsSeededFromParentIdAndFacetName() {
        UIForm form = new UIForm();
        HtmlPanelGrid parent = new HtmlPanelGrid();
        parent.setId("grid");
        parent.setParent(form);
        parent.getAttributes().put(FacetHandler.KEY, "footer");
        FaceletContext ctx = faceletContext(null);
        Application application = mock(Application.class);
        when(ctx.getFacesContext().getApplication()).thenReturn(application);
        when(application.createComponent(UIPanel.COMPONENT_TYPE)).thenReturn(new UIPanel());

        ComponentSupport.addComponent(ctx, parent, new HtmlOutputText());
        ComponentSupport.addComponent(ctx, parent, new HtmlOutputText());

        UIComponent panel = parent.getFacet("footer");
        assertEquals(UIPanel.class, panel.getClass());
        assertEquals("j_idgrid__f_footer", panel.getId());
    }

    private static FaceletContext faceletContext(UIViewRoot viewRoot) {
        FaceletContext ctx = mock(FaceletContext.class);
        FacesContext facesContext = mock(FacesContext.class);
        when(ctx.getFacesContext()).thenReturn(facesContext);
        when(facesContext.getViewRoot()).thenReturn(viewRoot);
        return ctx;
    }
}
