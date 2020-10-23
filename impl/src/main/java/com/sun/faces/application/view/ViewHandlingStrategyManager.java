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

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.DisableFaceletJSFViewHandler;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.DisableFaceletJSFViewHandlerDeprecated;

import com.sun.faces.config.WebConfiguration;

/**
 * Interface for working with multiple {@link com.sun.faces.application.view.ViewHandlingStrategy} implementations.
 */
public class ViewHandlingStrategyManager {

    // The strategies associated with this instance
    private volatile ViewHandlingStrategy[] strategies;

    // ------------------------------------------------------------- Constructor

    /**
     * Be default, if
     * {@link com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter#DisableFaceletJSFViewHandler} isn't
     * enabled, the strategies available (in order) will be {@link FaceletViewHandlingStrategy} and
     * {@link com.sun.faces.application.view.JspViewHandlingStrategy}.
     * <p>
     * Otherwise, only the {@link com.sun.faces.application.view.JspViewHandlingStrategy} will be available.
     */
    public ViewHandlingStrategyManager() {

        WebConfiguration webConfig = WebConfiguration.getInstance();
        boolean pdlDisabled = webConfig.isOptionEnabled(DisableFaceletJSFViewHandler) || webConfig.isOptionEnabled(DisableFaceletJSFViewHandlerDeprecated);

        strategies = pdlDisabled ? new ViewHandlingStrategy[] { new JspViewHandlingStrategy() }
                : new ViewHandlingStrategy[] { new FaceletViewHandlingStrategy(), new JspViewHandlingStrategy() };

    }

    // ---------------------------------------------------------- Public Methods

    /**
     * <p>
     * Iterate through the available {@link com.sun.faces.application.view.ViewHandlingStrategy} implementations. The first
     * one to return true from {@link com.sun.faces.application.view.ViewHandlingStrategy#handlesViewId(String)} will be the
     * {@link com.sun.faces.application.view.ViewHandlingStrategy} returned.
     * <p>
     *
     * @param viewId the viewId to match a {@link com.sun.faces.application.view.ViewHandlingStrategy} to
     *
     * @throws ViewHandlingStrategyNotFoundException if no match is found.
     *
     * @return a {@link com.sun.faces.application.view.ViewHandlingStrategy} for the specifed <code>viewId</code>
     */
    public ViewHandlingStrategy getStrategy(String viewId) {

        for (ViewHandlingStrategy strategy : strategies) {
            if (strategy.handlesViewId(viewId)) {
                return strategy;
            }
        }

        throw new ViewHandlingStrategyNotFoundException();
    }

    /**
     * @return the currently registered {@link com.sun.faces.application.view.ViewHandlingStrategy} implementations.
     */
    public ViewHandlingStrategy[] getViewHandlingStrategies() {
        return strategies.clone();
    }

    /**
     * Update the {@link com.sun.faces.application.view.ViewHandlingStrategy} implementations to be applied when processing
     * JSF requests.
     *
     * @param stratagies the new view handling strategies
     */
    public synchronized void setViewHandlingStrategies(ViewHandlingStrategy[] stratagies) {
        strategies = stratagies.clone();
    }

}
