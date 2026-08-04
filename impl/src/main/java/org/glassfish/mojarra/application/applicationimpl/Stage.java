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

package org.glassfish.mojarra.application.applicationimpl;

import static jakarta.faces.application.ProjectStage.Development;
import static java.util.logging.Level.FINE;

import java.util.logging.Logger;

import jakarta.faces.application.Application;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PostAddToViewEvent;

import org.glassfish.mojarra.application.ValidateComponentNesting;
import org.glassfish.mojarra.config.WebConfiguration;
import org.glassfish.mojarra.context.FacesContextParam;
import org.glassfish.mojarra.util.FacesLogger;

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
        projectStage = WebConfiguration.getInstance(context.getExternalContext()).getProjectStage(context);

        LOGGER.log(FINE, "ProjectStage is {0}", projectStage);

        return projectStage;
    }

}
