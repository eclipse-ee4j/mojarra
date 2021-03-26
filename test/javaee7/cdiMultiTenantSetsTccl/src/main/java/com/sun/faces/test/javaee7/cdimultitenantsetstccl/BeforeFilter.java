/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.javaee7.cdimultitenantsetstccl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BeforeFilter implements Filter {
    
    private FilterConfig filterConfig = null;
    
    private static final String INIT_HAS_LIFECYCLE_KEY = "BeforeServlet_hasLifecycle";
    private static final String INIT_HAS_INITFACESCONTEXT_KEY = "BeforeServlet_hasInitFacesContext";
    
    private static final String REQUEST_HAS_LIFECYCLE = "BeforeServlet_requestHasLifecycle";
    private static final String REQUEST_HAS_FACESCONTEXT = "BeforeServlet_requestHasFacesContext";

    public BeforeFilter() {
    }    
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {
        
        Thread thread = Thread.currentThread();
        ClassLoader tccl = thread.getContextClassLoader();
        ClassLoader tcclp1 = new URLClassLoader(new URL[0], tccl); //new URLClassLoader(new URL [0]);
        thread.setContextClassLoader(tcclp1);
        
        HttpServletRequest req = (HttpServletRequest) request;
        LifecycleFactory lifecycle = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        req.setAttribute(REQUEST_HAS_LIFECYCLE, 
                (null != lifecycle) ? "TRUE":"FALSE");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        req.setAttribute(REQUEST_HAS_FACESCONTEXT, 
                (null != facesContext) ? "TRUE":"FALSE");
        
        // Dispatching to JSF throws this exception:
        /**
         * 
         * 

java.lang.IllegalStateException: Singleton not set for java.net.URLClassLoader@511e5bf4
	at org.glassfish.weld.ACLSingletonProvider$ACLSingleton.get(ACLSingletonProvider.java:110)
	at org.jboss.weld.Container.instance(Container.java:54)
	at org.jboss.weld.jsf.ConversationAwareViewHandler.getConversationContext(ConversationAwareViewHandler.java:80)
	at org.jboss.weld.jsf.ConversationAwareViewHandler.getActionURL(ConversationAwareViewHandler.java:102)
	at com.sun.faces.renderkit.html_basic.FormRenderer.getActionStr(FormRenderer.java:250)
	at com.sun.faces.renderkit.html_basic.FormRenderer.encodeBegin(FormRenderer.java:143)
	at jakarta.faces.component.UIComponentBase.encodeBegin(UIComponentBase.java:864)
	at jakarta.faces.component.UIComponent.encodeAll(UIComponent.java:1854)
	at jakarta.faces.component.UIComponent.encodeAll(UIComponent.java:1859)
	at com.sun.faces.application.view.FaceletViewHandlingStrategy.renderView(FaceletViewHandlingStrategy.java:456)
	at com.sun.faces.application.view.MultiViewHandler.renderView(MultiViewHandler.java:133)
	at jakarta.faces.application.ViewHandlerWrapper.renderView(ViewHandlerWrapper.java:337)
	at com.sun.faces.lifecycle.RenderResponsePhase.execute(RenderResponsePhase.java:120)
	at com.sun.faces.lifecycle.Phase.doPhase(Phase.java:101)
	at com.sun.faces.lifecycle.LifecycleImpl.render(LifecycleImpl.java:219)
         * 
         */
        // I think this is a side-effect of Weld also not being resilient
        // to TCCL replacement.  To continue with the job of exercising 
        // the fix in FactoryFinder, we just exercise it directly here.
        // I confirmed this is fixed in Weld 2.2.2 Final, which is in GlassFish 4.0.1
        final boolean weldIsTCCLReplacementResilient = false;
        
        if (weldIsTCCLReplacementResilient) {
            try {
                chain.doFilter(request, response);
            } catch (Exception t) {
                throw new ServletException(t);
            } finally {
                thread.setContextClassLoader(tccl);
            }
        } else {
            FacesContextFactory fcFactory = (FacesContextFactory) 
                    FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
            HttpServletResponse resp = (HttpServletResponse) response;
            PrintWriter pw = resp.getWriter();
            try {
                if (null != fcFactory) {
                    pw.print("<html><body><p id=\"result\">SUCCESS</p></body></html>");
                } else {
                    pw.print("<html><body><p id=\"result\">FAILURE</p></body></html>");
                }
                resp.setStatus(200);
                pw.close();
            } catch (Exception e) {
            }
        }
        
    }

    public void destroy() {        
    }

    public void init(FilterConfig filterConfig) {        
        this.filterConfig = filterConfig;
        ServletContext sc = this.filterConfig.getServletContext();
        LifecycleFactory lifecycle = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        sc.setAttribute(INIT_HAS_LIFECYCLE_KEY, 
                (null != lifecycle) ? "TRUE":"FALSE");
        FacesContext initFacesContext = FacesContext.getCurrentInstance();
        sc.setAttribute(INIT_HAS_INITFACESCONTEXT_KEY, 
                (null != initFacesContext) ? "TRUE":"FALSE");
        
        
    }
    
}
