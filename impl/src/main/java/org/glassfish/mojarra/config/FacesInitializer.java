/*
 * Copyright (c) 2021, 2022 Contributors to Eclipse Foundation.
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

import static java.util.logging.Level.WARNING;
import static org.glassfish.mojarra.RIConstants.FACES_SERVLET_MAPPINGS;
import static org.glassfish.mojarra.RIConstants.FACES_SERVLET_REGISTRATION;
import static org.glassfish.mojarra.util.Util.getExistingFacesServletRegistration;
import static org.glassfish.mojarra.util.Util.isEmpty;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.annotation.FacesConfig;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.behavior.FacesBehavior;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.event.NamedEvent;
import jakarta.faces.flow.FlowScoped;
import jakarta.faces.lifecycle.ClientWindowScoped;
import jakarta.faces.model.FacesDataModel;
import jakarta.faces.push.Push;
import jakarta.faces.render.FacesBehaviorRenderer;
import jakarta.faces.render.FacesRenderer;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.annotation.HandlesTypes;
import jakarta.websocket.server.ServerContainer;

import org.glassfish.mojarra.context.FacesContextParam;
import org.glassfish.mojarra.util.FacesLogger;

/**
 * Initializes Jakarta Faces if at least one of the following conditions is met:
 *
 * <ul>
 * <li>The <code>Set</code> of classes passed to this initializer contains a user-defined Faces type, or</li>
 * <li><code>FacesServlet</code> has been explicitly mapped, or</li>
 * <li><code>/WEB-INF/faces-config.xml</code> exists</li>
 * </ul>
 *
 * If it is met, and the <code>FacesServlet</code> has not been explicitly mapped,
 * then add mappings <em>*.xhtml</em>, <em>/faces</em>, <em>*.jsf</em>, and <em>*.faces</em> for the FacesServlet.
 *
 * <p>
 * The {@link HandlesTypes @HandlesTypes} annotation lists the Faces annotation types whose presence in the application
 * indicates that Jakarta Faces should be initialized. The actual registration of annotated classes with the Faces
 * runtime is handled by CDI via {@link org.glassfish.mojarra.cdi.CdiExtension}; these types are only used here as a boolean
 * check to auto-detect Faces applications. The list contains:
 *
 * <ul>
 * <li>Faces configuration annotations ({@link FacesBehavior}, {@link FacesBehaviorRenderer}, {@link FacesComponent},
 * {@link FacesConverter}, {@link FacesDataModel}, {@link FacesRenderer}, {@link FacesValidator}, {@link NamedEvent})
 * &mdash; the primary indicators that an application defines Faces artifacts.</li>
 * <li>{@link FacesConfig} &mdash; the annotation-based alternative to <code>faces-config.xml</code> for activating
 * Faces.</li>
 * <li>Faces CDI scopes and qualifiers ({@link ViewScoped}, {@link FlowScoped}, {@link ClientWindowScoped},
 * {@link Push}) &mdash; these may be present in applications that use Faces CDI integration without any of the above
 * configuration annotations or <code>faces-config.xml</code>.</li>
 * </ul>
 *
 * <p>
 * Notably excluded are Faces API interfaces and abstract classes (such as <code>UIComponent</code>,
 * <code>Converter</code>, <code>Validator</code>, <code>Renderer</code>, <code>DataModel</code>, and
 * <code>PhaseListener</code>) as well as non-configuration annotations (such as <code>ListenerFor</code>,
 * <code>ResourceDependency</code>, <code>FlowDefinition</code>, and <code>FlowBuilderParameter</code>). In previous
 * versions these were included for two reasons: to broaden Faces auto-detection, and to collect annotated classes for
 * registration via class scanning. Since Faces 5.0, annotation discovery is handled entirely by CDI, so the collected
 * classes are no longer used for registration. As for auto-detection, any application that implements these interfaces
 * or uses these annotations will in practice also have at least one of the types listed above, a
 * <code>faces-config.xml</code>, or an explicit <code>FacesServlet</code> mapping.
 */
@HandlesTypes({
    ClientWindowScoped.class, FacesBehavior.class, FacesBehaviorRenderer.class, FacesComponent.class, FacesConfig.class,
    FacesConverter.class, FacesDataModel.class, FacesRenderer.class, FacesValidator.class, FlowScoped.class,
    NamedEvent.class, Push.class, ViewScoped.class,
})
public class FacesInitializer implements ServletContainerInitializer {

    // NOTE: Logging should not be used with this class.
    // NOTE: It can, we only need to ensure that the logger is only invoked when there's a (Init)FacesContext.
    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    public static final String FACES_PACKAGE_PREFIX = "jakarta.faces.";
    public static final String MOJARRA_PACKAGE_PREFIX = "org.glassfish.mojarra.";
    public static final String MOJARRA_TEST_PACKAGE_PREFIX = "org.glassfish.mojarra.test.";

    private static final String FACES_CONFIG_RESOURCE_PATH = "/WEB-INF/faces-config.xml";
    private static final String FACES_SERVLET_CLASS_NAME = FacesServlet.class.getName();

    // -------------------------------- Methods from ServletContainerInitializer

    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {
        boolean appHasFacesContent = !isEmpty(classes) || isFacesConfigFilePresent(servletContext);
        boolean appHasFacesServlet = isFacesServletRegistrationPresent(servletContext);

        if (appHasFacesContent || appHasFacesServlet) {
            InitFacesContext initFacesContext = new InitFacesContext(servletContext);

            try {
                handleMappingConcerns(servletContext, initFacesContext);
                handleCdiConcerns(servletContext);
                handleWebSocketConcerns(servletContext, initFacesContext);

                // The Configure listener will do the bulk of initializing (configuring) Faces in a later phase.
                servletContext.addListener(ConfigureListener.class);
            }
            finally {
                initFacesContext.release();
            }
        }
    }

    // --------------------------------------------------------- Private Methods

    private static boolean isFacesConfigFilePresent(ServletContext context) {
        try {
            return context.getResource(FACES_CONFIG_RESOURCE_PATH) != null;
        }
        catch (Exception ignore) {
            return false;
        }
    }

    private static boolean isFacesServletRegistrationPresent(ServletContext context) {
        return getExistingFacesServletRegistration(context) != null;
    }

    private static void handleMappingConcerns(ServletContext servletContext, FacesContext facesContext) throws ServletException {
        ServletRegistration existingFacesServletRegistration = getExistingFacesServletRegistration(servletContext);

        if (existingFacesServletRegistration != null) {
            // FacesServlet has already been defined, so we're not going to add additional mappings.
            servletContext.setAttribute(FACES_SERVLET_REGISTRATION, existingFacesServletRegistration);
            return;
        }

        ServletRegistration newFacesServletRegistration = servletContext.addServlet(FacesServlet.class.getSimpleName(), FACES_SERVLET_CLASS_NAME);
        servletContext.setAttribute(FACES_SERVLET_MAPPINGS, newFacesServletRegistration.getMappings());
        servletContext.setAttribute(FACES_SERVLET_REGISTRATION, newFacesServletRegistration);
    }

    private static void handleCdiConcerns(ServletContext context) throws ServletException {
        // If Weld is used as CDI impl then make sure it doesn't skip initialization when there's no BDA. See also #5232 and #5321

        if (context.getAttribute("org.jboss.weld.environment.servlet.jakarta.enterprise.inject.spi.BeanManager") instanceof BeanManager) {
            // Already initialized.
            return;
        }

        ClassLoader cl = context.getClassLoader();
        Class<?> weldInitializerClass;

        try {
            weldInitializerClass = cl.loadClass("org.jboss.weld.environment.servlet.EnhancedListener");
        }
        catch (ClassNotFoundException ignore) {
            // Weld is actually not being used. That's OK for now, so just continue as usual.
            return;
        }

        // Weld is being used so let's make sure it doesn't skip initialization when there's no BDA.
        context.setInitParameter("org.jboss.weld.environment.servlet.archive.isolation", "false");

        if (context.getAttribute("org.jboss.weld.environment.servlet.enhancedListenerUsed") == Boolean.TRUE) {
            try {
                LOGGER.log(WARNING, "Weld skipped initialization - forcing it to reinitialize");
                ServletContainerInitializer weldInitializer = (ServletContainerInitializer) weldInitializerClass.getConstructor().newInstance();
                weldInitializer.onStartup(null, context);
            }
            catch (Exception | LinkageError e) {
                // Weld is being used but it failed for unclear reason while CDI should be enabled. That's not OK, so rethrow as ServletException.
                throw new ServletException("Reinitializing Weld failed - giving up, please make sure your project contains at least one bean class with a bean defining annotation and retry", e);
            }
        }
    }

    private static void handleWebSocketConcerns(ServletContext context, FacesContext facesContext) throws ServletException {
        if (context.getAttribute(ServerContainer.class.getName()) != null) {
            // Already initialized
            return;
        }

        if (!FacesContextParam.ENABLE_WEBSOCKET_ENDPOINT.isSet(facesContext)) {
            // Register websocket endpoint is not enabled
            return;
        }

        // Below work around is specific for Tyrus websocket impl.
        // As Mojarra is to be designed as a container-provided JAR (not an user-provided JAR),
        // the WebsocketEndpoint needs to be programmatically added in a ServletContextListener (in Mojarra's case the ConfigureListener),
        // but at that point, servletContext.getAttribute(ServerContainer.class.getName()) returns null when Tyrus is used (Payara/GlassFish).
        // Upon inspection it turns out that the TyrusServletContainerInitializer#onStartup() immediately returns when there are no user-provided endpoints
        // and doesn't register the ServerContainer anymore, causing it to not be placed in ServletContext anymore.
        // The below work around will thus manually take care of this.

        ClassLoader cl = context.getClassLoader();
        Class<?> tyrusInitializerClass;

        try {
            tyrusInitializerClass = cl.loadClass("org.glassfish.tyrus.servlet.TyrusServletContainerInitializer");
        }
        catch (ClassNotFoundException ignore) {
            // Tyrus is actually not being used (all other impls known so far do not skip ServerContainer). That's OK for now, so just continue as usual.
            return;
        }

        try {
            ServletContainerInitializer tyrusInitializer = (ServletContainerInitializer) tyrusInitializerClass.getDeclaredConstructor().newInstance();
            Class<?> tyrusConfigClass = cl.loadClass("org.glassfish.tyrus.server.TyrusServerConfiguration");

            // Set of classes must be mutable and contain the TyrusServerConfiguration class.
            Set<Class<?>> handledTypes = new HashSet<>();
            handledTypes.add(tyrusConfigClass);
            tyrusInitializer.onStartup(handledTypes, context);
        }
        catch (Exception e) {
            // Tyrus is being used but it failed for unclear reason while websocket endpoint should be enabled. That's not OK, so rethrow as ServletException.
            throw new ServletException(e);
        }

    }

}
