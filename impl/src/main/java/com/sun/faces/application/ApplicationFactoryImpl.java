/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.application;

import static com.sun.faces.util.Util.generateCreatedBy;
import static com.sun.faces.util.Util.notNull;
import static java.text.MessageFormat.format;
import static java.util.logging.Level.FINE;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;

import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationFactory;
import jakarta.faces.context.FacesContext;

/**
 * Default implementation of {@link ApplicationFactory}.
 */
public class ApplicationFactoryImpl extends ApplicationFactory {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    // Attribute Instance Variables

    private final Map<String, Application> applicationHolder = new ConcurrentHashMap<>(1);

    private final String createdBy;

    // Constructors and Initializers

    public ApplicationFactoryImpl() {
        super(null);
        createdBy = generateCreatedBy(FacesContext.getCurrentInstance());
        LOGGER.log(FINE, "Created ApplicationFactory ");
    }

    /**
     * <p>
     * Create (if needed) and return an {@link Application} instance for this web application.
     * </p>
     */
    @Override
    public Application getApplication() {
        return applicationHolder.computeIfAbsent("default", e -> {
            Application applicationImpl = new ApplicationImpl();
            InjectionApplicationFactory.setApplicationInstance(applicationImpl);
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.fine(format("Created Application instance ''{0}''", applicationHolder));
            }

            return applicationImpl;
        });
    }

    /**
     * <p>
     * Replace the {@link Application} instance that will be returned for this web application.
     * </p>
     *
     * @param application The replacement {@link Application} instance
     */
    @Override
    public void setApplication(Application application) {

        notNull("application", application);

        applicationHolder.put("default", application);

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.fine(format("set Application Instance to ''{0}''", application.getClass().getName()));
        }
    }

    @Override
    public String toString() {
        return super.toString() + " created by " + createdBy;
    }
}
