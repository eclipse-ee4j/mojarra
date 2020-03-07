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

package com.sun.faces.cdi;

import jakarta.faces.component.StateHolder;
import jakarta.faces.component.behavior.Behavior;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.BehaviorEvent;

/**
 * A delegate to the CDI managed behavior.
 */
class CdiBehavior implements Behavior, StateHolder {

    /**
     * Stores the behavior id.
     */
    private String behaviorId;

    /**
     * Stores the transient delegate.
     */
    private transient Behavior delegate;

    /**
     * Constructor.
     *
     * @param behaviorId the behavior id.
     * @param delegate the delegate.
     */
    public CdiBehavior(String behaviorId, Behavior delegate) {
        this.behaviorId = behaviorId;
        this.delegate = delegate;
    }

    /**
     * Broadcast the event.
     *
     * @param event the event.
     */
    @Override
    public void broadcast(BehaviorEvent event) {
        getDelegate(event.getFacesContext()).broadcast(event);
    }

    /**
     * Get the delegate.
     *
     * @param facesContext the Faces context.
     * @return the delegate.
     */
    private Behavior getDelegate(FacesContext facesContext) {
        if (delegate == null) {
            delegate = facesContext.getApplication().createBehavior(behaviorId);
        }
        return delegate;
    }

    /**
     * Are we transient?
     *
     * @return false.
     */
    @Override
    public boolean isTransient() {
        return false;
    }

    /**
     * Restore the state.
     *
     * @param facesContext the Faces context.
     * @param state the state.
     */
    @Override
    public void restoreState(FacesContext facesContext, Object state) {
        Object[] stateArray = (Object[]) state;
        behaviorId = (String) stateArray[0];
    }

    /**
     * Save the state.
     *
     * @param facesContext the Faces context.
     * @return the state.
     */
    @Override
    public Object saveState(FacesContext facesContext) {
        return new Object[] { behaviorId };
    }

    /**
     * Set the transient flag.
     *
     * <p>
     * Since our proxy is required to be non-transient we ignore any calls here.
     * </p>
     *
     * @param transientValue the transient value.
     */
    @Override
    public void setTransient(boolean transientValue) {
    }
}
