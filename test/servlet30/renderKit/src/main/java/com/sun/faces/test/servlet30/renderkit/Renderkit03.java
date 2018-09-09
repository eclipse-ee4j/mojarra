package com.sun.faces.test.servlet30.renderkit;

import static javax.faces.FactoryFinder.FACES_CONTEXT_FACTORY;
import static javax.faces.FactoryFinder.LIFECYCLE_FACTORY;
import static javax.faces.lifecycle.LifecycleFactory.DEFAULT_LIFECYCLE;

import java.io.IOException;
import java.io.PrintWriter;

import javax.faces.FactoryFinder;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/renderkit03")
public class Renderkit03 extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        try {

            // This test demonstrates the request processing lifecycle of
            // a "non-faces" request ---> faces response
            // It uses a "custom" renderkit to show how a renderkit can be
            // set.


            // Create a Lifecycle
            LifecycleFactory lFactory = (LifecycleFactory) FactoryFinder.getFactory(LIFECYCLE_FACTORY);
            Lifecycle lifecycle = lFactory.getLifecycle(DEFAULT_LIFECYCLE);
            if (lifecycle == null) {
                out.println("/renderkit03.jsp FAILED - Could not create Lifecycle");
                return;
            }

            // Create a FacesContext
            FacesContextFactory facesContextFactory = (FacesContextFactory) FactoryFinder.getFactory(FACES_CONTEXT_FACTORY);
            FacesContext facesContext = facesContextFactory.getFacesContext(request.getServletContext(), request, response, lifecycle);
            if (facesContext == null) {
                out.println("/renderkit03.jsp FAILED - Could not create FacesContext");
                return;
            }

            // Acquire a view
            UIViewRoot view = facesContext.getApplication().getViewHandler().createView(facesContext, "/renderkit03A.xhtml");

            // Set the RenderKitFactory.HTML_BASIC_RENDER_KIT renderkit Id
            view.setRenderKitId("CUSTOM");
            facesContext.setViewRoot(view);
            facesContext.renderResponse();

            lifecycle.execute(facesContext);
            lifecycle.render(facesContext);

            // All tests passed
            out.println("/renderkit03.jsp PASSED");

        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace(out);
        }
    }

}
