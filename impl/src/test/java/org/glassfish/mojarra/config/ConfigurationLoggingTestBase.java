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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glassfish.mojarra.util.FacesLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * <p>
 * Collects what {@link WebConfiguration} logs while it reads a configuration, for the tests whose subject is what a
 * deployment is told rather than what it resolves to.
 * </p>
 *
 * <p>
 * The records are kept unformatted, so an assertion reads the arguments of a message rather than the sentence a
 * resource bundle happens to make of them, and stays readable in every locale.
 * </p>
 */
abstract class ConfigurationLoggingTestBase {

    private final Logger logger = FacesLogger.CONFIG.getLogger();
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

    protected final List<LogRecord> records = new ArrayList<>();

    @BeforeEach
    void captureLogging() {
        logger.addHandler(handler);
    }

    @AfterEach
    void stopCapturingLogging() {
        logger.removeHandler(handler);
    }

    /**
     * @param messageKey the key of the message of interest, which identifies it well enough on its own, because each
     * one is logged at a level of its own choosing and from a single place.
     * @param index the position of the argument to read, counted as the message declares its arguments.
     * @return that argument of every message logged under the key, in the order they were logged.
     */
    protected List<String> loggedArguments(String messageKey, int index) {
        return records.stream()
                .filter(record -> messageKey.equals(record.getMessage()))
                .map(record -> String.valueOf(record.getParameters()[index]))
                .toList();
    }
}
