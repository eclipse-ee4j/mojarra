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

/**
 * The enumeration of all our context-param entries.
 */
public enum ContextParam {

    /**
     * Enable distributable code.
     */
    EnableDistributable("com.sun.faces.enableDistributable", Boolean.class, false),
    /**
     * Send the "X-Powered-By" header.
     */
    SendPoweredByHeader("com.sun.faces.sendPoweredByHeader", Boolean.class, false);

    /**
     * Stores the default value.
     */
    private final Object defaultValue;

    /**
     * Stores the name.
     */
    private final String name;

    /**
     * Stores the type.
     */
    private final Class<?> type;

    /**
     * Constructor.
     */
    ContextParam(String name, Class type, Object defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    /**
     * Get the default value.
     *
     * @return the default value.
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Get the default value.
     *
     * @param <T> the type.
     * @param clazz the class.
     * @return the default value.
     */
    public <T extends Object> T getDefaultValue(Class<T> clazz) {
        return clazz.cast(defaultValue);
    }

    /**
     * Get the name.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the type.
     *
     * @return the type.
     */
    public Class getType() {
        return type;
    }
}
