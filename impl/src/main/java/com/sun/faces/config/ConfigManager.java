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
package com.sun.faces.config;

import static com.sun.faces.RIConstants.FACES_PREFIX;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableThreading;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.ValidateFacesConfigFiles;
import static com.sun.faces.config.manager.Documents.getProgrammaticDocuments;
import static com.sun.faces.config.manager.Documents.getXMLDocuments;
import static com.sun.faces.config.manager.Documents.mergeDocuments;
import static com.sun.faces.config.manager.Documents.sortDocuments;
import static com.sun.faces.spi.ConfigurationResourceProviderFactory.ProviderType.FaceletConfig;
import static com.sun.faces.spi.ConfigurationResourceProviderFactory.ProviderType.FacesConfig;
import static com.sun.faces.spi.ConfigurationResourceProviderFactory.createProviders;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.logging.Level.FINE;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sun.faces.config.configpopulator.MojarraRuntimePopulator;
import com.sun.faces.config.configprovider.MetaInfFaceletTaglibraryConfigProvider;
import com.sun.faces.config.configprovider.MetaInfFacesConfigResourceProvider;
import com.sun.faces.config.configprovider.WebAppFlowConfigResourceProvider;
import com.sun.faces.config.configprovider.WebFaceletTaglibResourceProvider;
import com.sun.faces.config.configprovider.WebFacesConfigResourceProvider;
import com.sun.faces.config.manager.DbfFactory;
import com.sun.faces.config.manager.FacesConfigInfo;
import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.config.manager.tasks.FindAnnotatedConfigClasses;
import com.sun.faces.config.manager.tasks.ProvideMetadataToAnnotationScanTask;
import com.sun.faces.config.processor.ApplicationConfigProcessor;
import com.sun.faces.config.processor.BehaviorConfigProcessor;
import com.sun.faces.config.processor.ComponentConfigProcessor;
import com.sun.faces.config.processor.ConfigProcessor;
import com.sun.faces.config.processor.ConverterConfigProcessor;
import com.sun.faces.config.processor.FaceletTaglibConfigProcessor;
import com.sun.faces.config.processor.FacesConfigExtensionProcessor;
import com.sun.faces.config.processor.FacesFlowDefinitionConfigProcessor;
import com.sun.faces.config.processor.FactoryConfigProcessor;
import com.sun.faces.config.processor.LifecycleConfigProcessor;
import com.sun.faces.config.processor.NavigationConfigProcessor;
import com.sun.faces.config.processor.ProtectedViewsConfigProcessor;
import com.sun.faces.config.processor.RenderKitConfigProcessor;
import com.sun.faces.config.processor.ResourceLibraryContractsConfigProcessor;
import com.sun.faces.config.processor.ValidatorConfigProcessor;
import com.sun.faces.el.ELContextImpl;
import com.sun.faces.spi.ConfigurationResourceProvider;
import com.sun.faces.spi.ConfigurationResourceProviderFactory;
import com.sun.faces.spi.HighAvailabilityEnabler;
import com.sun.faces.spi.InjectionProvider;
import com.sun.faces.spi.InjectionProviderFactory;
import com.sun.faces.spi.ThreadContext;
import com.sun.faces.util.FacesLogger;

import jakarta.el.ELContext;
import jakarta.el.ELContextEvent;
import jakarta.el.ELContextListener;
import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationConfigurationPopulator;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PostConstructApplicationEvent;
import jakarta.servlet.ServletContext;

/**
 * <p>
 * This class manages the initialization of each web application that uses Faces.
 * </p>
 */
public class ConfigManager {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    /**
     * The initialization time FacesContext scoped key under which the InjectionProvider is stored.
     */
    public static final String INJECTION_PROVIDER_KEY = ConfigManager.class.getName() + "_INJECTION_PROVIDER_TASK";

    /**
     * <p>
     * The <code>ConfigManager</code> will multithread the calls to the <code>ConfigurationResourceProvider</code>s as well
     * as any calls to parse a resources into a DOM. By default, we'll use only 5 threads per web application.
     * </p>
     */
    private static final int NUMBER_OF_TASK_THREADS = 5;

    private static final String CONFIG_MANAGER_INSTANCE_KEY = FACES_PREFIX + "CONFIG_MANAGER_KEY";

    /**
     * The application-scoped key under which the Future responsible for annotation scanning is associated with.
     */
    private static final String ANNOTATIONS_SCAN_TASK_KEY = ConfigManager.class.getName() + "_ANNOTATION_SCAN_TASK";

    /**
     * <p>
     * Contains each <code>ServletContext</code> that we've initialized. The <code>ServletContext</code> will be removed
     * when the application is destroyed.
     * </p>
     */
    private final List<ServletContext> initializedContexts = new CopyOnWriteArrayList<>();

    private final List<ConfigProcessor> configProcessors = List.of(
                new FactoryConfigProcessor(),
                new LifecycleConfigProcessor(),
                new ApplicationConfigProcessor(),
                new ComponentConfigProcessor(),
                new ConverterConfigProcessor(),
                new ValidatorConfigProcessor(),
                new RenderKitConfigProcessor(),
                new NavigationConfigProcessor(),
                new BehaviorConfigProcessor(),
                new FacesConfigExtensionProcessor(),
                new ProtectedViewsConfigProcessor(),
                new FacesFlowDefinitionConfigProcessor(),
                new ResourceLibraryContractsConfigProcessor());

    /**
     * <p>
     * A List of resource providers that search for faces-config documents. By default, this contains a provider for the
     * Mojarra, and two other providers to satisfy the requirements of the specification.
     * </p>
     */
    private final List<ConfigurationResourceProvider> facesConfigProviders = List.of(
            new MetaInfFacesConfigResourceProvider(), new WebAppFlowConfigResourceProvider(), new WebFacesConfigResourceProvider());

    /**
     * <p>
     * A List of resource providers that search for faces-config documents. By default, this contains a provider for the
     * Mojarra, and one other providers to satisfy the requirements of the specification.
     * </p>
     */
    private final List<ConfigurationResourceProvider> facesletsTagLibConfigProviders = List.of(
            new MetaInfFaceletTaglibraryConfigProvider(), new WebFaceletTaglibResourceProvider());

    /**
     * <p>
     * The chain of {@link ConfigProcessor} instances to processing of facelet-taglib documents.
     * </p>
     */
    private final ConfigProcessor faceletTaglibConfigProcessor = new FaceletTaglibConfigProcessor();

    // ---------------------------------------------------------- Public STATIC Methods

    public static ConfigManager createInstance(ServletContext servletContext) {
        ConfigManager result = new ConfigManager();
        servletContext.setAttribute(CONFIG_MANAGER_INSTANCE_KEY, result);
        return result;
    }

    /**
     * @param servletContext the involved servlet context
     * @return a <code>ConfigManager</code> instance
     */
    public static ConfigManager getInstance(ServletContext servletContext) {
        return (ConfigManager) servletContext.getAttribute(CONFIG_MANAGER_INSTANCE_KEY);
    }

    /**
     * @param ctx the involved faces context
     * @return the results of the annotation scan task
     */
    public static Map<Class<? extends Annotation>, Set<Class<?>>> getAnnotatedClasses(FacesContext ctx) {
        Map<String, Object> appMap = ctx.getExternalContext().getApplicationMap();

        @SuppressWarnings("unchecked")
        Future<Map<Class<? extends Annotation>, Set<Class<?>>>> scanTask = (Future<Map<Class<? extends Annotation>, Set<Class<?>>>>) appMap
                .get(ANNOTATIONS_SCAN_TASK_KEY);

        try {
            return scanTask != null ? scanTask.get() : emptyMap();
        } catch (InterruptedException | ExecutionException e) {
            throw new FacesException(e);
        }

    }

    public static void removeInstance(ServletContext servletContext) {
        servletContext.removeAttribute(CONFIG_MANAGER_INSTANCE_KEY);
    }

    // ---------------------------------------------------------- Public instance Methods

    /**
     * <p>
     * This method bootstraps Faces based on the parsed configuration resources.
     * </p>
     *
     * @param servletContext the <code>ServletContext</code> for the application that requires initialization
     * @param facesContext the involved initialization faces context
     */
    public void initialize(ServletContext servletContext, InitFacesContext facesContext) {
        if (!hasBeenInitialized(servletContext)) {

            initializedContexts.add(servletContext);
            initializeConfigProcessors(servletContext, facesContext);
            ExecutorService executor = null;

            try {
                WebConfiguration webConfig = WebConfiguration.getInstance(servletContext);
                boolean validating = webConfig.isOptionEnabled(ValidateFacesConfigFiles);

                if (useThreads(servletContext)) {
                    executor = createExecutorService();
                }

                // Obtain and merge the XML and Programmatic documents
                DocumentInfo[] facesDocuments = mergeDocuments(getXMLDocuments(servletContext, getFacesConfigResourceProviders(), executor, validating),
                        getProgrammaticDocuments(getConfigPopulators()));

                FacesConfigInfo lastFacesConfigInfo = new FacesConfigInfo(facesDocuments[facesDocuments.length - 1]);

                facesDocuments = sortDocuments(facesDocuments, lastFacesConfigInfo);

                InjectionProvider containerConnector = InjectionProviderFactory.createInstance(facesContext.getExternalContext());
                facesContext.getAttributes().put(INJECTION_PROVIDER_KEY, containerConnector);

                if (!lastFacesConfigInfo.isWebInfFacesConfig() || !lastFacesConfigInfo.isMetadataComplete()) {
                    findAnnotations(facesDocuments, containerConnector, servletContext, facesContext, executor);
                }

                // See if the app is running in a HA enabled env
                if (containerConnector instanceof HighAvailabilityEnabler) {
                    ((HighAvailabilityEnabler) containerConnector).enableHighAvailability(servletContext);
                }

                // Process the ordered and merged documents
                // This invokes a chain or processors where each processor grabs its own elements of interest
                // from each document.

                DocumentInfo[] facesDocuments2 = facesDocuments;
                configProcessors.subList(0, 3).stream().forEach(e -> {
                    try {
                        e.process(servletContext, facesContext, facesDocuments2);
                    } catch (Exception e2) {
                        // TODO Auto-generated catch block
                        e2.printStackTrace();
                    }
                });

                long parentThreadId = Thread.currentThread().getId();
                ClassLoader parentContextClassLoader = Thread.currentThread().getContextClassLoader();

                ThreadContext threadContext = getThreadContext(containerConnector);
                Object parentWebContext = threadContext != null ? threadContext.getParentWebContext() : null;

                configProcessors.subList(3, configProcessors.size()).stream().forEach(e -> {

                    long currentThreadId = Thread.currentThread().getId();

                    InitFacesContext initFacesContext = null;
                    if (currentThreadId != parentThreadId) {
                        Thread.currentThread().setContextClassLoader(parentContextClassLoader);
                        initFacesContext = InitFacesContext.getInstance(servletContext);
                        if (parentWebContext != null) {
                            threadContext.propagateWebContextToChild(parentWebContext);
                        }

                    } else {
                        initFacesContext = facesContext;
                    }

                    try {
                        e.process(servletContext, initFacesContext, facesDocuments2);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } finally {
                        if (currentThreadId != parentThreadId) {
                            Thread.currentThread().setContextClassLoader(null);
                            if (parentWebContext != null) {
                                threadContext.clearChildContext();
                            }
                        }

                    }
                });

                faceletTaglibConfigProcessor.process(servletContext, facesContext,
                    getXMLDocuments(servletContext, getFaceletConfigResourceProviders(), executor, validating));

            } catch (Exception e) {
                // Clear out any configured factories
                releaseFactories();

                Throwable t = e;
                if (!(e instanceof ConfigurationException)) {
                    t = new ConfigurationException("CONFIGURATION FAILED! " + t.getMessage(), t);
                }

                throw (ConfigurationException) t;
            } finally {
                if (executor != null) {
                    executor.shutdown();
                }
                servletContext.removeAttribute(ANNOTATIONS_SCAN_TASK_KEY);
            }
        }

        DbfFactory.removeSchemaMap(servletContext);
    }

    /**
     * @param servletContext the <code>ServletContext</code> for the application in question
     * @return <code>true</code> if this application has already been initialized, otherwise returns <code>fase</code>
     */
    public boolean hasBeenInitialized(ServletContext servletContext) {
        return initializedContexts.contains(servletContext);
    }

    // --------------------------------------------------------- Private Methods

    /**
     * Execute the Task responsible for finding annotation classes
     *
     */
    private void findAnnotations(DocumentInfo[] facesDocuments, InjectionProvider containerConnector, ServletContext servletContext, InitFacesContext context, ExecutorService executor) {
        ProvideMetadataToAnnotationScanTask taskMetadata = new ProvideMetadataToAnnotationScanTask(facesDocuments, containerConnector);

        Future<Map<Class<? extends Annotation>, Set<Class<?>>>> annotationScan;

        if (executor != null) {
            annotationScan = executor.submit(new FindAnnotatedConfigClasses(servletContext, context, taskMetadata));
        } else {
            annotationScan = new FutureTask<>(new FindAnnotatedConfigClasses(servletContext, context, taskMetadata));
            ((FutureTask<Map<Class<? extends Annotation>, Set<Class<?>>>>) annotationScan).run();
        }

        pushTaskToContext(servletContext, annotationScan);
    }

    /**
     * Push the provided <code>Future</code> to the specified <code>ServletContext</code>.
     */
    private void pushTaskToContext(ServletContext servletContext, Future<Map<Class<? extends Annotation>, Set<Class<?>>>> scanTask) {
        servletContext.setAttribute(ANNOTATIONS_SCAN_TASK_KEY, scanTask);
    }

    private boolean useThreads(ServletContext ctx) {
        return WebConfiguration.getInstance(ctx).isOptionEnabled(EnableThreading);
    }

    private List<ConfigurationResourceProvider> getFacesConfigResourceProviders() {
        return getConfigurationResourceProviders(facesConfigProviders, FacesConfig);
    }

    private List<ConfigurationResourceProvider> getFaceletConfigResourceProviders() {
        return getConfigurationResourceProviders(facesletsTagLibConfigProviders, FaceletConfig);
    }

    private List<ConfigurationResourceProvider> getConfigurationResourceProviders(List<ConfigurationResourceProvider> defaultProviders, ConfigurationResourceProviderFactory.ProviderType providerType) {
        ConfigurationResourceProvider[] customProviders = createProviders(providerType);
        if (customProviders.length == 0) {
            return defaultProviders;
        }

        List<ConfigurationResourceProvider> providers = new ArrayList<>(defaultProviders);

        // Insert the custom providers after the META-INF providers and
        // before those that scan /WEB-INF
        providers.addAll(defaultProviders.size() - 1, asList(customProviders));

        return unmodifiableList(providers);
    }

    private void initializeConfigProcessors(ServletContext servletContext, FacesContext facesContext) {
        configProcessors.stream().parallel().forEach(e -> e.initializeClassMetadataMap(servletContext, facesContext));
    }

    private List<ApplicationConfigurationPopulator> getConfigPopulators() {
        List<ApplicationConfigurationPopulator> configPopulators = new ArrayList<>();

        configPopulators.add(new MojarraRuntimePopulator());

        ServiceLoader.load(ApplicationConfigurationPopulator.class).forEach(configPopulators::add);

        return configPopulators;
    }

    /**
     * Publishes a {@link jakarta.faces.event.PostConstructApplicationEvent} event for the current {@link Application}
     * instance.
     */
    void publishPostConfigEvent() {
        InitFacesContext facesContext = (InitFacesContext) FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();

        if ( facesContext.getELContext() == null) {
            ELContext elContext = new ELContextImpl(facesContext);

            ELContextListener[] listeners = application.getELContextListeners();
            if (listeners.length > 0) {
                ELContextEvent event = new ELContextEvent(elContext);
                for (ELContextListener listener : listeners) {
                    listener.contextCreated(event);
                }
            }

            facesContext.setELContext(elContext);
        }

        application.publishEvent(facesContext, PostConstructApplicationEvent.class, Application.class, application);
    }

    /**
     * Create a new <code>ExecutorService</code> with {@link #NUMBER_OF_TASK_THREADS} threads.
     */
    private static ExecutorService createExecutorService() {
        int tc = Runtime.getRuntime().availableProcessors();
        if (tc > NUMBER_OF_TASK_THREADS) {
            tc = NUMBER_OF_TASK_THREADS;
        }

        try {
            return (ExecutorService) new InitialContext().lookup("java:comp/env/concurrent/ThreadPool");
        } catch (NamingException e) {
            // Ignore
        }

        return Executors.newFixedThreadPool(tc);
    }

    private ThreadContext getThreadContext(InjectionProvider containerConnector) {
        if (containerConnector instanceof ThreadContext) {
            return (ThreadContext) containerConnector;
        }

        return null;
    }

    /**
     * Calls through to {@link jakarta.faces.FactoryFinder#releaseFactories()} ignoring any exceptions.
     */
    private void releaseFactories() {
        try {
            FactoryFinder.releaseFactories();
        } catch (FacesException ignored) {
            LOGGER.log(FINE, "Exception thrown from FactoryFinder.releaseFactories()", ignored);
        }
    }

    /**
     * This method will remove any information about the application.
     *
     * @param facesContext the <code>FacesContext</code> for the application that needs to be removed
     * @param servletContext the <code>ServletContext</code> for the application that needs to be removed
     */
    public void destroy(ServletContext servletContext, FacesContext facesContext) {
        configProcessors.forEach( processor -> processor.destroy(servletContext, facesContext) );
        initializedContexts.remove(servletContext);
    }

}
