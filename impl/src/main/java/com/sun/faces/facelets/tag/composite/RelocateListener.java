/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.facelets.tag.composite;

import jakarta.faces.application.Resource;
import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.view.Location;

/**
 * Base class for listeners used to relocate children and facets within the context of composite components.
 */
abstract class RelocateListener implements ComponentSystemEventListener, StateHolder {

    // ------------------------------------------------ Methods from StateHolder

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        return null;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }
    }

    @Override
    public boolean isTransient() {
        return true;
    }

    @Override
    public void setTransient(boolean newTransientValue) {
        // no-op
    }

    // ----------------------------------------------------- Private Methods

    /**
     * @return the <code>Resource</code> instance that was used to create the argument composite component.
     */
    protected Resource getBackingResource(UIComponent component) {

        assert UIComponent.isCompositeComponent(component);
        Resource resource = (Resource) component.getAttributes().get(Resource.COMPONENT_RESOURCE_KEY);
        if (resource == null) {
            throw new IllegalStateException("Backing resource information not found in composite component attribute map");
        }
        return resource;

    }

    /**
     * @return <code>true</code> if the argument handler is from the same template source as the argument
     * <code>Resource</code> otherwise <code>false</code>
     */
    protected boolean resourcesMatch(Resource compositeResource, Location handlerLocation) {

        String resName = compositeResource.getResourceName();
        return handlerLocation.getPath().contains(resName);

    }

}
