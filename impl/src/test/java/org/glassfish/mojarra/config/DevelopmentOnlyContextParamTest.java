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
import static java.util.logging.Level.WARNING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import jakarta.faces.application.ProjectStage;

import org.glassfish.mojarra.context.MojarraContextParam;
import org.glassfish.mojarra.mock.MockServletContext;
import org.glassfish.mojarra.util.FacesLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

/**
 * A parameter which only makes debugging easier weakens the deployment anywhere else, so it is honored in Development
 * alone. Note the two covered here have opposite polarity, which is why reverting to the default is the rule rather
 * than forcing a particular value.
 */
class DevelopmentOnlyContextParamTest {

    private static final String DEVELOPMENT_ONLY = "faces.config.webconfig.param.development_only";

    private final Logger logger = FacesLogger.CONFIG.getLogger();
    private final List<LogRecord> records = new ArrayList<>();
    private final Handler handler = new Handler() {

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    };

    @BeforeEach
    void captureLogging() {
        logger.addHandler(handler);
    }

    @AfterEach
    void stopCapturingLogging() {
        logger.removeHandler(handler);
    }

    @Test
    void areHonoredInDevelopment() {
        WebConfiguration webConfiguration = configure(ProjectStage.Development);

        assertTrue(webConfiguration.isEnabled(MojarraContextParam.ENABLE_CLIENT_STATE_DEBUGGING), "client state debugging");
        assertFalse(webConfiguration.isEnabled(MojarraContextParam.GENERATE_UNIQUE_SERVER_STATE_IDS), "unique server state ids");
        assertEquals(List.of(), revertedParameterNames(), "nothing is reverted, so nothing is reported");
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStage.class, names = "Development", mode = Mode.EXCLUDE)
    void revertToTheirDefaultElsewhere(ProjectStage projectStage) {
        WebConfiguration webConfiguration = configure(projectStage);

        assertFalse(webConfiguration.isEnabled(MojarraContextParam.ENABLE_CLIENT_STATE_DEBUGGING), "client state debugging");
        assertTrue(webConfiguration.isEnabled(MojarraContextParam.GENERATE_UNIQUE_SERVER_STATE_IDS), "unique server state ids");
        assertEquals(
                List.of(MojarraContextParam.ENABLE_CLIENT_STATE_DEBUGGING.getName(), MojarraContextParam.GENERATE_UNIQUE_SERVER_STATE_IDS.getName()),
                revertedParameterNames().stream().sorted().toList(),
                "silently dropping a setting which weakens the deployment would be worse than honoring it");
    }

    /**
     * The deprecated parameter it replaces hands its value over, so an application which has not migrated keeps its
     * setting rather than silently losing it.
     */
    @Test
    void disableClientStateEncryptionStillEnablesClientStateDebugging() {
        assertTrue(configureDeprecated(ProjectStage.Development).isEnabled(MojarraContextParam.ENABLE_CLIENT_STATE_DEBUGGING));
    }

    /**
     * And it is gated exactly like the parameter it was carried into. The value arrives under a name the application
     * never declared, so a gate which asks whether the replacement was set would let this one straight through and drop
     * the ByteArrayGuard in a deployed application.
     */
    @ParameterizedTest
    @EnumSource(value = ProjectStage.class, names = "Development", mode = Mode.EXCLUDE)
    void disableClientStateEncryptionIsGatedLikeItsReplacement(ProjectStage projectStage) {
        assertFalse(configureDeprecated(projectStage).isEnabled(MojarraContextParam.ENABLE_CLIENT_STATE_DEBUGGING));
    }

    private static WebConfiguration configureDeprecated(ProjectStage projectStage) {
        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter(PROJECT_STAGE_PARAM_NAME, projectStage.name());
        servletContext.addInitParameter("org.glassfish.mojarra.disableClientStateEncryption", "true");

        return gate(servletContext);
    }

    private static WebConfiguration configure(ProjectStage projectStage) {
        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter(PROJECT_STAGE_PARAM_NAME, projectStage.name());
        servletContext.addInitParameter(MojarraContextParam.ENABLE_CLIENT_STATE_DEBUGGING.getName(), "true");
        servletContext.addInitParameter(MojarraContextParam.GENERATE_UNIQUE_SERVER_STATE_IDS.getName(), "false");

        return gate(servletContext);
    }

    private static WebConfiguration gate(MockServletContext servletContext) {
        WebConfiguration webConfiguration = WebConfiguration.getInstance(servletContext);
        webConfiguration.processDevelopmentOnlyParameters();

        return webConfiguration;
    }

    private List<String> revertedParameterNames() {
        return records.stream()
                .filter(record -> record.getLevel() == WARNING && DEVELOPMENT_ONLY.equals(record.getMessage()))
                .map(record -> String.valueOf(record.getParameters()[1]))
                .toList();
    }
}
