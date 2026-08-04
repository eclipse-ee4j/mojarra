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
import static org.glassfish.mojarra.config.WebConfiguration.BooleanWebContextInitParameter.CacheResourceModificationTimestamp;
import static org.glassfish.mojarra.config.WebConfiguration.WebContextInitParameter.ResourceUpdateCheckPeriod;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.faces.application.ProjectStage;

import org.glassfish.mojarra.mock.MockServletContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

/**
 * The resource caching parameters default to what production wants, and only Development, where a resource which
 * changed on disk has to be noticed, gets the slower values.
 */
class StageDerivedDefaultTest {

    @Test
    void resourceCachingIsRelaxedInDevelopment() {
        WebConfiguration webConfiguration = configure(ProjectStage.Development);

        assertFalse(webConfiguration.isOptionEnabled(CacheResourceModificationTimestamp), "cache the modification timestamp");
        assertEquals("5", webConfiguration.getOptionValue(ResourceUpdateCheckPeriod), "update check period");
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStage.class, names = "Development", mode = Mode.EXCLUDE)
    void resourceCachingIsFullEverywhereElse(ProjectStage projectStage) {
        WebConfiguration webConfiguration = configure(projectStage);

        assertTrue(webConfiguration.isOptionEnabled(CacheResourceModificationTimestamp), "cache the modification timestamp");
        assertEquals("-1", webConfiguration.getOptionValue(ResourceUpdateCheckPeriod), "update check period");
    }

    /**
     * An explicit setting beats the stage either way, which is what keeps the parameters worth having.
     */
    @Test
    void anExplicitSettingWinsOverTheStage() {
        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter(PROJECT_STAGE_PARAM_NAME, ProjectStage.Development.name());
        servletContext.addInitParameter(CacheResourceModificationTimestamp.getQualifiedName(), "true");
        servletContext.addInitParameter(ResourceUpdateCheckPeriod.getQualifiedName(), "9");

        WebConfiguration webConfiguration = WebConfiguration.getInstance(servletContext);

        assertTrue(webConfiguration.isOptionEnabled(CacheResourceModificationTimestamp), "cache the modification timestamp");
        assertEquals("9", webConfiguration.getOptionValue(ResourceUpdateCheckPeriod), "update check period");
    }

    private static WebConfiguration configure(ProjectStage projectStage) {
        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter(PROJECT_STAGE_PARAM_NAME, projectStage.name());

        return WebConfiguration.getInstance(servletContext);
    }
}
