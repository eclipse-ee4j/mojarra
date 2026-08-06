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
package org.glassfish.mojarra.config;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.logging.Level.FINE;
import static org.glassfish.mojarra.RIConstants.RI_PREFIX;
import static org.glassfish.mojarra.config.manager.Documents.getProgrammaticDocuments;
import static org.glassfish.mojarra.config.manager.Documents.getXMLDocuments;
import static org.glassfish.mojarra.config.manager.Documents.mergeDocuments;
import static org.glassfish.mojarra.config.manager.Documents.sortDocuments;
import static org.glassfish.mojarra.spi.ConfigurationResourceProviderFactory.createProviders;
import static org.glassfish.mojarra.spi.ConfigurationResourceProviderFactory.ProviderType.FaceletConfig;
import static org.glassfish.mojarra.spi.ConfigurationResourceProviderFactory.ProviderType.FacesConfig;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import jakarta.el.ELContext;
import jakarta.el.ELContextEvent;
import jakarta.el.ELContextListener;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationConfigurationPopulator;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PostConstructApplicationEvent;
import jakarta.servlet.ServletContext;

import org.glassfish.mojarra.cdi.CdiExtension;
import org.glassfish.mojarra.config.configpopulator.MojarraRuntimePopulator;
import org.glassfish.mojarra.config.configprovider.MetaInfFaceletTaglibraryConfigProvider;
import org.glassfish.mojarra.config.configprovider.MetaInfFacesConfigResourceProvider;
import org.glassfish.mojarra.config.configprovider.WebAppFlowConfigResourceProvider;
import org.glassfish.mojarra.config.configprovider.WebFaceletTaglibResourceProvider;
import org.glassfish.mojarra.config.configprovider.WebFacesConfigResourceProvider;
import org.glassfish.mojarra.config.manager.DbfFactory;
import org.glassfish.mojarra.config.manager.FacesConfigInfo;
import org.glassfish.mojarra.config.manager.documents.DocumentInfo;
import org.glassfish.mojarra.config.processor.ApplicationConfigProcessor;
import org.glassfish.mojarra.config.processor.BehaviorConfigProcessor;
import org.glassfish.mojarra.config.processor.ComponentConfigProcessor;
import org.glassfish.mojarra.config.processor.ConfigProcessor;
import org.glassfish.mojarra.config.processor.ConverterConfigProcessor;
import org.glassfish.mojarra.config.processor.FaceletTaglibConfigProcessor;
import org.glassfish.mojarra.config.processor.FacesConfigExtensionProcessor;
import org.glassfish.mojarra.config.processor.FacesFlowDefinitionConfigProcessor;
import org.glassfish.mojarra.config.processor.FactoryConfigProcessor;
import org.glassfish.mojarra.config.processor.LifecycleConfigProcessor;
import org.glassfish.mojarra.config.processor.NavigationConfigProcessor;
import org.glassfish.mojarra.config.processor.ProtectedViewsConfigProcessor;
import org.glassfish.mojarra.config.processor.RenderKitConfigProcessor;
import org.glassfish.mojarra.config.processor.ResourceLibraryContractsConfigProcessor;
import org.glassfish.mojarra.config.processor.ValidatorConfigProcessor;
import org.glassfish.mojarra.el.ELContextImpl;
import org.glassfish.mojarra.spi.ConfigurationResourceProvider;
import org.glassfish.mojarra.spi.ConfigurationResourceProviderFactory;
import org.glassfish.mojarra.spi.HighAvailabilityEnabler;
import org.glassfish.mojarra.spi.InjectionProvider;
import org.glassfish.mojarra.spi.InjectionProviderFactory;
import org.glassfish.mojarra.util.FacesLogger;

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

    private static final String CONFIG_MANAGER_INSTANCE_KEY = RI_PREFIX + "CONFIG_MANAGER_KEY";

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
     * @return the annotated classes discovered during CDI bean discovery
     */
    public static Map<Class<? extends Annotation>, Set<Class<?>>> getAnnotatedClasses(FacesContext ctx) {
        return CDI.current().select(CdiExtension.class).get().getAnnotatedClasses();
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

            try {
                // Validate outside Production, so that a configuration mistake is caught before it ships.
                boolean validating = WebConfiguration.getInstance(servletContext).getProjectStage() != ProjectStage.Production;

                // Obtain and merge the XML and Programmatic documents
                DocumentInfo[] facesDocuments = mergeDocuments(getXMLDocuments(servletContext, getFacesConfigResourceProviders(), validating),
                        getProgrammaticDocuments(getConfigPopulators()));

                FacesConfigInfo lastFacesConfigInfo = new FacesConfigInfo(facesDocuments[facesDocuments.length - 1]);

                facesDocuments = sortDocuments(facesDocuments, lastFacesConfigInfo);

                InjectionProvider containerConnector = InjectionProviderFactory.createInstance(facesContext);
                facesContext.getAttributes().put(INJECTION_PROVIDER_KEY, containerConnector);

                // See if the app is running in a HA enabled env
                if (containerConnector instanceof HighAvailabilityEnabler) {
                    ((HighAvailabilityEnabler) containerConnector).enableHighAvailability(servletContext);
                }

                // Process the ordered and merged documents
                // This invokes a chain or processors where each processor grabs its own elements of interest
                // from each document.

                for (ConfigProcessor configProcessor : configProcessors) {
                    configProcessor.process(servletContext, facesContext, facesDocuments);
                }

                faceletTaglibConfigProcessor.process(servletContext, facesContext,
                    getXMLDocuments(servletContext, getFaceletConfigResourceProviders(), validating));

            } catch (Exception e) {
                // Clear out any configured factories
                releaseFactories();

                Throwable t = e;
                if (!(e instanceof ConfigurationException)) {
                    t = new ConfigurationException("CONFIGURATION FAILED! " + t.getMessage(), t);
                }

                throw (ConfigurationException) t;
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
        configProcessors.forEach(configProcessor -> configProcessor.initializeClassMetadataMap(servletContext, facesContext));
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
