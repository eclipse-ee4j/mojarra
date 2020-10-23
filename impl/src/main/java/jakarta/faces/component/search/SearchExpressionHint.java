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
 * An enum that specifies hints that impact the behavior of a component tree search.
 * </p>
 *
 * @since 2.3
 */
public enum SearchExpressionHint {

    /**
     * <p class="changed_added_2_3">
     * Hint that indicates that if a expression resolves to <code>null</code>, <code>null</code> will be returned. Otherwise
     * a {@link ComponentNotFoundException} will be thrown.
     * </p>
     *
     * @since 2.3
     */
    IGNORE_NO_RESULT,

    /**
     * <p class="changed_added_2_3">
     * Hint that indicates that only real {@link UIComponent}s should be resolved. Virtual components are components, which
     * are reused in repeatable components like {@link jakarta.faces.component.UIData} or <code>ui:repeat</code>.
     * </p>
     *
     * @since 2.3
     */
    SKIP_VIRTUAL_COMPONENTS,

    /**
     * <p class="changed_added_2_3">
     * Hint that indicates that only one component should be resolved.
     *
     * This hint is important if a {@link SearchKeywordResolver} uses {@link UIComponent#visitTree}, as the tree visit can
     * be terminated after the first component was resolved.
     *
     * This hint will be automatically added internally if
     * {@link SearchExpressionHandler#resolveClientId(jakarta.faces.component.search.SearchExpressionContext, java.lang.String)}
     * or
     * {@link SearchExpressionHandler#resolveComponent(jakarta.faces.component.search.SearchExpressionContext, java.lang.String, jakarta.faces.component.ContextCallback)}
     * is used.
     * </p>
     *
     * @since 2.3
     */
    RESOLVE_SINGLE_COMPONENT,

    /**
     * <p class="changed_added_2_3">
     * Hint that indicates that a keyword can be resolved later and will just be returned as passthrough, if supported by
     * the keyword. For example: The AJAX client- and server-side is able to handle @all or @form. So it's not necessary at
     * all to resolve them to their clientId's on the server side.
     * </p>
     *
     * @see SearchKeywordResolver#isPassthrough(jakarta.faces.component.search.SearchExpressionContext, java.lang.String)
     *
     * @since 2.3
     */
    RESOLVE_CLIENT_SIDE
}
