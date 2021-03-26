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

import jakarta.faces.component.ContextCallback;
import jakarta.faces.component.UIComponent;

/**
 * <p class="changed_added_2_3">
 * <strong>SearchKeywordContext</strong> provides context information that may be useful to
 * {@link SearchKeywordResolver#resolve} implementations.
 * </p>
 *
 * @since 2.3
 */
public class SearchKeywordContext {

    private final SearchExpressionContext searchExpressionContext;
    private final ContextCallback callback;
    private final String remainingExpression;

    private boolean keywordResolved;

    /**
     * <p class="changed_added_2_3">
     * Construct a new context with the given arguments.
     * </p>
     *
     * @param searchExpressionContext the {@link SearchExpressionContext} for the current request.
     * @param callback the {@link ContextCallback}.
     * @param remainingExpression the remaining expression.
     */
    public SearchKeywordContext(SearchExpressionContext searchExpressionContext, ContextCallback callback, String remainingExpression) {
        this.searchExpressionContext = searchExpressionContext;
        this.callback = callback;
        this.remainingExpression = remainingExpression;
    }

    /**
     * <p class="changed_added_2_3">
     * This method will be called by an implementation of {@link SearchKeywordResolver#resolve} with the resolved component
     * for the keyword.
     * </p>
     *
     * @param target the resolved {@link UIComponent}.
     *
     * @since 2.3
     */
    public void invokeContextCallback(UIComponent target) {
        keywordResolved = true;
        callback.invokeContextCallback(searchExpressionContext.getFacesContext(), target);
    }

    /**
     * <p class="changed_added_2_3">
     * Returns the {@link SearchExpressionContext} for the current request.
     * </p>
     *
     * @return the {@link SearchExpressionContext}.
     *
     * @since 2.3
     */
    public SearchExpressionContext getSearchExpressionContext() {
        return searchExpressionContext;
    }

    /**
     * <p class="changed_added_2_3">
     * Returns the {@link ContextCallback} for the current request.
     * </p>
     *
     * @return the {@link ContextCallback}.
     *
     * @since 2.3
     */
    public ContextCallback getCallback() {
        return callback;
    }

    /**
     * <p class="changed_added_2_3">
     * Returns the remaining expression for the current request.
     * </p>
     *
     * @return the remaining expression.
     *
     * @since 2.3
     */
    public String getRemainingExpression() {
        return remainingExpression;
    }

    /**
     * <p class="changed_added_2_3">
     * Returns if the keyword was resolved.
     * </p>
     *
     * @return if the keyword was resolved.
     *
     * @since 2.3
     */
    public boolean isKeywordResolved() {
        return keywordResolved;
    }

    /**
     * <p class="changed_added_2_3">
     * Sets if the keyword was resolved.
     * </p>
     *
     * @param keywordResolved if the keyword was resolved.
     *
     * @since 2.3
     */
    public void setKeywordResolved(boolean keywordResolved) {
        this.keywordResolved = keywordResolved;
    }
}
