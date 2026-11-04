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
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RestoredComponentResourcesTest {

    private FacesContext context;
    private PartialViewContext partialViewContext;
    private List<UIComponent> componentResources;
    private UIComponent restoredResource;
    private UIComponent addedResource;

    @BeforeEach
    void setUp() {
        ComponentResourcesFixture fixture = new ComponentResourcesFixture();
        context = fixture.context;
        partialViewContext = fixture.partialViewContext;
        componentResources = fixture.componentResources;
        restoredResource = fixture.restoredResource;
        addedResource = fixture.addedResource;
    }

    @Test
    void componentResourceAddedAfterRestoreIsAddedDuringRequest() {
        RestoredComponentResources.remember(context);
        componentResources.add(addedResource);

        assertTrue(RestoredComponentResources.isAddedDuringRequest(context, addedResource));
        assertFalse(RestoredComponentResources.isAddedDuringRequest(context, restoredResource));
    }

    @Test
    void componentResourceAddedAgainAfterRestoreIsNotAddedDuringRequest() {
        RestoredComponentResources.remember(context);
        componentResources.remove(restoredResource);
        componentResources.add(restoredResource);

        assertFalse(RestoredComponentResources.isAddedDuringRequest(context, restoredResource));
    }

    @Test
    void noComponentResourceIsAddedDuringRequestWhileViewRootIsReplaced() {
        RestoredComponentResources.remember(context);
        componentResources.add(addedResource);
        UIViewRoot restoredViewRoot = context.getViewRoot();

        when(context.getViewRoot()).thenReturn(new UIViewRoot());
        assertFalse(RestoredComponentResources.isAddedDuringRequest(context, addedResource));

        when(context.getViewRoot()).thenReturn(restoredViewRoot);
        assertTrue(RestoredComponentResources.isAddedDuringRequest(context, addedResource));
    }

    @Test
    void noComponentResourceIsAddedDuringRequestWhenNoViewWasRestored() {
        componentResources.add(addedResource);

        assertFalse(RestoredComponentResources.isAddedDuringRequest(context, addedResource));
    }

    @Test
    void noComponentResourceIsAddedDuringRequestWhenNotPartialRequest() {
        when(partialViewContext.isPartialRequest()).thenReturn(false);

        RestoredComponentResources.remember(context);
        componentResources.add(addedResource);

        assertFalse(RestoredComponentResources.isAddedDuringRequest(context, addedResource));
    }

}
