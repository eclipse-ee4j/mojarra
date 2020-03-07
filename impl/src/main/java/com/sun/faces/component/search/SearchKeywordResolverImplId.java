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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.faces.FacesException;
import jakarta.faces.component.ContextCallback;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.search.SearchExpressionContext;
import jakarta.faces.component.search.SearchExpressionHint;
import jakarta.faces.component.search.SearchKeywordContext;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;

public class SearchKeywordResolverImplId extends AbstractSearchKeywordResolverImpl {

    private static final Pattern PATTERN = Pattern.compile("id\\(([\\w-]+)\\)");

    @Override
    public void resolve(SearchKeywordContext searchKeywordContext, UIComponent current, String keyword) {
        FacesContext facesContext = searchKeywordContext.getSearchExpressionContext().getFacesContext();

        String id = extractId(keyword);

        if (isHintSet(searchKeywordContext.getSearchExpressionContext(), SearchExpressionHint.SKIP_VIRTUAL_COMPONENTS)) {
            // Avoid visit tree because in this case we need real component instances.
            // This means components inside UIData will not be scanned.
            findWithId(facesContext, id, current, searchKeywordContext.getCallback());
        } else {
            current.visitTree(VisitContext.createVisitContext(facesContext, null, searchKeywordContext.getSearchExpressionContext().getVisitHints()),
                    (context, target) -> {
                        if (id.equals(target.getId())) {
                            searchKeywordContext.invokeContextCallback(target);

                            if (isHintSet(searchKeywordContext.getSearchExpressionContext(), SearchExpressionHint.RESOLVE_SINGLE_COMPONENT)) {
                                return VisitResult.COMPLETE;
                            }

                            return VisitResult.ACCEPT;
                        } else {
                            return VisitResult.ACCEPT;
                        }
                    });
        }

        searchKeywordContext.setKeywordResolved(true);
    }

    @Override
    public boolean isResolverForKeyword(SearchExpressionContext searchExpressionContext, String keyword) {

        if (keyword.startsWith("id")) {
            try {
                Matcher matcher = PATTERN.matcher(keyword);
                return matcher.matches();
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    protected String extractId(String expression) {
        Matcher matcher = PATTERN.matcher(expression);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            throw new FacesException("Expression does not match following pattern @id(id). Expression: \"" + expression + "\"");
        }
    }

    private void findWithId(FacesContext context, String id, UIComponent base, ContextCallback callback) {

        if (id.equals(base.getId())) {
            callback.invokeContextCallback(context, base);
        }

        if (base.getFacetCount() > 0) {
            for (UIComponent facet : base.getFacets().values()) {
                findWithId(context, id, facet, callback);
            }
        }

        if (base.getChildCount() > 0) {
            for (int i = 0, childCount = base.getChildCount(); i < childCount; i++) {
                UIComponent child = base.getChildren().get(i);
                findWithId(context, id, child, callback);
            }
        }
    }
}
