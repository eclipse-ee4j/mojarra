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

package com.sun.faces.application.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.FacesContext;

/**
 * An abstract RuntimeAnnotationHandler that is capable of dealing with JNDI.
 */
public abstract class JndiHandler implements RuntimeAnnotationHandler {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JndiHandler.class.getName());
    /**
     * Stores the java:comp/env/ prefix
     */
    protected static final String JAVA_COMP_ENV = "java:comp/env/";

    /**
     * Look up the given object using its JNDI name.
     *
     * @param facesContext the Faces context.
     * @param name the JNDI name.
     * @return the object, or null if an error occurs.
     */
    public Object lookup(FacesContext facesContext, String name) {
        Object object = null;
        try {
            InitialContext context = new InitialContext();
            object = context.lookup(name);
        } catch (NamingException ne) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "Unable to lookup: " + name, ne);
            }
            if (facesContext.isProjectStage(ProjectStage.Development)) {
                facesContext.addMessage(null, new FacesMessage("Unable to lookup: " + name, "Unable to lookup: " + name));
            }
        }
        return object;
    }

    /**
     * Set the field.
     *
     * @param facesContext the Faces context.
     * @param field the field.
     * @param instance the instance.
     * @param value the value.
     */
    public void setField(FacesContext facesContext, Field field, Object instance, Object value) {
        synchronized (instance) {
            try {
                boolean fieldAccessible = true;
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                    fieldAccessible = false;
                }
                field.set(instance, value);
                if (!fieldAccessible) {
                    field.setAccessible(false);
                }
            } catch (IllegalArgumentException | IllegalAccessException iae) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING, "Unable to set field: " + field.getName(), iae);
                }
                if (facesContext.isProjectStage(ProjectStage.Development)) {
                    facesContext.addMessage(null, new FacesMessage("Unable to set field: " + field.getName(), "Unable to set field: " + field.getName()));
                }
            }
        }
    }

    /**
     * Invoke the method.
     *
     * @param facesContext the Faces context.
     * @param method the method.
     * @param instance the instance.
     * @param value the value.
     */
    protected void invokeMethod(FacesContext facesContext, Method method, Object instance, Object value) {
        synchronized (instance) {
            try {
                boolean accessible = method.isAccessible();
                method.setAccessible(false);
                method.invoke(instance, value);
                method.setAccessible(accessible);
            } catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException ite) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING, "Unable to call method: " + method.getName(), ite);
                }
                if (facesContext.isProjectStage(ProjectStage.Development)) {
                    facesContext.addMessage(null, new FacesMessage("Unable to call method: " + method.getName(), "Unable to call method: " + method.getName()));
                }
            }
        }
    }
}
