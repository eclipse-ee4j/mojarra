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

import jakarta.faces.FactoryFinder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_3">
 * A context object that is used to hold state relating to resolve a search expression.
 * </p>
 *
 * @see SearchExpressionHandler
 *
 * @since 2.3
 */
public abstract class SearchExpressionContext {

    /**
     * <p class="changed_added_2_3">
     * Returns the source / base component from which we will start to perform our search.
     * </p>
     *
     * @return the source component.
     *
     * @since 2.3
     */
    public abstract UIComponent getSource();

    /**
     * <p class="changed_added_2_3">
     * Returns hints that influence the behavior of the tree visit, if it's used by an {@link SearchKeywordResolver}
     * implementation.
     * </p>
     *
     * @return a non-empty, unmodifiable collection of {@link VisitHint}s
     *
     * @since 2.3
     * @see jakarta.faces.component.visit.VisitContext#getHints()
     */
    public abstract Set<VisitHint> getVisitHints();

    /**
     * <p class="changed_added_2_3">
     * Returns hints that influence the behavior of resolving the expression.
     * </p>
     *
     * @return a non-empty, unmodifiable collection of {@link SearchExpressionHint}s
     *
     * @since 2.3
     */
    public abstract Set<SearchExpressionHint> getExpressionHints();

    /**
     * <p class="changed_added_2_3">
     * Returns the FacesContext for the current request.
     * </p>
     *
     * @return the FacesContext.
     *
     * @since 2.3
     */
    public abstract FacesContext getFacesContext();

    /**
     * <p class="changed_added_2_3">
     * Creates a {@link SearchExpressionContext} instance for use with the {@link SearchExpressionHandler}. This method can
     * be used to obtain a SearchExpressionContext instance without any {@link VisitHint} or {@link SearchExpressionHint}.
     * </p>
     *
     * @param context the FacesContext for the current request
     * @param source the source / base component from which we will start to perform our search.
     *
     * @return a {@link SearchExpressionContext} instance
     *
     * @since 2.3
     */
    public static SearchExpressionContext createSearchExpressionContext(FacesContext context, UIComponent source) {
        return createSearchExpressionContext(context, source, null, null);
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
    public static SearchExpressionContext createSearchExpressionContext(FacesContext context, UIComponent source, Set<SearchExpressionHint> expressionHints,
            Set<VisitHint> visitHints) {

        SearchExpressionContextFactory factory = (SearchExpressionContextFactory) FactoryFinder.getFactory(FactoryFinder.SEARCH_EXPRESSION_CONTEXT_FACTORY);
        return factory.getSearchExpressionContext(context, source, expressionHints, visitHints);
    }
}
