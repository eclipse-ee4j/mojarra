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

package com.sun.faces.application.view;

import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.logging.Logger;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;

/**
 * The CDI context for CDI ViewScoped beans.
 */
public class ViewScopeContext implements Context, Serializable {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ViewScopeContext.class.getName());

    /**
     * Stores the serial version UID.
     */
    private static final long serialVersionUID = -6245899073989073951L;

    /**
     * Constructor.
     */
    public ViewScopeContext() {
        LOGGER.log(FINEST, "Creating ViewScope CDI context");
    }

    /**
     * Returns the current FacesContext, asserting the context is active (its view root is present) and otherwise
     * throwing {@link ContextNotActiveException}. Resolving it once per {@code get} avoids the repeated thread-local
     * lookups that a separate {@code assertNotReleased()} plus {@link #isActive()} would each incur.
     *
     * @return the active FacesContext.
     */
    private static FacesContext getActiveFacesContext() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null || facesContext.getViewRoot() == null) {
            LOGGER.log(SEVERE, "Trying to access ViewScope CDI context while it is not active");
            throw new ContextNotActiveException();
        }
        return facesContext;
    }

    /**
     * Get the ViewScoped bean for the given contextual.
     *
     * @param <T> the type.
     * @param contextual the contextual.
     * @return the view scoped bean, or null if not found.
     */
    @Override
    public <T> T get(Contextual<T> contextual) {
        FacesContext facesContext = getActiveFacesContext();
        ViewScopeManager manager = ViewScopeManager.getInstance(facesContext);

        if (manager == null) {
            return null;
        }

        ViewScopeManager.acquireViewMap(facesContext);
        return manager.getContextManager().getBean(facesContext, contextual);
    }

    /**
     * Get the existing instance of the ViewScoped bean for the given contextual or create a new one.
     *
     * @param <T> the type.
     * @param contextual the contextual.
     * @param creational the creational.
     * @return the instance.
     * @throws ContextNotActiveException when the context is not active.
     */
    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creational) {
        FacesContext facesContext = getActiveFacesContext();
        ViewScopeManager manager = ViewScopeManager.getInstance(facesContext);
        if (manager == null) {
            return null;
        }

        ViewScopeManager.acquireViewMap(facesContext);

        T result = manager.getContextManager().getBean(facesContext, contextual);
        if (result == null) {
            result = manager.getContextManager().createBean(facesContext, contextual, creational);
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
        return ViewScoped.class;
    }

    /**
     * Determine if the context is active.
     *
     * @return true if there is a view root, false otherwise.
     */
    @Override
    public boolean isActive() {
        boolean result = false;

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            UIViewRoot viewRoot = facesContext.getViewRoot();
            if (viewRoot != null) {
                result = true;
            }
        }

        return result;
    }
}
