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

import java.util.Set;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.search.SearchExpressionContext;
import jakarta.faces.component.search.SearchExpressionHint;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.context.FacesContext;

public class SearchExpressionContextImpl extends SearchExpressionContext {

    private final FacesContext facesContext;

    private UIComponent source;
    private Set<VisitHint> visitHints;
    private Set<SearchExpressionHint> expressionHints;

    public SearchExpressionContextImpl(FacesContext facesContext) {
        this.facesContext = facesContext;
    }

    @Override
    public UIComponent getSource() {
        return source;
    }

    public void setSource(UIComponent source) {
        this.source = source;
    }

    @Override
    public Set<VisitHint> getVisitHints() {
        return visitHints;
    }

    public void setVisitHints(Set<VisitHint> visitHints) {
        this.visitHints = visitHints;
    }

    @Override
    public Set<SearchExpressionHint> getExpressionHints() {
        return expressionHints;
    }

    public void setExpressionHints(Set<SearchExpressionHint> expressionHints) {
        this.expressionHints = expressionHints;
    }

    @Override
    public FacesContext getFacesContext() {
        return facesContext;
    }
}
