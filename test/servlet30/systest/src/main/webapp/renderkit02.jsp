<%--

    Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Public License v. 2.0, which is available at
    http://www.eclipse.org/legal/epl-2.0.

    This Source Code may also be made available under the following Secondary
    Licenses when the conditions for such availability set forth in the
    Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
    version 2 with the GNU Classpath Exception, which is available at
    https://www.gnu.org/software/classpath/license.html.

    SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

--%>

<%@ page contentType="text/html"
%><%@ page import="jakarta.faces.FactoryFinder"
%><%@ page import="jakarta.faces.context.FacesContext"
%><%@ page import="jakarta.faces.context.FacesContextFactory"
%><%@ page import="jakarta.faces.component.UIViewRoot"
%><%@ page import="jakarta.faces.lifecycle.Lifecycle"
%><%@ page import="jakarta.faces.render.RenderKitFactory"
%><%@ page import="jakarta.faces.lifecycle.LifecycleFactory"
%><%

// This test demonstrates the request processing lifecycle of 
// a "non-faces" request --->  faces response
// It uses the "default" renderkit to show how a renderkit can be
// set.
//
    // Create a Lifecycle
    //
    LifecycleFactory lFactory = (LifecycleFactory)
        FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
    Lifecycle lifecycle = lFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
    if (lifecycle == null) {
        out.println("/renderkit02.jsp FAILED - Could not create Lifecycle");
        return;
    }

    // Create a FacesContext 
    //
    FacesContextFactory facesContextFactory = (FacesContextFactory)
        FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
    FacesContext facesContext = facesContextFactory.getFacesContext(
        config.getServletContext(), request, response, lifecycle);
    if (facesContext == null) {
        out.println("/renderkit02.jsp FAILED - Could not create FacesContext");
        return;
    }

    // Acquire a View..
    //
    UIViewRoot view = facesContext.getApplication().getViewHandler().restoreView(facesContext, "/renderkit02A.jsp");
    if ( view == null)  {
        view = facesContext.getApplication().getViewHandler().createView(facesContext, "/renderkit02A.jsp");
    }
    // Set the RenderKitFactory.HTML_BASIC_RENDER_KIT renderkit Id
    //
    view.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
    facesContext.setViewRoot(view);

    facesContext.renderResponse();

    lifecycle.execute(facesContext);
    lifecycle.render(facesContext);

    // All tests passed
    //
    out.println("/renderkit02.jsp PASSED");
%>
