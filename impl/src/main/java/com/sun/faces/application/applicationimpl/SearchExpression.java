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

package com.sun.faces.application.applicationimpl;

import static com.sun.faces.util.MessageUtils.ILLEGAL_ATTEMPT_SETTING_APPLICATION_ARTIFACT_ID;
import static com.sun.faces.util.Util.notNull;
import static java.util.logging.Level.FINE;

import java.text.MessageFormat;
import java.util.logging.Logger;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.component.search.CompositeSearchKeywordResolver;
import com.sun.faces.component.search.SearchKeywordResolverImplAll;
import com.sun.faces.component.search.SearchKeywordResolverImplChild;
import com.sun.faces.component.search.SearchKeywordResolverImplComposite;
import com.sun.faces.component.search.SearchKeywordResolverImplForm;
import com.sun.faces.component.search.SearchKeywordResolverImplId;
import com.sun.faces.component.search.SearchKeywordResolverImplNamingContainer;
import com.sun.faces.component.search.SearchKeywordResolverImplNext;
import com.sun.faces.component.search.SearchKeywordResolverImplNone;
import com.sun.faces.component.search.SearchKeywordResolverImplParent;
import com.sun.faces.component.search.SearchKeywordResolverImplPrevious;
import com.sun.faces.component.search.SearchKeywordResolverImplRoot;
import com.sun.faces.component.search.SearchKeywordResolverImplThis;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;

import jakarta.faces.component.search.SearchExpressionHandler;
import jakarta.faces.component.search.SearchKeywordResolver;

public class SearchExpression {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    private final ApplicationAssociate associate;

    private final CompositeSearchKeywordResolver searchKeywordResolvers;

    public SearchExpression(ApplicationAssociate applicationAssociate) {
        associate = applicationAssociate;

        searchKeywordResolvers = new CompositeSearchKeywordResolver();

        searchKeywordResolvers.add(new SearchKeywordResolverImplThis());
        searchKeywordResolvers.add(new SearchKeywordResolverImplParent());
        searchKeywordResolvers.add(new SearchKeywordResolverImplForm());
        searchKeywordResolvers.add(new SearchKeywordResolverImplComposite());
        searchKeywordResolvers.add(new SearchKeywordResolverImplNext());
        searchKeywordResolvers.add(new SearchKeywordResolverImplPrevious());
        searchKeywordResolvers.add(new SearchKeywordResolverImplNone());
        searchKeywordResolvers.add(new SearchKeywordResolverImplNamingContainer());
        searchKeywordResolvers.add(new SearchKeywordResolverImplRoot());
        searchKeywordResolvers.add(new SearchKeywordResolverImplId());
        searchKeywordResolvers.add(new SearchKeywordResolverImplChild());
        searchKeywordResolvers.add(new SearchKeywordResolverImplAll());
    }

    public SearchExpressionHandler getSearchExpressionHandler() {
        return associate.getSearchExpressionHandler();
    }

    public void setSearchExpressionHandler(SearchExpressionHandler searchExpressionHandler) {
        notNull("searchExpressionHandler", searchExpressionHandler);

        associate.setSearchExpressionHandler(searchExpressionHandler);

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.fine(MessageFormat.format("Set SearchExpressionHandler Instance to ''{0}''", searchExpressionHandler.getClass().getName()));
        }
    }

    public void addSearchKeywordResolver(SearchKeywordResolver resolver) {
        if (associate.hasRequestBeenServiced()) {
            throw new IllegalStateException(MessageUtils.getExceptionMessageString(ILLEGAL_ATTEMPT_SETTING_APPLICATION_ARTIFACT_ID, "SearchKeywordResolver"));
        }

        searchKeywordResolvers.add(resolver);
    }

    public SearchKeywordResolver getSearchKeywordResolver() {
        return searchKeywordResolvers;
    }

}
