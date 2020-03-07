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

import jakarta.faces.component.UIComponent;

/**
 * <p class="changed_added_2_3">
 * A <strong>SearchKeywordResolver</strong> is responsible for resolving a single keyword. Implementations must support
 * the following set of {@code SearchKeywordResolver} implementations, each with the associated behavior.
 * </p>
 *
 * <div class="changed_added_2_3">
 *
 * <table border="1">
 * <caption>List of required supported keywords and their behaviors</caption>
 *
 * <tr>
 *
 * <th>Search Keyword</th>
 *
 * <th>Behavior</th>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>&#64;all</td>
 *
 * <td>All components in the view</td>
 *
 * <tr>
 *
 * <td>&#64;child(n)</td>
 *
 * <td>The nth child of the base component</td>
 *
 * <tr>
 *
 * <td>&#64;composite</td>
 *
 * <td>The composite component parent of the base component</td>
 *
 * <tr>
 *
 * <td>&#64;form</td>
 *
 * <td>The closest form ancestor of the base component</td>
 *
 * <tr>
 *
 * <td>&#64;id(id)</td>
 *
 *
 * <td>Resolves to the components with the specified component id (not clientId). This is useful when the exact location
 * of the component tree is unknown, but must be used with caution when there multiple occurrences of the given id
 * within the view.</td>
 *
 *
 * <tr>
 *
 * <td>&#64;namingcontainer</td>
 *
 * <td>The closest {@link jakarta.faces.component.NamingContainer} ancestor component of the base component</td>
 *
 * <tr>
 *
 * <td>&#64;next</td>
 *
 * <td>The next component in the view after the base component</td>
 *
 * <tr>
 *
 * <td>&#64;none</td>
 *
 * <td>No component</td>
 *
 * <tr>
 *
 * <td>&#64;parent</td>
 *
 * <td>The parent of the base component</td>
 *
 * <tr>
 *
 * <td>&#64;previous</td>
 *
 * <td>The previous component to the base component</td>
 *
 * <tr>
 *
 * <td>&#64;root</td>
 *
 * <td>The {@link jakarta.faces.component.UIViewRoot}</td>
 *
 * <tr>
 *
 * <td>&#64;this</td>
 *
 * <td>The base component</td>
 *
 *
 * </table>
 *
 * <p>
 * New {@link SearchKeywordResolver}s can be registered via
 * {@link jakarta.faces.application.Application#addSearchKeywordResolver(jakarta.faces.component.search.SearchKeywordResolver)}
 * or in the application configuration resources.
 * </p>
 *
 * <pre>
 * <code>
 * &lt;application&gt;
 *   &lt;search-keyword-resolver&gt;...&lt;/search-keyword-resolver&gt;
 * &lt;/application&gt;
 * </code>
 * </pre>
 *
 * </div>
 *
 * @since 2.3
 */
public abstract class SearchKeywordResolver {

    /**
     * <p class="changed_added_2_3">
     * Try to resolve one or multiple {@link UIComponent}s based on the keyword and calls
     * {@link SearchKeywordContext#invokeContextCallback(jakarta.faces.component.UIComponent)} for each resolved component.
     * </p>
     *
     * @param searchKeywordContext the {@code SearchKeywordContext}
     * @param current the previous resolved component or the source component (if called for the first keyword in the chain)
     * @param keyword the keyword
     *
     * @since 2.3
     */
    public abstract void resolve(SearchKeywordContext searchKeywordContext, UIComponent current, String keyword);

    /**
     * <p class="changed_added_2_3">
     * Checks if the current instance of the {@link SearchKeywordResolver} is responsible for resolving the keyword.
     * </p>
     *
     * @param searchExpressionContext the {@link SearchExpressionContext}
     * @param keyword the keyword
     *
     * @return <code>true</code> if it's responsible for resolving this keyword
     *
     * @since 2.3
     */
    public abstract boolean isResolverForKeyword(SearchExpressionContext searchExpressionContext, String keyword);

    /**
     * <p class="changed_added_2_3">
     * A passthrough keyword is a keyword, that according to the context, does not require to be resolved on the server, and
     * can be passed "unresolved" to the client.
     * </p>
     *
     * @param searchExpressionContext the {@link SearchExpressionContext}
     * @param keyword the keyword
     *
     * @return <code>true</code> if it's passthrough keyword.
     *
     * @since 2.3
     */
    public boolean isPassthrough(SearchExpressionContext searchExpressionContext, String keyword) {
        return false;
    }

    /**
     * <p class="changed_added_2_3">
     * A leaf keyword is a keyword that does not allow to be combined with keywords or id chains to the right. For
     * example: @none:@parent.
     * </p>
     *
     * @param searchExpressionContext the {@link SearchExpressionContext}
     * @param keyword the keyword
     *
     * @return <code>true</code> if it's leaf keyword.
     *
     * @since 2.3
     */
    public boolean isLeaf(SearchExpressionContext searchExpressionContext, String keyword) {
        return false;
    }
}
