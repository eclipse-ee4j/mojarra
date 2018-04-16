/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package javax.faces.application;

import java.io.IOException;

import javax.faces.FacesWrapper;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

/**
 * <p>
 * <span class="changed_modified_2_3">Provides</span> a simple implementation of
 * {@link StateManager} that can be subclassed by developers wishing to provide specialized behavior
 * to an existing {@link StateManager} instance. The default implementation of all methods is to
 * call through to the wrapped {@link StateManager}.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use
 * {@link #getWrapped} to access the instance being wrapped.
 * </p>
 *
 * @since 1.2
 */
public abstract class StateManagerWrapper extends StateManager implements FacesWrapper<StateManager> {

    private StateManager wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public StateManagerWrapper() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this state manager has been decorated, the implementation doing the decorating should push
     * the implementation being wrapped to this constructor. The {@link #getWrapped()} will then
     * return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     * @since 2.3
     */
    public StateManagerWrapper(StateManager wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public StateManager getWrapped() {
        return wrapped;
    }
    

    // ----------------------- Methods from javax.faces.application.StateManager

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link StateManager#saveSerializedView(javax.faces.context.FacesContext)} on the wrapped
     * {@link StateManager} object.
     * </p>
     *
     * @see StateManager#saveSerializedView(javax.faces.context.FacesContext)
     * @since 1.2
     */
    @Override
    public SerializedView saveSerializedView(FacesContext context) {
        return getWrapped().saveSerializedView(context);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link StateManager#saveView(javax.faces.context.FacesContext)} on the wrapped
     * {@link StateManager} object.
     * </p>
     *
     * @see StateManager#saveView(javax.faces.context.FacesContext)
     * @since 1.2
     */
    @Override
    public Object saveView(FacesContext context) {
        return getWrapped().saveView(context);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link StateManager#getTreeStructureToSave(javax.faces.context.FacesContext)} on the wrapped
     * {@link StateManager} object.
     * </p>
     *
     * @see StateManager#getTreeStructureToSave(javax.faces.context.FacesContext)
     * @since 1.2
     */
    @Override
    protected Object getTreeStructureToSave(FacesContext context) {
        return getWrapped().getTreeStructureToSave(context);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link StateManager#getComponentStateToSave(javax.faces.context.FacesContext)} on the wrapped
     * {@link StateManager} object.
     * </p>
     *
     * @see StateManager#getComponentStateToSave(javax.faces.context.FacesContext)
     * @since 1.2
     */
    @Override
    protected Object getComponentStateToSave(FacesContext context) {
        return getWrapped().getComponentStateToSave(context);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link StateManager#writeState(javax.faces.context.FacesContext, java.lang.Object)} on the
     * wrapped {@link StateManager} object.
     * </p>
     *
     * @see StateManager#writeState(javax.faces.context.FacesContext, java.lang.Object)
     * @since 1.2
     */
    @Override
    public void writeState(FacesContext context, Object state) throws IOException {
        getWrapped().writeState(context, state);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link StateManager#writeState(javax.faces.context.FacesContext, javax.faces.application.StateManager.SerializedView)}
     * on the wrapped {@link StateManager} object.
     * </p>
     *
     * @see StateManager#writeState(javax.faces.context.FacesContext,
     *      javax.faces.application.StateManager.SerializedView)
     * @since 1.2
     */
    @Override
    public void writeState(FacesContext context, SerializedView state) throws IOException {
        getWrapped().writeState(context, state);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link StateManager#restoreView(javax.faces.context.FacesContext, String, String)} on the
     * wrapped {@link StateManager} object.
     * </p>
     *
     * @see StateManager#restoreView(javax.faces.context.FacesContext, String, String)
     * @since 1.2
     */
    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId, String renderKitId) {
        return getWrapped().restoreView(context, viewId, renderKitId);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link StateManager#restoreTreeStructure(javax.faces.context.FacesContext, String, String)}
     * on the wrapped {@link StateManager} object.
     * </p>
     *
     * @see StateManager#restoreTreeStructure(javax.faces.context.FacesContext, String, String)
     * @since 1.2
     */
    @Override
    protected UIViewRoot restoreTreeStructure(FacesContext context, String viewId, String renderKitId) {
        return getWrapped().restoreTreeStructure(context, viewId, renderKitId);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link StateManager#restoreComponentState(javax.faces.context.FacesContext, javax.faces.component.UIViewRoot, String)}
     * on the wrapped {@link StateManager} object.
     * </p>
     *
     * @see StateManager#restoreComponentState(javax.faces.context.FacesContext,
     *      javax.faces.component.UIViewRoot, String)
     * @since 1.2
     */
    @Override
    protected void restoreComponentState(FacesContext context, UIViewRoot viewRoot, String renderKitId) {
        getWrapped().restoreComponentState(context, viewRoot, renderKitId);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link StateManager#isSavingStateInClient(javax.faces.context.FacesContext)} on the wrapped
     * {@link StateManager} object.
     * </p>
     *
     * @see StateManager#isSavingStateInClient(javax.faces.context.FacesContext)
     * @since 1.2
     */
    @Override
    public boolean isSavingStateInClient(FacesContext context) {
        return getWrapped().isSavingStateInClient(context);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call
     * {@link StateManager#getViewState(javax.faces.context.FacesContext)} on the wrapped
     * {@link StateManager} object.
     * </p>
     *
     * @since 2.0
     */
    @Override
    public String getViewState(FacesContext context) {
        return getWrapped().getViewState(context);
    }
}
