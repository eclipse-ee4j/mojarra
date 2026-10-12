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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;

/**
 * A web application may register {@link ConfigureListener} more than once, e.g. in <code>web.xml</code> in addition
 * to the registration by {@link FacesInitializer}. The container may pass each instance a different
 * {@link ServletContext} object for the same application. Only the instance which initialized the application may
 * initialize and destroy it.
 */
class ConfigureListenerTest {

    private ServletContext servletContext;
    private ConfigManager configManager;
    private ServletContextEvent event;

    @BeforeEach
    void initializedApplication() {
        servletContext = mock(ServletContext.class);
        configManager = mock(ConfigManager.class);
        when(servletContext.getAttribute(endsWith("CONFIG_MANAGER_KEY"))).thenReturn(configManager);
        when(configManager.hasBeenInitialized(servletContext)).thenReturn(true);
        event = new ServletContextEvent(servletContext);
    }

    @Test
    void duplicateListenerDoesNotInitializeApplication() {
        new ConfigureListener().contextInitialized(event);

        verify(configManager, never()).initialize(any(), any());
        verify(servletContext, never()).setAttribute(anyString(), any());
    }

    @Test
    void duplicateListenerDoesNotDestroyApplication() {
        ConfigureListener duplicate = new ConfigureListener();
        duplicate.contextInitialized(event);
        duplicate.contextDestroyed(event);

        verify(configManager, never()).destroy(any(), any());
        verify(servletContext, never()).removeAttribute(anyString());
    }

}
