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

package com.sun.faces.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.Test;

import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.config.processor.ConfigProcessor;
import com.sun.faces.junit.LogRecords;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

class ConfigManagerTest {

    /**
     * A config processor which fails is logged as SEVERE on the config logger, with the exception.
     */
    @Test
    void failingConfigProcessorIsLoggedAsSevere() throws Exception {
        ConfigProcessor configProcessor = mock(ConfigProcessor.class);
        Exception failure = new IllegalArgumentException("broken faces-config");
        doThrow(failure).when(configProcessor).process(any(), any(), any());

        List<LogRecord> records = LogRecords.capture(FacesLogger.CONFIG.getLogger(), Level.SEVERE,
                () -> ConfigManager.process(configProcessor, mock(ServletContext.class), mock(FacesContext.class), new DocumentInfo[0]));

        assertEquals(1, records.size(), "one SEVERE record per failing config processor");
        assertSame(failure, records.get(0).getThrown());
    }

}
