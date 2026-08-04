/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

package org.glassfish.mojarra.config;

import static jakarta.faces.application.ProjectStage.PROJECT_STAGE_PARAM_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import org.glassfish.mojarra.mock.MockServletContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Startup validation asks {@link WebConfiguration} for the project stage rather than the {@link jakarta.faces.application.Application},
 * which does not exist yet while the configuration is being processed, so it has to answer the same thing.
 */
class ProjectStageResolutionTest {

    @Test
    void defaultsToProduction() {
        assertEquals(ProjectStage.Production, resolve(null));
    }

    @ParameterizedTest
    @EnumSource(ProjectStage.class)
    void readsTheContextParameter(ProjectStage stage) {
        assertEquals(stage, resolve(stage.name()));
    }

    private static ProjectStage resolve(String contextParameterValue) {
        MockServletContext servletContext = new MockServletContext();

        if (contextParameterValue != null) {
            servletContext.addInitParameter(PROJECT_STAGE_PARAM_NAME, contextParameterValue);
        }

        ExternalContext externalContext = mock(ExternalContext.class);
        when(externalContext.getContext()).thenReturn(servletContext);
        when(externalContext.getInitParameter(any())).thenAnswer(
                invocation -> servletContext.getInitParameter(invocation.getArgument(0)));

        FacesContext context = mock(FacesContext.class);
        when(context.getExternalContext()).thenReturn(externalContext);

        return WebConfiguration.getInstance(servletContext).getProjectStage();
    }
}
