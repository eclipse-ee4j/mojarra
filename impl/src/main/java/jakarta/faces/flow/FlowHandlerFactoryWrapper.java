/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.faces.FacesWrapper;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_2 changed_modified_2_3">
 * <strong>FlowHandlerFactoryWrapper</strong> provides a simple implementation of {@link FlowHandlerFactory} that can be
 * subclassed by developers wishing to provide specialized behavior to an existing {@link FlowHandlerFactory} instance.
 * The default implementation of all methods is to call through to the wrapped {@link FlowHandlerFactory} instance.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 2.2
 */
public abstract class FlowHandlerFactoryWrapper extends FlowHandlerFactory implements FacesWrapper<FlowHandlerFactory> {

    private FlowHandlerFactory wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public FlowHandlerFactoryWrapper() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this flow handler factory has been decorated, the implementation doing the decorating should push the
     * implementation being wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being
     * wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     * @since 2.3
     */
    public FlowHandlerFactoryWrapper(FlowHandlerFactory wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public FlowHandlerFactory getWrapped() {
        return wrapped;
    }

    @Override
    public FlowHandler createFlowHandler(FacesContext context) {
        return getWrapped().createFlowHandler(context);
    }

}
