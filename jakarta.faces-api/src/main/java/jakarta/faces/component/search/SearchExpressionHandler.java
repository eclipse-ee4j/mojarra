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

import jakarta.faces.FacesException;
import jakarta.faces.component.ContextCallback;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <div class="changed_added_2_3">The <strong>SearchExpressionHandler</strong> is responsible for resolving <em>search
 * expression(s)</em>
 *
 * <p>
 * A <em>search expression</em> consists of either an identifier (which is matched exactly against the <code>id</code>
 * property of a {@link UIComponent}, or a keyword (like <code>@this</code> or <code>@form</code>), or a series of such
 * identifiers and keywords linked by the {@link jakarta.faces.component.UINamingContainer#getSeparatorChar} character
 * value. See {@link SearchKeywordResolver} for the list of supported keywords. The search algorithm must operate as
 * follows, though alternate alogrithms may be used as long as the end result is the same:
 * </p>
 *
 * <ul>
 * <li>Identify the {@link UIComponent} that will be the base for searching:
 * <ul>
 * <li>If the search expression begins with the separator character (called an "absolute" search expression), the base
 * will be the {@link jakarta.faces.component.UIViewRoot}. The leading separator character will be stripped off, and the
 * remainder of the search expression will be treated as a "relative" search expression as described below.</li>
 * <li>Otherwise, the {@link SearchExpressionContext#getSource()} will be used.</li>
 * </ul>
 * </li>
 * <li>The search expression (possibly modified in the previous step) is now a "relative" search expression that will be
 * used to locate the component (if any) based on the identifier and/or keywords:
 * <ul>
 * <li>The expression will be splitted by {@link jakarta.faces.component.UINamingContainer#getSeparatorChar} into
 * "commands". The commands will be resolved one by one. For the first command, the source component, like mentioned
 * above, will be used to start the lookup. For all further commands, the previous resolved component, from the previous
 * command, will be used the start the lookup.</li>
 * <li>If the command starts with the {@link #KEYWORD_PREFIX}, then it is considered as a keyword and the
 * {@link SearchKeywordResolver}s will be used the resolve the keyword.</li>
 * <li>Otherwise, if the command does not start with {@link #KEYWORD_PREFIX}, then the component will be resolved based
 * on the component ID.</li>
 * </ul>
 * </li>
 * </ul>
 * </div>
 *
 * @since 2.3
 */
public abstract class SearchExpressionHandler {

    /**
     * <p class="changed_added_2_3">
     * The prefix to identify a keyword.
     * </p>
     *
     * @since 2.3
     */
    public static final String KEYWORD_PREFIX = "@";

    /**
     * <p class="changed_added_2_3">
     * The default characters used to separate expressions in a series of expressions. Expressions are per default separated
     * by space and comma.
     * </p>
     *
     * @since 2.3
     */
    protected static final char[] EXPRESSION_SEPARATOR_CHARS = new char[] { ',', ' ' };

    /**
     * <p class="changed_added_2_3">
     * Resolves to a single clientId or passthrough expression for the given expression.
     * </p>
     *
     * @param searchExpressionContext the {@link SearchExpressionContext}
     * @param expression the search expression
     *
     * @throws ComponentNotFoundException if the expression can not be resolved and if
     * {@link SearchExpressionHint#IGNORE_NO_RESULT} was not passed.
     * @throws FacesException if the expression is not valid.
     *
     * @return The resolved clientId or passtrough expression. If the expression can not be resolved and if
     * {@link SearchExpressionHint#IGNORE_NO_RESULT} was passed, <code>null</code> will be returned.
     *
     * @since 2.3
     */
    public abstract String resolveClientId(SearchExpressionContext searchExpressionContext, String expression);

    /**
     * <p class="changed_added_2_3">
     * Resolves to a {@link List} with clientIds or passthrough expressions for the given expressions. The expressions will
     * be splitted by {@link #splitExpressions(jakarta.faces.context.FacesContext, java.lang.String)} and resolved one by
     * one.
     * </p>
     *
     * @param searchExpressionContext the {@link SearchExpressionContext}
     * @param expressions the search expressions
     *
     * @throws ComponentNotFoundException if one of the expression can not be resolved and if
     * {@link SearchExpressionHint#IGNORE_NO_RESULT} was not passed.
     * @throws FacesException if the expression is not valid.
     *
     * @return The resolved clientIds and passtrough expressions.
     *
     * @since 2.3
     */
    public abstract List<String> resolveClientIds(SearchExpressionContext searchExpressionContext, String expressions);

    /**
     * <p class="changed_added_2_3">
     * Resolves a single {@link UIComponent}s for the given expression. If the component is resolved, the
     * {@link ContextCallback} will be invoked.
     * </p>
     *
     * @param searchExpressionContext the {@link SearchExpressionContext}
     * @param expression the search expression
     * @param callback the callback for the resolved component
     *
     * @throws ComponentNotFoundException if the expression can not be resolved and if
     * {@link SearchExpressionHint#IGNORE_NO_RESULT} was not passed.
     * @throws FacesException if the expression is not valid.
     *
     * @since 2.3
     */
    public abstract void resolveComponent(SearchExpressionContext searchExpressionContext, String expression, ContextCallback callback);

    /**
     * <p class="changed_added_2_3">
     * Resolves multiple {@link UIComponent}s for the given expression(s). The expressions will be splitted by
     * {@link #splitExpressions(jakarta.faces.context.FacesContext, java.lang.String)} and resolved one by one. For each
     * resolved component, the {@link ContextCallback} will be invoked.
     * </p>
     *
     * @param searchExpressionContext the {@link SearchExpressionContext}
     * @param expressions the search expression(s)
     * @param callback the callback for each resolved component
     *
     * @throws ComponentNotFoundException if any of the expressions can not be resolved and if
     * {@link SearchExpressionHint#IGNORE_NO_RESULT} was not passed.
     * @throws FacesException if the expression is not valid.
     *
     * @since 2.3
     */
    public abstract void resolveComponents(SearchExpressionContext searchExpressionContext, String expressions, ContextCallback callback);

    /**
     * <p class="changed_added_2_3">
     * Resolves multiple {@link UIComponent}s for the given expression. For each resolved component, the
     * {@link ContextCallback} will be invoked.
     *
     * This method is the most essential method in the API. It implements the algorithm which handles the recursion of the
     * keywords and id's.
     * </p>
     *
     * @param searchExpressionContext the {@link SearchExpressionContext}
     * @param expression the search expression
     * @param callback the callback for the resolved component
     *
     * @throws FacesException if the expression is not valid.
     *
     * @since 2.3
     */
    public void invokeOnComponent(SearchExpressionContext searchExpressionContext, String expression, ContextCallback callback) {
        invokeOnComponent(searchExpressionContext, searchExpressionContext.getSource(), expression, callback);
    }

    /**
     * <p class="changed_added_2_3">
     * Resolves multiple {@link UIComponent}s for the given expression. For each resolved component, the
     * {@link ContextCallback} will be invoked.
     *
     * This method is the most essential method in the API. It implements the algorithm which handles the recursion of the
     * keywords and id's.
     * </p>
     *
     * @param searchExpressionContext the {@link SearchExpressionContext}
     * @param previous The previous resolved component, that will be the base for searching
     * @param expression the search expression
     * @param callback the callback for the resolved component
     *
     * @throws FacesException if the expression is not valid.
     *
     * @since 2.3
     */
    public abstract void invokeOnComponent(SearchExpressionContext searchExpressionContext, UIComponent previous, String expression, ContextCallback callback);

    /**
     * <p class="changed_added_2_3">
     * Splits an string, based on {@link #getExpressionSeperatorChars(jakarta.faces.context.FacesContext)} with possible
     * multiple expressions into an array.
     * </p>
     *
     * @param context the {@link FacesContext} for the current request
     * @param expressions The expressions as string
     * @return the expression(s) as array
     *
     * @since 2.3
     */
    public abstract String[] splitExpressions(FacesContext context, String expressions);

    /**
     * <p class="changed_added_2_3">
     * Checks if the given expression is a "passtrough expression". A passthrough expression must only be a keyword. This
     * keyword will not be resolved by the {@link SearchKeywordResolver} and will be returned untouched.
     *
     * The client is responsible to resolve it later.
     * </p>
     *
     * @param searchExpressionContext the {@link SearchExpressionContext}
     * @param expression the expression
     * @return If the given expression is a passtrough expression
     *
     * @since 2.3
     */
    public abstract boolean isPassthroughExpression(SearchExpressionContext searchExpressionContext, String expression);

    /**
     * <p class="changed_added_2_3">
     * Checks if the given expression is a valid expression.
     * </p>
     *
     * <div class="changed_added_2_3"> A expression is invalid if:
     * <ul>
     * <li>No {@link SearchKeywordResolver} matches the requested keyword</li>
     * <li>A keyword or id is placed after a leaf keyword (@none:@form)</li>
     * </ul>
     * </div>
     *
     * @param searchExpressionContext the {@link SearchExpressionContext}
     * @param expression the expression
     *
     * @return If the given expression is a valid expression
     *
     * @since 2.3
     */
    public abstract boolean isValidExpression(SearchExpressionContext searchExpressionContext, String expression);

    /**
     * <p class="changed_added_2_3">
     * Return the characters used to separate expressions in a series of expressions. The default implementation returns
     * {@link SearchExpressionHandler#EXPRESSION_SEPARATOR_CHARS}.
     * </p>
     *
     * @param context the {@link FacesContext} for the current request
     * @return the separator chars
     *
     * @since 2.3
     */
    public char[] getExpressionSeperatorChars(FacesContext context) {
        return EXPRESSION_SEPARATOR_CHARS;
    }
}
