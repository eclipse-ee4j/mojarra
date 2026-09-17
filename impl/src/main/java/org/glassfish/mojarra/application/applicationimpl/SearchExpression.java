/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.mojarra.application.applicationimpl;

import static java.util.logging.Level.FINE;
import static org.glassfish.mojarra.util.MessageUtils.ILLEGAL_ATTEMPT_SETTING_APPLICATION_ARTIFACT_ID;
import static org.glassfish.mojarra.util.Util.notNull;

import java.text.MessageFormat;
import java.util.logging.Logger;

import jakarta.faces.component.search.SearchExpressionHandler;
import jakarta.faces.component.search.SearchKeywordResolver;

import org.glassfish.mojarra.application.ApplicationAssociate;
import org.glassfish.mojarra.component.search.CompositeSearchKeywordResolver;
import org.glassfish.mojarra.component.search.SearchKeywordResolverImplAll;
import org.glassfish.mojarra.component.search.SearchKeywordResolverImplChild;
import org.glassfish.mojarra.component.search.SearchKeywordResolverImplComposite;
import org.glassfish.mojarra.component.search.SearchKeywordResolverImplForm;
import org.glassfish.mojarra.component.search.SearchKeywordResolverImplId;
import org.glassfish.mojarra.component.search.SearchKeywordResolverImplNamingContainer;
import org.glassfish.mojarra.component.search.SearchKeywordResolverImplNext;
import org.glassfish.mojarra.component.search.SearchKeywordResolverImplNone;
import org.glassfish.mojarra.component.search.SearchKeywordResolverImplParent;
import org.glassfish.mojarra.component.search.SearchKeywordResolverImplPrevious;
import org.glassfish.mojarra.component.search.SearchKeywordResolverImplRoot;
import org.glassfish.mojarra.component.search.SearchKeywordResolverImplThis;
import org.glassfish.mojarra.util.FacesLogger;
import org.glassfish.mojarra.util.MessageUtils;

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
