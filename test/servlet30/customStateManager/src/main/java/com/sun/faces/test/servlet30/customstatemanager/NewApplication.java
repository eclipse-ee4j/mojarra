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
import javax.faces.el.MethodBinding;
import javax.faces.el.PropertyResolver;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;
import javax.faces.el.VariableResolver;
import javax.faces.event.ActionListener;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.faces.validator.Validator;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

public class NewApplication extends ApplicationWrapper {


    private Application oldApp = null;

    public Application getWrapped() {
        return oldApp;
    }


    public NewApplication(Application oldApp) {

        this.oldApp = oldApp;

    }


    public ActionListener getActionListener() {

        return oldApp.getActionListener();

    }

    @Override
    public ProjectStage getProjectStage() {
        return oldApp.getProjectStage();
    }

    @Override
    public void publishEvent(FacesContext ctx,
                             Class<? extends SystemEvent> systemEventClass,
                             Object source) {
        oldApp.publishEvent(ctx, systemEventClass, source);
    }

    @Override
    public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass,
                                 Class<?> sourceClass,
                                 SystemEventListener listener) {
        oldApp.subscribeToEvent(systemEventClass, sourceClass, listener);
    }

    @Override
    public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass,
                                 SystemEventListener listener) {
        oldApp.subscribeToEvent(systemEventClass, listener);
    }

    @Override
    public void unsubscribeFromEvent(Class<? extends SystemEvent> systemEventClass,
                                     Class<?> sourceClass,
                                     SystemEventListener listener) {
        oldApp.unsubscribeFromEvent(systemEventClass,
                                    sourceClass,
                                    listener);
    }

    @Override
    public void unsubscribeFromEvent(Class<? extends SystemEvent> systemEventClass,
                                     SystemEventListener listener) {
        oldApp.unsubscribeFromEvent(systemEventClass, listener);
    }


    public void setActionListener(ActionListener listener) {

        oldApp.setActionListener(listener);

    }


    public Locale getDefaultLocale() {

        return oldApp.getDefaultLocale();

    }


    public void setDefaultLocale(Locale locale) {

        oldApp.setDefaultLocale(locale);

    }


    public String getDefaultRenderKitId() {

        return oldApp.getDefaultRenderKitId();

    }


    public void setDefaultRenderKitId(String renderKitId) {

        oldApp.setDefaultRenderKitId(renderKitId);

    }


    public String getMessageBundle() {

        return oldApp.getMessageBundle();

    }


    public void setMessageBundle(String bundle) {

        oldApp.setMessageBundle(bundle);

    }


    public NavigationHandler getNavigationHandler() {

        return oldApp.getNavigationHandler();

    }


    public void setNavigationHandler(NavigationHandler handler) {

        oldApp.setNavigationHandler(handler);

    }


    public void setResourceHandler(ResourceHandler rh) {
        oldApp.setResourceHandler(rh);
    }


    public ResourceHandler getResourceHandler() {
        return oldApp.getResourceHandler();
    }


    public PropertyResolver getPropertyResolver() {

        return oldApp.getPropertyResolver();

    }


    public void setPropertyResolver(PropertyResolver resolver) {

        oldApp.setPropertyResolver(resolver);

    }

    public ELResolver getELResolver() {

        return oldApp.getELResolver();

    }

    public ExpressionFactory getExpressionFactory() {
        return oldApp.getExpressionFactory();
    }

    public void addELContextListener(ELContextListener listener) {
        oldApp.addELContextListener(listener);
    }

    public void removeELContextListener(ELContextListener listener) {
        oldApp.removeELContextListener(listener);
    }

    public void addELResolver(ELResolver resolver) {
        oldApp.addELResolver(resolver);
    }

    public ELContextListener[] getELContextListeners() {
        return oldApp.getELContextListeners();
    }

    public Object evaluateExpressionGet(FacesContext context,
                                        String expression,
                                        Class expectedType) throws ELException {
        return oldApp.evaluateExpressionGet(context, expression, expectedType);
    }

    public VariableResolver getVariableResolver() {

        return oldApp.getVariableResolver();

    }


    public void setVariableResolver(VariableResolver resolver) {

        oldApp.setVariableResolver(resolver);

    }


    public ViewHandler getViewHandler() {

        return oldApp.getViewHandler();

    }


    public void setViewHandler(ViewHandler handler) {

        oldApp.setViewHandler(handler);

    }


    public StateManager getStateManager() {

        return oldApp.getStateManager();

    }


    public void setStateManager(StateManager manager) {

        oldApp.setStateManager(manager);

    }


    public ResourceBundle getResourceBundle(FacesContext ctx, String name) {

        return oldApp.getResourceBundle(ctx, name);

    }


    // ------------------------------------------------------- Object Factories


    public void addComponent(String componentType,

                             String componentClass) {

        oldApp.addComponent(componentType, componentClass);

    }


    public UIComponent createComponent(String componentType)

          throws FacesException {

        return oldApp.createComponent(componentType);

    }


    public UIComponent createComponent(ValueBinding componentBinding,

                                       FacesContext context,

                                       String componentType)

          throws FacesException {

        return oldApp.createComponent(componentBinding, context,

                                      componentType);

    }


    public Iterator getComponentTypes() {

        return oldApp.getComponentTypes();

    }


    public void addConverter(String converterId,

                             String converterClass) {

        oldApp.addConverter(converterId, converterClass);

    }


    public void addConverter(Class targetClass,

                             String converterClass) {

        oldApp.addConverter(targetClass, converterClass);

    }


    public Converter createConverter(String converterId) {

        return oldApp.createConverter(converterId);

    }


    public Converter createConverter(Class targetClass) {

        return oldApp.createConverter(targetClass);

    }


    public Iterator getConverterIds() {

        return oldApp.getConverterIds();

    }


    public Iterator getConverterTypes() {

        return oldApp.getConverterTypes();

    }


    public MethodBinding createMethodBinding(String ref,

                                             Class params[])

          throws ReferenceSyntaxException {

        return oldApp.createMethodBinding(ref, params);

    }


    public Iterator getSupportedLocales() {

        return oldApp.getSupportedLocales();

    }


    public void setSupportedLocales(Collection locales) {

        oldApp.setSupportedLocales(locales);

    }


    public void addValidator(String validatorId,

                             String validatorClass) {

        oldApp.addValidator(validatorId, validatorClass);

    }


    public Validator createValidator(String validatorId)

          throws FacesException {

        return oldApp.createValidator(validatorId);

    }


    public Iterator getValidatorIds() {

        return oldApp.getValidatorIds();

    }


    public ValueBinding createValueBinding(String ref)

          throws ReferenceSyntaxException {

        return oldApp.createValueBinding(ref);

    }


}

