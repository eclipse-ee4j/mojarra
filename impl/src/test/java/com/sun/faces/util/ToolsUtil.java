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

package com.sun.faces.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Various static utility methods.
 */
public class ToolsUtil {

    public static final String FACES_LOGGER = "jakarta.enterprise.resource.webcontainer.faces";

    public static final String CONFIG_LOGGER = ".config";

    public static final String BEANS_LOGGER = ".config.beans";

    public static final String RULES_LOGGER = ".config.rules";

    public static final String GENERATE_LOGGER = ".config.generate";

    public static final String FACES_LOG_STRINGS
            = "com.sun.faces.LogStrings";

    public static final String TOOLS_LOG_STRINGS
            = "com.sun.faces.ToolsLogStrings";

    private static final String RESOURCE_BUNDLE_BASE_NAME
            = "com.sun.faces.resources.JsfToolsMessages";

    // --------------------------------------------------- Message Key Constants
    public static final String MANAGED_BEAN_NO_MANAGED_BEAN_NAME_ID
            = "com.sun.faces.MANAGED_BEAN_NO_MANAGED_BEAN_NAME";

    public static final String MANAGED_BEAN_NO_MANAGED_BEAN_CLASS_ID
            = "com.sun.faces.MANAGED_BEAN_NO_MANAGED_BEAN_CLASS";

    public static final String MANAGED_BEAN_NO_MANAGED_BEAN_SCOPE_ID
            = "com.sun.faces.MANAGED_BEAN_NO_MANAGED_BEAN_SCOPE";

    public static final String MANAGED_BEAN_INVALID_SCOPE_ID
            = "com.sun.faces.MANAGED_BEAN_INVALID_SCOPE";

    public static final String MANAGED_BEAN_AS_LIST_CONFIG_ERROR_ID
            = "com.sun.faces.MANAGED_BEAN_AS_LIST_CONFIG_ERROR";

    public static final String MANAGED_BEAN_AS_MAP_CONFIG_ERROR_ID
            = "com.sun.faces.MANAGED_BEAN_AS_MAP_CONFIG_ERROR";

    public static final String MANAGED_BEAN_LIST_PROPERTY_CONFIG_ERROR_ID
            = "com.sun.faces.MANAGED_BEAN_LIST_PROPERTY_CONFIG_ERROR";

    public static final String MANAGED_BEAN_MAP_PROPERTY_CONFIG_ERROR_ID
            = "com.sun.faces.MANAGED_BEAN_MAP_PROPERTY_CONFIG_ERROR";

    public static final String MANAGED_BEAN_PROPERTY_CONFIG_ERROR_ID
            = "com.sun.faces.MANAGED_BEAN_PROPERTY_CONFIG_ERROR";

    public static final String MANAGED_BEAN_NO_MANAGED_PROPERTY_NAME_ID
            = "com.sun.faces.MANAGED_BEAN_NO_MANAGED_PROPERTY_NAME";

    // ---------------------------------------------------------- Public Methods
    public static String getMessage(String messageKey, Object[] params) {

        ResourceBundle bundle
                = ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME,
                        Locale.getDefault(),
                        Thread.currentThread().getContextClassLoader());
        return MessageFormat.format(bundle.getString(messageKey), params);

    } // END getMessage

    public static String getMessage(String messageKey) {

        return getMessage(messageKey, null);

    } // END getMessage

    public static Logger getLogger(String loggerName) {
        return Logger.getLogger(loggerName, FACES_LOG_STRINGS);
    }

    public static Class<?> loadClass(String name,
            Object fallbackClass)
            throws ClassNotFoundException {
        ClassLoader loader = ToolsUtil.getCurrentLoader(fallbackClass);
        return Class.forName(name, false, loader);
    }

    public static ClassLoader getCurrentLoader(Object fallbackClass) {
        ClassLoader loader
                = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = fallbackClass.getClass().getClassLoader();
        }
        return loader;
    }
}
