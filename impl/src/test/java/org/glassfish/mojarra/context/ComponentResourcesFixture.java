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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;

/**
 * A mocked partial request whose view root has the component resources of {@link #componentResources}, initially only {@link #restoredResource}.
 */
final class ComponentResourcesFixture {

    final FacesContext context = mock(FacesContext.class);
    final PartialViewContext partialViewContext = mock(PartialViewContext.class);
    final List<UIComponent> componentResources = new ArrayList<>();
    final UIComponent restoredResource = new UIOutput();
    final UIComponent addedResource = new UIOutput();

    ComponentResourcesFixture() {
        UIViewRoot viewRoot = new UIViewRoot() {

            @Override
            public List<UIComponent> getComponentResources(FacesContext context) {
                return componentResources;
            }

        };

        when(context.getAttributes()).thenReturn(new HashMap<>());
        when(context.getViewRoot()).thenReturn(viewRoot);
        when(context.getPartialViewContext()).thenReturn(partialViewContext);
        when(partialViewContext.isPartialRequest()).thenReturn(true);
        componentResources.add(restoredResource);
    }

}
