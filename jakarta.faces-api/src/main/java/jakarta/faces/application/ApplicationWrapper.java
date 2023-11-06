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

package jakarta.faces.application;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import jakarta.el.ELContextListener;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.FacesWrapper;
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
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_2">Provides</span> a simple implementation of {@link Application} that can be
 * subclassed by developers wishing to provide specialized behavior to an existing {@link Application} instance. The
 * default implementation of all methods is to call through to the wrapped {@link Application}.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 2.0
 */
public abstract class ApplicationWrapper extends Application implements FacesWrapper<Application> {

    private Application wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public ApplicationWrapper() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this application has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     * @since 2.3
     */
    public ApplicationWrapper(Application wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Application getWrapped() {
        return wrapped;
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getActionListener} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public ActionListener getActionListener() {
        return getWrapped().getActionListener();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call
     * {@link Application#setActionListener(jakarta.faces.event.ActionListener)} on the wrapped {@link Application} object.
     * </p>
     */
    @Override
    public void setActionListener(ActionListener listener) {
        getWrapped().setActionListener(listener);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getDefaultLocale} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public Locale getDefaultLocale() {
        return getWrapped().getDefaultLocale();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#setDefaultLocale(java.util.Locale)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public void setDefaultLocale(Locale locale) {
        getWrapped().setDefaultLocale(locale);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getDefaultRenderKitId} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public String getDefaultRenderKitId() {
        return getWrapped().getDefaultRenderKitId();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#addDefaultValidatorId(String)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public void addDefaultValidatorId(String validatorId) {
        getWrapped().addDefaultValidatorId(validatorId);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getDefaultValidatorInfo} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public Map<String, String> getDefaultValidatorInfo() {
        return getWrapped().getDefaultValidatorInfo();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#setDefaultRenderKitId(String)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public void setDefaultRenderKitId(String renderKitId) {
        getWrapped().setDefaultRenderKitId(renderKitId);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getMessageBundle} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public String getMessageBundle() {
        return getWrapped().getMessageBundle();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#setMessageBundle(String)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public void setMessageBundle(String bundle) {
        getWrapped().setMessageBundle(bundle);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getNavigationHandler} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public NavigationHandler getNavigationHandler() {
        return getWrapped().getNavigationHandler();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#setNavigationHandler(NavigationHandler)} on the
     * wrapped {@link Application} object.
     * </p>
     */
    @Override
    public void setNavigationHandler(NavigationHandler handler) {
        getWrapped().setNavigationHandler(handler);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getViewHandler} on the wrapped {@link Application}
     * object.
     * </p>
     */
    @Override
    public ViewHandler getViewHandler() {
        return getWrapped().getViewHandler();
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">The</span> default behavior of this method is to call
     * {@link Application#setViewHandler(ViewHandler)} on the wrapped {@link Application} object.
     * </p>
     *
     * @throws IllegalStateException if this method is called after at least one request
     * has been processed by the <code>Lifecycle</code> instance for this application.
     * @throws NullPointerException if <code>manager</code> is <code>null</code>
     */
    @Override
    public void setViewHandler(ViewHandler handler) {
        getWrapped().setViewHandler(handler);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getStateManager} on the wrapped {@link Application}
     * object.
     * </p>
     */
    @Override
    public StateManager getStateManager() {
        return getWrapped().getStateManager();
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">The</span> default behavior of this method is to call
     * {@link Application#setStateManager(StateManager)} on the wrapped {@link Application} object.
     * </p>
     *
     * @throws IllegalStateException if this method is called after at least one request has
     * been processed by the <code>Lifecycle</code> instance for this application.
     * @throws NullPointerException if <code>manager</code> is <code>null</code>
     */
    @Override
    public void setStateManager(StateManager manager) {
        getWrapped().setStateManager(manager);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#addComponent(String, String)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public void addComponent(String componentType, String componentClass) {
        getWrapped().addComponent(componentType, componentClass);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#createComponent(String)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public UIComponent createComponent(String componentType) throws FacesException {
        return getWrapped().createComponent(componentType);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getComponentTypes} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public Iterator<String> getComponentTypes() {
        return getWrapped().getComponentTypes();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#addConverter(String, String)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public void addConverter(String converterId, String converterClass) {
        getWrapped().addConverter(converterId, converterClass);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#addConverter(Class, String)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public void addConverter(Class<?> targetClass, String converterClass) {
        getWrapped().addConverter(targetClass, converterClass);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#createConverter(String)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public Converter createConverter(String converterId) {
        return getWrapped().createConverter(converterId);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#createConverter(Class)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public Converter createConverter(Class<?> targetClass) {
        return getWrapped().createConverter(targetClass);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getConverterIds} on the wrapped {@link Application}
     * object.
     * </p>
     */
    @Override
    public Iterator<String> getConverterIds() {
        return getWrapped().getConverterIds();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getConverterTypes} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public Iterator<Class<?>> getConverterTypes() {
        return getWrapped().getConverterTypes();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getSupportedLocales} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public Iterator<Locale> getSupportedLocales() {
        return getWrapped().getSupportedLocales();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#setSupportedLocales(java.util.Collection)} on the
     * wrapped {@link Application} object.
     * </p>
     */
    @Override
    public void setSupportedLocales(Collection<Locale> locales) {
        getWrapped().setSupportedLocales(locales);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#addBehavior(String, String)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public void addBehavior(String behaviorId, String behaviorClass) {
        getWrapped().addBehavior(behaviorId, behaviorClass);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#createBehavior(String)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public Behavior createBehavior(String behaviorId) throws FacesException {
        return getWrapped().createBehavior(behaviorId);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getBehaviorIds} on the wrapped {@link Application}
     * object.
     * </p>
     */
    @Override
    public Iterator<String> getBehaviorIds() {
        return getWrapped().getBehaviorIds();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#addValidator(String, String)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public void addValidator(String validatorId, String validatorClass) {
        getWrapped().addValidator(validatorId, validatorClass);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#createValidator(String)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public Validator createValidator(String validatorId) throws FacesException {
        return getWrapped().createValidator(validatorId);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getValidatorIds} on the wrapped {@link Application}
     * object.
     * </p>
     */
    @Override
    public Iterator<String> getValidatorIds() {
        return getWrapped().getValidatorIds();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getResourceHandler} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public ResourceHandler getResourceHandler() {
        return getWrapped().getResourceHandler();
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">The</span> default behavior of this method is to call
     * {@link Application#setResourceHandler(ResourceHandler)} on the wrapped {@link Application} object.
     * </p>
     *
     * <span class="changed_added_2_2">
     * This method can throw <code>IllegalStateException</code> and <code>NullPointerException</code>.
     * </span>
     *
     * @throws IllegalStateException if this method is called after at least one request has
     * been processed by the <code>Lifecycle</code> instance for this application.
     * @throws NullPointerException if <code>resourceHandler</code> is <code>null</code>
     */
    @Override
    public void setResourceHandler(ResourceHandler resourceHandler) {
        getWrapped().setResourceHandler(resourceHandler);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call
     * {@link Application#getResourceBundle(jakarta.faces.context.FacesContext, String)} on the wrapped {@link Application}
     * object.
     * </p>
     */
    @Override
    public ResourceBundle getResourceBundle(FacesContext ctx, String name) {
        return getWrapped().getResourceBundle(ctx, name);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getProjectStage} on the wrapped {@link Application}
     * object.
     * </p>
     */
    @Override
    public ProjectStage getProjectStage() {
        return getWrapped().getProjectStage();
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">The</span> default behavior of this method is to call
     * {@link Application#addELResolver(jakarta.el.ELResolver)} on the wrapped {@link Application} object.
     * </p>
     *
     * @throws IllegalStateException <span class="changed_added_2_2">if called after the first request to the
     * {@link jakarta.faces.webapp.FacesServlet} has been serviced.</span>
     */
    @Override
    public void addELResolver(ELResolver resolver) {
        getWrapped().addELResolver(resolver);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getELResolver} on the wrapped {@link Application}
     * object.
     * </p>
     */
    @Override
    public ELResolver getELResolver() {
        return getWrapped().getELResolver();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call
     * {@link Application#createComponent(jakarta.el.ValueExpression, jakarta.faces.context.FacesContext, String)} on the
     * wrapped {@link Application} object.
     * </p>
     */
    @Override
    public UIComponent createComponent(ValueExpression componentExpression, FacesContext context, String componentType) throws FacesException {
        return getWrapped().createComponent(componentExpression, context, componentType);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call
     * {@link Application#createComponent(jakarta.el.ValueExpression, jakarta.faces.context.FacesContext, String, String)}
     * on the wrapped {@link Application} object.
     * </p>
     */
    @Override
    public UIComponent createComponent(ValueExpression componentExpression, FacesContext context, String componentType, String rendererType) {
        return getWrapped().createComponent(componentExpression, context, componentType, rendererType);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call
     * {@link Application#createComponent(jakarta.faces.context.FacesContext, String, String)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public UIComponent createComponent(FacesContext context, String componentType, String rendererType) {
        return getWrapped().createComponent(context, componentType, rendererType);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call
     * {@link Application#createComponent(jakarta.faces.context.FacesContext, Resource)} on the wrapped {@link Application}
     * object.
     * </p>
     */
    @Override
    public UIComponent createComponent(FacesContext context, Resource componentResource) {
        return getWrapped().createComponent(context, componentResource);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getExpressionFactory} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public ExpressionFactory getExpressionFactory() {
        return getWrapped().getExpressionFactory();
    }

    @Override
    public FlowHandler getFlowHandler() {
        return getWrapped().getFlowHandler();
    }

    @Override
    public void setFlowHandler(FlowHandler newHandler) {
        super.setFlowHandler(newHandler);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call
     * {@link Application#evaluateExpressionGet(jakarta.faces.context.FacesContext, String, Class)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public <T> T evaluateExpressionGet(FacesContext context, String expression, Class<? extends T> expectedType) throws ELException {
        return getWrapped().evaluateExpressionGet(context, expression, expectedType);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#addELContextListener(jakarta.el.ELContextListener)}
     * on the wrapped {@link Application} object.
     * </p>
     */
    @Override
    public void addELContextListener(ELContextListener listener) {
        getWrapped().addELContextListener(listener);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call
     * {@link Application#removeELContextListener(jakarta.el.ELContextListener)} on the wrapped {@link Application} object.
     * </p>
     */
    @Override
    public void removeELContextListener(ELContextListener listener) {
        getWrapped().removeELContextListener(listener);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Application#getELContextListeners} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public ELContextListener[] getELContextListeners() {
        return getWrapped().getELContextListeners();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call
     * {@link Application#publishEvent(jakarta.faces.context.FacesContext, Class, Object)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public void publishEvent(FacesContext context, Class<? extends SystemEvent> systemEventClass, Object source) {
        getWrapped().publishEvent(context, systemEventClass, source);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call
     * {@link Application#publishEvent(jakarta.faces.context.FacesContext, Class, Class, Object)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public void publishEvent(FacesContext context, Class<? extends SystemEvent> systemEventClass, Class<?> sourceBaseType, Object source) {
        getWrapped().publishEvent(context, systemEventClass, sourceBaseType, source);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call
     * {@link Application#subscribeToEvent(Class, Class, jakarta.faces.event.SystemEventListener)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass, Class<?> sourceClass, SystemEventListener listener) {
        getWrapped().subscribeToEvent(systemEventClass, sourceClass, listener);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call
     * {@link Application#subscribeToEvent(Class, jakarta.faces.event.SystemEventListener)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass, SystemEventListener listener) {
        getWrapped().subscribeToEvent(systemEventClass, listener);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call
     * {@link Application#unsubscribeFromEvent(Class, Class, jakarta.faces.event.SystemEventListener)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public void unsubscribeFromEvent(Class<? extends SystemEvent> systemEventClass, Class<?> sourceClass, SystemEventListener listener) {
        getWrapped().unsubscribeFromEvent(systemEventClass, sourceClass, listener);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call
     * {@link Application#unsubscribeFromEvent(Class, jakarta.faces.event.SystemEventListener)} on the wrapped
     * {@link Application} object.
     * </p>
     */
    @Override
    public void unsubscribeFromEvent(Class<? extends SystemEvent> systemEventClass, SystemEventListener listener) {
        getWrapped().unsubscribeFromEvent(systemEventClass, listener);
    }

    @Override
    public SearchExpressionHandler getSearchExpressionHandler() {
        return getWrapped().getSearchExpressionHandler();
    }

    @Override
    public void setSearchExpressionHandler(SearchExpressionHandler searchExpressionHandler) {
        getWrapped().setSearchExpressionHandler(searchExpressionHandler);
    }

    @Override
    public void addSearchKeywordResolver(SearchKeywordResolver resolver) {
        getWrapped().addSearchKeywordResolver(resolver);
    }

    @Override
    public SearchKeywordResolver getSearchKeywordResolver() {
        return getWrapped().getSearchKeywordResolver();
    }
}
