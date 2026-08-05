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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.faces.application.ProjectStage;

import org.glassfish.mojarra.context.MojarraContextParam;
import org.glassfish.mojarra.mock.MockServletContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

/**
 * The resource caching parameters are tri-state, where <code>auto</code> leaves the decision to the project stage:
 * only Development, where a resource which changed on disk has to be noticed, gets the slower values.
 */
class StageDerivedDefaultTest {

    @Test
    void autoRelaxesResourceCachingInDevelopment() {
        WebConfiguration webConfiguration = configure(ProjectStage.Development);

        assertFalse(webConfiguration.isEnabled(MojarraContextParam.CACHE_RESOURCE_MODIFICATION_TIMESTAMP), "cache the modification timestamp");
        assertEquals(5, (int) webConfiguration.getInt(MojarraContextParam.RESOURCE_UPDATE_CHECK_PERIOD), "update check period");
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStage.class, names = "Development", mode = Mode.EXCLUDE)
    void autoCachesFullyEverywhereElse(ProjectStage projectStage) {
        WebConfiguration webConfiguration = configure(projectStage);

        assertTrue(webConfiguration.isEnabled(MojarraContextParam.CACHE_RESOURCE_MODIFICATION_TIMESTAMP), "cache the modification timestamp");
        assertEquals(-1, (int) webConfiguration.getInt(MojarraContextParam.RESOURCE_UPDATE_CHECK_PERIOD), "update check period");
    }

    /**
     * An explicit value pins it in either direction, including in Development, which is what distinguishes it from
     * <code>auto</code> and is why the stage is no longer also consulted where the value is used.
     */
    @Test
    void anExplicitValueWinsOverTheStage() {
        WebConfiguration inDevelopment = configure(ProjectStage.Development, "true", "9");

        assertTrue(inDevelopment.isEnabled(MojarraContextParam.CACHE_RESOURCE_MODIFICATION_TIMESTAMP), "cache the modification timestamp");
        assertEquals(9, (int) inDevelopment.getInt(MojarraContextParam.RESOURCE_UPDATE_CHECK_PERIOD), "update check period");

        WebConfiguration inProduction = configure(ProjectStage.Production, "false", "3");

        assertFalse(inProduction.isEnabled(MojarraContextParam.CACHE_RESOURCE_MODIFICATION_TIMESTAMP), "cache the modification timestamp");
        assertEquals(3, (int) inProduction.getInt(MojarraContextParam.RESOURCE_UPDATE_CHECK_PERIOD), "update check period");
    }

    /**
     * An unusable value behaves as though the parameter was never set, rather than as the value a bare parse happens to
     * produce, which for a boolean would silently be false and for the period would silently be never.
     */
    @Test
    void anUnusableValueFallsBackToAuto() {
        WebConfiguration inDevelopment = configure(ProjectStage.Development, "treu", "soon");

        assertFalse(inDevelopment.isEnabled(MojarraContextParam.CACHE_RESOURCE_MODIFICATION_TIMESTAMP), "cache the modification timestamp");
        assertEquals(5, (int) inDevelopment.getInt(MojarraContextParam.RESOURCE_UPDATE_CHECK_PERIOD), "update check period");

        WebConfiguration inProduction = configure(ProjectStage.Production, "treu", "soon");

        assertTrue(inProduction.isEnabled(MojarraContextParam.CACHE_RESOURCE_MODIFICATION_TIMESTAMP), "cache the modification timestamp");
        assertEquals(-1, (int) inProduction.getInt(MojarraContextParam.RESOURCE_UPDATE_CHECK_PERIOD), "update check period");
    }

    /**
     * And surrounding whitespace, which a container is not obliged to strip from a context parameter, does not turn a
     * usable value into an unusable one.
     */
    @Test
    void surroundingWhitespaceIsTolerated() {
        WebConfiguration webConfiguration = configure(ProjectStage.Production, "  false  ", "  7  ");

        assertFalse(webConfiguration.isEnabled(MojarraContextParam.CACHE_RESOURCE_MODIFICATION_TIMESTAMP), "cache the modification timestamp");
        assertEquals(7, (int) webConfiguration.getInt(MojarraContextParam.RESOURCE_UPDATE_CHECK_PERIOD), "update check period");
    }

    private static WebConfiguration configure(ProjectStage projectStage) {
        return configure(projectStage, null, null);
    }

    private static WebConfiguration configure(ProjectStage projectStage, String cacheTimestamp, String checkPeriod) {
        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter(PROJECT_STAGE_PARAM_NAME, projectStage.name());

        if (cacheTimestamp != null) {
            servletContext.addInitParameter(MojarraContextParam.CACHE_RESOURCE_MODIFICATION_TIMESTAMP.getName(), cacheTimestamp);
        }

        if (checkPeriod != null) {
            servletContext.addInitParameter(MojarraContextParam.RESOURCE_UPDATE_CHECK_PERIOD.getName(), checkPeriod);
        }

        return WebConfiguration.getInstance(servletContext);
    }
}
