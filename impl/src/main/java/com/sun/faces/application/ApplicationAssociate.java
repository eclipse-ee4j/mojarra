/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

import static com.sun.faces.RIConstants.FACES_CONFIG_VERSION;
import static com.sun.faces.RIConstants.FACES_PREFIX;
import static com.sun.faces.el.ELUtils.buildFacesResolver;
import static com.sun.faces.el.FacesCompositeELResolver.ELResolverChainType.Faces;
import static com.sun.faces.facelets.util.ReflectionUtil.forName;
import static com.sun.faces.util.MessageUtils.APPLICATION_ASSOCIATE_EXISTS_ID;
import static com.sun.faces.util.MessageUtils.getExceptionMessageString;
import static com.sun.faces.util.Util.getFacesConfigXmlVersion;
import static com.sun.faces.util.Util.getFacesServletRegistration;
import static jakarta.faces.FactoryFinder.FACELET_CACHE_FACTORY;
import static jakarta.faces.FactoryFinder.FLOW_HANDLER_FACTORY;
import static jakarta.faces.application.ProjectStage.Development;
import static jakarta.faces.application.ViewVisitOption.RETURN_AS_MINIMAL_IMPLICIT_OUTCOME;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.sun.faces.RIConstants;
import com.sun.faces.application.annotation.AnnotationManager;
import com.sun.faces.application.annotation.FacesComponentUsage;
import com.sun.faces.application.resource.ResourceCache;
import com.sun.faces.application.resource.ResourceManager;
import com.sun.faces.component.search.SearchExpressionHandlerImpl;
import com.sun.faces.config.ConfigManager;
import com.sun.faces.el.DemuxCompositeELResolver;
import com.sun.faces.facelets.compiler.Compiler;
import com.sun.faces.facelets.compiler.SAXCompiler;
import com.sun.faces.facelets.impl.DefaultFaceletFactory;
import com.sun.faces.facelets.impl.DefaultResourceResolver;
import com.sun.faces.facelets.tag.composite.CompositeLibrary;
import com.sun.faces.facelets.tag.faces.PassThroughAttributeLibrary;
import com.sun.faces.facelets.tag.faces.PassThroughElementLibrary;
import com.sun.faces.facelets.tag.faces.core.CoreLibrary;
import com.sun.faces.facelets.tag.faces.html.HtmlLibrary;
import com.sun.faces.facelets.tag.jstl.core.JstlCoreLibrary;
import com.sun.faces.facelets.tag.jstl.fn.JstlFunction;
import com.sun.faces.facelets.tag.ui.UILibrary;
import com.sun.faces.facelets.util.DevTools;
import com.sun.faces.facelets.util.FunctionLibrary;
import com.sun.faces.spi.InjectionProvider;
import com.sun.faces.util.FacesLogger;

import jakarta.el.CompositeELResolver;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.annotation.FacesConfig.ContextParam;
import jakarta.faces.application.Application;
import jakarta.faces.application.NavigationCase;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.search.SearchExpressionHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PostConstructApplicationEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import jakarta.faces.flow.FlowHandler;
import jakarta.faces.flow.FlowHandlerFactory;
import jakarta.faces.view.facelets.FaceletCache;
import jakarta.faces.view.facelets.FaceletCacheFactory;
import jakarta.faces.view.facelets.TagDecorator;
import jakarta.servlet.ServletContext;

/**
 * <p>
 * Break out the things that are associated with the Application, but need to be present even when the user has replaced
 * the Application instance.
 * </p>
 *
 * <p>
 * For example: the user replaces ApplicationFactory, and wants to intercept calls to createValueExpression() and
 * createMethodExpression() for certain kinds of expressions, but allow the existing application to handle the rest.
 * </p>
 */
public class ApplicationAssociate {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    private final static String FacesComponentJcpNamespace = "http://xmlns.jcp.org/jsf/component";

    private final ApplicationImpl applicationImpl;

    /**
     * Overall Map containing <code>from-view-id</code> key and <code>Set</code> of <code>NavigationCase</code> objects for
     * that key; The <code>from-view-id</code> strings in this map will be stored as specified in the configuration file -
     * some of them will have a trailing asterisk "*" signifying wild card, and some may be specified as an asterisk "*".
     */
    private final Map<String, Set<NavigationCase>> navigationMap;

    /*
     * The FacesComponentTagLibrary uses the information in this map to help it fabricate tag handlers for components
     * annotated with FacesComponent. Key: namespace
     */
    private Map<String, List<FacesComponentUsage>> facesComponentsByNamespace;

    // Flag indicating that a response has been rendered.
    private boolean responseRendered;

    private static final String ASSOCIATE_KEY = RIConstants.FACES_PREFIX + "ApplicationAssociate";

    private static final ThreadLocal<ApplicationAssociate> instance = ThreadLocal.withInitial(() -> null);

    private List<ELResolver> elResolversFromFacesConfig;
    private ExpressionFactory expressionFactory;

    private final InjectionProvider injectionProvider;
    private ResourceCache resourceCache;

    private String contextName;
    private boolean requestServiced;
    private boolean errorPagePresent;

    private final AnnotationManager annotationManager;
    private final boolean devModeEnabled;
    private Compiler compiler;
    private DefaultFaceletFactory faceletFactory;
    private ResourceManager resourceManager;
    private final ApplicationStateInfo applicationStateInfo;

    private final PropertyEditorHelper propertyEditorHelper;

    private final NamedEventManager namedEventManager;

    private FlowHandler flowHandler;

    private SearchExpressionHandler searchExpressionHandler;

    private final Map<String, String> definingDocumentIdsToTruncatedJarUrls;

    private final long timeOfInstantiation;

    private Map<String, List<String>> resourceLibraryContracts;

    Map<String, ApplicationResourceBundle> resourceBundles = new HashMap<>();


    public static void setCurrentInstance(ApplicationAssociate associate) {
        if (associate == null) {
            instance.remove();
        } else {
            instance.set(associate);
        }
    }

    public static ApplicationAssociate getCurrentInstance() {
        ApplicationAssociate associate = instance.get();
        if (associate == null) {
            // Fallback to ExternalContext lookup
            return getInstance();
        }

        return associate;
    }

    public static ApplicationAssociate getInstance() {
        return getInstance(FacesContext.getCurrentInstance());
    }

    public static ApplicationAssociate getInstance(FacesContext facesContext) {
        if (facesContext == null) {
            return null;
        }

        return ApplicationAssociate.getInstance(facesContext.getExternalContext());
    }

    public static ApplicationAssociate getInstance(ExternalContext externalContext) {
        if (externalContext == null) {
            return null;
        }

        return (ApplicationAssociate) externalContext.getApplicationMap().get(ASSOCIATE_KEY);
    }

    public static ApplicationAssociate getInstance(ServletContext context) {
        if (context == null) {
            return null;
        }

        return (ApplicationAssociate) context.getAttribute(ASSOCIATE_KEY);
    }



    public ApplicationAssociate(ApplicationImpl appImpl) {
        applicationImpl = appImpl;

        propertyEditorHelper = new PropertyEditorHelper(appImpl);

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            throw new IllegalStateException("ApplicationAssociate ctor not called in same callstack as ConfigureListener.contextInitialized()");
        }

        ExternalContext externalContext = facesContext.getExternalContext();
        if (externalContext.getApplicationMap().get(ASSOCIATE_KEY) != null) {
            throw new IllegalStateException(getExceptionMessageString(APPLICATION_ASSOCIATE_EXISTS_ID));
        }

        Map<String, Object> applicationMap = externalContext.getApplicationMap();
        applicationMap.put(ASSOCIATE_KEY, this);

        navigationMap = new ConcurrentHashMap<>();
        injectionProvider = (InjectionProvider) facesContext.getAttributes().get(ConfigManager.INJECTION_PROVIDER_KEY);

        annotationManager = new AnnotationManager();

        devModeEnabled = appImpl.getProjectStage() == Development;

        if (!devModeEnabled) {
            resourceCache = new ResourceCache();
        }

        resourceManager = new ResourceManager(applicationMap, resourceCache);
        namedEventManager = new NamedEventManager();
        applicationStateInfo = new ApplicationStateInfo();

        appImpl.subscribeToEvent(PostConstructApplicationEvent.class, Application.class, new PostConstructApplicationListener());

        definingDocumentIdsToTruncatedJarUrls = new ConcurrentHashMap<>();
        timeOfInstantiation = System.currentTimeMillis();
    }

    public Application getApplication() {
        return applicationImpl;
    }

    public void setResourceLibraryContracts(Map<String, List<String>> map) {
        resourceLibraryContracts = map;
    }

    private class PostConstructApplicationListener implements SystemEventListener {

        @Override
        public boolean isListenerForSource(Object source) {
            return source instanceof Application;
        }

        @Override
        public void processEvent(SystemEvent event) {
            initializeFacelets();

            if (flowHandler == null) {
                FlowHandlerFactory flowHandlerFactory = (FlowHandlerFactory) FactoryFinder.getFactory(FLOW_HANDLER_FACTORY);
                flowHandler = flowHandlerFactory.createFlowHandler(FacesContext.getCurrentInstance());
            }

            if (searchExpressionHandler == null) {
                searchExpressionHandler = new SearchExpressionHandlerImpl();
            }

            FacesContext context = FacesContext.getCurrentInstance();

            try {
                new JavaFlowLoaderHelper().loadFlows(context, flowHandler);
            } catch (IOException ex) {
                LOGGER.log(SEVERE, null, ex);
            }

            // cause the Facelet VDL to be instantiated eagerly, so it can
            // become aware of the resource library contracts

            ViewHandler viewHandler = context.getApplication().getViewHandler();

            // FindBugs: ignore the return value, this is just to get the
            // ctor called at this time.
            viewHandler.getViewDeclarationLanguage(context, FACES_PREFIX + "xhtml");

            String facesConfigVersion = getFacesConfigXmlVersion(context);
            context.getExternalContext().getApplicationMap().put(FACES_CONFIG_VERSION, facesConfigVersion);

            if (ContextParam.AUTOMATIC_EXTENSIONLESS_MAPPING.isSet(context)) {
                getFacesServletRegistration(context)
                    .ifPresent(registration ->
                        viewHandler.getViews(context, "/", RETURN_AS_MINIMAL_IMPLICIT_OUTCOME)
                                   .forEach(view -> registration.addMapping(view)));
            }

        }

    }

    public void initializeFacelets() {
        if (compiler != null) {
            return;
        }

        FacesContext ctx = FacesContext.getCurrentInstance();

        compiler = createCompiler(ctx);
        faceletFactory = createFaceletFactory(ctx, compiler);
    }

    public long getTimeOfInstantiation() {
        return timeOfInstantiation;
    }

    public ApplicationStateInfo getApplicationStateInfo() {
        return applicationStateInfo;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    // Return the resource library contracts and mappings from the
    // application configuration resources
    public Map<String, List<String>> getResourceLibraryContracts() {
        return resourceLibraryContracts;
    }

    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public ResourceCache getResourceCache() {
        return resourceCache;
    }

    public AnnotationManager getAnnotationManager() {
        return annotationManager;
    }

    public Compiler getCompiler() {
        if (compiler == null) {
            initializeFacelets();
        }

        return compiler;
    }

    public boolean isErrorPagePresent() {
        return errorPagePresent;
    }

    public void setErrorPagePresent(boolean errorPagePresent) {
        this.errorPagePresent = errorPagePresent;
    }

    public DefaultFaceletFactory getFaceletFactory() {
        return faceletFactory;
    }

    public static void clearInstance(ExternalContext externalContext) {
        Map<String, Object> applicationMap = externalContext.getApplicationMap();
        ApplicationAssociate me = (ApplicationAssociate) applicationMap.get(ASSOCIATE_KEY);

        if (me != null && me.resourceBundles != null) {
            me.resourceBundles.clear();
        }

        applicationMap.remove(ASSOCIATE_KEY);
    }

    public static void clearInstance(ServletContext servletContext) {
        ApplicationAssociate me = (ApplicationAssociate) servletContext.getAttribute(ASSOCIATE_KEY);

        if (me != null && me.resourceBundles != null) {
            me.resourceBundles.clear();
        }

        servletContext.removeAttribute(ASSOCIATE_KEY);
    }

    public void initializeELResolverChains() {
        // 1. initialize the chains with default values
        if (applicationImpl.getCompositeELResolver() == null) {
            applicationImpl.setCompositeELResolver(new DemuxCompositeELResolver(Faces));
            buildFacesResolver(applicationImpl.getCompositeELResolver(), this);
        }
    }

    public boolean isDevModeEnabled() {
        return devModeEnabled;
    }

    /**
     * Obtain the PropertyEditorHelper instance for this app.
     *
     * @return The PropertyEditorHeler instance for this app.
     */
    public PropertyEditorHelper getPropertyEditorHelper() {
        return propertyEditorHelper;
    }

    public FlowHandler getFlowHandler() {
        return flowHandler;
    }

    public void setFlowHandler(FlowHandler flowHandler) {
        this.flowHandler = flowHandler;
    }

    public SearchExpressionHandler getSearchExpressionHandler() {
        return searchExpressionHandler;
    }

    public void setSearchExpressionHandler(SearchExpressionHandler searchExpressionHandler) {
        this.searchExpressionHandler = searchExpressionHandler;
    }

    public void setELResolversFromFacesConfig(List<ELResolver> resolvers) {
        elResolversFromFacesConfig = resolvers;
    }

    public List<ELResolver> getELResolversFromFacesConfig() {
        return elResolversFromFacesConfig;
    }

    public void setExpressionFactory(ExpressionFactory expressionFactory) {
        this.expressionFactory = expressionFactory;
    }

    public ExpressionFactory getExpressionFactory() {
        return expressionFactory;
    }

    public CompositeELResolver getApplicationELResolvers() {
        return applicationImpl.getApplicationELResolvers();
    }

    public InjectionProvider getInjectionProvider() {
        return injectionProvider;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public String getContextName() {
        return contextName;
    }

    /**
     * Called by application code to indicate we've processed the first request to the application.
     */
    public void setRequestServiced() {
        requestServiced = true;
    }

    /**
     * @return <code>true</code> if we've processed a request, otherwise <code>false</code>
     */
    public boolean hasRequestBeenServiced() {
        return requestServiced;
    }

    public void addFacesComponent(FacesComponentUsage facesComponentUsage) {
        if (facesComponentsByNamespace == null) {
            facesComponentsByNamespace = new HashMap<>();
        }

        facesComponentsByNamespace.computeIfAbsent(facesComponentUsage.getAnnotation().namespace(), k -> new ArrayList<>()).add(facesComponentUsage);
        facesComponentsByNamespace.computeIfAbsent(FacesComponentJcpNamespace, k -> new ArrayList<>()).add(facesComponentUsage);
    }

    public List<FacesComponentUsage> getComponentsForNamespace(String namespace) {
        if (facesComponentsByNamespace != null && facesComponentsByNamespace.containsKey(namespace)) {
            return facesComponentsByNamespace.get(namespace);
        }

        return emptyList();
    }

    /**
     * Add a navigation case to the internal case set. If a case set does not already exist in the case list map containing
     * this case (identified by <code>from-view-id</code>), start a new list, add the case to it, and store the set in the
     * case set map. If a case set already exists, overwrite the previous case.
     *
     * @param navigationCase the navigation case containing navigation mapping information from the configuration file.
     */
    public void addNavigationCase(NavigationCase navigationCase) {

        // If there already is a case existing for the fromviewid/fromaction.fromoutcome
        // combination,
        // replace it ... (last one wins).
        navigationMap.computeIfAbsent(navigationCase.getFromViewId(), k -> new LinkedHashSet<>()).add(navigationCase);
    }

    public NamedEventManager getNamedEventManager() {
        return namedEventManager;
    }

    /**
     * Return a <code>Map</code> of navigation mappings loaded from the configuration system. The key for the returned
     * <code>Map</code> is <code>from-view-id</code>, and the value is a <code>List</code> of navigation cases.
     *
     * @return Map the map of navigation mappings.
     */
    public Map<String, Set<NavigationCase>> getNavigationCaseListMappings() {
        if (navigationMap == null) {
            return emptyMap();
        }

        return navigationMap;
    }

    public ResourceBundle getResourceBundle(FacesContext context, String var) {
        ApplicationResourceBundle bundle = resourceBundles.get(var);

        if (bundle == null) {
            return null;
        }

        // Start out with the default locale
        Locale defaultLocale = Locale.getDefault();
        Locale locale = defaultLocale;

        // See if this FacesContext has a ViewRoot
        UIViewRoot root = context.getViewRoot();
        if (root != null) {
            locale = root.getLocale();
            if (locale == null) {
                // If the ViewRoot has no Locale, fall back to the default.
                locale = defaultLocale;
            }
        }

        return bundle.getResourceBundle(locale);
    }

    /**
     * keys: element from faces-config
     * <p>
     *
     * values: ResourceBundleBean instances.
     *
     * @param var the variable name
     * @param bundle the application resource bundle
     */

    public void addResourceBundle(String var, ApplicationResourceBundle bundle) {
        resourceBundles.put(var, bundle);
    }

    public Map<String, ApplicationResourceBundle> getResourceBundles() {
        return resourceBundles;
    }

    // This is called by ViewHandlerImpl.renderView().
    public void responseRendered() {
        responseRendered = true;
    }

    public boolean isResponseRendered() {
        return responseRendered;
    }

    public boolean urlIsRelatedToDefiningDocumentInJar(URL candidateUrl, String definingDocumentId) {
        boolean result = false;
        String match = definingDocumentIdsToTruncatedJarUrls.get(definingDocumentId);
        if (match != null) {
            String candidate = candidateUrl.toExternalForm();
            if (candidate != null) {
                int i = candidate.lastIndexOf("/META-INF");
                if (i == -1) {
                    throw new FacesException("Invalid url for application configuration resources file with respect to faces flows");
                }
                candidate = candidate.substring(0, i);
                result = candidate.equals(match);
            }
        }

        return result;
    }

    public void relateUrlToDefiningDocumentInJar(URL url, String definingDocumentId) {
        String candidate = url.toExternalForm();
        int i = candidate.lastIndexOf("/META-INF");
        if (i == -1) {
            return;
        }
        candidate = candidate.substring(0, i);

        definingDocumentIdsToTruncatedJarUrls.put(definingDocumentId, candidate);
    }

    protected DefaultFaceletFactory createFaceletFactory(FacesContext context, Compiler compiler) {

        // refresh period
        int period = ContextParam.FACELETS_REFRESH_PERIOD.getValue(context);

        // resource resolver
        DefaultResourceResolver resolver = new DefaultResourceResolver(applicationImpl.getResourceHandler());

        FaceletCacheFactory cacheFactory = (FaceletCacheFactory) FactoryFinder.getFactory(FACELET_CACHE_FACTORY);
        FaceletCache<?> cache = cacheFactory.getFaceletCache();

        DefaultFaceletFactory toReturn = new DefaultFaceletFactory();
        toReturn.init(context, compiler, resolver, period, cache);

        return toReturn;
    }

    protected Compiler createCompiler(FacesContext context) {
        Compiler newCompiler = new SAXCompiler();

        loadDecorators(context, newCompiler);

        // Skip params?
        newCompiler.setTrimmingComments(ContextParam.FACELETS_SKIP_COMMENTS.isSet(context));

        addTagLibraries(newCompiler);

        return newCompiler;
    }

    protected void loadDecorators(FacesContext context, Compiler newCompiler) {
        String[] decorators = ContextParam.FACELETS_DECORATORS.getValue(context);

        for (String decorator : decorators) {
            try {
                newCompiler
                        .addTagDecorator((TagDecorator) forName(decorator).getDeclaredConstructor().newInstance());

                if (LOGGER.isLoggable(FINE)) {
                    LOGGER.log(FINE, "Successfully Loaded Decorator: {0}", decorator);
                }
            } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException e) {
                if (LOGGER.isLoggable(SEVERE)) {
                    LOGGER.log(SEVERE, "Error Loading Decorator: " + decorator, e);
                }
            }
        }
    }

    protected void addTagLibraries(Compiler newCompiler) {
        CoreLibrary.NAMESPACES.forEach(namespace -> newCompiler.addTagLibrary(new CoreLibrary(namespace)));
        HtmlLibrary.NAMESPACES.forEach(namespace -> newCompiler.addTagLibrary(new HtmlLibrary(namespace)));
        UILibrary.NAMESPACES.forEach(namespace -> newCompiler.addTagLibrary(new UILibrary(namespace)));
        JstlCoreLibrary.NAMESPACES.forEach(namespace -> newCompiler.addTagLibrary(new JstlCoreLibrary(namespace)));
        PassThroughAttributeLibrary.NAMESPACES.forEach(namespace -> newCompiler.addTagLibrary(new PassThroughAttributeLibrary(namespace)));
        PassThroughElementLibrary.NAMESPACES.forEach(namespace -> newCompiler.addTagLibrary(new PassThroughElementLibrary(namespace)));
        FunctionLibrary.NAMESPACES.forEach(namespace -> newCompiler.addTagLibrary(new FunctionLibrary(JstlFunction.class, namespace)));

        if (isDevModeEnabled()) {
            DevTools.NAMESPACES.forEach(namespace -> newCompiler.addTagLibrary(new FunctionLibrary(DevTools.class, namespace)));
        }

        CompositeLibrary.NAMESPACES.forEach(namespace -> newCompiler.addTagLibrary(new CompositeLibrary(namespace)));
    }

}
