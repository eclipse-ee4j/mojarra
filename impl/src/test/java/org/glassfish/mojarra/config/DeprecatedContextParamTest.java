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

import static java.util.logging.Level.WARNING;
import static org.glassfish.mojarra.config.WebConfiguration.BooleanWebContextInitParameter.AllowTextChildren;
import static org.glassfish.mojarra.config.WebConfiguration.BooleanWebContextInitParameter.CompressViewState;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glassfish.mojarra.mock.MockServletContext;
import org.glassfish.mojarra.util.FacesLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Covers the deprecation support of {@link WebConfiguration} for a parameter which has no replacement.
 */
class DeprecatedContextParamTest {

    private static final String FOR_REMOVAL = "faces.config.webconfig.param.deprecated.for_removal";

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

    /**
     * The warning is not gated on the project stage, because it announces a change in the runtime rather than a mistake
     * in the application.
     */
    @Test
    void aDeprecatedParameterWarnsWhenSet() {
        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter(AllowTextChildren.getQualifiedName(), "true");

        WebConfiguration webConfiguration = WebConfiguration.getInstance(servletContext);

        assertTrue(webConfiguration.isOptionEnabled(AllowTextChildren), "the value is still honored");
        assertEquals(List.of(AllowTextChildren.getQualifiedName()), warnedParameterNames());
    }

    /**
     * Warning about a deprecation which the application did not opt into would be noise.
     */
    @Test
    void aDeprecatedParameterIsSilentWhenUnset() {
        WebConfiguration.getInstance(new MockServletContext());

        assertEquals(List.of(), warnedParameterNames());
    }

    /**
     * And a parameter which is merely set is not a deprecation.
     */
    @Test
    void aSupportedParameterIsSilentWhenSet() {
        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter(CompressViewState.getQualifiedName(), "false");

        WebConfiguration.getInstance(servletContext);

        assertEquals(List.of(), warnedParameterNames());
    }

    private List<String> warnedParameterNames() {
        return records.stream()
                .filter(record -> record.getLevel() == WARNING && FOR_REMOVAL.equals(record.getMessage()))
                .map(record -> String.valueOf(record.getParameters()[1]))
                .toList();
    }
}
