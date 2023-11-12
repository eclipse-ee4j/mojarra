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

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.el.MethodExpression;
import jakarta.faces.application.NavigationCase;
import jakarta.faces.lifecycle.ClientWindow;

/**
 * <p class="changed_added_2_2">
 * <strong>Flow</strong> is the runtime representation of a Faces Flow. Once placed into service by the runtime, an
 * instance of this class is immutable. The implementation must be thread-safe because instances will be shared across
 * all usages of the flow within the application.
 * </p>
 *
 * <p class="changed_added_4_1">
 * The current {@code Flow} can be injected into a CDI managed bean using {@code @Inject Flow currentFlow;}
 * </p>
 *
 * @since 2.2
 */

public abstract class Flow {

    // <editor-fold defaultstate="collapsed" desc="Simple properties">

    /**
     * <p class="changed_added_2_2">
     * Return the immutable id for this Flow. This must be unique within the defining document (such as an Application
     * Configuration Resources file), but need not be unique within the entire application.
     * </p>
     *
     * @return the id.
     *
     * @since 2.2
     */

    public abstract String getId();

    /**
     * <p class="changed_added_2_2">
     * Return the immutable application unique identifier for the document in which the argument flow is defined.
     * </p>
     *
     * @return the defining document id
     *
     * @since 2.2
     */

    public abstract String getDefiningDocumentId();

    /**
     * <p class="changed_added_2_2">
     * Return the immutable id for the default node that should be activated when this flow is entered.
     * </p>
     *
     * @return the id of the start node
     *
     * @since 2.2
     */

    public abstract String getStartNodeId();

    /**
     * <p class="changed_added_2_2">
     * Return the {@code MethodExpression} that must be called by the runtime as the last thing that happens before exiting
     * this flow. Any {@link FlowScoped} beans declared for this flow must remain in scope until after control returns from
     * the method referenced by this {@code MethodExpression}.
     * </p>
     *
     * @return the {@code MethodExpresion} for the finalizer.
     *
     * @since 2.2
     */

    public abstract MethodExpression getFinalizer();

    /**
     * <p class="changed_added_2_2">
     * Return the {@code MethodExpression} that must be called by the runtime immediately after activating any
     * {@link FlowScoped} beans declared for this flow.
     * </p>
     *
     * @return the {@code MethodExpresion} for the initializer.
     *
     * @since 2.2
     */
    public abstract MethodExpression getInitializer();

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Graph properties">

    /**
     * <p class="changed_added_2_2">
     * Return an immutable data structure containing the inbound parameters that have been declared for this flow. See
     * {@link FlowHandler#transition} for the specification of how these parameters are used. Inbound parameters are
     * associated with a specific flow instance, while outbound parameters are associated with a {@link FlowCallNode} that
     * causes the transition to a new flow.
     * </p>
     *
     * @return the inbound parameters
     *
     * @since 2.2
     */

    public abstract Map<String, Parameter> getInboundParameters();

    /**
     * <p class="changed_added_2_2">
     * Return an immutable data structure containing all of the view nodes declared for this flow.
     * </p>
     *
     * @return the view nodes for this flow
     * @since 2.2
     */
    public abstract List<ViewNode> getViews();

    /**
     * <p class="changed_added_2_2">
     * Return an immutable data structure containing all of the return nodes declared for this flow.
     * </p>
     *
     * @return the return nodes for this flow.
     *
     * @since 2.2
     */
    public abstract Map<String, ReturnNode> getReturns();

    /**
     * <p class="changed_added_2_2">
     * Return an immutable data structure containing all of the switch nodes declared for this flow.
     * </p>
     *
     * @return the switch nodes for this flow
     *
     * @since 2.2
     */
    public abstract Map<String, SwitchNode> getSwitches();

    /**
     * <p class="changed_added_2_2">
     * Return an immutable data structure containing all the flow call nodes declared for this flow.
     * </p>
     *
     * @return the flow call nodes for this flow
     *
     * @since 2.2
     */
    public abstract Map<String, FlowCallNode> getFlowCalls();

    /**
     * <p class="changed_added_2_2">
     * Return the {@link FlowCallNode} that represents calling the {@code targetFlow} from this flow, or {@code null} if
     * {@code targetFlow} cannot be reached from this flow.
     * </p>
     *
     * @param targetFlow the flow for which the {@link FlowCallNode} is to be returned
     *
     * @return a {@link FlowCallNode} for the argument flow or {@code null}
     *
     * @since 2.2
     */
    public abstract FlowCallNode getFlowCall(Flow targetFlow);

    /**
     * <p class="changed_added_2_2">
     * Return an immutable data structure containing all the method call nodes declared for this flow.
     * </p>
     *
     * @return the method call nodes for this flow
     *
     * @since 2.2
     */
    public abstract List<MethodCallNode> getMethodCalls();

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Graph navigation">

    /**
     * <p class="changed_added_2_2">
     * Return the generic {@link FlowNode} by id, or {@code null} if not found.
     * </p>
     *
     * @param nodeId the node id for which the {@link FlowNode} is to be returned
     *
     * @return the {@link FlowNode} or {@code null}
     */

    public abstract FlowNode getNode(String nodeId);

    /**
     * <p class="changed_added_2_2">
     * Return an unmodifiable view of the navigation cases within this flow.
     * </p>
     *
     * @return the navigation cases,
     */
    public abstract Map<String, Set<NavigationCase>> getNavigationCases();

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Outside interaction">

    /**
     * <p class="changed_added_2_2">
     * Get the {@link ClientWindow}'s id and append "_" and the return from {@link #getId}. Return the result.
     * </p>
     *
     * @return the generated client window id for this flow.
     *
     * @param curWindow the
     *
     * @since 2.2
     */

    public abstract String getClientWindowFlowId(ClientWindow curWindow);

    // </editor-fold>

}
