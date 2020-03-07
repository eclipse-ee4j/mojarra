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

package jakarta.faces.component.visit;

import java.util.Collection;
import java.util.Set;

import jakarta.faces.FacesWrapper;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_3">Provide</span> for separation of interface and implementation for the
 * {@link VisitContext} contract.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 2.0
 */
public abstract class VisitContextFactory implements FacesWrapper<VisitContextFactory> {

    private VisitContextFactory wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public VisitContextFactory() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this factory has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     */
    public VisitContextFactory(VisitContextFactory wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * <p class="changed_added_2_0 changed_modified_2_3">
     * If this factory has been decorated, the implementation doing the decorating may override this method to provide
     * access to the implementation being wrapped.
     * </p>
     */
    @Override
    public VisitContextFactory getWrapped() {
        return wrapped;
    }

    /**
     * <p class="changed_added_2_0">
     * Return a new {@link VisitContext} instance.
     * </p>
     *
     * @param context the <code>FacesContext</code> for this request.
     * @param ids a <code>Collection</code> of clientIds to visit. If <code>null</code> all components will be visited.
     * @param hints the <code>VisitHints</code> that apply to this visit.
     *
     * @return the instance of <code>VisitContext</code>.
     *
     * @since 2.0
     */
    public abstract VisitContext getVisitContext(FacesContext context, Collection<String> ids, Set<VisitHint> hints);

}
