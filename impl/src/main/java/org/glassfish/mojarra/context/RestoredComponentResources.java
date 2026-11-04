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

import static java.util.Collections.newSetFromMap;
import static org.glassfish.mojarra.util.RequestStateManager.RESTORED_COMPONENT_RESOURCES;

import java.util.IdentityHashMap;
import java.util.Set;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.event.PhaseId;

import org.glassfish.mojarra.util.RequestStateManager;

/**
 * The component resources which the view had when the <em>Restore View</em> phase of a partial postback restored or obtained it. A component resource counts as
 * added during the current request if it is not among them, and only while the current view root is still the restored or obtained one. See
 * {@link PartialViewContext#processPartial(PhaseId)}.
 */
public final class RestoredComponentResources {

    private final UIViewRoot viewRoot;
    private final Set<UIComponent> componentResources;

    private RestoredComponentResources(FacesContext context) {
        viewRoot = context.getViewRoot();
        componentResources = newSetFromMap(new IdentityHashMap<>());
        componentResources.addAll(viewRoot.getComponentResources(context));
    }

    /**
     * Remembers the component resources of the restored or obtained view, when the current request is a partial request.
     *
     * @param context the involved faces context
     */
    public static void remember(FacesContext context) {
        if (context.getPartialViewContext().isPartialRequest()) {
            RequestStateManager.set(context, RESTORED_COMPONENT_RESOURCES, new RestoredComponentResources(context));
        }
    }

    /**
     * Returns whether the given component resource counts as added during the current request. This is never the case when no view was restored or obtained,
     * nor while the current view root is another one than the restored or obtained one.
     *
     * @param context the involved faces context
     * @param componentResource the component resource
     * @return whether the given component resource counts as added during the current request
     */
    static boolean isAddedDuringRequest(FacesContext context, UIComponent componentResource) {
        RestoredComponentResources restored = RequestStateManager.get(context, RESTORED_COMPONENT_RESOURCES);
        return restored != null && context.getViewRoot() == restored.viewRoot && !restored.componentResources.contains(componentResource);
    }

}
