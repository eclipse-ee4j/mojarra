/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.servlet30.customstatemanager;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.el.ELContextListener;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ApplicationWrapper;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ProjectStage;
import javax.faces.application.ResourceHandler;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.event.ActionListener;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.faces.validator.Validator;

public class NewApplication extends ApplicationWrapper {

    private Application oldApp = null;

    @Override
    public Application getWrapped() {
        return oldApp;
    }

    public NewApplication(Application oldApp) {
        this.oldApp = oldApp;
    }

    @Override
    public ActionListener getActionListener() {
        return oldApp.getActionListener();
    }

    @Override
    public ProjectStage getProjectStage() {
        return oldApp.getProjectStage();
    }

    @Override
    public void publishEvent(FacesContext ctx, Class<? extends SystemEvent> systemEventClass, Object source) {
        oldApp.publishEvent(ctx, systemEventClass, source);
    }

    @Override
    public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass, Class<?> sourceClass, SystemEventListener listener) {
        oldApp.subscribeToEvent(systemEventClass, sourceClass, listener);
    }

    @Override
    public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass, SystemEventListener listener) {
        oldApp.subscribeToEvent(systemEventClass, listener);
    }

    @Override
    public void unsubscribeFromEvent(Class<? extends SystemEvent> systemEventClass, Class<?> sourceClass, SystemEventListener listener) {
        oldApp.unsubscribeFromEvent(systemEventClass, sourceClass, listener);
    }

    @Override
    public void unsubscribeFromEvent(Class<? extends SystemEvent> systemEventClass, SystemEventListener listener) {
        oldApp.unsubscribeFromEvent(systemEventClass, listener);
    }

    @Override
    public void setActionListener(ActionListener listener) {
        oldApp.setActionListener(listener);
    }

    @Override
    public Locale getDefaultLocale() {
        return oldApp.getDefaultLocale();
    }

    @Override
    public void setDefaultLocale(Locale locale) {
        oldApp.setDefaultLocale(locale);
    }

    @Override
    public String getDefaultRenderKitId() {
        return oldApp.getDefaultRenderKitId();
    }

    @Override
    public void setDefaultRenderKitId(String renderKitId) {
        oldApp.setDefaultRenderKitId(renderKitId);
    }

    @Override
    public String getMessageBundle() {
        return oldApp.getMessageBundle();
    }

    @Override
    public void setMessageBundle(String bundle) {
        oldApp.setMessageBundle(bundle);
    }

    @Override
    public NavigationHandler getNavigationHandler() {
        return oldApp.getNavigationHandler();
    }

    @Override
    public void setNavigationHandler(NavigationHandler handler) {
        oldApp.setNavigationHandler(handler);
    }

    @Override
    public void setResourceHandler(ResourceHandler rh) {
        oldApp.setResourceHandler(rh);
    }

    @Override
    public ResourceHandler getResourceHandler() {
        return oldApp.getResourceHandler();
    }

    @Override
    public ELResolver getELResolver() {
        return oldApp.getELResolver();
    }

    @Override
    public ExpressionFactory getExpressionFactory() {
        return oldApp.getExpressionFactory();
    }

    @Override
    public void addELContextListener(ELContextListener listener) {
        oldApp.addELContextListener(listener);
    }

    @Override
    public void removeELContextListener(ELContextListener listener) {
        oldApp.removeELContextListener(listener);
    }

    @Override
    public void addELResolver(ELResolver resolver) {
        oldApp.addELResolver(resolver);
    }

    @Override
    public ELContextListener[] getELContextListeners() {
        return oldApp.getELContextListeners();
    }

    @Override
    public Object evaluateExpressionGet(FacesContext context, String expression, Class expectedType) throws ELException {
        return oldApp.evaluateExpressionGet(context, expression, expectedType);
    }

    @Override
    public ViewHandler getViewHandler() {
        return oldApp.getViewHandler();
    }

    @Override
    public void setViewHandler(ViewHandler handler) {
        oldApp.setViewHandler(handler);
    }

    @Override
    public StateManager getStateManager() {
        return oldApp.getStateManager();
    }

    @Override
    public void setStateManager(StateManager manager) {
        oldApp.setStateManager(manager);
    }

    @Override
    public ResourceBundle getResourceBundle(FacesContext ctx, String name) {
        return oldApp.getResourceBundle(ctx, name);
    }

    // ------------------------------------------------------- Object Factories

    @Override
    public void addComponent(String componentType, String componentClass) {
        oldApp.addComponent(componentType, componentClass);
    }

    @Override
    public UIComponent createComponent(String componentType) throws FacesException {
        return oldApp.createComponent(componentType);
    }

    @Override
    public Iterator getComponentTypes() {
        return oldApp.getComponentTypes();
    }

    @Override
    public void addConverter(String converterId, String converterClass) {
        oldApp.addConverter(converterId, converterClass);
    }

    @Override
    public void addConverter(Class targetClass, String converterClass) {
        oldApp.addConverter(targetClass, converterClass);
    }

    @Override
    public Converter createConverter(String converterId) {
        return oldApp.createConverter(converterId);
    }

    @Override
    public Converter createConverter(Class targetClass) {
        return oldApp.createConverter(targetClass);
    }

    @Override
    public Iterator getConverterIds() {
        return oldApp.getConverterIds();
    }

    @Override
    public Iterator getConverterTypes() {
        return oldApp.getConverterTypes();
    }

    @Override
    public Iterator getSupportedLocales() {
        return oldApp.getSupportedLocales();
    }

    @Override
    public void setSupportedLocales(Collection locales) {
        oldApp.setSupportedLocales(locales);
    }

    @Override
    public void addValidator(String validatorId, String validatorClass) {
        oldApp.addValidator(validatorId, validatorClass);
    }

    @Override
    public Validator createValidator(String validatorId)

            throws FacesException {

        return oldApp.createValidator(validatorId);

    }

    @Override
    public Iterator getValidatorIds() {

        return oldApp.getValidatorIds();

    }


}
