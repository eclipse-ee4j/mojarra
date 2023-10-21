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

package jakarta.faces.application;

import static java.lang.Boolean.TRUE;

import java.io.IOException;
import java.util.Map;

import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.ResponseStateManager;
import jakarta.faces.view.StateManagementStrategy;
import jakarta.faces.view.ViewDeclarationLanguage;

/**
 * <p>
 * <strong class="changed_modified_2_0 changed_modified_2_1 changed_modified_2_2
 * changed_modified_2_3">StateManager</strong> directs the process of saving and restoring the view between requests.
 * <span class="changed_added_2_0">An implementation of this class must be thread-safe.</span> The {@link StateManager}
 * instance for an application is retrieved from the {@link Application} instance, and thus cannot know any details of
 * the markup language created by the {@link RenderKit} being used to render a view.
 *
 * The {@link StateManager} utilizes a helper object ({@link ResponseStateManager}), that is provided by the
 * {@link RenderKit} implementation and is therefore aware of the markup language details.
 * </p>
 */
public abstract class StateManager {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The <code>ServletContext</code> init parameter consulted by the <code>StateManager</code> to tell where the state
     * should be saved. Valid values are given as the values of the <span class="changed_modified_5_0">enum constants
     * {@link StateSavingMethod}, case insensitive</span>.
     * </p>
     *
     * <p>
     * If this parameter is not specified, the default value is {@link StateSavingMethod#CLIENT}
     * </p>
     */
    public static final String STATE_SAVING_METHOD_PARAM_NAME = "jakarta.faces.STATE_SAVING_METHOD";

    /**
     * <p class="changed_added_2_0">
     * The <code>ServletContext</code> init parameter consulted by the runtime to determine if the partial state saving
     * mechanism should be used.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * If undefined, the runtime must determine the version level of the application.
     * </p>
     *
     * <ul>
     *
     * <li>
     * <p>
     * For applications versioned at 1.2 and under, the runtime must not use the partial state saving mechanism.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * For applications versioned at 2.0 and above, the runtime must use the partial state saving mechanism.
     * </p>
     * </li>
     *
     * </ul>
     *
     * <p>
     * If this parameter is defined, and the application is versioned at 1.2 and under, the runtime must not use the partial
     * state saving mechanism. Otherwise, If this param is defined, and calling <code>toLowerCase().equals("true")</code> on
     * a <code>String</code> representation of its value returns <code>true</code>, the runtime must use partial state
     * mechanism. Otherwise the partial state saving mechanism must not be used.
     * </p>
     *
     * </div>
     *
     * @since 2.0
     * @deprecated Full state saving will be removed in favor of partial state saving in order to keep the spec simple.
     * Therefore disabling partial state saving via this context parameter will not anymore be an option.
     */
    @Deprecated(forRemoval = true, since = "4.1")
    public static final String PARTIAL_STATE_SAVING_PARAM_NAME = "jakarta.faces.PARTIAL_STATE_SAVING";

    /**
     * <p class="changed_added_2_0">
     * The runtime must interpret the value of this parameter as a comma separated list of view IDs, each of which must have
     * their state saved using the state saving mechanism specified in Jakarta Faces 1.2.
     * </p>
     * 
     * @deprecated Full state saving will be removed in favor of partial state saving in order to keep the spec simple.
     * Therefore specifying full state saving view IDs via this context parameter will not anymore be an option.
     */
    @Deprecated(forRemoval = true, since = "4.1")
    public static final String FULL_STATE_SAVING_VIEW_IDS_PARAM_NAME = "jakarta.faces.FULL_STATE_SAVING_VIEW_IDS";

    /**
     * <p class="changed_added_2_1">
     * Marker within the <code>FacesContext</code> attributes map to indicate we are saving state. The implementation must
     * set this marker into the map <b>before</b> starting the state saving traversal and the marker must be cleared, in a
     * finally block, <b>after</b> the traversal is complete.
     * </p>
     */
    public static final String IS_SAVING_STATE = "jakarta.faces.IS_SAVING_STATE";

    /**
     * <p class="changed_added_2_1">
     * Marker within the <code>FacesContext</code> attributes map to indicate we are marking initial state, so the
     * <code>markInitialState()</code> method of iterating components such as {@link jakarta.faces.component.UIData} could
     * recognize this fact and save the initial state of descendents.
     * </p>
     *
     * @since 2.1
     *
     */
    public final static String IS_BUILDING_INITIAL_STATE = "jakarta.faces.IS_BUILDING_INITIAL_STATE";

    /**
     * <p class="changed_added_2_2">
     * If this param is set, and calling toLowerCase().equals("true") on a String representation of its value returns true,
     * and the jakarta.faces.STATE_SAVING_METHOD is set to "server" (as indicated below), the server state must be
     * guaranteed to be Serializable such that the aggregate state implements java.io.Serializable. The intent of this
     * parameter is to ensure that the act of writing out the state to an ObjectOutputStream would not throw a
     * NotSerializableException, but the runtime is not required verify this before saving the state.
     * </p>
     *
     * @since 2.2
     */
    public static final String SERIALIZE_SERVER_STATE_PARAM_NAME = "jakarta.faces.SERIALIZE_SERVER_STATE";

    /**
     * <p>
     * Constant value for the initialization parameter named by the <code>STATE_SAVING_METHOD_PARAM_NAME</code> that
     * indicates state saving should take place on the client.
     * </p>
     * 
     * @deprecated Use {@link StateSavingMethod#CLIENT} instead.
     */
    @Deprecated(since = "5.0", forRemoval = true)
    public static final String STATE_SAVING_METHOD_CLIENT = "client";

    /**
     * <p>
     * Constant value for the initialization parameter named by the <code>STATE_SAVING_METHOD_PARAM_NAME</code> that
     * indicates state saving should take place on the server.
     * </p>
     * 
     * @deprecated Use {@link StateSavingMethod#SERVER} instead.
     */
    @Deprecated(since = "5.0", forRemoval = true)
    public static final String STATE_SAVING_METHOD_SERVER = "server";

    /**
     * <p class="changed_added_5_0">
     * Allowed values for the initialization parameter named by the {@value StateManager#STATE_SAVING_METHOD_PARAM_NAME}.
     * </p>
     * 
     * @since 5.0
     */
    public enum StateSavingMethod {
        
        /**
         * <p>
         * Indicates that state saving should take place on the client.
         * </p>
         */
        CLIENT,
        
        /**
         * <p>
         * Indicates that state saving should take place on the server.
         * </p>
         */
        SERVER;
    }
    

    private Boolean savingStateInClient;

    // ---------------------------------------------------- State Saving Methods


    /**
     * <p>
     * Save the state represented in the specified state <code>Object</code> instance, in an implementation dependent
     * manner.
     * </p>
     *
     * <p>
     * This method will typically simply delegate the actual writing to the <code>writeState()</code> method of the
     * {@link ResponseStateManager} instance provided by the {@link RenderKit} being used to render this view. This method
     * assumes that the caller has positioned the {@link ResponseWriter} at the correct position for the saved state to be
     * written.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @param state the state to be written
     * @throws IOException when an I/O error occurs.
     * @since 1.2
     */
    public void writeState(FacesContext context, Object state) throws IOException {
    }


    // ------------------------------------------------- State Restoring Methods


    /**
     * <p>
     * <span class="changed_modified_2_3">Method</span> to determine if the state is saved on the client.
     * </p>
     *
     * @param context the Faces context.
     * @return <code>true</code> if and only if the value of the <code>ServletContext</code> init parameter named by the
     * value of the constant {@link #STATE_SAVING_METHOD_PARAM_NAME} is equal <span class="changed_modified_2_3">(ignoring
     * case)</span> to the value of the constant <span class="changed_modified_5_0">{@link StateSavingMethod#CLIENT}</span>. <code>false</code> otherwise.
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>.
     */
    public boolean isSavingStateInClient(FacesContext context) {
        if (savingStateInClient != null) {
            return savingStateInClient;
        }
        savingStateInClient = false;

        String saveStateParam = context.getExternalContext().getInitParameter(STATE_SAVING_METHOD_PARAM_NAME);
        if (StateSavingMethod.CLIENT.name().equalsIgnoreCase(saveStateParam)) {
            savingStateInClient = true;
        }

        return savingStateInClient;
    }

    /**
     * <p class="changed_added_2_0">
     * Convenience method to return the view state as a <code>String</code> with no <code>RenderKit</code> specific markup.
     *
     * This default implementation of this method will call {@link StateManagementStrategy#saveView(FacesContext)} and
     * passing the result to and returning the resulting value from
     * {@link ResponseStateManager#getViewState(jakarta.faces.context.FacesContext, Object)}.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @return the view state.
     * @since 2.0
     */
    public String getViewState(FacesContext context) {
        Object savedView = null;

        if (context != null && !context.getViewRoot().isTransient()) {
            String viewId = context.getViewRoot().getViewId();

            ViewDeclarationLanguage vdl = context.getApplication().getViewHandler().getViewDeclarationLanguage(context, viewId);
            if (vdl != null) {
                Map<Object, Object> contextAttributes = context.getAttributes();
                try {
                    contextAttributes.put(IS_SAVING_STATE, TRUE);

                    savedView = vdl.getStateManagementStrategy(context, viewId)
                                      .saveView(context);
                } finally {
                    contextAttributes.remove(IS_SAVING_STATE);
                }
            }
        }

        return context.getRenderKit().getResponseStateManager().getViewState(context, savedView);
    }
}
