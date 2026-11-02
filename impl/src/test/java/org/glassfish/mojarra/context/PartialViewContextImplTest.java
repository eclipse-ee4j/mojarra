/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.mojarra.context;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.faces.application.ResourceHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.context.FacesContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PartialViewContextImplTest {

    private final ResourceHandler resourceHandler = mock(ResourceHandler.class);
    private FacesContext context;
    private List<UIComponent> componentResources;
    private UIComponent restoredResource;
    private UIComponent addedResource;

    @BeforeEach
    void setUp() {
        ComponentResourcesFixture fixture = new ComponentResourcesFixture();
        context = fixture.context;
        componentResources = fixture.componentResources;
        restoredResource = fixture.restoredResource;
        addedResource = fixture.addedResource;
    }

    @Test
    void addedNamelessComponentResourceIsRendered() {
        RestoredComponentResources.remember(context);
        componentResources.add(addedResource);

        assertTrue(PartialViewContextImpl.isComponentResourceToRender(context, resourceHandler, addedResource));
    }

    @Test
    void addedNamedComponentResourceWithRendererTypeIsRendered() {
        RestoredComponentResources.remember(context);
        UIComponent resource = namedResource("jakarta.faces.resource.Script");
        componentResources.add(resource);

        assertTrue(PartialViewContextImpl.isComponentResourceToRender(context, resourceHandler, resource));
    }

    @Test
    void addedNamedComponentResourceWhichIsRenderedAlreadyIsNotRendered() {
        RestoredComponentResources.remember(context);
        UIComponent resource = namedResource("jakarta.faces.resource.Script");
        componentResources.add(resource);
        when(resourceHandler.isResourceRendered(context, "x.js", null)).thenReturn(true);

        assertFalse(PartialViewContextImpl.isComponentResourceToRender(context, resourceHandler, resource));
    }

    @Test
    void addedNamedComponentResourceWithoutRendererTypeIsNotRendered() {
        RestoredComponentResources.remember(context);
        UIComponent resource = namedResource(null);
        componentResources.add(resource);

        assertFalse(PartialViewContextImpl.isComponentResourceToRender(context, resourceHandler, resource));
    }

    @Test
    void addedNamedComponentResourceWithChildrenIsNotRendered() {
        RestoredComponentResources.remember(context);
        UIComponent resource = namedResource("jakarta.faces.resource.Script");
        resource.getChildren().add(new UIOutput());
        componentResources.add(resource);

        assertFalse(PartialViewContextImpl.isComponentResourceToRender(context, resourceHandler, resource));
    }

    @Test
    void restoredComponentResourceIsNotRendered() {
        RestoredComponentResources.remember(context);

        assertFalse(PartialViewContextImpl.isComponentResourceToRender(context, resourceHandler, restoredResource));
    }

    private static UIComponent namedResource(String rendererType) {
        UIComponent resource = new UIOutput();
        resource.getAttributes().put("name", "x.js");
        resource.setRendererType(rendererType);
        return resource;
    }

}
