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
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

/**
 * An <code>enum</code> of all application <code>Logger</code>s.
 */
public enum FacesLogger {

    APPLICATION("application"), APPLICATION_VIEW("application.view"), RESOURCE("resource"), CONFIG("config"), CONTEXT("context"),
    FACELETS_COMPILER("facelets.compiler"), FACELETS_COMPONENT("facelets.tag.component"), FACELETS_EL("facelets.el"), FACELETS_META("facelets.tag.meta"),
    FACELETS_COMPOSITION("facelets.tag.ui.composition"), FACELETS_DECORATE("facelets.tag.ui.decorate"), FACELETS_INCLUDE("facelets.tag.ui.include"),
    FACELETS_FACELET("faclets.facelet"), FACELETS_FACTORY("facelets.factory"), FLOW("flow"), LIFECYCLE("lifecycle"), MANAGEDBEAN("managedbean"),
    RENDERKIT("renderkit"), TAGLIB("taglib"), TIMING("timing"), UTIL("util"), FLASH("flash"), CLIENTWINDOW("clientwindow");

    private static final String LOGGER_RESOURCES = "com.sun.faces.LogStrings";
    public static final String FACES_LOGGER_NAME_PREFIX = "jakarta.enterprise.resource.webcontainer.faces.";
    private String loggerName;

    FacesLogger(String loggerName) {
        this.loggerName = FACES_LOGGER_NAME_PREFIX + loggerName;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public String getResourcesName() {
        return LOGGER_RESOURCES;
    }

    public Logger getLogger() {
        return Logger.getLogger(loggerName, LOGGER_RESOURCES);
    }

    public String interpolateMessage(FacesContext context, String messageId, Object[] params) {
        String result = null;
        ResourceBundle rb = null;
        UIViewRoot root = context.getViewRoot();
        Locale curLocale;
        ClassLoader loader = Util.getCurrentLoader(this);
        if (null == root) {
            curLocale = Locale.getDefault();
        } else {
            curLocale = root.getLocale();
        }
        try {
            rb = ResourceBundle.getBundle(getResourcesName(), curLocale, loader);
            String message = rb.getString(messageId);
            if (params != null) {
                result = MessageFormat.format(message, params);
            } else {
                result = message;
            }
        } catch (MissingResourceException mre) {
            result = messageId;
        }

        return result;
    }

}
