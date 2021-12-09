/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

import static com.sun.faces.RIConstants.HANDLED_CLASSES;
import static com.sun.faces.RIConstants.FACES_INITIALIZER_MAPPINGS_ADDED;
import static com.sun.faces.RIConstants.FACES_SERVLET_MAPPINGS;
import static com.sun.faces.RIConstants.FACES_SERVLET_REGISTRATION;
import static com.sun.faces.util.Util.isEmpty;
import static java.lang.Boolean.TRUE;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sun.faces.cdi.CdiExtension;

import jakarta.annotation.Resource;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.annotation.FacesConfig;
import jakarta.faces.application.ResourceDependencies;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.FacesBehavior;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.event.ListenerFor;
import jakarta.faces.event.ListenersFor;
import jakarta.faces.event.NamedEvent;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.render.FacesBehaviorRenderer;
import jakarta.faces.render.Renderer;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.annotation.HandlesTypes;
import jakarta.websocket.Endpoint;
import jakarta.websocket.server.ServerApplicationConfig;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpoint;

/**
 * Adds mappings <em>*.xhtml</em>, <em>/faces</em>, <em>*.jsf</em>, and <em>*.faces</em> for the FacesServlet (if it
 * hasn't already been mapped) if the following conditions are met:
 *
 * <ul>
 * <li>The <code>Set</code> of classes passed to this initializer is not empty, or</li>
 * <li>/WEB-INF/faces-config.xml exists, or</li>
 * <li>A CDI enabled bean with qualifier FacesConfig can be obtained</li>
 * </ul>
 */
@HandlesTypes({ Converter.class, Endpoint.class, FacesBehavior.class, FacesBehaviorRenderer.class, FacesComponent.class,
        FacesConverter.class, FacesConfig.class, // Should actually be check for enabled bean, but difficult to guarantee, see SERVLET_SPEC-79
        FacesValidator.class, ListenerFor.class, ListenersFor.class, NamedEvent.class, PhaseListener.class, Renderer.class, Resource.class,
        ResourceDependencies.class, ResourceDependency.class, ServerApplicationConfig.class, ServerEndpoint.class, UIComponent.class, Validator.class })
public class FacesInitializer implements ServletContainerInitializer {

    // NOTE: Loggins should not be used with this class.

    private static final String FACES_SERVLET_CLASS = FacesServlet.class.getName();

    // -------------------------------- Methods from ServletContainerInitializer

    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {

        Set<Class<?>> handledClasses = new HashSet<>();
        if (classes != null) {
            handledClasses.addAll(classes);
        }
        servletContext.setAttribute(HANDLED_CLASSES, handledClasses);

        boolean appHasSomeFacesContent = appMayHaveSomeFacesContent(classes, servletContext);
        boolean appHasFacesServlet = getExistingFacesServletRegistration(servletContext) != null;

        if (appHasSomeFacesContent || appHasFacesServlet) {
            InitFacesContext initFacesContext = new InitFacesContext(servletContext);
            try {
                if (appHasSomeFacesContent) {
                    // Only look at mapping concerns if there is Faces content
                    handleMappingConcerns(servletContext);
                }
                // Other concerns also handled if there is an existing Faces Servlet mapping

                // The Configure listener will do the bulk of initializing (configuring) Faces in a later phase.
                servletContext.addListener(ConfigureListener.class);
                handleWebSocketConcerns(servletContext);
            } finally {
                // Bug 20458755: The InitFacesContext was not being cleaned up, resulting in
                // a partially constructed FacesContext being made available
                // to other code that re-uses this Thread at init time.
                initFacesContext.releaseCurrentInstance();
                initFacesContext.release();
            }
        }
    }

    // --------------------------------------------------------- Private Methods

    private boolean appMayHaveSomeFacesContent(Set<Class<?>> classes, ServletContext context) {
        if (!isEmpty(classes)) {
            return true;
        }

        // No Faces specific classes found, check for a WEB-INF/faces-config.xml
        try {
            if (context.getResource("/WEB-INF/faces-config.xml") != null) {
                return true;
            }
        } catch (MalformedURLException mue) {

        }

        // In the future remove FacesConfig.class from the @HandlesTypes annotation
        // and only check via CDI
        try {
            CDI<Object> cdi = null;
            try {
                cdi = CDI.current();

                if (cdi != null) {
                    Instance<CdiExtension> extension = cdi.select(CdiExtension.class);
                    if (extension.isResolvable()) {
                        return extension.get().isAddBeansForFacesImplicitObjects();
                    }
                }

            } catch (IllegalStateException e) {
                // Ignore, CDI not active for this module
            }

        } catch (Exception e) {
            // Any other exception; Ignore too
        }

        return false;
    }

    private void handleMappingConcerns(ServletContext servletContext) throws ServletException {
        ServletRegistration existingFacesServletRegistration = getExistingFacesServletRegistration(servletContext);
        if (existingFacesServletRegistration != null) {
            // FacesServlet has already been defined, so we're not going to add additional mappings;

            servletContext.setAttribute(FACES_SERVLET_REGISTRATION, existingFacesServletRegistration);
            return;
        }

        ServletRegistration newFacesServletRegistration = servletContext.addServlet("FacesServlet", "jakarta.faces.webapp.FacesServlet");

        if ("true".equalsIgnoreCase(servletContext.getInitParameter("jakarta.faces.DISABLE_FACESSERVLET_TO_XHTML"))) {
            newFacesServletRegistration.addMapping("/faces/*", "*.jsf", "*.faces");
        } else {
            newFacesServletRegistration.addMapping("/faces/*", "*.jsf", "*.faces", "*.xhtml");
        }

        servletContext.setAttribute(FACES_INITIALIZER_MAPPINGS_ADDED, TRUE);
        servletContext.setAttribute(FACES_SERVLET_MAPPINGS, newFacesServletRegistration.getMappings());
        servletContext.setAttribute(FACES_SERVLET_REGISTRATION, newFacesServletRegistration);
    }

    private ServletRegistration getExistingFacesServletRegistration(ServletContext servletContext) {
        Map<String, ? extends ServletRegistration> existing = servletContext.getServletRegistrations();
        for (ServletRegistration registration : existing.values()) {
            if (FACES_SERVLET_CLASS.equals(registration.getClassName())) {
                return registration;
            }
        }

        return null;
    }

    private void handleWebSocketConcerns(ServletContext ctx) throws ServletException {
        if (ctx.getAttribute(ServerContainer.class.getName()) != null) {
            // Already initialized
            return;
        }

        if (!Boolean.valueOf(ctx.getInitParameter("jakarta.faces.ENABLE_WEBSOCKET_ENDPOINT"))) {
            // Register websocket endpoint is not enabled
            return;
        }

        ClassLoader cl = ctx.getClassLoader();

        Class<?> tyrusInitializerClass;
        try {
            tyrusInitializerClass = cl.loadClass("org.glassfish.tyrus.servlet.TyrusServletContainerInitializer");
        } catch (ClassNotFoundException cnfe) {
            // No possibility of WebSocket.
            return;
        }

        try {
            ServletContainerInitializer tyrusInitializer = (ServletContainerInitializer) tyrusInitializerClass.newInstance();
            Class<?> configClass = cl.loadClass("org.glassfish.tyrus.server.TyrusServerConfiguration");

            // List of classes must be non empty. TyrusServerConfiguration is ignored,
            // so we add that as class to trigger Tyrus to initialize
            HashSet<Class<?>> filteredClasses = new HashSet<>();
            filteredClasses.add(configClass);

            tyrusInitializer.onStartup(filteredClasses, ctx);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new ServletException(ex);
        }

    }

}
