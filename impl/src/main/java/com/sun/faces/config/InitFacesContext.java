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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.el.ELContext;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationFactory;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.servlet.ServletContext;

import com.sun.faces.RIConstants;
import com.sun.faces.config.initfacescontext.NoOpELContext;
import com.sun.faces.config.initfacescontext.NoOpFacesContext;
import com.sun.faces.config.initfacescontext.ServletContextAdapter;
import com.sun.faces.context.ExceptionHandlerImpl;
import com.sun.faces.util.Util;

/**
 * A special, minimal implementation of FacesContext used at application initialization time. The ExternalContext
 * returned by this FacesContext only exposes the ApplicationMap.
 */
public class InitFacesContext extends NoOpFacesContext {

    private static final String INIT_FACES_CONTEXT_ATTR_NAME = RIConstants.FACES_PREFIX + "InitFacesContext";

    private ServletContextAdapter servletContextAdapter;
    private UIViewRoot viewRoot;
    private Map<Object, Object> attributes;

    private ELContext elContext = new NoOpELContext();

    private ExceptionHandler exceptionHandler;

    public InitFacesContext(ServletContext servletContext) {
        servletContextAdapter = new ServletContextAdapter(servletContext);
        servletContext.setAttribute(INIT_FACES_CONTEXT_ATTR_NAME, this);
        setCurrentInstance(this);
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
    public ExceptionHandler getExceptionHandler() {
        if (exceptionHandler == null) {
            exceptionHandler = new ExceptionHandlerImpl(false);
        }
        return exceptionHandler;
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

        releaseCurrentInstance();
        if (servletContextAdapter != null) {
            ((ServletContext) servletContextAdapter.getContext()).removeAttribute(INIT_FACES_CONTEXT_ATTR_NAME);
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
        setCurrentInstance(null);
    }

    public static InitFacesContext getInstance(ServletContext servletContext) {
        InitFacesContext result = (InitFacesContext) servletContext.getAttribute(INIT_FACES_CONTEXT_ATTR_NAME);
        if (result != null) {
            setCurrentInstance(result);
        }

        return result;
    }

    // Cactus / unit test only

    public void reInitializeExternalContext(ServletContext sc) {
        assert Util.isUnitTestModeEnabled();
        servletContextAdapter = new ServletContextAdapter(sc);
    }

}
