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

package com.sun.faces.config.processor;

import static com.sun.faces.util.Util.getLocaleFromString;
import static java.text.MessageFormat.format;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.ApplicationResourceBundle;
import com.sun.faces.config.ConfigurationException;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.el.ELResolver;
import jakarta.faces.FacesException;
import jakarta.faces.application.Application;
import jakarta.faces.application.ConfigurableNavigationHandler;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.application.StateManager;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.search.SearchExpressionHandler;
import jakarta.faces.component.search.SearchKeywordResolver;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionListener;
import jakarta.faces.event.NamedEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import jakarta.faces.validator.BeanValidator;
import jakarta.servlet.ServletContext;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

/**
 * <p>
 * This <code>ConfigProcessor</code> handles all elements defined under <code>/faces-config/application</code>.
 * </p>
 */
public class ApplicationConfigProcessor extends AbstractConfigProcessor {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    /**
     * <code>/faces-config/application</code>
     */
    private static final String APPLICATION = "application";

    /**
     * <code>/faces-config/application/action-listener</code>
     */
    private static final String ACTION_LISTENER = "action-listener";

    /**
     * <code>/faces-config/application/default-render-kit-id
     */
    private static final String DEFAULT_RENDERKIT_ID = "default-render-kit-id";

    /**
     * <code>/faces-config/application/default-validators</code>
     */
    private static final String DEFAULT_VALIDATORS = "default-validators";

    /**
     * <code>/faces-config/application/default-validators/validator-id</code>
     */
    private static final String VALIDATOR_ID = "validator-id";

    /**
     * <code>/faces-config/application/message-bundle
     */
    private static final String MESSAGE_BUNDLE = "message-bundle";

    /**
     * <code>/faces-config/application/navigation-handler</code>
     */
    private static final String NAVIGATION_HANDLER = "navigation-handler";

    /**
     * <code>/faces-config/application/view-handler</code>
     */
    private static final String VIEW_HANDLER = "view-handler";

    /**
     * <code>/faces-config/application/state-manager</code>
     */
    private static final String STATE_MANAGER = "state-manager";

    /**
     * <code>/faces-config/application/resource-handler</code>
     */
    private static final String RESOURCE_HANDLER = "resource-handler";

    /**
     * <code>/faces-config/application/el-resolver</code>
     */
    private static final String EL_RESOLVER = "el-resolver";

    /**
     * <code>/faces-config/application/search-expression-handler</code>
     */
    private static final String SEARCH_EXPRESSION_HANDLER = "search-expression-handler";

    /**
     * <code>/faces-config/application/search-keyword-resolver</code>
     */
    private static final String SEARCH_KEYWORD_RESOLVER = "search-keyword-resolver";

    /**
     * <code>/faces-config/application/locale-config/default-locale</code>
     */
    private static final String DEFAULT_LOCALE = "default-locale";

    /**
     * <code>/faces-config/application/locale-config/supported-locale</code>
     */
    private static final String SUPPORTED_LOCALE = "supported-locale";

    /**
     * <code>/faces-config/application/resource-bundle</code>
     */
    private static final String RESOURCE_BUNDLE = "resource-bundle";

    /**
     * <code>/faces-config/application/resource-bundle/base-name</code>
     */
    private static final String BASE_NAME = "base-name";

    /**
     * <code>/faces-config/application/resource-bundle/var</code>
     */
    private static final String VAR = "var";

    /**
     * <code>/faces-config/application/resource-bundle/description</code>
     */
    private static final String RES_DESCRIPTIONS = "description";

    /**
     * <code>/faces-config/application/resource-bundle/display-name</code>
     */
    private static final String RES_DISPLAY_NAMES = "display-name";

    /**
     * <code>/faces-config/application/system-event-listener</code>
     */
    private static final String SYSTEM_EVENT_LISTENER = "system-event-listener";

    /**
     * <code>/faces-config/application/system-event-listener/system-event-listener-class</code>
     */
    private static final String SYSTEM_EVENT_LISTENER_CLASS = "system-event-listener-class";

    /**
     * <code>/faces-config/application/system-event-listener/system-event-class</code>
     */
    private static final String SYSTEM_EVENT_CLASS = "system-event-class";

    /**
     * <code>/faces-config/application/system-event-listener/source-class</code>
     */
    private static final String SOURCE_CLASS = "source-class";

    private List<ActionListener> actionListeners = new CopyOnWriteArrayList<>();
    private List<NavigationHandler> navigationHandlers = new CopyOnWriteArrayList<>();
    private List<ViewHandler> viewHandlers = new CopyOnWriteArrayList<>();
    private List<StateManager> stateManagers = new CopyOnWriteArrayList<>();
    private List<ResourceHandler> resourceHandlers = new CopyOnWriteArrayList<>();
    private List<ELResolver> elResolvers = new CopyOnWriteArrayList<>();
    private List<SystemEventListener> systemEventListeners = new CopyOnWriteArrayList<>();
    private List<SearchExpressionHandler> searchExpressionHandlers = new CopyOnWriteArrayList<>();
    private List<SearchKeywordResolver> searchKeywordResolvers = new CopyOnWriteArrayList<>();

    // -------------------------------------------- Methods from ConfigProcessor

    @Override
    public void process(ServletContext servletContext, FacesContext facesContext, DocumentInfo[] documentInfos) throws Exception {
        Application application = getApplication();
        ApplicationAssociate associate = ApplicationAssociate.getInstance(facesContext.getExternalContext());
        LinkedHashMap<String, Node> viewHandlers = new LinkedHashMap<>();
        LinkedHashSet<String> defaultValidatorIds = null;

        for (int i = 0; i < documentInfos.length; i++) {
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, format("Processing application elements for document: ''{0}''", documentInfos[i].getSourceURI()));
            }

            Document document = documentInfos[i].getDocument();
            String namespace = document.getDocumentElement().getNamespaceURI();
            NodeList applicationElements = document.getDocumentElement().getElementsByTagNameNS(namespace, APPLICATION);

            if (applicationElements != null && applicationElements.getLength() > 0) {
                for (int a = 0, asize = applicationElements.getLength(); a < asize; a++) {
                    Node appElement = applicationElements.item(a);
                    NodeList children = ((Element) appElement).getElementsByTagNameNS(namespace, "*");
                    if (children != null && children.getLength() > 0) {
                        for (int c = 0, csize = children.getLength(); c < csize; c++) {
                            Node n = children.item(c);
                            switch (n.getLocalName()) {
                            case MESSAGE_BUNDLE:
                                setMessageBundle(application, n);
                                break;
                            case DEFAULT_RENDERKIT_ID:
                                setDefaultRenderKitId(application, n);
                                break;
                            case ACTION_LISTENER:
                                addActionListener(servletContext, facesContext, application, n);
                                break;
                            case NAVIGATION_HANDLER:
                                setNavigationHandler(servletContext, facesContext, application, n);
                                break;
                            case VIEW_HANDLER:
                                String viewHandler = getNodeText(n);
                                if (viewHandler != null) {
                                    viewHandlers.put(viewHandler, n);
                                }
                                break;
                            case STATE_MANAGER:
                                setStateManager(servletContext, facesContext, application, n);
                                break;
                            case EL_RESOLVER:
                                addELResolver(servletContext, facesContext, associate, n);
                                break;
                            case DEFAULT_LOCALE:
                                setDefaultLocale(application, n);
                                break;
                            case SUPPORTED_LOCALE:
                                addSupportedLocale(application, n);
                                break;
                            case RESOURCE_BUNDLE:
                                addResouceBundle(associate, n);
                                break;
                            case RESOURCE_HANDLER:
                                setResourceHandler(servletContext, facesContext, application, n);
                                break;
                            case SYSTEM_EVENT_LISTENER:
                                addSystemEventListener(servletContext, facesContext, application, n);
                                break;
                            case DEFAULT_VALIDATORS:
                                if (defaultValidatorIds == null) {
                                    defaultValidatorIds = new LinkedHashSet<>();
                                } else {
                                    defaultValidatorIds.clear();
                                }
                                break;
                            case VALIDATOR_ID:
                                defaultValidatorIds.add(getNodeText(n));
                                break;
                            case SEARCH_EXPRESSION_HANDLER:
                                setSearchExpressionHandler(servletContext, facesContext, application, n);
                                break;
                            case SEARCH_KEYWORD_RESOLVER:
                                addSearchKeywordResolver(servletContext, facesContext, application, n);
                                break;
                            }
                        }
                    }
                }
            }
        }

        registerDefaultValidatorIds(facesContext, application, defaultValidatorIds);

        // perform any special processing for ViewHandlers...
        processViewHandlers(servletContext, facesContext, application, viewHandlers);

        // process NamedEvent annotations, if any
        processAnnotations(facesContext, NamedEvent.class);
    }

    @Override
    public void destroy(ServletContext sc, FacesContext facesContext) {
        destroyInstances(sc, facesContext, actionListeners);
        destroyInstances(sc, facesContext, navigationHandlers);
        destroyInstances(sc, facesContext, stateManagers);
        destroyInstances(sc, facesContext, viewHandlers);
        destroyInstances(sc, facesContext, elResolvers);
        destroyInstances(sc, facesContext, resourceHandlers);
        destroyInstances(sc, facesContext, systemEventListeners);
        destroyInstances(sc, facesContext, searchExpressionHandlers);
        destroyInstances(sc, facesContext, searchKeywordResolvers);
    }

    private void destroyInstances(ServletContext sc, FacesContext facesContext, List<?> instances) {
        for (Object instance : instances) {
            destroyInstance(sc, facesContext, instance.getClass().getName(), instance);
        }

        instances.clear();
    }

    // --------------------------------------------------------- Private Methods

    /**
     * If defaultValidatorIds is null, then no &lt;default-validators&gt; element appeared in any configuration file. In
     * that case, add jakarta.faces.Bean if Bean Validation is available. If the &lt;default-validators&gt; appeared at
     * least once, don't add the default (and empty &lt;default-validator&gt; element disabled default validators)
     */
    private void registerDefaultValidatorIds(FacesContext facesContext, Application application, LinkedHashSet<String> defaultValidatorIds) {
        if (defaultValidatorIds == null) {
            defaultValidatorIds = new LinkedHashSet<>();
            if (isBeanValidatorAvailable(facesContext)) {
                WebConfiguration webConfig = WebConfiguration.getInstance();
                if (!webConfig.isOptionEnabled(WebConfiguration.BooleanWebContextInitParameter.DisableDefaultBeanValidator)) {
                    defaultValidatorIds.add(BeanValidator.VALIDATOR_ID);
                }
            }
        }

        for (String validatorId : defaultValidatorIds) {
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, format("Calling Application.addDefaultValidatorId({0})", validatorId));
            }

            application.addDefaultValidatorId(validatorId);
        }
    }

    static boolean isBeanValidatorAvailable(FacesContext facesContext) {
        boolean result = false;
        final String beansValidationAvailabilityCacheKey = "jakarta.faces.BEANS_VALIDATION_AVAILABLE";
        Map<String, Object> appMap = facesContext.getExternalContext().getApplicationMap();

        if (appMap.containsKey(beansValidationAvailabilityCacheKey)) {
            result = (Boolean) appMap.get(beansValidationAvailabilityCacheKey);
        } else {
            try {
                Thread.currentThread().getContextClassLoader().loadClass("jakarta.validation.MessageInterpolator");

                // Check if the Implementation is available.
                Object cachedObject = appMap.get(BeanValidator.VALIDATOR_FACTORY_KEY);
                if (cachedObject instanceof ValidatorFactory) {
                    result = true;
                } else {
                    Context initialContext = null;
                    try {
                        initialContext = new InitialContext();
                    } catch (NoClassDefFoundError nde) {
                        // On google app engine InitialContext is forbidden to use and GAE throws
                        // NoClassDefFoundError
                        LOGGER.log(FINE, nde, nde::toString);
                    } catch (NamingException ne) {
                        LOGGER.log(WARNING, ne, ne::toString);
                    }

                    try {
                        Object validatorFactory = initialContext.lookup("java:comp/ValidatorFactory");
                        if (validatorFactory != null) {
                            appMap.put(BeanValidator.VALIDATOR_FACTORY_KEY, validatorFactory);
                            result = true;
                        }
                    } catch (NamingException root) {
                        LOGGER.fine(() -> "Could not build a default Bean Validator factory: " + root.getMessage());
                    }

                    if (!result) {
                        try {
                            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
                            factory.getValidator();
                            appMap.put(BeanValidator.VALIDATOR_FACTORY_KEY, factory);
                            result = true;
                        } catch (Throwable throwable) {
                        }
                    }
                }

            } catch (Throwable t) { // CNFE or ValidationException or any other
                LOGGER.fine("Unable to load Beans Validation");
            }

            appMap.put(beansValidationAvailabilityCacheKey, result);
        }

        return result;
    }

    private void setMessageBundle(Application application, Node messageBundle) {
        if (messageBundle != null) {
            String bundle = getNodeText(messageBundle);
            if (bundle != null) {

                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, format("Calling Application.setMessageBundle({0})", bundle));
                }
                application.setMessageBundle(bundle);
            }
        }
    }

    private void setDefaultRenderKitId(Application application, Node defaultId) {
        if (defaultId != null) {
            String id = getNodeText(defaultId);
            if (id != null) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, format("Calling Application.setDefaultRenderKitId({0})", id));
                }
                application.setDefaultRenderKitId(id);
            }
        }
    }

    private void addActionListener(ServletContext servletContext, FacesContext facesContext, Application application, Node actionListener) {
        if (actionListener != null) {

            String listener = getNodeText(actionListener);
            if (listener != null) {
                boolean[] didPerformInjection = { false };
                ActionListener instance = (ActionListener) createInstance(servletContext, facesContext, listener, ActionListener.class, application.getActionListener(),
                        actionListener, true, didPerformInjection);

                if (instance != null) {
                    if (didPerformInjection[0]) {
                        actionListeners.add(instance);
                    }

                    if (LOGGER.isLoggable(FINE)) {
                        LOGGER.log(FINE, format("Calling Application.setActionListeners({0})", listener));
                    }

                    application.setActionListener(instance);
                }
            }
        }
    }

    private void setNavigationHandler(ServletContext servletContext, FacesContext facesContext, Application application, Node navigationHandler) {
        if (navigationHandler != null) {

            String handler = getNodeText(navigationHandler);
            if (handler != null) {
                Class<?> rootType = findRootType(servletContext, facesContext, handler, navigationHandler,
                        new Class<?>[] { ConfigurableNavigationHandler.class, NavigationHandler.class });
                boolean[] didPerformInjection = { false };
                NavigationHandler instance = (NavigationHandler) createInstance(servletContext, facesContext, handler,
                        rootType != null ? rootType : NavigationHandler.class, application.getNavigationHandler(), navigationHandler, true,
                        didPerformInjection);

                if (instance != null) {
                    if (didPerformInjection[0]) {
                        navigationHandlers.add(instance);
                    }

                    if (LOGGER.isLoggable(FINE)) {
                        LOGGER.log(FINE, format("Calling Application.setNavigationHandlers({0})", handler));
                    }

                    application.setNavigationHandler(instance);
                }
            }
        }

    }

    private void setStateManager(ServletContext servletContext, FacesContext facesContext, Application application, Node stateManager) {
        if (stateManager != null) {

            String manager = getNodeText(stateManager);
            if (manager != null) {
                boolean[] didPerformInjection = { false };
                StateManager instance = (StateManager) createInstance(servletContext, facesContext, manager, StateManager.class, application.getStateManager(),
                        stateManager, true, didPerformInjection);
                if (instance != null) {
                    if (didPerformInjection[0]) {
                        stateManagers.add(instance);
                    }
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, format("Calling Application.setStateManagers({0})", manager));
                    }

                    application.setStateManager(instance);
                }
            }
        }
    }

    private void setViewHandler(ServletContext servletContext, FacesContext facesContext, Application application, Node viewHandler) {
        if (viewHandler != null) {

            String handler = getNodeText(viewHandler);
            if (handler != null) {
                boolean[] didPerformInjection = { false };
                ViewHandler instance = (ViewHandler) createInstance(servletContext, facesContext, handler, ViewHandler.class, application.getViewHandler(), viewHandler,
                        true, didPerformInjection);

                if (instance != null) {
                    if (didPerformInjection[0]) {
                        viewHandlers.add(instance);
                    }

                    if (LOGGER.isLoggable(FINE)) {
                        LOGGER.log(FINE, format("Calling Application.setViewHandler({0})", handler));
                    }

                    application.setViewHandler(instance);
                }
            }
        }

    }

    private void addELResolver(ServletContext servletContext, FacesContext facesContext, ApplicationAssociate associate, Node elResolver) {
        if (elResolver != null) {
            if (associate != null) {
                List<ELResolver> resolvers = associate.getELResolversFromFacesConfig();
                if (resolvers == null) {
                    resolvers = new ArrayList<>();
                    associate.setELResolversFromFacesConfig(resolvers);
                }

                String elResolverClass = getNodeText(elResolver);
                if (elResolverClass != null) {
                    boolean[] didPerformInjection = { false };
                    ELResolver elRes = (ELResolver) createInstance(servletContext, facesContext, elResolverClass, ELResolver.class, null, elResolver, true,
                            didPerformInjection);
                    if (elRes != null) {
                        if (didPerformInjection[0]) {
                            elResolvers.add(elRes);
                        }

                        if (LOGGER.isLoggable(FINE)) {
                            LOGGER.log(FINE, format("Adding ''{0}'' to ELResolver chain", elResolverClass));
                        }

                        resolvers.add(elRes);
                    }
                }
            }
        }
    }

    private void setSearchExpressionHandler(ServletContext sc, FacesContext facesContext, Application application, Node searchExpressionHandler) {
        if (searchExpressionHandler != null) {

            String handler = getNodeText(searchExpressionHandler);
            if (handler != null) {
                Class<?> rootType = findRootType(sc, facesContext, handler, searchExpressionHandler, new Class<?>[] { SearchExpressionHandler.class });
                boolean[] didPerformInjection = { false };

                SearchExpressionHandler instance = (SearchExpressionHandler) createInstance(sc, facesContext, handler,
                        rootType != null ? rootType : SearchExpressionHandler.class, application.getSearchExpressionHandler(), searchExpressionHandler, true,
                        didPerformInjection);

                if (instance != null) {
                    if (didPerformInjection[0]) {
                        searchExpressionHandlers.add(instance);
                    }
                    if (LOGGER.isLoggable(FINE)) {
                        LOGGER.log(FINE, format("Calling Application.setSearchExpressionHandler({0})", handler));
                    }

                    application.setSearchExpressionHandler(instance);
                }
            }
        }
    }

    private void addSearchKeywordResolver(ServletContext sc, FacesContext facesContext, Application application, Node searchKeywordResolver) {
        if (searchKeywordResolver != null) {

            String searchKeywordResolverClass = getNodeText(searchKeywordResolver);
            if (searchKeywordResolverClass != null) {
                boolean[] didPerformInjection = { false };

                SearchKeywordResolver keywordResolver = (SearchKeywordResolver) createInstance(sc, facesContext, searchKeywordResolverClass,
                        SearchKeywordResolver.class, null, searchKeywordResolver, true, didPerformInjection);

                if (keywordResolver != null) {
                    if (didPerformInjection[0]) {
                        searchKeywordResolvers.add(keywordResolver);
                    }
                    LOGGER.log(FINE, () -> format("Adding ''{0}'' to SearchKeywordResolver chain", searchKeywordResolverClass));

                    application.addSearchKeywordResolver(keywordResolver);
                }
            }
        }
    }

    private void setDefaultLocale(Application application, Node defaultLocale) {
        if (defaultLocale != null) {
            String defLocale = getNodeText(defaultLocale);
            if (defLocale != null) {
                Locale def = getLocaleFromString(defLocale);
                if (def != null) {
                    LOGGER.log(FINE, () -> format("Setting default Locale to ''{0}''", defLocale));
                    application.setDefaultLocale(def);
                }
            }
        }
    }

    private void addSupportedLocale(Application application, Node supportedLocale) {

        if (supportedLocale != null) {
            Set<Locale> sLocales = getCurrentLocales(application);
            String locString = getNodeText(supportedLocale);
            if (locString != null) {
                Locale loc = Util.getLocaleFromString(locString);
                if (loc != null) {
                    LOGGER.log(Level.FINE, () -> format("Adding supported Locale ''{0}''", locString));
                    sLocales.add(loc);
                }
                application.setSupportedLocales(sLocales);
            }
        }
    }

    private void addResouceBundle(ApplicationAssociate associate, Node resourceBundle) {
        if (resourceBundle != null) {

            NodeList children = resourceBundle.getChildNodes();
            if (children != null) {
                String baseName = null;
                String var = null;
                List<Node> descriptions = null;
                List<Node> displayNames = null;
                for (int i = 0, size = children.getLength(); i < size; i++) {
                    Node n = children.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        switch (n.getLocalName()) {
                        case BASE_NAME:
                            baseName = getNodeText(n);
                            break;
                        case VAR:
                            var = getNodeText(n);
                            break;
                        case RES_DESCRIPTIONS:
                            if (descriptions == null) {
                                descriptions = new ArrayList<>(2);
                            }
                            descriptions.add(n);
                            break;
                        case RES_DISPLAY_NAMES:
                            if (displayNames == null) {
                                displayNames = new ArrayList<>(2);
                            }
                            displayNames.add(n);
                            break;
                        }
                    }
                }
                if (baseName != null && var != null) {
                    associate.addResourceBundle(var, new ApplicationResourceBundle(baseName, getTextMap(displayNames), getTextMap(descriptions)));
                }
            }
        }
    }

    private Set<Locale> getCurrentLocales(Application application) {
        Set<Locale> supportedLocales = new HashSet<>();
        for (Iterator<Locale> i = application.getSupportedLocales(); i.hasNext();) {
            supportedLocales.add(i.next());
        }

        return supportedLocales;
    }

    private void setResourceHandler(ServletContext servletContext, FacesContext facesContext, Application application, Node resourceHandler) {
        if (resourceHandler != null) {

            String handler = getNodeText(resourceHandler);
            if (handler != null) {
                boolean[] didPerformInjection = { false };

                ResourceHandler instance = (ResourceHandler) createInstance(servletContext, facesContext, handler, ResourceHandler.class, application.getResourceHandler(),
                        resourceHandler, true, didPerformInjection);

                if (instance != null) {
                    if (didPerformInjection[0]) {
                        resourceHandlers.add(instance);
                    }
                    if (LOGGER.isLoggable(FINE)) {
                        LOGGER.log(FINE, format("Calling Application.setResourceHandler({0})", handler));
                    }

                    application.setResourceHandler(instance);
                }
            }
        }
    }

    private void addSystemEventListener(ServletContext servletContext, FacesContext facesContext, Application application, Node systemEventListener) {
        NodeList children = systemEventListener.getChildNodes();
        String listenerClass = null;
        String eventClass = null;
        String sourceClass = null;
        for (int j = 0, len = children.getLength(); j < len; j++) {
            Node n = children.item(j);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getLocalName()) {
                case SYSTEM_EVENT_LISTENER_CLASS:
                    listenerClass = getNodeText(n);
                    break;
                case SYSTEM_EVENT_CLASS:
                    eventClass = getNodeText(n);
                    break;
                case SOURCE_CLASS:
                    sourceClass = getNodeText(n);
                    break;
                }
            }
        }

        if (listenerClass != null) {
            SystemEventListener systemEventListenerInstance = (SystemEventListener) createInstance(servletContext, facesContext, listenerClass, SystemEventListener.class, null, systemEventListener);
            if (systemEventListenerInstance != null) {
                systemEventListeners.add(systemEventListenerInstance);
                try {
                    // If there is an eventClass, use it, otherwise use
                    // SystemEvent.class
                    // noinspection unchecked
                    Class<? extends SystemEvent> eventClazz;

                    if (eventClass != null) {
                        eventClazz = (Class<? extends SystemEvent>) loadClass(servletContext, facesContext, eventClass, this, null);
                    } else {
                        eventClazz = SystemEvent.class;
                    }

                    // If there is a sourceClass, use it, otherwise use null
                    Class<?> sourceClazz = sourceClass != null && sourceClass.length() != 0 ? Util.loadClass(sourceClass, this.getClass()) : null;
                    application.subscribeToEvent(eventClazz, sourceClazz, systemEventListenerInstance);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "Subscribing for event {0} and source {1} using listener {2}", new Object[] { eventClazz.getName(),
                                sourceClazz != null ? sourceClazz.getName() : "ANY", systemEventListenerInstance.getClass().getName() });
                    }
                } catch (ClassNotFoundException cnfe) {
                    throw new ConfigurationException(cnfe);
                }
            }
        }
    }

    private void processViewHandlers(ServletContext servletContext, FacesContext facesContext, Application application, LinkedHashMap<String, Node> viewHandlers) {
        if (viewHandlers.containsKey("com.sun.facelets.FaceletViewHandler")) {
            LOGGER.log(SEVERE, "faces.application.legacy_facelet_viewhandler_detected", "com.sun.facelets.FaceletViewHandler");
            throw new FacesException("Use of com.sun.facelets.FaceletViewHandler is no longer supported");
        }

        for (Node viewHandlerNode : viewHandlers.values()) {
            setViewHandler(servletContext, facesContext, application, viewHandlerNode);
        }
    }

}
