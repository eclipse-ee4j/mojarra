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

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.search.SearchExpressionContext;
import jakarta.faces.component.search.SearchExpressionHint;
import jakarta.faces.component.search.SearchKeywordContext;

public class SearchKeywordResolverImplAll extends AbstractSearchKeywordResolverImpl {

    @Override
    public void resolve(SearchKeywordContext searchKeywordContext, UIComponent current, String keyword) {

        if (current.getParent() != null) {
            UIComponent parent = current.getParent();

            while (parent.getParent() != null) {
                parent = parent.getParent();
            }

            searchKeywordContext.invokeContextCallback(parent);
        } else {
            searchKeywordContext.setKeywordResolved(true);
        }
    }

    @Override
    public boolean isResolverForKeyword(SearchExpressionContext searchExpressionContext, String keyword) {
        return "all".equals(keyword);
    }

    @Override
    public boolean isPassthrough(SearchExpressionContext searchExpressionContext, String keyword) {
        return isHintSet(searchExpressionContext, SearchExpressionHint.RESOLVE_CLIENT_SIDE);
    }

    @Override
    public boolean isLeaf(SearchExpressionContext searchExpressionContext, String keyword) {
        return true;
    }
}
