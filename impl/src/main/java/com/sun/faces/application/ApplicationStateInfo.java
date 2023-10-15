/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.application;

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.PartialStateSaving;
import static com.sun.faces.config.WebConfiguration.WebContextInitParameter.FullStateSavingViewIds;
import static com.sun.faces.util.Util.notNullViewId;
import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.application.StateManager;

/**
 * This class maintains per-application information pertaining to partail or full state saving as a whole or partial
 * state saving with some views using full state saving.
 */
public class ApplicationStateInfo {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    private final boolean partialStateSaving;
    private Set<String> fullStateViewIds;

    // ------------------------------------------------------------ Constructors

    public ApplicationStateInfo() {

        WebConfiguration config = WebConfiguration.getInstance();
        partialStateSaving = config.isOptionEnabled(PartialStateSaving);

        if (partialStateSaving) {
            String[] viewIds = config.getOptionValue(FullStateSavingViewIds, ",");
            fullStateViewIds = new HashSet<>(viewIds.length, 1.0f);
            fullStateViewIds.addAll(asList(viewIds));
        }
        else {
            LOGGER.warning("The configuration '" + StateManager.PARTIAL_STATE_SAVING_PARAM_NAME
                + "' is deprecated as of Faces 4.1 and should not longer be used.");
        }

    }

    // --------------------------------------------------------- Private Methods

    /**
     * @param viewId the view ID to check
     * @throws IllegalArgumentException if viewId is null
     * @return <code>true</code> if partial state saving should be used for the specified view ID, otherwise
     * <code>false</code>
     */
    public boolean usePartialStateSaving(String viewId) {
        notNullViewId(viewId);

        return partialStateSaving && !fullStateViewIds.contains(viewId);
    }

}
