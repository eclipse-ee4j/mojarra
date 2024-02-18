/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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
package com.sun.faces.cdi.clientwindow;

import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.logging.Logger;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Typed;
import jakarta.faces.context.FacesContext;
import jakarta.faces.lifecycle.ClientWindowScoped;

/**
 * The CDI context for CDI ClientWindowScoped beans.
 */
@Typed()
public class ClientWindowScopeContext implements Context, Serializable
{

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ClientWindowScopeContext.class.getName());

    /**
     * Stores the serial version UID.
     */
    private static final long serialVersionUID = -6559364724281899428L;

    /**
     * Constructor.
     */
    public ClientWindowScopeContext() {
        LOGGER.log(FINEST, "Creating ClientWindowScope CDI context");
    }

    /**
     * Assert the context is active, otherwise throw ContextNotActiveException.
     */
    private void assertNotReleased() {
        if (!isActive()) {
            LOGGER.log(SEVERE, "Trying to access ClientWindowScope CDI context while it is not active");
            throw new ContextNotActiveException();
        }
    }

    /**
     * Get the ClientWindowScoped bean for the given contextual.
     *
     * @param <T> the type.
     * @param contextual the contextual.
     * @return the client window scoped bean, or null if not found.
     */
    @Override
    public <T> T get(Contextual<T> contextual) {
        assertNotReleased();

        T result = null;

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            ClientWindowScopeManager manager = ClientWindowScopeManager.getInstance(facesContext);
            if (manager != null) {
                result = manager.getContextManager().getBean(facesContext, contextual);
            }
        }

        return result;
    }

    /**
     * Get the existing instance of the ClientWindowScoped bean for the given contextual or create a new one.
     *
     * @param <T> the type.
     * @param contextual the contextual.
     * @param creational the creational.
     * @return the instance.
     * @throws ContextNotActiveException when the context is not active.
     */
    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creational) {
        assertNotReleased();

        T result = get(contextual);

        if (result == null) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                ClientWindowScopeManager manager = ClientWindowScopeManager.getInstance(facesContext);
                result = manager.getContextManager().getBean(facesContext, contextual);
                if (result == null) {
                    result = manager.getContextManager().createBean(facesContext, contextual, creational);
                }
            }
        }

        return result;
    }

    /**
     * Get the class of the scope object.
     *
     * @return the class.
     */
    @Override
    public Class<? extends Annotation> getScope() {
        return ClientWindowScoped.class;
    }

    @Override
    public boolean isActive() {
        boolean result = false;

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            result = (facesContext.getExternalContext().getClientWindow() != null);
        }

        return result;
    }

}
