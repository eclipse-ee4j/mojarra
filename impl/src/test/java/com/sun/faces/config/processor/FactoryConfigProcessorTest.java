/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.faces.config.processor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.Test;

import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.junit.LogRecords;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.FactoryFinder;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

class FactoryConfigProcessorTest {

    /**
     * A factory which cannot be found when the factories are validated is logged as SEVERE on the config logger, with the
     * exception and the name of the factory.
     */
    @Test
    void missingFactoryIsLoggedAsSevere() {
        FactoryFinder.releaseFactories();
        FactoryConfigProcessor processor = new FactoryConfigProcessor(true);

        List<LogRecord> records = LogRecords.capture(FacesLogger.CONFIG.getLogger(), Level.SEVERE, () -> {
            try {
                processor.process(mock(ServletContext.class), mock(FacesContext.class), new DocumentInfo[0]);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        assertFalse(records.isEmpty(), "SEVERE records for the missing factories");
        for (LogRecord logRecord : records) {
            assertTrue(logRecord.getMessage().startsWith("Factory '"), logRecord.getMessage());
            assertNotNull(logRecord.getThrown(), logRecord.getMessage());
        }
    }

}
