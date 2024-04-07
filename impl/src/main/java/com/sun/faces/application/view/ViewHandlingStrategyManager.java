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

import java.util.Arrays;

/**
 * Interface for working with multiple {@link com.sun.faces.application.view.ViewHandlingStrategy} implementations.
 */
public class ViewHandlingStrategyManager {

    // The strategies associated with this instance
    private volatile ViewHandlingStrategy[] strategies;

    // ------------------------------------------------------------- Constructor

    /**
     * By default the strategies available (in order) will be {@link FaceletViewHandlingStrategy}.
     */
    public ViewHandlingStrategyManager() {
        strategies = new ViewHandlingStrategy[] { new FaceletViewHandlingStrategy() };
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
        return Arrays.stream(strategies)
                     .filter(strategy -> strategy.handlesViewId(viewId))
                     .findFirst()
                     .orElseThrow(()-> new ViewHandlingStrategyNotFoundException(viewId));
    }

    /**
     * @return the currently registered {@link com.sun.faces.application.view.ViewHandlingStrategy} implementations.
     */
    public ViewHandlingStrategy[] getViewHandlingStrategies() {
        return strategies.clone();
    }

    /**
     * Update the {@link com.sun.faces.application.view.ViewHandlingStrategy} implementations to be applied when processing
     * Faces requests.
     *
     * @param stratagies the new view handling strategies
     */
    public synchronized void setViewHandlingStrategies(ViewHandlingStrategy[] stratagies) {
        strategies = stratagies.clone();
    }

}
