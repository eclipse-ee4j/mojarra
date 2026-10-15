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

package org.glassfish.mojarra.config.processor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import jakarta.faces.FactoryFinder;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

import org.glassfish.mojarra.config.ConfigurationException;
import org.glassfish.mojarra.config.manager.documents.DocumentInfo;
import org.junit.jupiter.api.Test;

class FactoryConfigProcessorTest {

    /**
     * A factory which cannot be found when the factories are validated fails the startup with a ConfigurationException which names the factory and carries the
     * cause.
     */
    @Test
    void missingFactoryFailsStartup() {
        FactoryFinder.releaseFactories();
        FactoryConfigProcessor processor = new FactoryConfigProcessor(true);

        ConfigurationException exception = assertThrows(
            ConfigurationException.class,
            () -> processor.process(mock(ServletContext.class), mock(FacesContext.class), new DocumentInfo[0])
        );

        assertTrue(exception.getMessage().startsWith("Factory '"), exception.getMessage());
        assertNotNull(exception.getCause());
    }

}
