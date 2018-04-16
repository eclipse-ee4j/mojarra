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

import static java.lang.Boolean.TRUE;

import java.io.IOException;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.render.ResponseStateManager;

/**
 * <p>
 * <strong class="changed_modified_2_0 changed_modified_2_1 changed_modified_2_2
 * changed_modified_2_3">StateManager</strong> directs the process of saving and restoring the view
 * between requests. <span class="changed_added_2_0">An implementation of this class must be
 * thread-safe.</span> The {@link StateManager} instance for an application is retrieved from the
 * {@link Application} instance, and thus cannot know any details of the markup language created by
 * the {@link RenderKit} being used to render a view. 
 * 
 * The {@link StateManager} utilizes a helper
 * object ({@link ResponseStateManager}), that is provided by the {@link RenderKit} implementation
 * and is therefore aware of the markup language details.
 * </p>
 */
public abstract class StateManager {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The <code>ServletContext</code> init parameter consulted by the <code>StateManager</code> to
     * tell where the state should be saved. Valid values are given as the values of the constants:
     * {@link #STATE_SAVING_METHOD_CLIENT} or {@link #STATE_SAVING_METHOD_SERVER}.
     * </p>
     * 
     * <p>
     * If this parameter is not specified, the default value is the value of the constant
     * {@link #STATE_SAVING_METHOD_CLIENT}.
     * </p>
     */
    public static final String STATE_SAVING_METHOD_PARAM_NAME = "javax.faces.STATE_SAVING_METHOD";

    /**
     * <p class="changed_added_2_0">
     * The <code>ServletContext</code> init parameter consulted by the runtime to determine if the
     * partial state saving mechanism should be used.
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
     * For applications versioned at 1.2 and under, the runtime must not use the partial state
     * saving mechanism.
     * </p>
     * </li>
     * 
     * <li>
     * <p>
     * For applications versioned at 2.0 and above, the runtime must use the partial state saving
     * mechanism.
     * </p>
     * </li>
     * 
     * </ul>
     * 
     * <p>
     * If this parameter is defined, and the application is versioned at 1.2 and under, the runtime
     * must not use the partial state saving mechanism. Otherwise, If this param is defined, and
     * calling <code>toLowerCase().equals("true")</code> on a <code>String</code> representation of
     * its value returns <code>true</code>, the runtime must use partial state mechanism. Otherwise
     * the partial state saving mechanism must not be used.
     * </p>
     * 
     * </div>
     *
     * @since 2.0
     */
    public static final String PARTIAL_STATE_SAVING_PARAM_NAME = "javax.faces.PARTIAL_STATE_SAVING";

    /**
     * <p class="changed_added_2_0">
     * The runtime must interpret the value of this parameter as a comma separated list of view IDs,
     * each of which must have their state saved using the state saving mechanism specified in JSF
     * 1.2.
     * </p>
     */
    public static final String FULL_STATE_SAVING_VIEW_IDS_PARAM_NAME = "javax.faces.FULL_STATE_SAVING_VIEW_IDS";

    /**
     * <p class="changed_added_2_1">
     * Marker within the <code>FacesContext</code> attributes map to indicate we are saving state.
     * The implementation must set this marker into the map <b>before</b> starting the state saving
     * traversal and the marker must be cleared, in a finally block, <b>after</b> the traversal is
     * complete.
     * </p>
     */
    public static final String IS_SAVING_STATE = "javax.faces.IS_SAVING_STATE";

    /**
     * <p class="changed_added_2_1">
     * Marker within the <code>FacesContext</code> attributes map to indicate we are marking initial
     * state, so the <code>markInitialState()</code> method of iterating components such as
     * {@link javax.faces.component.UIData} could recognize this fact and save the initial state of
     * descendents.
     * </p>
     * 
     * @since 2.1
     * 
     */
    public final static String IS_BUILDING_INITIAL_STATE = "javax.faces.IS_BUILDING_INITIAL_STATE";

    /**
     * <p class="changed_added_2_2">
     * If this param is set, and calling toLowerCase().equals("true") on a String representation of
     * its value returns true, and the javax.faces.STATE_SAVING_METHOD is set to "server" (as
     * indicated below), the server state must be guaranteed to be Serializable such that the
     * aggregate state implements java.io.Serializable. The intent of this parameter is to ensure
     * that the act of writing out the state to an ObjectOutputStream would not throw a
     * NotSerializableException, but the runtime is not required verify this before saving the
     * state.
     * </p>
     * 
     * @since 2.2
     */
    public static final String SERIALIZE_SERVER_STATE_PARAM_NAME = "javax.faces.SERIALIZE_SERVER_STATE";

    /**
     * <p>
     * Constant value for the initialization parameter named by the
     * <code>STATE_SAVING_METHOD_PARAM_NAME</code> that indicates state saving should take place on
     * the client.
     * </p>
     */
    public static final String STATE_SAVING_METHOD_CLIENT = "client";

    /**
     * <p>
     * Constant value for the initialization parameter named by the
     * <code>STATE_SAVING_METHOD_PARAM_NAME</code> that indicates state saving should take place on
     * the server.
     * </p>
     */
    public static final String STATE_SAVING_METHOD_SERVER = "server";


    private static final String IS_CALLED_FROM_API_CLASS = "javax.faces.ensureOverriddenInvocation";
    
    private Boolean savingStateInClient;
    
    
    // ---------------------------------------------------- State Saving Methods

    /**
     * <p>
     * Return the tree structure and component state information for the view contained in the
     * specified {@link FacesContext} instance as an object of type
     * <code>StateManager.SerializedView</code>. If there is no state information to be saved,
     * return <code>null</code> instead.
     * </p>
     * 
     * <p>
     * Components may opt out of being included in the serialized view by setting their
     * <code>transient</code> property to <code>true</code>. This must cause the component itself,
     * as well as all of that component's children and facets, to be omitted from the saved tree
     * structure and component state information.
     * </p>
     * 
     * <p>
     * This method must also enforce the rule that, for components with non-null <code>id</code>s,
     * all components that are descendants of the same nearest {@link NamingContainer} must have
     * unique identifiers.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @return the serialized view, or <b>null</b>.
     * @throws IllegalStateException if more than one component or facet within the same
     *             {@link NamingContainer} in this view has the same non-<code>null</code> component
     *             id
     * @deprecated this has been replaced by {@link #saveView}. The default implementation calls
     *             <code>saveView</code> and inspects the return. If the return is an
     *             <code>Object []</code>, it casts the result to an <code>Object []</code> wrapping
     *             the first and second elements in an instance of {@link SerializedView}, which it
     *             then returns. Otherwise, it returns <code>null</code>
     * 
     */
    public SerializedView saveSerializedView(FacesContext context) {

        context.getAttributes().put(IS_CALLED_FROM_API_CLASS, TRUE);
        Object stateObj = null;
        try {
            stateObj = saveView(context);
        } finally {
            context.getAttributes().remove(IS_CALLED_FROM_API_CLASS);
        }

        SerializedView result = null;
        if (stateObj instanceof Object[]) {
            Object[] state = (Object[]) stateObj;
            if (state.length == 2) {
                result = new SerializedView(state[0], state[1]);
            }
        }
        
        return result;
    }

    /**
     * <p>
     * <span class="changed_deleted_2_2">The functionality of this method is now handled by
     * {@link javax.faces.view.StateManagementStrategy#saveView}. </span> Return an opaque
     * <code>Object</code> containing sufficient information for this same instance to restore the
     * state of the current {@link UIViewRoot} on a subsequent request. The returned object must
     * implement <code>java.io.Serializable</code>. If there is no state information to be saved,
     * return <code>null</code> instead.
     * </p>
     * 
     * <p>
     * Components may opt out of being included in the serialized view by setting their
     * <code>transient</code> property to <code>true</code>. This must cause the component itself,
     * as well as all of that component's children and facets, to be omitted from the saved tree
     * structure and component state information.
     * </p>
     * 
     * <p>
     * This method must also enforce the rule that, for components with non-null <code>id</code>s,
     * all components that are descendants of the same nearest {@link NamingContainer} must have
     * unique identifiers.
     * </p>
     * 
     * <p>
     * For backwards compatability with existing <code>StateManager</code> implementations, the
     * default implementation of this method calls {@link #saveSerializedView} and creates and
     * returns a two element <code>Object</code> array with element zero containing the
     * <code>structure</code> property and element one containing the <code>state</code> property of
     * the <code>SerializedView</code>.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @return the saved view.
     * @throws IllegalStateException if more than one component or facet within the same
     *             {@link NamingContainer} in this view has the same non-<code>null</code> component
     *             id
     * @since 1.2
     */
    @Deprecated
    public Object saveView(FacesContext context) {
        Object stateArray[] = null;

        if (!context.getAttributes().containsKey(IS_CALLED_FROM_API_CLASS)) {
            SerializedView view = saveSerializedView(context);
            if (view != null) {
                stateArray = new Object[] { view.getStructure(), view.getState() };
            }
        }
        
        return stateArray;
    }

    /**
     * <p>
     * Convenience method, which must be called by <code>saveSerializedView()</code>, to construct
     * and return a <code>Serializable</code> object that represents the structure of the entire
     * component tree (including children and facets) of this view.
     * </p>
     * 
     * <p>
     * Components may opt-out of being included in the tree structure by setting their
     * <code>transient</code> property to <code>true</code>. This must cause the component itself,
     * as well as all of that component's children and facets, to be omitted from the saved tree
     * structure information.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @return the tree structure, or <b>null</b>.
     * @deprecated the distinction between tree structure and component state is now an
     *             implementation detail. The default implementation returns <code>null</code>.
     */
    protected Object getTreeStructureToSave(FacesContext context) {
        return null;
    }

    /**
     * <p>
     * Convenience method, which must be called by <code>saveSerializedView()</code>, to construct
     * and return a <code>Serializable</code> object that represents the state of all component
     * properties, attributes, and attached objects, for the entire component tree (including
     * children and facets) of this view.
     * </p>
     * 
     * <p>
     * Components may opt-out of being included in the component state by setting their
     * <code>transient</code> property to <code>true</code>. This must cause the component itself,
     * as well as all of that component's children and facets, to be omitted from the saved
     * component state information.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @return the component state, or <b>null</b>.
     * @deprecated the distinction between tree structure and component state is now an
     *             implementation detail. The default implementation returns <code>null</code>.
     */
    protected Object getComponentStateToSave(FacesContext context) {
        return null;
    }

    /**
     * <p>
     * Save the state represented in the specified state <code>Object</code> instance, in an
     * implementation dependent manner.
     * </p>
     * 
     * <p>
     * This method will typically simply delegate the actual writing to the
     * <code>writeState()</code> method of the {@link ResponseStateManager} instance provided by the
     * {@link RenderKit} being used to render this view. This method assumes that the caller has
     * positioned the {@link ResponseWriter} at the correct position for the saved state to be
     * written.
     * </p>
     * 
     * <p>
     * For backwards compatability with existing <code>StateManager</code> implementations, the
     * default implementation of this method checks if the argument is an instance of
     * <code>Object []</code> of length greater than or equal to two. If so, it creates a
     * <code>SerializedView</code> instance with the tree structure coming from element zero and the
     * component state coming from element one and calls through to
     * {@link #writeState(javax.faces.context.FacesContext,javax.faces.application.StateManager.SerializedView)}.
     * If not, does nothing.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @param state the Serializable state to be written, as returned by {@link #saveSerializedView}
     * @throws IOException when an I/O error occurs.
     * @since 1.2
     */
    public void writeState(FacesContext context, Object state) throws IOException {
        if (state != null && state.getClass().isArray() && state.getClass().getComponentType().equals(Object.class)) {
            Object stateArray[] = (Object[]) state;
            if (stateArray.length == 2) {
                SerializedView view = new SerializedView(stateArray[0], stateArray[1]);
                writeState(context, view);
            }
        }
    }

    /**
     * <p>
     * Save the state represented in the specified <code>SerializedView</code> isntance, in an
     * implementation dependent manner.
     * </p>
     * 
     * <p>
     * This method must consult the context initialization parameter named by the symbolic constant
     * <code>StateManager.STATE_SAVING_METHOD_PARAM_NAME</code> to determine whether state should be
     * saved on the client or the server. If not present, client side state saving is assumed.
     * </p>
     * 
     * <p>
     * If the init parameter indicates that client side state saving should be used, this method
     * must delegate the actual writing to the <code>writeState()</code> method of the
     * {@link ResponseStateManager} instance provided by the {@link RenderKit} being used to render
     * this view. This method assumes that the caller has positioned the {@link ResponseWriter} at
     * the correct position for the saved state to be written.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @param state the serialized state to be written
     * @throws IOException when an I/O error occurs.
     * @deprecated This method has been replaced by
     *             {@link #writeState(javax.faces.context.FacesContext,java.lang.Object)}. The
     *             default implementation calls the non-deprecated variant of the method passing an
     *             <code>Object []</code> as the second argument, where the first element of the
     *             array is the return from <code>getStructure()</code> and the second is the return
     *             from <code>getState()</code> on the argument <code>state</code>.
     *
     */
    public void writeState(FacesContext context, SerializedView state) throws IOException {
        if (state != null) {
            writeState(context, new Object[] { state.getStructure(), state.getState() });
        }
    }
    

    // ------------------------------------------------- State Restoring Methods

    /**
     * <p>
     * <span class="changed_deleted_2_2">The functionality of this method is now handled by
     * {@link javax.faces.view.StateManagementStrategy#restoreView}. </span> Restore the tree
     * structure and the component state of the view for the specified <code>viewId</code>, in an
     * implementation dependent manner, and return the restored {@link UIViewRoot}. If there is no
     * saved state information available for this <code>viewId</code>, return <code>null</code>
     * instead.
     * </p>
     * 
     * <p>
     * This method must consult the context initialization parameter named by the symbolic constant
     * <code>StateManager.STATE_SAVING_METHOD_PARAM_NAME</code> to determine whether state should be
     * saved on the client or the server. If not present, client side state saving is assumed.
     * </p>
     * 
     * <p>
     * If the init parameter indicates that client side state saving should be used, this method
     * must call the <code>getTreeStructureToRestore()</code> and (if the previous method call
     * returned a non-null value) <code>getComponentStateToRestore()</code> methods of the
     * {@link ResponseStateManager} instance provided by the {@link RenderKit} responsible for this
     * view.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @param viewId View identifier of the view to be restored
     * @param renderKitId the renderKitId used to render this response. Must not be
     *            <code>null</code>.
     *
     * @return the view root, or <code>null</code>.
     * @throws IllegalArgumentException if <code>renderKitId</code> is <code>null</code>.
     */
    @Deprecated
    public abstract UIViewRoot restoreView(FacesContext context, String viewId, String renderKitId);

    /**
     * <p>
     * Convenience method, which must be called by <code>restoreView()</code>, to construct and
     * return a {@link UIViewRoot} instance (populated with children and facets) representing the
     * tree structure of the component tree being restored. If no saved state information is
     * available, return <code>null</code> instead.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @param viewId View identifier of the view to be restored
     * @param renderKitId the renderKitId used to render this response. Must not be
     *            <code>null</code>.
     * @return the view root, or <code>null</code>.
     * @throws IllegalArgumentException if <code>renderKitId</code> is <code>null</code>.
     * @deprecated the distinction between tree structure and component state is now an
     *             implementation detail. The default implementation returns <code>null</code>.
     */
    protected UIViewRoot restoreTreeStructure(FacesContext context, String viewId, String renderKitId) {
        return null;
    }

    /**
     * <p>
     * Convenience method, which must be called by <code>restoreView()</code>, to restore the
     * attributes, properties, and attached objects of all components in the restored component
     * tree.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @param viewRoot {@link UIViewRoot} returned by a previous call to
     *            <code>restoreTreeStructure()</code>
     * @param renderKitId the renderKitId used to render this response. Must not be
     *            <code>null</code>.
     *
     * @throws IllegalArgumentException if <code>renderKitId</code> is <code>null</code>.
     * @deprecated the distinction between tree structure and component state is now an
     *             implementation detail. The default implementation does nothing.
     */
    protected void restoreComponentState(FacesContext context, UIViewRoot viewRoot, String renderKitId) {
    }

    /**
     * <p>
     * <span class="changed_modified_2_3">Method</span> to determine if the state is saved on the
     * client.
     * </p>
     * 
     * @param context the Faces context.
     * @return <code>true</code> if and only if the value of the <code>ServletContext</code> init
     *         parameter named by the value of the constant {@link #STATE_SAVING_METHOD_PARAM_NAME}
     *         is equal <span class="changed_modified_2_3">(ignoring case)</span> to the value of
     *         the constant {@link #STATE_SAVING_METHOD_CLIENT}. <code>false</code> otherwise.
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>.
     */
    public boolean isSavingStateInClient(FacesContext context) {
        if (null != savingStateInClient) {
            return savingStateInClient.booleanValue();
        }
        savingStateInClient = Boolean.FALSE;

        String saveStateParam = context.getExternalContext().getInitParameter(STATE_SAVING_METHOD_PARAM_NAME);
        if (saveStateParam != null && saveStateParam.equalsIgnoreCase(STATE_SAVING_METHOD_CLIENT)) {
            savingStateInClient = Boolean.TRUE;
        }
        return savingStateInClient.booleanValue();
    }

    /**
     * <p>
     * Convenience struct for encapsulating tree structure and component state. This is necessary to
     * allow the API to be flexible enough to work in JSP and non-JSP environments.
     * </p>
     *
     * @deprecated This class was not marked <code>Serializable</code> in the 1.0 version of the
     *             spec. It was also not a static inner class, so it can't be made to be
     *             <code>Serializable</code>. Therefore, it is being deprecated in version 1.2 of
     *             the spec. The replacement is to use an implementation dependent
     *             <code>Object</code>.
     */

    public class SerializedView extends Object {
        private Object structure = null;
        private Object state = null;

        public SerializedView(Object newStructure, Object newState) {
            structure = newStructure;
            state = newState;
        }

        public Object getStructure() {
            return structure;
        }

        public Object getState() {
            return state;
        }
    }

    /**
     * <p class="changed_added_2_0">
     * Convenience method to return the view state as a <code>String</code> with no
     * <code>RenderKit</code> specific markup.
     *
     * This default implementation of this method will call
     * {@link #saveView(javax.faces.context.FacesContext)} and passing the result to and returning
     * the resulting value from
     * {@link ResponseStateManager#getViewState(javax.faces.context.FacesContext, Object)}.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @return the view state.
     * @since 2.0
     */
    public String getViewState(FacesContext context) {
        Object state = saveView(context);
        return context.getRenderKit().getResponseStateManager().getViewState(context, state);
    }
}
