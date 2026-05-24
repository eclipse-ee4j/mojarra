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

package org.glassfish.mojarra.application.view;

/**
 * Interface for working with multiple {@link org.glassfish.mojarra.application.view.ViewHandlingStrategy} implementations.
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
     * Iterate through the available {@link org.glassfish.mojarra.application.view.ViewHandlingStrategy} implementations. The first
     * one to return true from {@link org.glassfish.mojarra.application.view.ViewHandlingStrategy#handlesViewId(String)} will be the
     * {@link org.glassfish.mojarra.application.view.ViewHandlingStrategy} returned.
     *
     * @param viewId the viewId to match a {@link org.glassfish.mojarra.application.view.ViewHandlingStrategy} to
     *
     * @throws ViewHandlingStrategyNotFoundException if no match is found.
     *
     * @return a {@link org.glassfish.mojarra.application.view.ViewHandlingStrategy} for the specified <code>viewId</code>
     */
    public ViewHandlingStrategy getStrategy(String viewId) {
        if (viewId != null) {
            var snapshot = strategies; // defensive copy of volatile "pointer"
            for (ViewHandlingStrategy strategy : snapshot)
                if (strategy.handlesViewId(viewId))
                    return strategy;
        }

        // viewId is null or strategy not found
        throw new ViewHandlingStrategyNotFoundException(viewId);
    }

    /**
     * @return the currently registered {@link org.glassfish.mojarra.application.view.ViewHandlingStrategy} implementations.
     */
    public ViewHandlingStrategy[] getViewHandlingStrategies() {
        return strategies.clone(); // defensive copy
    }

    /**
     * Update the {@link org.glassfish.mojarra.application.view.ViewHandlingStrategy} implementations to be applied when processing
     * Faces requests.
     *
     * @param strategies the new view handling strategies
     */
    public synchronized void setViewHandlingStrategies(ViewHandlingStrategy[] strategies) {
        this.strategies = strategies.clone(); // defensive copy
    }

}
