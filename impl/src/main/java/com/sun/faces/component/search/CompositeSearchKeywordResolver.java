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

package com.sun.faces.component.search;

import java.util.ArrayList;
import java.util.List;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.search.SearchExpressionContext;
import jakarta.faces.component.search.SearchKeywordContext;
import jakarta.faces.component.search.SearchKeywordResolver;

public class CompositeSearchKeywordResolver extends SearchKeywordResolver {

    private static final int NUMBER_OF_DEFAULT_IMPLEMENTATIONS = 12;

    private final List<SearchKeywordResolver> resolvers;

    public CompositeSearchKeywordResolver() {
        resolvers = new ArrayList<>(NUMBER_OF_DEFAULT_IMPLEMENTATIONS);
    }

    public void add(SearchKeywordResolver searchKeywordResolver) {
        if (searchKeywordResolver == null) {
            throw new NullPointerException();
        }

        resolvers.add(0, searchKeywordResolver);
    }

    @Override
    public void resolve(SearchKeywordContext context, UIComponent current, String keyword) {
        context.setKeywordResolved(false);

        for (SearchKeywordResolver resolver : resolvers) {
            if (resolver.isResolverForKeyword(context.getSearchExpressionContext(), keyword)) {
                resolver.resolve(context, current, keyword);
                if (context.isKeywordResolved()) {
                    return;
                }
            }
        }
    }

    @Override
    public boolean isResolverForKeyword(SearchExpressionContext searchExpressionContext, String keyword) {
        for (SearchKeywordResolver resolver : resolvers) {
            if (resolver.isResolverForKeyword(searchExpressionContext, keyword)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isPassthrough(SearchExpressionContext searchExpressionContext, String keyword) {
        for (SearchKeywordResolver resolver : resolvers) {
            if (resolver.isResolverForKeyword(searchExpressionContext, keyword)) {
                return resolver.isPassthrough(searchExpressionContext, keyword);
            }
        }

        return false;
    }

    @Override
    public boolean isLeaf(SearchExpressionContext searchExpressionContext, String keyword) {
        for (SearchKeywordResolver resolver : resolvers) {
            if (resolver.isResolverForKeyword(searchExpressionContext, keyword)) {
                return resolver.isLeaf(searchExpressionContext, keyword);
            }
        }

        return false;
    }
}
