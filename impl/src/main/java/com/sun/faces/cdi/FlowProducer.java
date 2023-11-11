/*
 * Copyright (c) Oracle and/or its affiliates. All rights reserved.
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

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.Flow;
import jakarta.faces.flow.FlowHandler;

/**
 * <p class="changed_added_4_1">
 * {@code FlowProducer} is the CDI producer that allows injection of the {@link Flow} using {@code @Inject}.
 * </p>
 *
 * @since 4.1
 * @see FlowHandler#getCurrentFlow()
 */
public class FlowProducer extends CdiProducer<Flow> {

    private static final long serialVersionUID = 1L;

    public FlowProducer() {
        super.name("flow").scope(RequestScoped.class).types(Flow.class)
            .create(e -> FacesContext.getCurrentInstance().getApplication().getFlowHandler().getCurrentFlow());
    }

}
