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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.RIConstants;
import com.sun.faces.config.initfacescontext.NoOpELContext;
import com.sun.faces.config.initfacescontext.NoOpFacesContext;
import com.sun.faces.config.initfacescontext.ServletContextAdapter;
import com.sun.faces.context.ApplicationMap;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.el.ELContext;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationFactory;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

/**
 * A special, minimal implementation of FacesContext used at application initialization time. The ExternalContext
 * returned by this FacesContext only exposes the ApplicationMap.
 */
public class InitFacesContext extends NoOpFacesContext {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();
    private static final String INIT_FACES_CONTEXT_ATTR_NAME = RIConstants.FACES_PREFIX + "InitFacesContext";

    private ServletContextAdapter servletContextAdapter;
    private UIViewRoot viewRoot;
    private Map<Object, Object> attributes;

    private ELContext elContext = new NoOpELContext();

    public InitFacesContext(ServletContext servletContext) {
        servletContextAdapter = new ServletContextAdapter(servletContext);
        servletContext.setAttribute(INIT_FACES_CONTEXT_ATTR_NAME, this);
        InitFacesContext.cleanupInitMaps(servletContext);

        addServletContextEntryForInitContext(servletContext);
        addInitContextEntryForCurrentThread();
    }

    @Override
    public Map<Object, Object> getAttributes() {
        if (attributes == null) {
            attributes = new HashMap<>();
        }

        return attributes;
    }

    @Override
    public ExternalContext getExternalContext() {
        return servletContextAdapter;
    }

    @Override
    public UIViewRoot getViewRoot() {
        if (viewRoot == null) {
            viewRoot = new UIViewRoot();
            viewRoot.setLocale(Locale.getDefault());
            viewRoot.setViewId(FACES_PREFIX + "xhtml");
        }

        return viewRoot;
    }

    @Override
    public ELContext getELContext() {
        return elContext;
    }

    public void setELContext(ELContext elContext) {
        this.elContext = elContext;
    }

    @Override
    public Application getApplication() {
        ApplicationFactory factory = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        return factory.getApplication();
    }

    @Override
    public boolean isProjectStage(ProjectStage stage) {
        if (stage == null) {
            throw new NullPointerException();
        }
        return stage.equals(getApplication().getProjectStage());
    }

    @Override
    public void release() {

        setCurrentInstance(null);
        if (servletContextAdapter != null) {
            Map<String, Object> applicationMap = servletContextAdapter.getApplicationMap();
            if (applicationMap instanceof ApplicationMap) {
                if (((ApplicationMap) applicationMap).getContext() != null) {
                    applicationMap.remove(INIT_FACES_CONTEXT_ATTR_NAME);
                }
            }
            servletContextAdapter.release();
        }

        if (attributes != null) {
            attributes.clear();
            attributes = null;
        }

        elContext = null;

        if (viewRoot != null) {
            Map<String, Object> viewMap = viewRoot.getViewMap(false);
            if (viewMap != null) {
                viewMap.clear();
            }
            viewRoot = null;
        }
    }

    public void releaseCurrentInstance() {
        removeInitContextEntryForCurrentThread();
        setCurrentInstance(null);
    }

    public void addInitContextEntryForCurrentThread() {
        getThreadInitContextMap().put(Thread.currentThread(), this);
    }

    public void removeInitContextEntryForCurrentThread() {
        getThreadInitContextMap().remove(Thread.currentThread());
    }

    public void addServletContextEntryForInitContext(ServletContext servletContext) {
        getInitContextServletContextMap().put(this, servletContext);
    }

    public void removeServletContextEntryForInitContext() {
        getInitContextServletContextMap().remove(this);
    }

    /**
     * Clean up entries from the threadInitContext and initContextServletContext maps using a ServletContext. First remove
     * entry(s) with matching ServletContext from initContextServletContext map. Then remove entries from threadInitContext
     * map where the entry value(s) match the initFacesContext (associated with the ServletContext).
     *
     * @param servletContext the involved servlet context
     */
    public static void cleanupInitMaps(ServletContext servletContext) {

        Map<InitFacesContext, ServletContext> facesContext2ServletContext = getInitContextServletContextMap();
        Map<Thread, InitFacesContext> thread2FacesContext = getThreadInitContextMap();

        // First remove entry(s) with matching ServletContext from the initContextServletContext map.

        for (Entry<InitFacesContext, ServletContext> facesContext2ServletContextEntry : new ArrayList<>(facesContext2ServletContext.entrySet())) {

            if (facesContext2ServletContextEntry.getValue() == servletContext) {

                facesContext2ServletContext.remove(facesContext2ServletContextEntry.getKey());

                // Then remove entries from the threadInitContext map where the entry value(s) match the initFacesContext
                // (associated with the ServletContext).

                for (Entry<Thread, InitFacesContext> thread2FacesContextEntry : new ArrayList<>(thread2FacesContext.entrySet())) {

                    if (thread2FacesContextEntry.getValue() == facesContext2ServletContextEntry.getKey()) {
                        thread2FacesContext.remove(thread2FacesContextEntry.getKey());
                    }

                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    static Map<Thread, InitFacesContext> getThreadInitContextMap() {
        try {
            Field threadMap = FacesContext.class.getDeclaredField("threadInitContext");
            threadMap.setAccessible(true);

            return (Map<Thread, InitFacesContext>) threadMap.get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            LOGGER.log(Level.FINEST, "Unable to get (thread, init context) map", e);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    static Map<InitFacesContext, ServletContext> getInitContextServletContextMap() {
        try {
            Field initContextMap = FacesContext.class.getDeclaredField("initContextServletContext");
            initContextMap.setAccessible(true);

            return (Map<InitFacesContext, ServletContext>) initContextMap.get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            LOGGER.log(Level.FINEST, "Unable to get (init context, servlet context) map", e);
        }

        return null;
    }

    public static InitFacesContext getInstance(ServletContext servletContext) {
        InitFacesContext result = (InitFacesContext) servletContext.getAttribute(INIT_FACES_CONTEXT_ATTR_NAME);
        if (result != null) {
            result.addInitContextEntryForCurrentThread();
        }

        return result;
    }

    // Cactus / unit test only

    public void reInitializeExternalContext(ServletContext sc) {
        assert Util.isUnitTestModeEnabled();
        servletContextAdapter = new ServletContextAdapter(sc);
    }

}
