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

package com.sun.faces.application.applicationimpl;

import static com.sun.faces.util.MessageUtils.ILLEGAL_ATTEMPT_SETTING_APPLICATION_ARTIFACT_ID;
import static com.sun.faces.util.MessageUtils.getExceptionMessageString;
import static com.sun.faces.util.Util.coalesce;
import static com.sun.faces.util.Util.notNull;
import static java.util.logging.Level.FINE;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.application.NavigationHandler;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.application.StateManager;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionListener;
import jakarta.faces.flow.FlowHandler;

public class Singletons {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();
    private static final String CONTEXT = "context";

    private final ApplicationAssociate associate;

    private volatile ActionListener actionListener;
    private volatile NavigationHandler navigationHandler;
    private volatile ViewHandler viewHandler;
    private volatile ResourceHandler resourceHandler;
    private volatile StateManager stateManager;

    private volatile ArrayList<Locale> supportedLocales;
    private volatile Locale defaultLocale;
    private volatile String messageBundle;

    private String defaultRenderKitId;

    public Singletons(ApplicationAssociate applicationAssociate) {
        associate = applicationAssociate;
    }

    /*
     * @see jakarta.faces.application.Application#getViewHandler()
     */
    public ViewHandler getViewHandler() {
        return viewHandler;
    }

    /*
     * @see jakarta.faces.application.Application#setViewHandler(jakarta.faces.application.ViewHandler)
     */
    public synchronized void setViewHandler(ViewHandler viewHandler) {

        notNull("viewHandler", viewHandler);
        notRequestServiced("ViewHandler");

        this.viewHandler = viewHandler;

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE, MessageFormat.format("set ViewHandler Instance to ''{0}''", viewHandler.getClass().getName()));
        }
    }

    /*
     * @see jakarta.faces.application.Application#getResourceHandler()
     */
    public ResourceHandler getResourceHandler() {
        return resourceHandler;
    }

    /*
     * @see jakarta.faces.application.Application#setResourceHandler(jakarta.faces.application.ResourceHandler)
     */
    public synchronized void setResourceHandler(ResourceHandler resourceHandler) {

        notNull("resourceHandler", resourceHandler);
        notRequestServiced("ResourceHandler");

        this.resourceHandler = resourceHandler;

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE, "set ResourceHandler Instance to ''{0}''", resourceHandler.getClass().getName());
        }
    }

    /*
     * @see jakarta.faces.application.Application#getStateManager()
     */
    public StateManager getStateManager() {
        return stateManager;
    }

    /*
     * @see jakarta.faces.application.Application#setStateManager(jakarta.faces.application.StateManager)
     */
    public synchronized void setStateManager(StateManager stateManager) {

        notNull("stateManager", stateManager);
        notRequestServiced("StateManager");

        this.stateManager = stateManager;

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE, MessageFormat.format("set StateManager Instance to ''{0}''", stateManager.getClass().getName()));
        }
    }

    /*
     * @see jakarta.faces.application.Application#getActionListener()
     */
    public ActionListener getActionListener() {
        return actionListener;
    }

    /*
     * @see Application#setActionListener(jakarta.faces.event.ActionListener)
     */
    public synchronized void setActionListener(ActionListener actionListener) {

        notNull("actionListener", actionListener);

        this.actionListener = actionListener;

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.fine(MessageFormat.format("set ActionListener Instance to ''{0}''", actionListener.getClass().getName()));
        }
    }

    /*
     * @see jakarta.faces.application.Application#getNavigationHandler()
     */
    public NavigationHandler getNavigationHandler() {
        return navigationHandler;
    }

    /*
     * @see jakarta.faces.application.Application#setNavigationHandler(jakarta.faces.application.NavigationHandler)
     */
    public synchronized void setNavigationHandler(NavigationHandler navigationHandler) {

        notNull("navigationHandler", navigationHandler);

        this.navigationHandler = navigationHandler;

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.fine(MessageFormat.format("set NavigationHandler Instance to ''{0}''", navigationHandler.getClass().getName()));
        }
    }

    public FlowHandler getFlowHandler() {
        return associate.getFlowHandler();
    }

    public synchronized void setFlowHandler(FlowHandler flowHandler) {

        notNull("flowHandler", flowHandler);

        associate.setFlowHandler(flowHandler);

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.fine(MessageFormat.format("set FlowHandler Instance to ''{0}''", flowHandler.getClass().getName()));
        }
    }

    /*
     * @see jakarta.faces.application.Application#getSupportedLocales()
     */
    public Iterator<Locale> getSupportedLocales() {
        return coalesce(supportedLocales, Collections.<Locale>emptyList()).iterator();
    }

    /*
     * @see jakarta.faces.application.Application#setSupportedLocales(java.util.Collection)
     */
    public synchronized void setSupportedLocales(Collection<Locale> newLocales) {

        notNull("newLocales", newLocales);

        supportedLocales = new ArrayList<>(newLocales);

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE, MessageFormat.format("set Supported Locales ''{0}''", supportedLocales.toString()));
        }

    }

    /*
     * @see jakarta.faces.application.Application#getDefaultLocale()
     */
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    /*
     * @see jakarta.faces.application.Application#setDefaultLocale(java.util.Locale)
     */
    public synchronized void setDefaultLocale(Locale locale) {

        notNull("locale", locale);

        defaultLocale = locale;

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE, MessageFormat.format("set defaultLocale ''{0}''", defaultLocale.getClass().getName()));
        }
    }

    /*
     * @see jakarta.faces.application.Application#setMessageBundle(String)
     */
    public synchronized void setMessageBundle(String messageBundle) {
        notNull("messageBundle", messageBundle);

        this.messageBundle = messageBundle;

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE, MessageFormat.format("set messageBundle ''{0}''", messageBundle));
        }
    }

    /*
     * @see jakarta.faces.application.Application#getMessageBundle()
     */
    public String getMessageBundle() {
        return messageBundle;
    }

    /*
     * @see jakarta.faces.application.Application#getDefaultRenderKitId()
     */
    public String getDefaultRenderKitId() {
        return defaultRenderKitId;
    }

    /*
     * @see jakarta.faces.application.Application#setDefaultRenderKitId(String)
     */
    public void setDefaultRenderKitId(String renderKitId) {
        defaultRenderKitId = renderKitId;
    }

    /*
     * @see jakarta.faces.application.Application#getResourceBundle(jakarta.faces.context.FacesContext, String)
     */
    public ResourceBundle getResourceBundle(FacesContext context, String var) {

        notNull(CONTEXT, context);
        notNull("var", var);

        return associate.getResourceBundle(context, var);
    }

    private void notRequestServiced(String artifactId) {
        if (associate.hasRequestBeenServiced()) {
            throw new IllegalStateException(getExceptionMessageString(ILLEGAL_ATTEMPT_SETTING_APPLICATION_ARTIFACT_ID, artifactId));
        }
    }

}
