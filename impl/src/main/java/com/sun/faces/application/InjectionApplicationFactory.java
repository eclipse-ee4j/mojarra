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

import static com.sun.faces.util.Util.notNull;
import static java.text.MessageFormat.format;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;

import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationFactory;
import jakarta.faces.context.FacesContext;

/**
 * This {@link jakarta.faces.application.ApplicationFactory} is responsible for injecting the default
 * {@link Application} instance into the top-level {@link Application} as configured by the runtime. Doing this allows
 * us to preserve backwards compatibility as the API evolves without having the API rely on implementation specific
 * details.
 */
public class InjectionApplicationFactory extends ApplicationFactory {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    private Application defaultApplication;
    private Field defaultApplicationField;
    private volatile Application application;

    // ------------------------------------------------------------ Constructors

    public InjectionApplicationFactory(ApplicationFactory delegate) {
        super(delegate);
        notNull("applicationFactory", delegate);
    }

    // ----------------------------------------- Methods from ApplicationFactory

    @Override
    public Application getApplication() {

        if (application == null) {
            application = getWrapped().getApplication();

            if (application == null) {
                throw new IllegalStateException(
                        format("Delegate ApplicationContextFactory, {0}, returned null when calling getApplication().", getWrapped().getClass().getName()));
            }

            injectDefaultApplication();
        }

        return application;
    }

    @Override
    public synchronized void setApplication(Application application) {
        this.application = application;
        getWrapped().setApplication(application);
        injectDefaultApplication();
    }

    // --------------------------------------------------------- Private Methods

    private void injectDefaultApplication() {

        if (defaultApplication == null) {
            FacesContext ctx = FacesContext.getCurrentInstance();
            defaultApplication = InjectionApplicationFactory.removeApplicationInstance(ctx.getExternalContext().getApplicationMap());
        }

        if (defaultApplication != null) {
            try {
                if (defaultApplicationField == null) {
                    defaultApplicationField = Application.class.getDeclaredField("defaultApplication");
                    defaultApplicationField.setAccessible(true);
                }
                defaultApplicationField.set(application, defaultApplication);

            } catch (NoSuchFieldException nsfe) {
                if (LOGGER.isLoggable(FINE)) {
                    LOGGER.log(FINE, "Unable to find private field named 'defaultApplication' in jakarta.faces.application.Application.");
                }
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                if (LOGGER.isLoggable(SEVERE)) {
                    LOGGER.log(SEVERE, e.toString(), e);
                }
            }
        }
    }

    // ------------------------------------------------- Package private Methods

    static void setApplicationInstance(Application app) {
        FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().put(InjectionApplicationFactory.class.getName(), app);
    }

    static Application removeApplicationInstance(Map<String, Object> appMap) {
        return (Application) appMap.remove(InjectionApplicationFactory.class.getName());
    }

}
