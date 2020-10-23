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

package com.sun.faces.mgbean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.el.ELUtils;
import com.sun.faces.spi.InjectionProvider;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.FunctionMapper;
import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.PreDestroyCustomScopeEvent;
import jakarta.faces.event.ScopeContext;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;

/**
 * <p>
 * Main interface for dealing with Faces managed beans
 * </p>
 */
public class BeanManager implements SystemEventListener {

    private static final Logger LOGGER = FacesLogger.MANAGEDBEAN.getLogger();

    @SuppressWarnings({ "CollectionWithoutInitialCapacity" })
    private Map<String, BeanBuilder> managedBeans = new HashMap<>();
    private InjectionProvider injectionProvider;
    private boolean configPreprocessed;
    private boolean lazyBeanValidation;
    private List<String> eagerBeans = new ArrayList<>(4);

    // ------------------------------------------------------------ Constructors

    public BeanManager(InjectionProvider injectionProvider, boolean lazyBeanValidation) {

        this.injectionProvider = injectionProvider;
        this.lazyBeanValidation = lazyBeanValidation;

    }

    public BeanManager(InjectionProvider injectionProvider, Map<String, BeanBuilder> managedBeans, boolean lazyBeanValidation) {

        this(injectionProvider, lazyBeanValidation);
        this.managedBeans = managedBeans;

    }

    // ---------------------------------------- Methods from SystemEventListener

    /**
     * <p>
     * Invoke PreDestroy methods on any managed beans within the provided scope.
     * </p>
     *
     * @param event the {@link ScopeContext}
     *
     * @throws AbortProcessingException
     */
    @Override
    public void processEvent(SystemEvent event) throws AbortProcessingException {

        ScopeContext scopeContext = ((PreDestroyCustomScopeEvent) event).getContext();
        Map<String, Object> scope = scopeContext.getScope();
        for (Map.Entry<String, Object> entry : scope.entrySet()) {
            String name = entry.getKey();
            if (isManaged(name)) {
                BeanBuilder builder = getBuilder(name);
                builder.destroy(injectionProvider, entry.getValue());
            }
        }

    }

    /**
     * @see SystemEventListener#isListenerForSource(Object)
     */
    @Override
    public boolean isListenerForSource(Object source) {

        return source instanceof ScopeContext;

    }

    // ---------------------------------------------------------- Public Methods

    public void register(ManagedBeanInfo beanInfo) {
        BeanBuilder builder;
        if (beanInfo.hasListEntry()) {
            if (beanInfo.hasMapEntry() || beanInfo.hasManagedProperties()) {
                String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_AS_LIST_CONFIG_ERROR_ID, beanInfo.getName());
                builder = new ErrorBean(beanInfo, message);
                // addBean(beanInfo.getName(), new ErrorBean(beanInfo, message));
            } else {
                builder = new ManagedListBeanBuilder(beanInfo);
                // addBean(beanInfo.getName(),
                // new ManagedListBeanBuilder(beanInfo));
            }
        } else if (beanInfo.hasMapEntry()) {
            if (beanInfo.hasManagedProperties()) {
                String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_AS_MAP_CONFIG_ERROR_ID, beanInfo.getName());
                builder = new ErrorBean(beanInfo, message);
                // addBean(beanInfo.getName(), new ErrorBean(beanInfo, message));
            } else {
                builder = new ManagedMapBeanBuilder(beanInfo);
                // addBean(beanInfo.getName(), new ManagedMapBeanBuilder(beanInfo));
            }
        } else {
            builder = new ManagedBeanBuilder(beanInfo);
            // addBean(beanInfo.getName(), new ManagedBeanBuilder(beanInfo));
        }

        addBean(beanInfo.getName(), builder);
        if (beanInfo.isEager()) {
            eagerBeans.add(beanInfo.getName());
        }

    }

    public List<String> getEagerBeanNames() {

        return eagerBeans;

    }

    public Map<String, BeanBuilder> getRegisteredBeans() {

        return managedBeans;

    }

    public boolean isManaged(String name) {

        return managedBeans != null && managedBeans.containsKey(name);

    }

    public BeanBuilder getBuilder(String name) {

        if (managedBeans != null) {
            return managedBeans.get(name);
        }
        return null;

    }

    /**
     * This should only be called during application init
     */
    public void preProcessesBeans() {

        if (!configPreprocessed && !lazyBeanValidation) {
            configPreprocessed = true;
            for (Map.Entry<String, BeanBuilder> entry : managedBeans.entrySet()) {
                preProcessBean(entry.getKey(), entry.getValue());
            }
        }

    }

    public boolean isBeanInScope(String name, BeanBuilder builder, FacesContext context) {
        return ScopeManager.isInScope(name, builder.getScope(), context);

    }

    public Object getBeanFromScope(String name, BeanBuilder builder, FacesContext context) {
        return ScopeManager.getFromScope(name, builder.getScope(), context);
    }

    public Object getBeanFromScope(String name, FacesContext context) {

        String scope = getBuilder(name).getScope();
        return ScopeManager.getFromScope(name, scope, context);

    }

    // ------------------------------------------------------- Lifecycle Methods

    public Object create(String name, FacesContext facesContext) {
        return create(name, managedBeans.get(name), facesContext);
    }

    public Object create(String name, BeanBuilder builder, FacesContext facesContext) {
        if (builder != null) {
            if (lazyBeanValidation && !builder.isBaked()) {
                preProcessBean(name, builder);
            }
            if (builder.hasMessages()) {
                throw new ManagedBeanCreationException(buildMessage(name, builder.getMessages(), true));
            } else {
                return createAndPush(name, builder, facesContext);
            }
        }

        return null;

    }

    public void destroy(String beanName, Object bean) {

        BeanBuilder builder = managedBeans.get(beanName);
        if (builder != null) {
            builder.destroy(injectionProvider, bean);
        }

    }

    // --------------------------------------------------------- Private Methods

    private void addBean(String beanName, BeanBuilder builder) {

        if (configPreprocessed) {
            preProcessBean(beanName, builder);
        }
        if (LOGGER.isLoggable(Level.WARNING) && managedBeans.containsKey(beanName)) {
            LOGGER.log(Level.WARNING, "jsf.managed.bean.duplicate",
                    new Object[] { beanName, managedBeans.get(beanName).beanInfo.getClassName(), builder.beanInfo.getClassName() });
        }
        managedBeans.put(beanName, builder);

    }

    private void validateReferences(BeanBuilder builder, List<String> references, List<String> messages) {

        List<String> refs = builder.getReferences();
        if (refs != null) {
            for (String ref : refs) {
                if (isManaged(ref)) {
                    if (references.contains(ref)) {
                        StringBuilder sb = new StringBuilder(64);
                        String[] ra = references.toArray(new String[references.size()]);
                        for (String reference : ra) {
                            sb.append(reference);
                            sb.append(" -> ");
                        }
                        sb.append(ref);

                        String message = MessageUtils.getExceptionMessageString(MessageUtils.CYCLIC_REFERENCE_ERROR_ID, ra[0], sb.toString());
                        messages.add(message);
                    } else {
                        BeanBuilder b = getBuilder(ref);
                        // If the bean has no references, then it's not
                        // a target for cyclic detection.
                        if (b.getReferences() != null) {
                            references.add(ref);
                            validateReferences(b, references, messages);
                            references.remove(ref);
                        }
                    }
                }
            }
        }

    }

    private synchronized void preProcessBean(String beanName, BeanBuilder builder) {
        if (!builder.isBaked()) {
            try {
                builder.bake();

                // preProcess any dependent beans
                List<String> propRefs = builder.getReferences();
                if (propRefs != null) {
                    for (String reference : propRefs) {
                        if (isManaged(reference)) {
                            BeanBuilder b = getBuilder(reference);
                            preProcessBean(reference, b);
                        }
                    }
                }

                // noinspection CollectionWithoutInitialCapacity
                List<String> refs = new ArrayList<>();
                refs.add(beanName);
                // noinspection CollectionWithoutInitialCapacity
                ArrayList<String> messages = new ArrayList<>();
                validateReferences(builder, refs, messages);
                if (!messages.isEmpty()) {
                    builder.queueMessages(messages);
                }

                if (builder.hasMessages()) {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE, buildMessage(beanName, builder.getMessages(), false));
                    }
                }
            } catch (ManagedBeanPreProcessingException mbpe) {
                if (ManagedBeanPreProcessingException.Type.CHECKED.equals(mbpe.getType())) {
                    builder.queueMessage(mbpe.getMessage());
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE, buildMessage(beanName, builder.getMessages(), false));
                    }
                } else {
                    String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_UNKNOWN_PROCESSING_ERROR_ID, beanName);
                    throw new ManagedBeanPreProcessingException(message, mbpe);
                }
            }
        }

    }

    private Object createAndPush(String name, BeanBuilder builder, FacesContext facesContext) {

        Object bean = builder.build(injectionProvider, facesContext);
        ScopeManager.pushToScope(name, bean, builder.getScope(), facesContext);
        return bean;

    }

    private String buildMessage(String name, List<String> messages, boolean runtime) {

        StringBuilder sb = new StringBuilder(128);
        if (runtime) {
            sb.append(MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_PROBLEMS_ERROR_ID, name));
        } else {
            sb.append(MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_PROBLEMS_STARTUP_ERROR_ID, name));
        }
        for (String message : messages) {
            sb.append("\n     - ").append(message);
        }
        return sb.toString();

    }

    // ----------------------------------------------------------- Inner Classes

    private static class ScopeManager {

        private static final ConcurrentMap<String, ScopeHandler> handlerMap = new ConcurrentHashMap<>(5);

        static {
            handlerMap.put(ELUtils.Scope.REQUEST.toString(), new RequestScopeHandler());
            handlerMap.put(ELUtils.Scope.VIEW.toString(), new ViewScopeHandler());
            handlerMap.put(ELUtils.Scope.SESSION.toString(), new SessionScopeHandler());
            handlerMap.put(ELUtils.Scope.APPLICATION.toString(), new ApplicationScopeHandler());
            handlerMap.put(ELUtils.Scope.NONE.toString(), new NoneScopeHandler());
        }

        static void pushToScope(String name, Object bean, String customScope, FacesContext context) {

            ScopeHandler handler = getScopeHandler(customScope, context);
            handler.handle(name, bean, context);

        }

        static boolean isInScope(String name, String customScope, FacesContext context) {

            ScopeHandler handler = getScopeHandler(customScope, context);
            return handler.isInScope(name, context);

        }

        static Object getFromScope(String name, String customScope, FacesContext context) {

            ScopeHandler handler = getScopeHandler(customScope, context);
            return handler.getFromScope(name, context);

        }

        private static ScopeHandler getScopeHandler(String customScope, FacesContext context) {

            ScopeHandler handler = handlerMap.get(customScope);
            if (handler == null) {
                ExpressionFactory factory = context.getApplication().getExpressionFactory();
                ValueExpression ve = factory.createValueExpression(context.getELContext(), customScope, Map.class);
                handler = new CustomScopeHandler(ve);
                handlerMap.putIfAbsent(customScope, handler);
            }
            return handler;

        }

        private interface ScopeHandler {

            void handle(String name, Object bean, FacesContext context);

            boolean isInScope(String name, FacesContext context);

            Object getFromScope(String name, FacesContext context);

        }

        private static class NoneScopeHandler implements ScopeHandler {

            @Override
            public void handle(String name, Object bean, FacesContext context) {
                // no-op
            }

            @Override
            public boolean isInScope(String name, FacesContext context) {
                return false;
            }

            @Override
            public Object getFromScope(String name, FacesContext context) {
                return null;
            }
        }

        private static class RequestScopeHandler implements ScopeHandler {

            @Override
            public void handle(String name, Object bean, FacesContext context) {

                context.getExternalContext().getRequestMap().put(name, bean);

            }

            @Override
            public boolean isInScope(String name, FacesContext context) {

                return context.getExternalContext().getRequestMap().containsKey(name);

            }

            @Override
            public Object getFromScope(String name, FacesContext context) {

                return context.getExternalContext().getRequestMap().get(name);

            }

        } // END RequestScopeHandler

        private static class ViewScopeHandler implements ScopeHandler {

            @Override
            public void handle(String name, Object bean, FacesContext context) {

                Map<String, Object> viewMap = context.getViewRoot().getViewMap();

                if (viewMap != null) {
                    viewMap.put(name, bean);
                }
            }

            @Override
            public boolean isInScope(String name, FacesContext context) {

                Map<String, Object> viewMap = context.getViewRoot().getViewMap(false);
                return viewMap != null && viewMap.containsKey(name);

            }

            @Override
            public Object getFromScope(String name, FacesContext context) {

                Map<String, Object> viewMap = context.getViewRoot().getViewMap(false);
                return viewMap != null ? viewMap.get(name) : null;

            }

        } // END ViewScopeHandler

        private static class SessionScopeHandler implements ScopeHandler {

            @Override
            public void handle(String name, Object bean, FacesContext context) {

                synchronized (context.getExternalContext().getSession(true)) {
                    context.getExternalContext().getSessionMap().put(name, bean);
                }

            }

            @Override
            public boolean isInScope(String name, FacesContext context) {

                return context.getExternalContext().getSessionMap().containsKey(name);

            }

            @Override
            public Object getFromScope(String name, FacesContext context) {

                return context.getExternalContext().getSessionMap().get(name);

            }

        } // END SessionScopeHandler

        private static class ApplicationScopeHandler implements ScopeHandler {

            @Override
            public void handle(String name, Object bean, FacesContext context) {

                synchronized (context.getExternalContext().getContext()) {
                    context.getExternalContext().getApplicationMap().put(name, bean);
                }

            }

            @Override
            public boolean isInScope(String name, FacesContext context) {

                return context.getExternalContext().getApplicationMap().containsKey(name);

            }

            @Override
            public Object getFromScope(String name, FacesContext context) {

                return context.getExternalContext().getApplicationMap().get(name);

            }

        } // END ApplicationScopeHandler

        private static class CustomScopeHandler implements ScopeHandler {

            private ValueExpression scope;

            CustomScopeHandler(ValueExpression scope) {
                this.scope = scope;
            }

            @Override
            public void handle(String name, Object bean, FacesContext context) {

                Map scopeMap = (Map) scope.getValue(getELContext(context));

                // IMPLEMENTATION PENDING. I've added this to the Frame doc:

                /**
                 * The runtime must must allow the value of this element to be an EL ValueExpression. If so, and the expression
                 * evaluates to null, an informative error message including the expression string and the name of the bean must be
                 * logged. If the expression evaluates to a Map, that Map is used as the scope into which the bean will be stored. If
                 * storing the bean into the Map causes an Exception, the exception is allowed to flow up to the ExceptionHandler. If
                 * the ValueExpression does not evaluate to a Map, a FacesException must be thrown with a message that includes the
                 * expression string, the toString() of the value, and the type of the value.
                 *
                 */

                if (scopeMap != null) {
                    synchronized (this) {
                        // noinspection unchecked
                        scopeMap.put(name, bean);
                    }
                } else {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.log(Level.WARNING, "jsf.managed.bean.custom.scope.eval.null", new Object[] { scope.getExpressionString() });
                    }
                }
            }

            @Override
            public boolean isInScope(String name, FacesContext context) {

                Map scopeMap = (Map) scope.getValue(getELContext(context));
                if (scopeMap != null) {
                    return scopeMap.containsKey(name);
                } else {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.log(Level.WARNING, "jsf.managed.bean.custom.scope.eval.null.existence", new Object[] { scope.getExpressionString() });
                    }
                    // since the scope evaluated to null, return true to prevent
                    // the managed bean from being needlessly created
                    return true;
                }
            }

            @Override
            public Object getFromScope(String name, FacesContext context) {

                Map scopeMap = (Map) scope.getValue(getELContext(context));
                if (scopeMap != null) {
                    return scopeMap.get(name);
                } else {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.log(Level.WARNING, "jsf.managed.bean.custom.scope.eval.null.existence", new Object[] { scope.getExpressionString() });
                    }
                    return null;
                }
            }

            // ------------------------------------------------- Private Methods

            private ELContext getELContext(FacesContext ctx) {

                return new CustomScopeELContext(ctx.getELContext());

            }

            // -------------------------------------------------- Nested Classes

            /**
             * We have to use a different ELContext when evaluating the expressions for the custom scopes as we don't want to cause
             * the resolved flag on the original ELContext to be changed.
             */
            private static final class CustomScopeELContext extends ELContext {

                private ELContext delegate;

                // ------------------------------------------------ Constructors

                public CustomScopeELContext(ELContext delegate) {

                    this.delegate = delegate;

                }

                // -------------------------------------- Methods from ELContext

                @Override
                public void putContext(Class aClass, Object o) {

                    delegate.putContext(aClass, o);

                }

                @Override
                public Object getContext(Class aClass) {

                    return delegate.getContext(aClass);

                }

                @Override
                public Locale getLocale() {

                    return delegate.getLocale();

                }

                @Override
                public void setLocale(Locale locale) {

                    delegate.setLocale(locale);

                }

                @Override
                public ELResolver getELResolver() {

                    return delegate.getELResolver();

                }

                @Override
                public FunctionMapper getFunctionMapper() {

                    return delegate.getFunctionMapper();

                }

                @Override
                public VariableMapper getVariableMapper() {

                    return delegate.getVariableMapper();

                }
            }

        } // END CustomScopeHandler
    }

}
