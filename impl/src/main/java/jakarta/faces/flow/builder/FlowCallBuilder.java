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

import jakarta.el.ValueExpression;

/**
 * <p class="changed_added_2_2">
 * Create a flow call node in the current {@link jakarta.faces.flow.Flow}.
 * </p>
 *
 * @since 2.2
 */
public abstract class FlowCallBuilder implements NodeBuilder {

    /**
     * <p class="changed_added_2_2">
     * Define the flow reference of the called flow.
     * </p>
     *
     * @param flowDocumentId the document id of the called flow. May not be {@code null}, but may be the empty string.
     * @param flowId the id of the called flow. May not be {@code null}
     * @throws NullPointerException if any of the parameters are {@code null}
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract FlowCallBuilder flowReference(String flowDocumentId, String flowId);

    /**
     * <p class="changed_added_2_2">
     * Define an outbound parameter for the flow call.
     * </p>
     *
     * @param name the name of the parameter
     * @param value the value of the parameter
     * @throws NullPointerException if any of the parameters are {@code null}
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract FlowCallBuilder outboundParameter(String name, ValueExpression value);

    /**
     * <p class="changed_added_2_2">
     * Define an outbound parameter for the flow call.
     * </p>
     *
     * @param name the name of the parameter
     * @param value the value of the parameter
     * @throws NullPointerException if any of the parameters are {@code null}
     * @since 2.2
     *
     * @return the builder instance
     */
    public abstract FlowCallBuilder outboundParameter(String name, String value);

    @Override
    public abstract FlowCallBuilder markAsStartNode();

}
