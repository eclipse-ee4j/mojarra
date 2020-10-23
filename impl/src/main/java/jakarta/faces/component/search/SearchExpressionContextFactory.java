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

package jakarta.faces.component.search;

import java.util.Set;

import jakarta.faces.FacesWrapper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_3">
 * Provide for separation of interface and implementation for the {@link SearchExpressionContext} contract. Usage:
 * extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to access
 * the instance being wrapped.
 * </p>
 *
 * @since 2.3
 */
public abstract class SearchExpressionContextFactory implements FacesWrapper<SearchExpressionContextFactory> {

    private final SearchExpressionContextFactory wrapped;

    /**
     * <p class="changed_added_2_3">
     * If this factory has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     */
    public SearchExpressionContextFactory(SearchExpressionContextFactory wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * <p class="changed_modified_2_3">
     * If this factory has been decorated, the implementation doing the decorating may override this method to provide
     * access to the implementation being wrapped.
     * </p>
     */
    @Override
    public SearchExpressionContextFactory getWrapped() {
        return wrapped;
    }

    /**
     * <p class="changed_added_2_3">
     * Creates a {@link SearchExpressionContext} instance for use with the {@link SearchExpressionHandler}.
     * </p>
     *
     * @param context the FacesContext for the current request
     * @param source the source / base component from which we will start to perform our search.
     * @param expressionHints the SearchExpressionHint to apply to the search. If <code>null</code>, no hints are applied.
     * @param visitHints the VisitHints to apply to the visit, if used by a {@link SearchKeywordResolver}. If
     * <code>null</code>, no hints are applied.
     *
     * @return a {@link SearchExpressionContext} instance
     *
     * @since 2.3
     */
    public abstract SearchExpressionContext getSearchExpressionContext(FacesContext context, UIComponent source, Set<SearchExpressionHint> expressionHints,
            Set<VisitHint> visitHints);
}
