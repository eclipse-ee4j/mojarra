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

import static jakarta.faces.application.ProjectStage.Development;
import static java.util.logging.Level.FINE;

import java.util.logging.Logger;

import com.sun.faces.application.ValidateComponentNesting;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.annotation.FacesConfig.ContextParam;
import jakarta.faces.application.Application;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PostAddToViewEvent;

public class Stage {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    private ProjectStage projectStage;

    /*
     * @see jakarta.faces.application.Application#getProjectStage()
     */
    public ProjectStage getProjectStage(Application application) {

        if (projectStage == null) {
            projectStage = fetchProjectStageFromConfig();

            if (projectStage == Development) {
                application.subscribeToEvent(PostAddToViewEvent.class, new ValidateComponentNesting());
            }
        }

        return projectStage;
    }

    // ----------------------------------------------------------- Private methods

    private ProjectStage fetchProjectStageFromConfig() {
        FacesContext context = FacesContext.getCurrentInstance();
        WebConfiguration webConfig = WebConfiguration.getInstance(context.getExternalContext());
        String value = webConfig.getEnvironmentEntry(WebConfiguration.WebEnvironmentEntry.ProjectStage);

        if (value != null) {
            projectStage = ProjectStage.valueOf(value);
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, "ProjectStage configured via JNDI: {0}", value);
            }
        } else {
            projectStage = ContextParam.PROJECT_STAGE.getValue(context);
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, "ProjectStage configured via servlet context init parameter: {0}", value);
            }
        }

        return projectStage;
    }

}
