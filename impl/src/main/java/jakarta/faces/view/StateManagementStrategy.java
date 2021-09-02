/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.faces.view;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_2">Encapsulate</span> the saving and restoring of the view to enable the VDL to take
 * over the responsibility for handling this feature.
 * </p>
 *
 * <p class="changed_added_2_2">
 * Implementations must call {@link jakarta.faces.component.UIComponent#visitTree} on the
 * {@link jakarta.faces.component.UIViewRoot} to perform the saving and restoring of the view in the {@link #saveView}
 * and {@link #restoreView} methods, respectively.
 * </p>
 *
 * @since 2.0
 */
public abstract class StateManagementStrategy {

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">Return</span> the state of the current view in an <code>Object</code> that
     * implements <code>Serializable</code> <span class="changed_modified_2_2">and can be passed to
     * <code>java.io.ObjectOutputStream.writeObject()</code> without causing a <code>java.io.NotSerializableException</code>
     * to be thrown.</span> The default implementation must perform the following algorithm or its semantic equivalent,
     * <span class="changed_modified_2_2">explicitly performing all the steps listed here.</span>
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <ol>
     *
     * <li>
     * <p>
     * If the <code>UIViewRoot</code> of the current view is marked <code>transient</code>, return <code>null</code>
     * immediately.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Traverse the view and verify that each of the client ids are unique. Throw <code>IllegalStateException</code> if more
     * than one client id are the same.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Visit the tree using {@link jakarta.faces.component.UIComponent#visitTree}. For each node, call
     * {@link jakarta.faces.component.UIComponent#saveState}, saving the returned <code>Object</code> in a way such that it
     * can be restored given only its client id. Special care must be taken to handle the case of components that were added
     * or deleted programmatically during this lifecycle traversal, rather than by the VDL.
     * </p>
     * </li>
     *
     * </ol>
     *
     * <p>
     * The implementation must ensure that the {@link jakarta.faces.component.UIComponent#saveState} method is called for
     * each node in the tree.
     * </p>
     *
     * <p>
     * The data structure used to save the state obtained by executing the above algorithm must be
     * <code>Serializable</code>, and all of the elements within the data structure must also be <code>Serializable</code>.
     * </p>
     *
     * </div>
     *
     * @param context the <code>FacesContext</code> for this request.
     *
     * @since 2.0
     *
     * @return the saved view state
     */
    public abstract Object saveView(FacesContext context);

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">Restore</span> the state of the view with information in the request. The default
     * implementation must perform the following algorithm or its semantic equivalent.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <ol>
     *
     * <li>
     *
     * <p class="changed_added_2_2">
     * As in the case of restore view on an initial request, the view metadata must be restored and properly handled as
     * well. Obtain the {@link ViewMetadata} for the current <code>viewId</code>, and from that call
     * {@link ViewMetadata#createMetadataView}. Store the resultant {@link UIViewRoot} in the {@link FacesContext}. Obtain
     * the state of the <code>UIViewRoot</code> from the state <code>Object</code> returned from
     * {@link jakarta.faces.render.ResponseStateManager#getState} and pass that to {@link UIViewRoot#restoreViewScopeState}.
     * </p>
     *
     *
     * <p>
     * Build the view from the markup. For all components in the view that do not have an explicitly assigned id in the
     * markup, the values of those ids must be the same as on an initial request for this view. This view will not contain
     * any components programmatically added during the previous lifecycle run, and it <b>will</b> contain components that
     * were programmatically deleted on the previous lifecycle run. Both of these cases must be handled.
     * </p>
     *
     *
     * </li>
     *
     * <li>
     * <p>
     * Call {@link jakarta.faces.render.ResponseStateManager#getState} to obtain the data structure returned from the
     * previous call to {@link #saveView}.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Visit the tree using {@link jakarta.faces.component.UIComponent#visitTree}. For each node, call
     * {@link jakarta.faces.component.UIComponent#restoreState}, passing the state saved corresponding to the current client
     * id.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Ensure that any programmatically deleted components are removed.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Ensure any programmatically added components are added.
     * </p>
     * </li>
     *
     * </ol>
     *
     * <p>
     * The implementation must ensure that the {@link jakarta.faces.component.UIComponent#restoreState} method is called for
     * each node in the tree, except for those that were programmatically deleted on the previous run through the lifecycle.
     * </p>
     *
     * </div>
     *
     * @param context the <code>FacesContext</code> for this request
     *
     * @param viewId the view identifier for which the state should be restored
     *
     * @param renderKitId the render kit id for this state.
     *
     * @since 2.0
     *
     * @return the root of the restored view
     */
    public abstract UIViewRoot restoreView(FacesContext context, String viewId, String renderKitId);

}
