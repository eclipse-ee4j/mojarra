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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.search.SearchExpressionContext;
import jakarta.faces.component.search.SearchKeywordContext;
import jakarta.faces.component.search.SearchKeywordResolver;
import jakarta.faces.component.search.UntargetableComponent;

public class SearchKeywordResolverImplChild extends SearchKeywordResolver {

    private static final Pattern PATTERN = Pattern.compile("child\\((\\d+)\\)");

    @Override
    public void resolve(SearchKeywordContext searchKeywordContext, UIComponent current, String keyword) {

        Matcher matcher = PATTERN.matcher(keyword);

        if (matcher.matches()) {

            int childNumber = Integer.parseInt(matcher.group(1));
            if (childNumber + 1 > current.getChildCount()) {
                throw new FacesException("Component with clientId \"" + current.getClientId(searchKeywordContext.getSearchExpressionContext().getFacesContext())
                        + "\" has fewer children as \"" + childNumber + "\". Expression: \"" + keyword + "\"");
            }

            List<UIComponent> list = current.getChildren();
            int count = 0;
            for (int i = 0; i < current.getChildCount(); i++) {
                if (!(list.get(i) instanceof UntargetableComponent)) {
                    count++;
                }
                if (count == childNumber + 1) {
                    searchKeywordContext.invokeContextCallback(current.getChildren().get(childNumber));
                    break;
                }
            }

            if (count < childNumber) {
                throw new FacesException("Component with clientId \"" + current.getClientId(searchKeywordContext.getSearchExpressionContext().getFacesContext())
                        + "\" has fewer children as \"" + childNumber + "\". Expression: \"" + keyword + "\"");
            }
        } else {
            throw new FacesException("Expression does not match following pattern @child(n). Expression: \"" + keyword + "\"");
        }
    }

    @Override
    public boolean isResolverForKeyword(SearchExpressionContext searchExpressionContext, String keyword) {

        if (keyword.startsWith("child")) {
            try {
                Matcher matcher = PATTERN.matcher(keyword);
                return matcher.matches();
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

}
