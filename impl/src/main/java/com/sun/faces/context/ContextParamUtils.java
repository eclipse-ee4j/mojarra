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

import jakarta.servlet.ServletContext;

/**
 * A utility class for dealing with context-param entries.
 */
public class ContextParamUtils {

    /**
     * Constructor.
     */
    private ContextParamUtils() {
        // nothing to do here.
    }

    /**
     * Get the value.
     *
     * @param servletContext the servlet context.
     * @param contextParam the context-param.
     * @return the value.
     */
    public static Object getValue(ServletContext servletContext, ContextParam contextParam) {
        Object result = contextParam.getDefaultValue();
        if (servletContext.getInitParameter(contextParam.getName()) != null) {
            if (contextParam.getType().equals(Boolean.class)) {
                result = Boolean.valueOf(servletContext.getInitParameter(contextParam.getName()));
            } else if (contextParam.getType().equals(Integer.class)) {
                result = Integer.valueOf(servletContext.getInitParameter(contextParam.getName()));
            }
        }
        return result;
    }

    /**
     * Get the value.
     *
     * @param <T> the type.
     * @param servletContext the servlet context.
     * @param contextParam the context-param.
     * @param clazz the class.
     * @return the value.
     */
    public static <T extends Object> T getValue(ServletContext servletContext, ContextParam contextParam, Class<T> clazz) {
        return clazz.cast(getValue(servletContext, contextParam));
    }
}
