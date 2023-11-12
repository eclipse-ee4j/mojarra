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

import java.util.List;

import jakarta.faces.FacesWrapper;
import jakarta.faces.component.ContextCallback;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_3">
 * Provides a simple implementation of {@link SearchExpressionHandler} that can be subclassed by developers wishing to
 * provide specialized behavior to an existing {@link SearchExpressionHandler} instance. The default implementation of
 * all methods is to call through to the wrapped {@link SearchExpressionHandler} instance. Usage: extend this class and
 * push the implementation being wrapped to the constructor and use {@link #getWrapped} to access the instance being
 * wrapped.
 * </p>
 *
 * @since 2.3
 */
public abstract class SearchExpressionHandlerWrapper extends SearchExpressionHandler implements FacesWrapper<SearchExpressionHandler> {

    private final SearchExpressionHandler wrapped;

    /**
     * <p class="changed_added_2_3">
     * If this search expression handler has been decorated, the implementation doing the decorating should push the
     * implementation being wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being
     * wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     * @since 2.3
     */
    public SearchExpressionHandlerWrapper(SearchExpressionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public SearchExpressionHandler getWrapped() {
        return wrapped;
    }

    @Override
    public String resolveClientId(SearchExpressionContext searchExpressionContext, String expression) {
        return getWrapped().resolveClientId(searchExpressionContext, expression);
    }

    @Override
    public List<String> resolveClientIds(SearchExpressionContext searchExpressionContext, String expressions) {
        return getWrapped().resolveClientIds(searchExpressionContext, expressions);
    }

    @Override
    public void resolveComponent(SearchExpressionContext searchExpressionContext, String expression, ContextCallback callback) {
        getWrapped().resolveComponent(searchExpressionContext, expression, callback);
    }

    @Override
    public void resolveComponents(SearchExpressionContext searchExpressionContext, String expressions, ContextCallback callback) {
        getWrapped().resolveComponents(searchExpressionContext, expressions, callback);
    }

    @Override
    public void invokeOnComponent(SearchExpressionContext searchExpressionContext, String expression, ContextCallback callback) {
        getWrapped().invokeOnComponent(searchExpressionContext, expression, callback);
    }

    @Override
    public void invokeOnComponent(SearchExpressionContext searchExpressionContext, UIComponent previous, String expression, ContextCallback callback) {
        getWrapped().invokeOnComponent(searchExpressionContext, previous, expression, callback);
    }

    @Override
    public boolean isValidExpression(SearchExpressionContext searchExpressionContext, String expression) {
        return getWrapped().isValidExpression(searchExpressionContext, expression);
    }

    @Override
    public boolean isPassthroughExpression(SearchExpressionContext searchExpressionContext, String expression) {
        return getWrapped().isPassthroughExpression(searchExpressionContext, expression);
    }

    @Override
    public String[] splitExpressions(FacesContext context, String expressions) {
        return getWrapped().splitExpressions(context, expressions);
    }

    @Override
    public char[] getExpressionSeperatorChars(FacesContext context) {
        return getWrapped().getExpressionSeperatorChars(context);
    }
}
