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

package jakarta.faces.flow.builder;

import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.flow.Flow;

/**
 * <p class="changed_added_2_2">
 * A Java language API for building {@link Flow}s. This API is semantically identical to the
 * <code>&lt;flow-definition&gt;</code> element in the Application Configuration Resources XML Schema Definition.
 * </p>
 *
 * <div class="changed_added_2_2">
 *
 * <p>
 * Usage example:
 * </p>
 *
 * <pre>
 * <code>public class FlowA implements Serializable {

    &#x40;Produces {@link FlowDefinition}
    public {@link Flow} buildMyFlow(&#x40;{@link FlowBuilderParameter} {@link FlowBuilder} flowBuilder) {
        String flowId = "flow-a";
        flowBuilder.id("unique", flowId);
        flowBuilder.returnNode("taskFlowReturn1").
                fromOutcome("#{flow_a_Bean.returnValue}");
        flowBuilder.inboundParameter("param1FromFlowB", "#{flowScope.param1Value}");
        flowBuilder.inboundParameter("param2FromFlowB", "#{flowScope.param2Value}");
        flowBuilder.flowCallNode("callB").flowReference("", "flow-b").
                outboundParameter("param1FromFlowA", "param1Value").
                outboundParameter("param2FromFlowA", "param2Value");

        return flowBuilder.getFlow();
    }
}
</code>
 * </pre>
 *
 * <p>
 * The runtime must discover all such methods at startup time and ensure that the returned flows are added to the
 * {@link jakarta.faces.flow.FlowHandler} using the
 * {@link jakarta.faces.flow.FlowHandler#addFlow(jakarta.faces.context.FacesContext, jakarta.faces.flow.Flow)} method.
 * </p>
 *
 * </div>
 *
 * @since 2.2
 */

public abstract class FlowBuilder {

    /**
     * <p class="changed_added_2_2">
     * Set the defining document id and flow id of this flow.
     * </p>
     *
     * @param definingDocumentId The defining document id of this flow, or the empty string if this flow does not need a
     * defining document id.
     * @param id the id of the flow
     * @throws NullPointerException if any of the parameters are {@code null}
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract FlowBuilder id(String definingDocumentId, String id);

    /**
     * <p class="changed_added_2_2">
     * Define a view node in a flow graph.
     * </p>
     *
     * @param viewNodeId Within the flow graph, the id of this view node
     * @param vdlDocumentId The fully qualified path to the view node within this flow.
     * @throws NullPointerException if any of the parameters are {@code null}
     * @since 2.2
     */

    /**
     * <p class="changed_added_2_2">
     * Define a view node in a flow graph.
     * </p>
     *
     * @param viewNodeId Within the flow graph, the id of this view node. Must be unique among all nodes in this flow graph.
     * @param vdlDocumentId The fully qualified path to the view node within this flow.
     * @throws NullPointerException if any of the parameters are {@code null}
     * @since 2.2
     *
     * @return the builder instance
     */

    public abstract ViewBuilder viewNode(String viewNodeId, String vdlDocumentId);

    /**
     * <p class="changed_added_2_2">
     * Define a particular combination of conditions that must match for this case to be executed, and the view id of the
     * component tree that should be selected next.
     * </p>
     *
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract NavigationCaseBuilder navigationCase();

    /**
     * <p class="changed_added_2_2">
     * Define a particular list of cases that will be inspected in the order they are defined to determine where to go next
     * in the flow graph. If none of the cases match, the outcome from the default case is chosen.
     * </p>
     *
     * @param switchNodeId Within the flow graph, the id of this switch node. Must be unique among all nodes in this flow
     * graph.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract SwitchBuilder switchNode(String switchNodeId);

    /**
     * <p class="changed_added_2_2">
     * Define a return node. This node will cause the specified outcome to be returned to the calling flow.
     * </p>
     *
     * @param returnNodeId Within the flow graph, the id of this return node. Must be unique among all nodes in this flow
     * graph.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract ReturnBuilder returnNode(String returnNodeId);

    /**
     * <p class="changed_added_2_2">
     * Define a method call node. This node will cause the specified method to be invoked, passing parameters if necessary.
     * The return from the method is used as the outcome for where to go next in the flow. If the method is a void method,
     * the default outcome is used.
     * </p>
     *
     * @param methodCallNodeId Within the flow graph, the id of this method call node. Must be unique among all nodes in
     * this flow graph.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract MethodCallBuilder methodCallNode(String methodCallNodeId);

    /**
     * <p class="changed_added_2_2">
     * Define a flow call node. This node will cause the specified flow to be called, passing parameters if necessary.
     * </p>
     *
     * @param flowCallNodeId Within the flow graph, the id of this return node. Must be unique among all nodes in this flow
     * graph.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract FlowCallBuilder flowCallNode(String flowCallNodeId);

    /**
     * <p class="changed_added_2_2">
     * A MethodExpression that will be invoked when the flow is entered.
     * </p>
     *
     * @param methodExpression the expression to invoke, must reference a zero-argument method.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract FlowBuilder initializer(MethodExpression methodExpression);

    /**
     * <p class="changed_added_2_2">
     * A MethodExpression that will be invoked when the flow is entered.
     * </p>
     *
     * @param methodExpression the expression to invoke, must reference a zero-argument method.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @since 2.2
     * @return the builder instance
     */
    public abstract FlowBuilder initializer(String methodExpression);

    /**
     * <p class="changed_added_2_2">
     * A MethodExpression that will be invoked when the flow is exited.
     * </p>
     *
     * @param methodExpression the expression to invoke, must reference a zero-argument method.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @since 2.2
     * @return the builder instance
     */
    public abstract FlowBuilder finalizer(MethodExpression methodExpression);

    /**
     * <p class="changed_added_2_2">
     * A MethodExpression that will be invoked when the flow is exited.
     * </p>
     *
     * @param methodExpression the expression to invoke, must reference a zero-argument method.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract FlowBuilder finalizer(String methodExpression);

    /**
     * <p class="changed_added_2_2">
     * A parameter that will be populated with the value from a correspondingly named outbound parameter from another flow
     * when this flow is entered from that flow.
     * </p>
     *
     * @param name the parameter name
     *
     * @param expression the {@code ValueExpression} to populate with the inbound value when the flow is called.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract FlowBuilder inboundParameter(String name, ValueExpression expression);

    /**
     * <p class="changed_added_2_2">
     * A parameter that will be populated with the value from a correspondingly named outbound parameter from another flow
     * when this flow is entered from that flow.
     * </p>
     *
     * @param name the parameter name
     *
     * @param expression the {@code ValueExpression} String to populate with the inbound value when the flow is called.
     *
     * @throws NullPointerException if any of the parameters are {@code null}
     *
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract FlowBuilder inboundParameter(String name, String expression);

    /**
     * <p class="changed_added_2_2">
     * Called as the last step in flow definition, this method must perform any implementation specific initialization and
     * return the built {@link Flow}. If called more than one time during a given flow building process, the second and
     * subsequent invocations must take no action and return the built flow.
     * </p>
     *
     * @throws IllegalStateException if the {@link #id} method had not been called prior to this method being called.
     *
     * @since 2.2
     *
     * @return the completely built {@code Flow}
     */
    public abstract Flow getFlow();

}
