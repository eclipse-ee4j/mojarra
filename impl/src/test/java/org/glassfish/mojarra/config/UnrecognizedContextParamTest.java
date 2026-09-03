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

import java.util.List;

import jakarta.faces.application.ProjectStage;

import org.glassfish.mojarra.mock.MockServletContext;
import org.junit.jupiter.api.Test;

/**
 * Nothing used to validate the names an application declares, so a typo or a parameter which no longer exists was silently ignored, with no way to tell from
 * the outside.
 */
class UnrecognizedContextParamTest extends ConfigurationLoggingTestBase {

    private static final String UNRECOGNIZED = "faces.config.webconfig.param.unrecognized";

    @Test
    void aTypoIsReported() {
        configure(ProjectStage.Development, "org.glassfish.mojarra.enableTreading", "true");

        assertEquals(List.of("org.glassfish.mojarra.enableTreading"), unrecognizedParameterNames());
    }

    @Test
    void aRecognizedParameterIsNotReported() {
        configure(ProjectStage.Development, MojarraContextParam.COMPRESS_VIEW_STATE.getName(), "false");

        assertEquals(List.of(), unrecognizedParameterNames());
    }

    /**
     * The legacy spelling is normalized into Mojarra's own namespace before this runs, so a name which is only known under the old prefix must not be mistaken
     * for a typo.
     */
    @Test
    void aLegacySpellingIsNotReported() {
        configure(ProjectStage.Development, "com.sun.faces.compressViewState", "false");

        assertEquals(List.of(), unrecognizedParameterNames());
    }

    /**
     * A parameter promoted to the specification keeps both of its deprecated spellings, and the one the 5.0 rename produces is a name which never existed, so
     * it would look exactly like a typo without the alias table.
     */
    @Test
    void aPromotedParameterIsNotReportedUnderEitherOldSpelling() {
        configure(ProjectStage.Development, "org.glassfish.mojarra.enableCspNonce", "true");

        assertEquals(List.of(), unrecognizedParameterNames());
    }

    /**
     * Nothing can be done about it in Production any more, so it stays quiet there.
     */
    @Test
    void nothingIsReportedInProduction() {
        configure(ProjectStage.Production, "org.glassfish.mojarra.enableTreading", "true");

        assertEquals(List.of(), unrecognizedParameterNames());
    }

    private static void configure(ProjectStage projectStage, String name, String value) {
        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter(PROJECT_STAGE_PARAM_NAME, projectStage.name());
        servletContext.addInitParameter(name, value);

        WebConfiguration.getInstance(servletContext);
    }

    private List<String> unrecognizedParameterNames() {
        return loggedArguments(UNRECOGNIZED, 1);
    }

}
