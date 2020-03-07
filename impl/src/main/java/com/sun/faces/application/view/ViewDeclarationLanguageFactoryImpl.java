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

package com.sun.faces.application.view;

import static java.util.Arrays.asList;

import java.util.List;

import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.faces.view.ViewDeclarationLanguageFactory;

/**
 * Default implementation of {@link ViewDeclarationLanguageFactory}.
 */
public class ViewDeclarationLanguageFactoryImpl extends ViewDeclarationLanguageFactory {

    public ViewDeclarationLanguageFactoryImpl() {
        super(null);
    }

    private ViewHandlingStrategyManager viewHandlingStrategyManager;
    private List<ViewDeclarationLanguage> allViewDeclarationLanguages;

    // ------------------------------------ Methods from ViewDeclarationLanguageFactory

    /**
     * @see jakarta.faces.view.ViewDeclarationLanguageFactory#getViewDeclarationLanguage(String)
     */
    @Override
    public ViewDeclarationLanguage getViewDeclarationLanguage(String viewId) {
        return getViewHandlingStrategyManager().getStrategy(viewId);
    }

    /**
     * @see jakarta.faces.view.ViewDeclarationLanguageFactory#getAllViewDeclarationLanguages()
     */
    @Override
    public List<ViewDeclarationLanguage> getAllViewDeclarationLanguages() {

        if (allViewDeclarationLanguages == null) {
            allViewDeclarationLanguages = asList(getViewHandlingStrategyManager().getViewHandlingStrategies());
        }

        return allViewDeclarationLanguages;
    }

    // --------------------------------------------------------- Private Methods

    private ViewHandlingStrategyManager getViewHandlingStrategyManager() {

        if (viewHandlingStrategyManager == null) {
            viewHandlingStrategyManager = new ViewHandlingStrategyManager();
        }

        return viewHandlingStrategyManager;
    }

}
