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

package jakarta.faces.flow;

import java.util.Map;

import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_2">
 * <strong class="changed_modified_2_3">FlowHandler</strong> is the main entry point that enables the runtime to
 * interact with the faces flows feature. {@link jakarta.faces.application.NavigationHandler} uses this class when it
 * needs to make navigational decisions related to flows. The faces flow feature entirely depends on the
 * {@link jakarta.faces.lifecycle.ClientWindow} feature and also on CDI.
 * </p>
 *
 * <div class="changed_added_2_2">
 *
 * <p>
 * <strong>Defining Flows</strong>
 * </p>
 *
 * <p>
 * The implementation must support defining faces flows using the <code>&lt;flow-definition&gt;</code> element as
 * specified in the Application Configuration Resources XML Schema Definition, or by using the
 * {@link jakarta.faces.flow.builder.FlowBuilder} API. Additional means of defining flows may be provided by decorating
 * the {@link FlowHandlerFactory}.
 * </p>
 *
 * <p>
 * <strong>Managing Flows</strong>
 * </p>
 *
 * <blockquote>
 *
 * <p>
 * The singleton instance of this class must be thread safe, and therefore must not store any per-user state. Flows are,
 * however, traversed in a per-user manner, and must be associated with the current
 * {@link jakarta.faces.lifecycle.ClientWindow}. Furthermore, Flows may be nested. These requirements strongly suggest
 * managing the flows with a stack-like runtime data structure, stored in a per-user fashion and associated with the
 * {@code ClientWindow}. <span class="changed_added_2_3">Because Flow instances are immutable, yet the flow stack is
 * per-user, implementations must make allowance for flow scoped data (managed beans declared to be {@link FlowScoped}
 * and data stored in the <code>Map</code> returned by {@link #getCurrentFlowScope}) to be fully re-entrant. For
 * example, consider an application with two flows, A and B, each with a single <code>FlowScoped</code> bean
 * <code>MyBeanA</code> and <code>MyBeanB</code>, respectively. Entry into flow A causes <code>MyBeanA</code> to become
 * active. Calling from A into B causes <code>MyBeanB</code> to become active. Calling back into A causes a new instance
 * of <code>MyBeanA</code> to become active, rather than reactivating the earlier instance of <code>MyBeanA</code>.
 * </span>
 * </p>
 *
 * <p>
 * <strong>The Flow Graph</strong>
 * </p>
 *
 * <p>
 * Prior versions of the specification defined a flow graph but the only kind of node in the graph was a VDL view. The
 * Faces Flow feature currently defines the following node types.
 * </p>
 *
 * <ul>
 *
 * <li>
 * <p>
 * View
 * </p>
 *
 * <p>
 * This is the regular Jakarta Faces VDL View that has been in the specification since the beginning.
 * </p>
 *
 * </li>
 *
 * <li>
 * <p>
 * Switch
 * </p>
 *
 * <p>
 * This is a list of Jakarta Expression Language expressions. When control is passed to a switch node, each expression
 * in the list is evaluated and the first one that returns {@code true} is used to define the id of the next node to
 * which control must be passed. If none of the expressions evaluates to {@code true}, control passes to the specified
 * default id.
 * </p>
 *
 * </li>
 *
 * <li>
 * <p>
 * Return
 * </p>
 *
 * <p>
 * This node type specifies an outcome that is returned to the calling flow.
 * </p>
 *
 * </li>
 *
 * <li>
 * <p>
 * Method Call
 * </p>
 *
 * <p>
 * This node type allows invocation of arbitrary application logic at any point in the executiong of the flow. An
 * outcome can be specified that will cause a navigation case to be navigated to after the method has been invoked.
 * </p>
 *
 * </li>
 *
 * <li>
 * <p>
 * Faces Flow Call
 * </p>
 *
 * <p>
 * This node type allows one flow to call another flow. The calling flow remains active and is not exited until control
 * returns from the called flow.
 * </p>
 *
 * </li>
 *
 * </ul>
 *
 * <p>
 * Edges in the graph are defined by the existing Jakarta Faces navigation rule system.
 * </p>
 *
 * <p>
 * <strong>Flows and Model Objects</strong>
 * </p>
 *
 * <blockquote>
 *
 * <p>
 * Managed beans annotated with the CDI annotation {@link FlowScoped} <span class="changed_added_2_3">are created
 * lazily, when referenced, after</span> a user agent's entry into the named scope, and must be made available for
 * garbage collection when the user agent leaves the flow.
 * </p>
 *
 * <p>
 * The <code>flowScope</code> Jakarta Expression Language implicit object is also available to store values in the
 * "current" slope. Values stored in this scope must be made available for garbage collection when the user agent leaves
 * the flow.
 * </p>
 * </blockquote>
 *
 * </blockquote>
 *
 * </div>
 *
 * @since 2.2
 *
 */

public abstract class FlowHandler {

    /**
     * <p class="changed_added_2_2">
     * Components that are rendered by <code>Renderers</code> of component-family <code>jakarta.faces.OutcomeTarget</code>
     * must use this constant as the parameter name for a parameter representing the flow id of the flow that this component
     * will cause to be entered.
     * </p>
     *
     * @since 2.2
     */
    public static final String FLOW_ID_REQUEST_PARAM_NAME = "jffi";

    /**
     * <p class="changed_added_2_2">
     * Components that are rendered by <code>Renderers</code> of component-family <code>jakarta.faces.OutcomeTarget</code>
     * must use this constant as the parameter name for a parameter representing the defining document id of the flow that
     * this component will cause to be entered.
     * </p>
     *
     * @since 2.2
     */
    public static final String TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME = "jftfdi";

    /**
     * <p class="changed_added_2_2">
     * Components that are rendered by <code>Renderers</code> of component-family <code>jakarta.faces.OutcomeTarget</code>
     * must use this constant as the value of the parameter named by {@link #TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME} when
     * returning from a flow (without entering another flow) using such a component.
     * </p>
     *
     * @since 2.2
     */

    public static final String NULL_FLOW = "jakarta.faces.flow.NullFlow";

    /**
     * <p class="changed_added_2_2">
     * Return the {@code Map} that backs the {@code #{flowScope}} Jakarta Expression Language implicit object or
     * {@code null} if no flow is currently active.
     * </p>
     *
     * @return the {@code Map} for this flow scope.
     *
     * @since 2.2
     */

    public abstract Map<Object, Object> getCurrentFlowScope();

    /**
     * <p class="changed_added_2_2">
     * Return the {@link Flow} whose {@code
     * id} is equivalent to the argument {@code id}, within the scope of the argument {@code definingDocument}.
     * </p>
     *
     * @param context the {@code FacesContext} for the current request.
     *
     * @param definingDocumentId An application unique identifier for the document in which the returned flow is defined.
     *
     * @param id the id of a {@link Flow}, unique within the scope of the {@code definingDocument}.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @return the {@code Flow} for the argument identifiers.
     *
     * @since 2.2
     */

    public abstract Flow getFlow(FacesContext context, String definingDocumentId, String id);

    /**
     * <p class="changed_added_2_2">
     * Add the argument {@link Flow} to the collection of {@code Flow}s known to the current application. The implementation
     * must be thread safe.
     * </p>
     *
     * @param context the {@code FacesContext} for the current request.
     *
     * @param toAdd the {@code Flow} to add.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @throws IllegalStateException if there is already a flow with the same {@code id} as the argument {@code Flow} within
     * the scope of the {@code definingDocument}.
     *
     * @throws IllegalArgumentException if the {@code id} of the flow to add is {@code null} or the empty string.
     *
     * @throws IllegalArgumentException if the {@code
     * definingDocumentId} of the {@code toAdd} is {@code null}.
     *
     * @since 2.2
     */
    public abstract void addFlow(FacesContext context, Flow toAdd);

    /**
     * <p class="changed_added_2_2">
     * Return the currently active {@link Flow} for the argument {@code FacesContext}, or {@code null} if no flow is active.
     * A {@code Flow} must always be associated with exactly one {@link jakarta.faces.lifecycle.ClientWindow}, but a
     * {@code ClientWindow} may have multiple {@code Flow}s.
     * </p>
     *
     * <div class="changed_added_2_2">
     *
     * <p>
     * If {@link #pushReturnMode} had been called with {@code true} as the argument before invoking this method, return the
     * preceding flow on the stack instead of the actual current flow, or {@code null} if there is no preceding flow.
     * Otherwise, return the current flow.
     * </p>
     *
     * </div>
     *
     * @param context the {@code FacesContext} for the current request.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @return the current {@code Flow} or {@code null} if no flow is active
     *
     * @since 2.2
     */
    public abstract Flow getCurrentFlow(FacesContext context);

    /**
     * <p class="changed_added_2_2">
     * Convenience overload that calls {@link FacesContext#getCurrentInstance()} and then calls through to
     * {@link #getCurrentFlow(jakarta.faces.context.FacesContext)}.
     * </p>
     *
     * @return the current {@code Flow} or {@code null} if no flow is active
     *
     * @since 2.2
     */
    public Flow getCurrentFlow() {
        return getCurrentFlow(FacesContext.getCurrentInstance());
    }

    /**
     * <p class="changed_added_2_2">
     * Return the last displayed viewId for the current flow, as returned by
     * {@link #getCurrentFlow(jakarta.faces.context.FacesContext)}, or {@code null} if there is no current flow.
     * </p>
     *
     * @param context the {@code FacesContext} for the current request.
     *
     * @throws NullPointerException if {@code context} is {@code null}
     *
     * @since 2.2
     *
     * @return the last displayed viewId.
     */

    public abstract String getLastDisplayedViewId(FacesContext context);

    /**
     * <p class="changed_added_2_2">
     * Enable the correct handling of navigation when processing a return node. The default
     * {@link jakarta.faces.application.NavigationHandler} specification requires calling this method before processing the
     * navigation rules for the flow return, and calling {@link #popReturnMode}, from a {@code finally} block, immediately
     * afterward.
     * </p>
     *
     * @param context the {@code FacesContext} for the current request.
     *
     * @throws NullPointerException if {@code context} is {@code null}.
     *
     * @since 2.2
     */

    public abstract void pushReturnMode(FacesContext context);

    /**
     * <p class="changed_added_2_2">
     * Enable the correct handling of navigation when processing a return node. The default
     * {@link jakarta.faces.application.NavigationHandler} specification requires calling this method from a {@code finally}
     * block, immediately attempting to process the navigation rules in the context of a flow return.
     * </p>
     *
     * @param context the {@code FacesContext} for the current request.
     *
     * @throws NullPointerException if {@code context} is {@code null}.
     *
     * @since 2.2
     */

    public abstract void popReturnMode(FacesContext context);

    /**
     * <p class="changed_added_2_2">
     * Perform a transition in the flow graph for the current user's {@link jakarta.faces.lifecycle.ClientWindow}. Obtain
     * references to the {@code Flow} instances corresponding to the {@code origin} and {@code destination} arguments. If
     * the {@code origin Flow} is equal to the {@code destination Flow}, take no action and return {@code null}. Otherwise,
     * if the {@code outboundCallNode} argument is non-{@code null} save aside the outbound parameters. For discussion
     * <strong>evaluatedParams</strong> is a data structure that stores the evaluated values of any outbound parameters. It
     * is necessary to evaluate these values before popping any flow scopes because the values may refer to scoped instances
     * that need to be passed to the target flow, but will not be available once the new scope is activated. Save aside the
     * outbound parameters using the following algorithm.
     * </p>
     *
     * <div class="changed_added_2_2">
     *
     * <blockquote>
     *
     * <p>
     * If the {@code outboundParameters} property of {@code
     * outboundCallNode} is non-{@code null} and not empty, and the {@code inboundParameters} property of the target flow is
     * non-{@code null} and not empty, for each entry in the outbound parameters whose name matches an entry in the inbound
     * parameters, evaluate the value of the parameter, and put the evaluated value into <strong>evaluatedParams</strong>
     * under the corresponding name. Otherwise, consider <strong>evaluatedParams</strong> to be empty.
     * </p>
     *
     * </blockquote>
     *
     * <p>
     * If the {@code destination Flow} is a sub-flow of the {@code
     * origin Flow} push the {@code destination Flow} onto the flow data structure and return {@code the destination Flow}.
     * Otherwise, pop the current {@code Flow} from the flow data structure. If the {@code destination Flow} is
     * non-{@code null}, make the {@code
     * destination Flow} the current flow, pushing it onto the data structure. If <strong>evaluatedParams</strong> is not
     * empty, for each entry, find the corresponding parameter in the target flow's inbound parameters and call its
     * {@code setValue} method, passing the value from <strong>evaluatedParams</strong>.
     * </p>
     *
     * </div>
     *
     * @param context the {@code FacesContext} for the current request.
     *
     * @param sourceFlow the current {@code Flow}, or {@code null} if there is no source flow.
     *
     * @param targetFlow the destination {@code Flow}, or {@code null} if there is no destination flow.
     *
     * @param outboundCallNode the flow call node causing this transition, or {@code null} if this transition is not caused
     * by a flow call.
     *
     * @param toViewId the viewId of the view being displayed as a result of this transition. This parameter makes it
     * possible to implement {@link #getLastDisplayedViewId}.
     *
     * @throws NullPointerException if {@code context} or {@code toViewId} is {@code null}.
     *
     *
     * @since 2.2
     */

    public abstract void transition(FacesContext context, Flow sourceFlow, Flow targetFlow, FlowCallNode outboundCallNode, String toViewId);

    /**
     * <p class="changed_added_2_2">
     * Allow for flow transitions in the case of components rendered by the renderers from component-family
     * <code>jakarta.faces.OutcomeTarget</code>. These transitions must happen at the front of the request processing
     * lifecycle due to the HTTP GET based nature of such components. Therefore, this method is called from the restore view
     * phase of the lifecycle.
     * </p>
     *
     * <div class="changed_added_2_2">
     *
     * <p>
     * Let <em>flowId</em> be the value in the request parameter map for the parameter whose name is given by the value of
     * {@link #FLOW_ID_REQUEST_PARAM_NAME}. Let <em>toFlowDocumentId</em> be the value in the request parameter map for the
     * paramater whose name is given by the value of {@link #TO_FLOW_DOCUMENT_ID_REQUEST_PARAM_NAME}. If
     * <em>toFlowDocumentId</em> is <code>null</code>, take no action and return. Otherwise, let <em>sourceFlow</em> be the
     * return from {@link #getCurrentFlow(jakarta.faces.context.FacesContext)}. A <code>null</code> value indicates there is
     * no current flow, which will be the case if this navigation is trying to enter a flow. If <em>flowId</em> is not
     * <code>null</code> and <em>toFlowDocumentId</em> is <strong>not</strong> equal to the value of {@link #NULL_FLOW}, let
     * <em>targetFlow</em> be the result of calling
     * {@link #getFlow(jakarta.faces.context.FacesContext, java.lang.String, java.lang.String)}, passing
     * <em>toFlowDocumentId</em> and <em>flowId</em> as the last two arguments, respectively. If the result is
     * non-<code>null</code>, let <em>flowCallNode</em> be the return from calling {@link Flow#getFlowCall} on the
     * <em>sourceFlow</em>, passing <em>targetFlow</em> as the argument. Otherwise, <em>targetFlow</em> and
     * <em>flowCallNode</em> must remain <code>null</code>, indicating that this is a flow return. Call
     * {@link FacesContext#getViewRoot()} and let <em>toViewId</em> be the the return from calling
     * {@link jakarta.faces.component.UIViewRoot#getViewId} on it.
     * </p>
     *
     * <p>
     * Call, {@link #transition}, passing the arguments gathered in the preceding algorithm.
     * </p>
     *
     * </div>
     *
     * @since 2.2
     *
     * @param context the {@code FacesContext} for the current request.
     *
     * @throws NullPointerException if {@code context} is {@code null}.
     */

    public abstract void clientWindowTransition(FacesContext context);

    /**
     * <p class="changed_added_2_2">
     * Return {@code true} if and only if the flow referenced by the argument {@code definingDocument} and {@code id} is
     * currently active.
     * </p>
     *
     * @param context the {@code FacesContext} for the current request.
     *
     * @param definingDocument An application unique identifier for the document in which the returned flow is defined.
     *
     * @param id the id of a {@link Flow}, unique within the scope of the {@code definingDocument}.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @return true if and only if the referenced flow is active
     *
     * @since 2.2
     */
    public abstract boolean isActive(FacesContext context, String definingDocument, String id);

}
