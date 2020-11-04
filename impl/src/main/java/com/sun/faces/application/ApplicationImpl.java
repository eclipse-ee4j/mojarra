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

package com.sun.faces.application;

import static java.util.logging.Level.FINE;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.sun.faces.application.applicationimpl.Events;
import com.sun.faces.application.applicationimpl.ExpressionLanguage;
import com.sun.faces.application.applicationimpl.InstanceFactory;
import com.sun.faces.application.applicationimpl.SearchExpression;
import com.sun.faces.application.applicationimpl.Singletons;
import com.sun.faces.application.applicationimpl.Stage;
import com.sun.faces.el.FacesCompositeELResolver;
import com.sun.faces.util.FacesLogger;

import jakarta.el.CompositeELResolver;
import jakarta.el.ELContextListener;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.application.Application;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.application.StateManager;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.Behavior;
import jakarta.faces.component.search.SearchExpressionHandler;
import jakarta.faces.component.search.SearchKeywordResolver;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.event.ActionListener;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import jakarta.faces.flow.FlowHandler;
import jakarta.faces.validator.Validator;

/**
 * <strong>Application</strong> represents a per-web-application singleton object where applications based on
 * Faces (or implementations wishing to provide extended functionality) can register application-wide singletons that
 * provide functionality required by Faces.
 */
public class ApplicationImpl extends Application {

    public static final String THIS_LIBRARY = "com.sun.faces.composite.this.library";

    // Log instance for this class
    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    // Relationship Instance Variables

    private final ApplicationAssociate associate;
    private final Events events;
    private final Singletons singletons;
    private final ExpressionLanguage expressionLanguage;
    private final InstanceFactory instanceFactory;
    private final SearchExpression searchExpression;
    private final Stage stage;

    /**
     * Constructor
     */
    public ApplicationImpl() {
        // stage and events are called back by ApplicationAssociate
        stage = new Stage();
        events = new Events();
        associate = new ApplicationAssociate(this);
        singletons = new Singletons(associate);
        expressionLanguage = new ExpressionLanguage(associate);
        instanceFactory = new InstanceFactory(associate);
        searchExpression = new SearchExpression(associate);

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE, "Created Application instance ");
        }
    }

    // ----------------------------------------------------------- Events

    /**
     * @see jakarta.faces.application.Application#publishEvent(FacesContext, Class, Object)
     */
    @Override
    public void publishEvent(FacesContext context, Class<? extends SystemEvent> systemEventClass, Object source) {
        publishEvent(context, systemEventClass, null, source);
    }

    /**
     * @see jakarta.faces.application.Application#publishEvent(FacesContext, Class, Object)
     */
    @Override
    public void publishEvent(FacesContext context, Class<? extends SystemEvent> systemEventClass, Class<?> sourceBaseType, Object source) {
        events.publishEvent(context, systemEventClass, sourceBaseType, source, getProjectStage());
    }

    /**
     * @see Application#subscribeToEvent(Class, jakarta.faces.event.SystemEventListener)
     */
    @Override
    public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass, SystemEventListener listener) {
        subscribeToEvent(systemEventClass, null, listener);
    }

    /**
     * @see Application#subscribeToEvent(Class, Class, jakarta.faces.event.SystemEventListener)
     */
    @Override
    public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass, Class<?> sourceClass, SystemEventListener listener) {
        events.subscribeToEvent(systemEventClass, sourceClass, listener);
    }

    /**
     * @see Application#unsubscribeFromEvent(Class, jakarta.faces.event.SystemEventListener)
     */
    @Override
    public void unsubscribeFromEvent(Class<? extends SystemEvent> systemEventClass, SystemEventListener listener) {
        unsubscribeFromEvent(systemEventClass, null, listener);
    }

    /**
     * @see Application#unsubscribeFromEvent(Class, Class, jakarta.faces.event.SystemEventListener)
     */
    @Override
    public void unsubscribeFromEvent(Class<? extends SystemEvent> systemEventClass, Class<?> sourceClass, SystemEventListener listener) {
        events.unsubscribeFromEvent(systemEventClass, sourceClass, listener);
    }

    // ----------------------------------------------------------- Expression language

    /**
     * @see jakarta.faces.application.Application#addELContextListener(jakarta.el.ELContextListener)
     */
    @Override
    public void addELContextListener(ELContextListener listener) {
        expressionLanguage.addELContextListener(listener);
    }

    /**
     * @see jakarta.faces.application.Application#removeELContextListener(jakarta.el.ELContextListener)
     */
    @Override
    public void removeELContextListener(ELContextListener listener) {
        expressionLanguage.removeELContextListener(listener);
    }

    /**
     * @see jakarta.faces.application.Application#getELContextListeners()
     */
    @Override
    public ELContextListener[] getELContextListeners() {
        return expressionLanguage.getELContextListeners();
    }

    /**
     * @see jakarta.faces.application.Application#getExpressionFactory()
     */
    @Override
    public ExpressionFactory getExpressionFactory() {
        return expressionLanguage.getExpressionFactory();
    }

    /**
     * @see jakarta.faces.application.Application#evaluateExpressionGet(jakarta.faces.context.FacesContext, String, Class)
     */
    @Override
    public <T> T evaluateExpressionGet(FacesContext context, String expression, Class<? extends T> expectedType) throws ELException {
        return expressionLanguage.evaluateExpressionGet(context, expression, expectedType);
    }

    /**
     * @see jakarta.faces.application.Application#getELResolver()
     */
    @Override
    public ELResolver getELResolver() {
        return expressionLanguage.getELResolver();
    }

    /**
     * @see jakarta.faces.application.Application#addELResolver(jakarta.el.ELResolver)
     */
    @Override
    public void addELResolver(ELResolver resolver) {
        expressionLanguage.addELResolver(resolver);
    }

    public CompositeELResolver getApplicationELResolvers() {
        return expressionLanguage.getApplicationELResolvers();
    }

    public FacesCompositeELResolver getCompositeELResolver() {
        return expressionLanguage.getCompositeELResolver();
    }

    public void setCompositeELResolver(FacesCompositeELResolver compositeELResolver) {
        expressionLanguage.setCompositeELResolver(compositeELResolver);
    }

    // ----------------------------------------------------------- Singletons

    /**
     * @see jakarta.faces.application.Application#getViewHandler()
     */
    @Override
    public ViewHandler getViewHandler() {
        return singletons.getViewHandler();
    }

    /**
     * @see jakarta.faces.application.Application#setViewHandler(jakarta.faces.application.ViewHandler)
     */
    @Override
    public void setViewHandler(ViewHandler viewHandler) {
        singletons.setViewHandler(viewHandler);
    }

    /**
     * @see jakarta.faces.application.Application#getResourceHandler()
     */
    @Override
    public ResourceHandler getResourceHandler() {
        return singletons.getResourceHandler();
    }

    /**
     * @see jakarta.faces.application.Application#setResourceHandler(jakarta.faces.application.ResourceHandler)
     */
    @Override
    public void setResourceHandler(ResourceHandler resourceHandler) {
        singletons.setResourceHandler(resourceHandler);
    }

    /**
     * @see jakarta.faces.application.Application#getStateManager()
     */
    @Override
    public StateManager getStateManager() {
        return singletons.getStateManager();
    }

    /**
     * @see jakarta.faces.application.Application#setStateManager(jakarta.faces.application.StateManager)
     */
    @Override
    public void setStateManager(StateManager stateManager) {
        singletons.setStateManager(stateManager);
    }

    /**
     * @see jakarta.faces.application.Application#getActionListener()
     */
    @Override
    public ActionListener getActionListener() {
        return singletons.getActionListener();
    }

    /**
     * @see Application#setActionListener(jakarta.faces.event.ActionListener)
     */
    @Override
    public void setActionListener(ActionListener actionListener) {
        singletons.setActionListener(actionListener);
    }

    /**
     * @see jakarta.faces.application.Application#getNavigationHandler()
     */
    @Override
    public NavigationHandler getNavigationHandler() {
        return singletons.getNavigationHandler();
    }

    /**
     * @see jakarta.faces.application.Application#setNavigationHandler(jakarta.faces.application.NavigationHandler)
     */
    @Override
    public void setNavigationHandler(NavigationHandler navigationHandler) {
        singletons.setNavigationHandler(navigationHandler);
    }

    @Override
    public FlowHandler getFlowHandler() {
        return singletons.getFlowHandler();
    }

    @Override
    public void setFlowHandler(FlowHandler flowHandler) {
        singletons.setFlowHandler(flowHandler);
    }

    /**
     * @see jakarta.faces.application.Application#getSupportedLocales()
     */
    @Override
    public Iterator<Locale> getSupportedLocales() {
        return singletons.getSupportedLocales();
    }

    /**
     * @see jakarta.faces.application.Application#setSupportedLocales(java.util.Collection)
     */
    @Override
    public void setSupportedLocales(Collection<Locale> newLocales) {
        singletons.setSupportedLocales(newLocales);
    }

    /**
     * @see jakarta.faces.application.Application#getDefaultLocale()
     */
    @Override
    public Locale getDefaultLocale() {
        return singletons.getDefaultLocale();
    }

    /**
     * @see jakarta.faces.application.Application#setDefaultLocale(java.util.Locale)
     */
    @Override
    public void setDefaultLocale(Locale locale) {
        singletons.setDefaultLocale(locale);
    }

    /**
     * @see jakarta.faces.application.Application#setMessageBundle(String)
     */
    @Override
    public void setMessageBundle(String messageBundle) {
        singletons.setMessageBundle(messageBundle);
    }

    /**
     * @see jakarta.faces.application.Application#getMessageBundle()
     */
    @Override
    public String getMessageBundle() {
        return singletons.getMessageBundle();
    }

    /**
     * @see jakarta.faces.application.Application#getDefaultRenderKitId()
     */
    @Override
    public String getDefaultRenderKitId() {
        return singletons.getDefaultRenderKitId();
    }

    /**
     * @see jakarta.faces.application.Application#setDefaultRenderKitId(String)
     */
    @Override
    public void setDefaultRenderKitId(String renderKitId) {
        singletons.setDefaultRenderKitId(renderKitId);
    }

    /**
     * @see jakarta.faces.application.Application#getResourceBundle(jakarta.faces.context.FacesContext, String)
     */
    @Override
    public ResourceBundle getResourceBundle(FacesContext context, String var) {
        return singletons.getResourceBundle(context, var);
    }

    // ----------------------------------------------------------- Instance factory

    /**
     * @see jakarta.faces.application.Application#addBehavior(String, String)
     */
    @Override
    public void addBehavior(String behaviorId, String behaviorClass) {
        instanceFactory.addBehavior(behaviorId, behaviorClass);
    }

    /**
     * @see jakarta.faces.application.Application#createBehavior(String)
     */
    @Override
    public Behavior createBehavior(String behaviorId) throws FacesException {
        return instanceFactory.createBehavior(behaviorId);
    }

    /**
     * @see jakarta.faces.application.Application#getBehaviorIds()
     */
    @Override
    public Iterator<String> getBehaviorIds() {
        return instanceFactory.getBehaviorIds();
    }

    @Override
    public UIComponent createComponent(String componentType) throws FacesException {
        return instanceFactory.createComponent(componentType);
    }

    /**
     * @see jakarta.faces.application.Application#addComponent(java.lang.String, java.lang.String)
     */
    @Override
    public void addComponent(String componentType, String componentClass) {
        instanceFactory.addComponent(componentType, componentClass);
    }

    @Override
    public UIComponent createComponent(ValueExpression componentExpression, FacesContext context, String componentType) throws FacesException {
        return instanceFactory.createComponent(componentExpression, context, componentType);
    }

    @Override
    public UIComponent createComponent(ValueExpression componentExpression, FacesContext context, String componentType, String rendererType) {
        return instanceFactory.createComponent(componentExpression, context, componentType, rendererType);
    }

    @Override
    public UIComponent createComponent(FacesContext context, String componentType, String rendererType) {
        return instanceFactory.createComponent(context, componentType, rendererType);
    }

    @Override
    public UIComponent createComponent(FacesContext context, Resource componentResource) throws FacesException {
        return instanceFactory.createComponent(context, componentResource, getExpressionFactory());
    }

    /**
     * @see jakarta.faces.application.Application#getComponentTypes()
     */
    @Override
    public Iterator<String> getComponentTypes() {
        return instanceFactory.getComponentTypes();
    }

    /**
     * @see jakarta.faces.application.Application#addConverter(String, String)
     */
    @Override
    public void addConverter(String converterId, String converterClass) {
        instanceFactory.addConverter(converterId, converterClass);
    }

    /**
     * @see jakarta.faces.application.Application#addConverter(Class, String)
     */
    @Override
    public void addConverter(Class<?> targetClass, String converterClass) {
        instanceFactory.addConverter(targetClass, converterClass);
    }

    /**
     * @see jakarta.faces.application.Application#createConverter(String)
     */
    @Override
    public Converter<?> createConverter(String converterId) {
        return instanceFactory.createConverter(converterId);
    }

    /**
     * @see jakarta.faces.application.Application#createConverter(Class)
     */
    @Override
    public Converter<?> createConverter(Class<?> targetClass) {
        return instanceFactory.createConverter(targetClass);
    }

    /**
     * @see jakarta.faces.application.Application#getConverterIds()
     */
    @Override
    public Iterator<String> getConverterIds() {
        return instanceFactory.getConverterIds();
    }

    /**
     * @see jakarta.faces.application.Application#getConverterTypes()
     */
    @Override
    public Iterator<Class<?>> getConverterTypes() {
        return instanceFactory.getConverterTypes();
    }

    /**
     * @see jakarta.faces.application.Application#addValidator(String, String)
     */
    @Override
    public void addValidator(String validatorId, String validatorClass) {
        instanceFactory.addValidator(validatorId, validatorClass);
    }

    /**
     * @see jakarta.faces.application.Application#createValidator(String)
     */
    @Override
    public Validator<?> createValidator(String validatorId) throws FacesException {
        return instanceFactory.createValidator(validatorId);
    }

    /**
     * @see jakarta.faces.application.Application#getValidatorIds()
     */
    @Override
    public Iterator<String> getValidatorIds() {
        return instanceFactory.getValidatorIds();
    }

    /**
     * @see jakarta.faces.application.Application#addDefaultValidatorId(String)
     */
    @Override
    public void addDefaultValidatorId(String validatorId) {
        instanceFactory.addDefaultValidatorId(validatorId);
    }

    /**
     * @see jakarta.faces.application.Application#getDefaultValidatorInfo()
     */
    @Override
    public Map<String, String> getDefaultValidatorInfo() {
        return instanceFactory.getDefaultValidatorInfo();
    }

    // ----------------------------------------------------------- Instance factory

    /**
     * @see jakarta.faces.application.Application#getProjectStage()
     */
    @Override
    public ProjectStage getProjectStage() {
        return stage.getProjectStage(this);
    }

    // ----------------------------------------------------------- Search expression

    @Override
    public SearchExpressionHandler getSearchExpressionHandler() {
        return searchExpression.getSearchExpressionHandler();
    }

    @Override
    public void setSearchExpressionHandler(SearchExpressionHandler searchExpressionHandler) {
        searchExpression.setSearchExpressionHandler(searchExpressionHandler);
    }

    @Override
    public void addSearchKeywordResolver(SearchKeywordResolver resolver) {
        searchExpression.addSearchKeywordResolver(resolver);
    }

    @Override
    public SearchKeywordResolver getSearchKeywordResolver() {
        return searchExpression.getSearchKeywordResolver();
    }

}
