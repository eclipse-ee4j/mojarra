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

package com.sun.faces.context;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.servlet.ServletRequest;

/**
 * This {@link FacesContextFactory} is responsible for injecting the default {@link FacesContext} instance into the
 * top-level {@link FacesContext} as configured by the runtime. Doing this allows us to preserve backwards compatibility
 * as the API evolves without having the API rely on implementation specific details.
 */
public class InjectionFacesContextFactory extends FacesContextFactory {

    private static final Logger LOGGER = FacesLogger.CONTEXT.getLogger();
    private Field defaultFacesContext;
    private Field defaultExternalContext;

    // ------------------------------------------------------------ Constructors

    public InjectionFacesContextFactory(FacesContextFactory delegate) {
        super(delegate);

        Util.notNull("facesContextFactory", delegate);

        try {
            defaultFacesContext = FacesContext.class.getDeclaredField("defaultFacesContext");
            defaultFacesContext.setAccessible(true);
        } catch (NoSuchFieldException nsfe) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Unable to find private field named 'defaultFacesContext' in jakarta.faces.context.FacesContext.");
            }
        } catch (SecurityException e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, e.toString(), e);
            }
            defaultFacesContext = null;
        }
        try {
            defaultExternalContext = ExternalContext.class.getDeclaredField("defaultExternalContext");
            defaultExternalContext.setAccessible(true);
        } catch (NoSuchFieldException nsfe) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Unable to find private field named 'defaultExternalContext' in jakarta.faces.context.ExternalContext.");
            }
        } catch (SecurityException e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, e.toString(), e);
            }
            defaultExternalContext = null;
        }

    }

    // ---------------------------------------- Methods from FacesContextFactory

    @Override
    public FacesContext getFacesContext(Object context, Object request, Object response, Lifecycle lifecycle) throws FacesException {

        FacesContext ctx = getWrapped().getFacesContext(context, request, response, lifecycle);
        if (ctx == null) {
            // No i18n here
            String message = MessageFormat.format("Delegate FacesContextFactory, {0}, returned null when calling getFacesContext().",
                    getWrapped().getClass().getName());
            throw new IllegalStateException(message);
        }
        injectDefaults(ctx, request);
        return ctx;

    }

    // --------------------------------------------------------- Private Methods

    private void injectDefaults(FacesContext target, Object request) {

        if (defaultFacesContext != null) {
            FacesContext defaultFC = FacesContextImpl.getDefaultFacesContext();
            if (defaultFC != null) {
                try {
                    defaultFacesContext.set(target, defaultFC);
                } catch (IllegalAccessException e) {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE, e.toString(), e);
                    }
                }
            }
        }
        if (defaultExternalContext != null) {
            ExternalContext defaultExtContext = null;
            if (request instanceof ServletRequest) {
                ServletRequest reqObj = (ServletRequest) request;
                defaultExtContext = (ExternalContext) reqObj.getAttribute(ExternalContextFactoryImpl.DEFAULT_EXTERNAL_CONTEXT_KEY);
                if (defaultExtContext != null) {
                    reqObj.removeAttribute(ExternalContextFactoryImpl.DEFAULT_EXTERNAL_CONTEXT_KEY);
                }
            }
            if (defaultExtContext != null) {
                try {
                    defaultExternalContext.set(target.getExternalContext(), defaultExtContext);
                } catch (IllegalAccessException e) {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE, e.toString(), e);
                    }
                }
            }
        }

    }

}
