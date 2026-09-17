/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

package com.sun.faces.cdi;

import java.lang.reflect.Method;
import java.net.URL;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.ApplicationImpl;
import com.sun.faces.context.ExternalContextImpl;
import com.sun.faces.context.FacesContextImpl;
import com.sun.faces.lifecycle.LifecycleImpl;
import com.sun.faces.mock.MockHttpServletRequest;
import com.sun.faces.mock.MockHttpServletResponse;
import com.sun.faces.mock.MockServletContext;

import jakarta.faces.FactoryFinder;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

/**
 * One deployed application, as far as {@link CdiUtils} can tell: a {@link ServletContext} of its own, holding
 * an {@link ApplicationAssociate} of its own, current on the calling thread.
 *
 * <p>Deploy one per test that expects its CDI bean resolutions to be cached, because that is what
 * {@link CdiUtils} caches them in. {@link #undeploy()} tears it down the way
 * {@code ConfigureListener#contextDestroyed} does.
 */
final class TestApplicationScope {

    private final MockServletContext servletContext;
    private final ApplicationAssociate associate;

    private TestApplicationScope(MockServletContext servletContext, ApplicationAssociate associate) {
        this.servletContext = servletContext;
        this.associate = associate;
    }

    /**
     * Deploys an application and makes it current on the calling thread.
     *
     * @return the deployed application.
     */
    static TestApplicationScope deploy() {
        MockServletContext servletContext = new MockServletContext() {
            @Override
            public URL getResource(String path) {
                return null;
            }
        };

        ExternalContextImpl externalContext = new ExternalContextImpl(
                servletContext, new MockHttpServletRequest(), new MockHttpServletResponse());

        FactoryFinder.setFactory(FactoryFinder.RENDER_KIT_FACTORY, "com.sun.faces.mock.MockRenderKitFactory");

        new FacesContextImpl(externalContext, new LifecycleImpl());
        new ApplicationImpl();

        TestApplicationScope application = new TestApplicationScope(
                servletContext, ApplicationAssociate.getInstance(servletContext));
        application.makeCurrent();
        return application;
    }

    /**
     * Makes this application current on the calling thread, as {@code WebappLifecycleListener} does at the
     * start of every request. A thread that is serving no application resolves uncached.
     */
    void makeCurrent() {
        ApplicationAssociate.setCurrentInstance(associate);
    }

    /**
     * Undeploys this application, dropping its associate and everything cached in it. Touches nothing another
     * deployed application shares, so undeploying one leaves the others intact; the shared state
     * {@link #deploy()} installs is dropped by {@link #clearThreadState()} instead.
     */
    void undeploy() {
        ApplicationAssociate.setCurrentInstance(null);
        ApplicationAssociate.clearInstance(servletContext);
    }

    /**
     * Drops the state {@link #deploy()} installs process-wide rather than per application: the current
     * {@link FacesContext} and the factories. Call it once, after the last application is undeployed.
     */
    static void clearThreadState() {
        clearCurrentFacesContext();
        FactoryFinder.releaseFactories();
    }

    /**
     * Makes no {@link FacesContext} current on the calling thread. Constructing one makes it current, and this
     * fixture has no CDI environment in which to release it, so the protected setter is reached directly.
     */
    static void clearCurrentFacesContext() {
        try {
            Method setCurrentInstance = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
            setCurrentInstance.setAccessible(true);
            setCurrentInstance.invoke(null, new Object[] { null });
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    ApplicationAssociate getAssociate() {
        return associate;
    }
}
