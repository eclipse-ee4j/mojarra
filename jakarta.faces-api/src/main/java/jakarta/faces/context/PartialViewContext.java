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

package jakarta.faces.context;

import java.util.Collection;
import java.util.List;

import jakarta.faces.application.ResourceHandler;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.event.PhaseId;

/**
 * <p>
 * <strong class="changed_added_2_0 changed_modified_2_2 changed_modified_2_3">PartialViewContext</strong> contains
 * methods and properties that pertain to partial request processing and partial response rendering on a view.
 * </p>
 *
 * <p>
 * The {@link PartialViewContext} instance is used to determine if the current request indicates the requirement to
 * perform <code>partial processing</code> and/or <code>partial rendering</code>. Partial processing is the processing
 * of selected components through the <code>execute</code> portion of the request processing lifecycle. Partial
 * rendering is the rendering of specified components in the <code>Render Response Phase</code> of the request
 * processing lifecycle.
 * </p>
 *
 */

public abstract class PartialViewContext {

    /**
     * <p class="changed_added_2_3">
     * The request parameter name whose request parameter value identifies the type of partial event.
     * </p>
     *
     * @since 2.3
     */
    public static final String PARTIAL_EVENT_PARAM_NAME = "jakarta.faces.partial.event";

    /**
     * <p class="changed_added_2_0">
     * The request parameter name whose request parameter value is a <code>Collection</code> of client identifiers
     * identifying the components that must be processed during the <em>Render Response</em> phase of the request processing
     * lifecycle.
     * </p>
     *
     * @since 2.0
     */
    public static final String PARTIAL_RENDER_PARAM_NAME = "jakarta.faces.partial.render";

    /**
     * <p class="changed_added_2_0">
     * The request parameter name whose request parameter value is a <code>Collection</code> of client identifiers
     * identifying the components that must be processed during the <em>Apply Request Values</em>, <em>Process
     * Validations</em>, and <em>Update Model Values</em> phases of the request processing lifecycle.
     * </p>
     *
     * @since 2.0
     */
    public static final String PARTIAL_EXECUTE_PARAM_NAME = "jakarta.faces.partial.execute";

    /**
     * <p class="changed_added_2_2">
     * If the request parameter named by the value of this constant has a parameter value of <code>true</code>, the
     * implementation must return <code>true</code> from {@link #isResetValues}.
     * </p>
     *
     * @since 2.2
     */
    public static final String RESET_VALUES_PARAM_NAME = "jakarta.faces.partial.resetValues";

    /**
     * <p class="changed_added_2_0">
     * The value that when used with {@link #PARTIAL_EXECUTE_PARAM_NAME} or {@link #PARTIAL_RENDER_PARAM_NAME} indicates
     * these phases must be skipped.
     * </p>
     *
     * @since 2.0
     */
    public static final String ALL_PARTIAL_PHASE_CLIENT_IDS = "@all";

    // -------------------------------------------------------------- Properties

    /**
     * <p class="changed_added_2_0">
     * Return a <code>Collection</code> of client identifiers from the current request with the request parameter name
     * {@link #PARTIAL_EXECUTE_PARAM_NAME}. If there is no such request parameter, return an empty <code>Collection</code>.
     * These client identifiers are used to identify components that will be processed during the <code>execute</code> phase
     * of the request processing lifecycle. The returned <code>Collection</code> is mutable.
     * </p>
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 2.0
     *
     * @return the ids for the execute portion of the lifecycle
     */
    public abstract Collection<String> getExecuteIds();

    /**
     * <p class="changed_added_2_0">
     * Return a <code>Collection</code> of client identifiers from the current request with the request parameter name
     * {@link #PARTIAL_RENDER_PARAM_NAME}. If there is no such request parameter, return an empty <code>Collection</code>.
     * These client identifiers are used to identify components that will be processed during the <code>render</code> phase
     * of the request processing lifecycle. The returned <code>Collection</code> is mutable.
     * </p>
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 2.0
     *
     * @return the ids for the render portion of the lifecycle
     */
    public abstract Collection<String> getRenderIds();

    /**
     * <p class="changed_added_2_3">
     * Returns a mutable <code>List</code> of scripts to be evaluated in client side on complete of ajax request.
     * </p>
     *
     * @return A mutable <code>List</code> of scripts to be evaluated in client side on complete of ajax request.
     * @throws IllegalStateException If this method is called after this instance has been released.
     * @since 2.3
     */
    public abstract List<String> getEvalScripts();

    /**
     * <p class="changed_added_2_0">
     * Return the {@link ResponseWriter} to which components should direct their output for partial view rendering. Within a
     * given response, components can use either the ResponseStream or the ResponseWriter, but not both.
     * </p>
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 2.0
     *
     * @return the {@code ResponseWriter} for output
     */
    public abstract PartialResponseWriter getPartialResponseWriter();

    /**
     * <p class="changed_added_2_0">
     * Return <code>true</code> if the request header <code>Faces-Request</code> is present with the value
     * <code>partial/ajax</code>. Otherwise, return <code>false</code>.
     * </p>
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 2.0
     *
     * @return whether or not this is an ajax request
     */
    public abstract boolean isAjaxRequest();

    /**
     * <p class="changed_added_2_0">
     * Return <code>true</code> {@link #isAjaxRequest} returns <code>true</code> or if the request header
     * <code>Faces-Request</code> is present with the value <code>partial/process</code>. Otherwise, return
     * <code>false</code>.
     * </p>
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 2.0
     *
     * @return whether or not this request is partial
     */
    public abstract boolean isPartialRequest();

    /**
     * <p class="changed_added_2_0">
     * Return <code>true</code> if {@link #isAjaxRequest} returns <code>true</code> and {@link #PARTIAL_EXECUTE_PARAM_NAME}
     * is present in the current request with the value {@link #ALL_PARTIAL_PHASE_CLIENT_IDS}. Otherwise, return
     * <code>false</code>.
     * </p>
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 2.0
     *
     * @return whether or not this is an execute all request
     */
    public abstract boolean isExecuteAll();

    /**
     * <p class="changed_added_2_0">
     * Return <code>true</code> if {@link #isAjaxRequest} returns <code>true</code> and {@link #PARTIAL_RENDER_PARAM_NAME}
     * is present in the current request with the value {@link #ALL_PARTIAL_PHASE_CLIENT_IDS}. Otherwise, return
     * <code>false</code>.
     * </p>
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 2.0
     *
     * @return whether or not this is a render all request
     */
    public abstract boolean isRenderAll();

    /**
     * <p class="chaged_added_2_2">
     * Return <code>true</code> if the incoming request has a parameter named by the value of
     * {@link #RESET_VALUES_PARAM_NAME} and that value is <code>true</code>. To preserve backward compatibility with custom
     * implementations that may have extended from an earlier version of this class, an implementation is provided that
     * returns <code>false</code>. A compliant implementation must override this method to take the specified action.
     * </p>
     *
     * @since 2.2
     *
     * @return whether or not this is a reset values request
     */
    public boolean isResetValues() {
        return false;
    }

    /**
     * <p class="changed_added_2_0">
     * Indicate the entire view must be rendered if <code>renderAll</code> is <code>true</code>.
     * </p>
     *
     * @param renderAll the value <code>true</code> indicates the entire view must be rendered.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 2.0
     */
    public abstract void setRenderAll(boolean renderAll);

    /**
     * <p class="changed_added_2_0">
     * Dynamically indicate that this is a partial request.
     * </p>
     *
     * @param isPartialRequest the value <code>true</code> indicates this is a partial request.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 2.0
     */

    public abstract void setPartialRequest(boolean isPartialRequest);

    /**
     * <p>
     * <span class="changed_added_2.0">Release</span> any resources associated with this <code>PartialViewContext</code>
     * instance.
     * </p>
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public abstract void release();

    /**
     * <p class="changed_added_2_0">
     * Perform lifecycle processing on components during the indicated <code>phaseId</code>. Only those components with
     * identifiers existing in the <code>Collection</code> returned from {@link #getExecuteIds} and {@link #getRenderIds}
     * will be processed.
     * </p>
     *
     * <div class="changed_added_2_3">
     * <p>
     * When the indicated <code>phaseId</code> equals {@link PhaseId#RENDER_RESPONSE}, then perform the following tasks in
     * sequence:
     * <ol>
     * <li>If {@link #isResetValues()} returns <code>true</code>, then call
     * {@link UIViewRoot#resetValues(FacesContext, Collection)}, passing {@link #getRenderIds()}.</li>
     * <li>If {@link #isRenderAll()} returns <code>false</code>, then render any component resource of {@link UIViewRoot}
     * whose {@link ResourceHandler#getRendererTypeForResourceName(String)} does not return <code>null</code>, and whose
     * {@link UIComponent#getChildCount()} is zero, and whose
     * {@link ResourceHandler#isResourceRendered(FacesContext, String, String)} returns <code>false</code>, in an
     * <code>update</code> element with an identifier of <code>jakarta.faces.Resource</code>.</li>
     * <li>Process the components.</li>
     * <li>Obtain the state by calling {@link StateManager#getViewState} and write it out as an <code>update</code> element
     * with an identifier of <code>&lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt;&lt;SEP&gt;jakarta.faces.ViewState</code> where
     * <code>&lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt;</code> is the return from
     * {@link UIViewRoot#getContainerClientId(FacesContext)} on the view from whence this state originated, and
     * <code>&lt;SEP&gt;</code> is the currently configured {@link UINamingContainer#getSeparatorChar(FacesContext)}.</li>
     * <li>If {@link #isRenderAll()} returns <code>false</code>, then write out each script of {@link #getEvalScripts()} as
     * an <code>eval</code> element.</li>
     * </ol>
     * </div>
     *
     * @param phaseId the {@link jakarta.faces.event.PhaseId} that indicates the lifecycle phase the components will be
     * processed in.
     */
    public abstract void processPartial(PhaseId phaseId);

}
