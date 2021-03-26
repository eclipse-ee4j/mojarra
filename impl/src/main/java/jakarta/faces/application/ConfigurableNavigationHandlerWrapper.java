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

package jakarta.faces.application;

import java.util.Map;
import java.util.Set;

import jakarta.faces.FacesWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.Flow;

/**
 * <p class="changed_added_2_2">
 * <span class="changed_modified_2_3">Provides</span> a simple implementation of {@link ConfigurableNavigationHandler}
 * that can be subclassed by developers wishing to provide specialized behavior to an existing
 * {@link ConfigurableNavigationHandler} instance. The default implementation of all methods is to call through to the
 * wrapped {@link ConfigurableNavigationHandler}.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 2.2
 */
public abstract class ConfigurableNavigationHandlerWrapper extends ConfigurableNavigationHandler implements FacesWrapper<ConfigurableNavigationHandler> {

    private ConfigurableNavigationHandler wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public ConfigurableNavigationHandlerWrapper() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this configurable navigation handler has been decorated, the implementation doing the decorating should push the
     * implementation being wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being
     * wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     * @since 2.3
     */
    public ConfigurableNavigationHandlerWrapper(ConfigurableNavigationHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ConfigurableNavigationHandler getWrapped() {
        return wrapped;
    }

    @Override
    public NavigationCase getNavigationCase(FacesContext context, String fromAction, String outcome) {
        return getWrapped().getNavigationCase(context, fromAction, outcome);
    }

    @Override
    public Map<String, Set<NavigationCase>> getNavigationCases() {
        return getWrapped().getNavigationCases();
    }

    @Override
    public NavigationCase getNavigationCase(FacesContext context, String fromAction, String outcome, String toFlowDocumentId) {
        return getWrapped().getNavigationCase(context, fromAction, outcome, toFlowDocumentId);
    }

    @Override
    public void handleNavigation(FacesContext context, String fromAction, String outcome) {
        getWrapped().handleNavigation(context, fromAction, outcome);
    }

    @Override
    public void performNavigation(String outcome) {
        getWrapped().performNavigation(outcome);
    }

    @Override
    public void inspectFlow(FacesContext context, Flow flow) {
        getWrapped().inspectFlow(context, flow);
    }

}
