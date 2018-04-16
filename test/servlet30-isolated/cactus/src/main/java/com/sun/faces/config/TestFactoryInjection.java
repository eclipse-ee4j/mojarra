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

package com.sun.faces.config;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.security.Principal;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.render.RenderKit;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.NavigationHandler;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.behavior.Behavior;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.context.ExternalContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.context.ExternalContextFactory;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.el.PropertyResolver;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;
import javax.faces.el.VariableResolver;
import javax.faces.event.ActionListener;
import javax.faces.validator.Validator;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.servlet.ServletContext;

import com.sun.faces.application.InjectionApplicationFactory;
import com.sun.faces.application.ApplicationFactoryImpl;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.ApplicationImpl;
import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.config.processor.FactoryConfigProcessor;
import com.sun.faces.context.FacesContextFactoryImpl;
import com.sun.faces.context.FacesContextImpl;
import com.sun.faces.context.ExceptionHandlerFactoryImpl;
import com.sun.faces.context.ExternalContextFactoryImpl;
import com.sun.faces.context.InjectionFacesContextFactory;
import com.sun.faces.context.ExternalContextImpl;
import com.sun.faces.lifecycle.ClientWindowFactoryImpl;
import com.sun.faces.lifecycle.LifecycleImpl;
import com.sun.faces.renderkit.RenderKitFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


@SuppressWarnings({"deprecation"})
public class TestFactoryInjection extends ServletFacesTestCase {


    // ------------------------------------------------------------ Constructors


    public TestFactoryInjection() {
        super("TestFactoryFinder");
    }


    public TestFactoryInjection(String name) {
        super(name);
    }


    // ------------------------------------------------------------ Test Methods


    public void testFactoryFinderApplicationInjection() throws Exception {

        Document d = newFacesConfigDocument();
        Node facesConfig = d.getFirstChild();
        Element factory = createElement(d, "factory");
        facesConfig.appendChild(factory);
        Element application = createElement(d, "application-factory");
        factory.appendChild(application);
        application.setTextContent("com.sun.faces.application.ApplicationFactoryImpl");

        // clear the factories
        FactoryFinder.releaseFactories();
        ApplicationAssociate.clearInstance(getFacesContext().getExternalContext());

        // invoke the FactoryConfigProcessor
        FactoryConfigProcessor fcp = new FactoryConfigProcessor(false);
        fcp.process((ServletContext) getFacesContext().getExternalContext().getContext(), getFacesContext(),
                    new DocumentInfo[] { new DocumentInfo(d, null) });

        // now get an Application instance from the Factory and ensure
        // no injection occured.
        ApplicationFactory appFactory = (ApplicationFactory)
              FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        assertNotNull(appFactory);
        assertNull(appFactory.getWrapped());
        Application app = appFactory.getApplication();
        // reflect the 'defaultApplication' field to ensure it's null
        Field field = Application.class.getDeclaredField("defaultApplication");
        field.setAccessible(true);
        assertNull(field.get(app));

        // reset for the injection portion of the test
        FactoryFinder.releaseFactories();
        ApplicationAssociate.clearInstance(getFacesContext().getExternalContext());

        // add another factory to our document
        Element application2 = createElement(d, "application-factory");
        factory.appendChild(application2);
        application2.setTextContent(BasicApplicationFactory.class.getName());

        // process the document.  This should cause the the InjectionApplicationFactory
        // to be put into play since there is more than one ApplicationFactory
        // being configured
        fcp.process((ServletContext) getFacesContext().getExternalContext().getContext(), getFacesContext(),
                    new DocumentInfo[] { new DocumentInfo(d, null) });

        // get the ApplicationFactory instance.  The top-level factory should
        // be the InjectionApplicationFactory.
        appFactory = (ApplicationFactory)
              FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        assertNotNull(appFactory);
        assertEquals(InjectionApplicationFactory.class, appFactory.getClass());
        ApplicationFactory wrapped1 = appFactory.getWrapped();
        assertNotNull(wrapped1);
        assertEquals(BasicApplicationFactory.class, wrapped1.getClass());
        ApplicationFactory wrapped2 = wrapped1.getWrapped();
        assertEquals(ApplicationFactoryImpl.class, wrapped2.getClass());

        // no ensure that the top level application instance's defaultApplication
        // instance was injected with the default Application instance.
        // This instance should be the same as that returned by wrapped2.
        app = appFactory.getApplication();
        assertNotNull(app);
        Application fieldResult = (Application) field.get(app);
        assertNotNull(fieldResult);
        assertEquals(ApplicationImpl.class, fieldResult.getClass());

        // basic application doesn't implement getProjetStage(), so without
        // injection, this method would throw an UnsupportedOperationException,
        // however, if we got this far, no exception should be thrown.
        try {
            app.getProjectStage();
        } catch (UnsupportedOperationException uso) {
            fail("Application.getProjectStage() threw an Exception; injection failed");
        }

    }


    public void testFactoryFinderFacesContextInjection() throws Exception {

        ExternalContext extContext = getFacesContext().getExternalContext();
        ServletContext sc = (ServletContext) extContext.getContext();
        Document d = newFacesConfigDocument();
        Node facesConfig = d.getFirstChild();
        Element factory = createElement(d, "factory");
        facesConfig.appendChild(factory);
        Element application = createElement(d, "application-factory");
        factory.appendChild(application);
        application
              .setTextContent("com.sun.faces.application.ApplicationFactoryImpl");
        Element facesContext = createElement(d, "faces-context-factory");
        factory.appendChild(facesContext);
        facesContext.setTextContent(FacesContextFactoryImpl.class.getName());
        Element exceptionHandler = createElement(d, "exception-handler-factory");
        factory.appendChild(exceptionHandler);
        exceptionHandler.setTextContent(ExceptionHandlerFactoryImpl.class.getName());
        Element externalContextFactory = createElement(d, "external-context-factory");
        factory.appendChild(externalContextFactory);
        externalContextFactory.setTextContent(ExternalContextFactoryImpl.class.getName());
        Element clientWindowFactory = createElement(d, "client-window-factory");
        factory.appendChild(clientWindowFactory);
        clientWindowFactory.setTextContent(ClientWindowFactoryImpl.class.getName());
        Element renderKitFactory = createElement(d, "render-"
                                                    + "kit-factory");
        factory.appendChild(renderKitFactory);
        renderKitFactory.setTextContent(RenderKitFactoryImpl.class.getName());


        // clear the factories
        FactoryFinder.releaseFactories();
        ApplicationAssociate.clearInstance(extContext);
        FacesContext.getCurrentInstance().release();

        // invoke the FactoryConfigProcessor
        FactoryConfigProcessor fcp = new FactoryConfigProcessor(false);
        InitFacesContext initContext = new InitFacesContext(sc);
        fcp.process(sc, initContext, new DocumentInfo[]{new DocumentInfo(d, null)});
        initContext.release();
        FacesContext.getCurrentInstance().release();

        // now get an FacesContext instance from the Factory and ensure
        // no injection occured.
        FacesContextFactory fcFactory = (FacesContextFactory)
              FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        assertNotNull(fcFactory);
        assertNull(fcFactory.getWrapped());
        FacesContext fc = fcFactory.getFacesContext(sc, request, response, new LifecycleImpl());
        // reflect the 'defaultFacesContext' field to ensure it's null
        Field field = FacesContext.class.getDeclaredField("defaultFacesContext");
        Field extField = ExternalContext.class.getDeclaredField("defaultExternalContext");
        field.setAccessible(true);
        extField.setAccessible(true);
        assertNull(field.get(fc));

        // reset for the injection portion of the test
        FactoryFinder.releaseFactories();
        ApplicationAssociate.clearInstance(extContext);
        FacesContext.getCurrentInstance().release();

        // add another factory to our document
        Element facesContext2 = createElement(d, "faces-context-factory");
        factory.appendChild(facesContext2);
        facesContext2.setTextContent(BasicFacesContextFactory.class.getName());

        // process the document.  This should cause the the InjectionFacesContextFactory
        // to be put into play since there is more than one FacesContextFactory
        // being configured
        initContext = new InitFacesContext(sc);
        fcp.process(sc, initContext, new DocumentInfo[]{new DocumentInfo(d, null)});
        initContext.release();

        // get the FacesContextFactory instance.  The top-level factory should
        // be the InjectionFacesContextFactory.
        fcFactory = (FacesContextFactory)
              FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        assertNotNull(fcFactory);
        assertEquals(InjectionFacesContextFactory.class, fcFactory.getClass());
        FacesContextFactory wrapped1 = fcFactory.getWrapped();
        assertNotNull(wrapped1);
        assertEquals(BasicFacesContextFactory.class, wrapped1.getClass());
        FacesContextFactory wrapped2 = wrapped1.getWrapped();
        assertEquals(FacesContextFactoryImpl.class, wrapped2.getClass());

        // now ensure that the top level facescontext instance's defaultFacesContext
        // field was injected with the default FacesContext implementation.
        // Also ensure that the top level ExternalContext instance's defaultExternalContext
        // field was injected with the default ExternalContext implementation.
        fc = fcFactory.getFacesContext(sc, request, response, new LifecycleImpl());

        assertNotNull(fc);
        FacesContext fieldResult = (FacesContext) field.get(fc);
        assertNotNull(fieldResult);
        assertEquals(FacesContextImpl.class, fieldResult.getClass());

        ExternalContext extFieldResult = (ExternalContext) extField.get(fc.getExternalContext());
        assertNotNull(extFieldResult);
        assertEquals(ExternalContextImpl.class, extFieldResult.getClass());

        // basic facescontext doesn't implement getAttributes(), so without
        // injection, this method would throw an UnsupportedOperationException,
        // however, if we got this far, no exception should be thrown.
        try {
            fc.getAttributes();
        } catch (UnsupportedOperationException uso) {
            fail("FacesContext.getAttributes() threw an Exception; injection failed");
        }

    }


    // --------------------------------------------------------- Private Methods


    private Document newFacesConfigDocument()
          throws ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        Document d = factory.newDocumentBuilder().newDocument();
        Element e = createElement(d, "faces-config");
        d.appendChild(e);
        return d;

    }


    private Element createElement(Document d, String elementName) {

        final String ns = "http://java.sun.com/xml/ns/javaee";
        return d.createElementNS(ns, elementName);

    }


    // ---------------------------------------------------------- Nested Classes


    public static final class BasicExternalContextFactory extends ExternalContextFactory {

        private ExternalContextFactory delegate;


        // -------------------------------------------------------- Constructors


        public BasicExternalContextFactory(ExternalContextFactory delegate) {

            this.delegate = delegate;

        }


        // ------------------------------------------- Methods from FacesWrapper


        @Override
        public ExternalContextFactory getWrapped() {

            return delegate;

        }


        // --------------------------------- Methods from ExternalContextFactory


        public ExternalContext getExternalContext(Object context,
                                                  Object request,
                                                  Object response)
              throws FacesException {

            ExternalContext extContext = delegate.getExternalContext(context,
                                                                     request,
                                                                     response);
            return new BasicExternalContext(extContext);

        }
    }


    public static final class BasicExternalContext extends ExternalContext {

        private ExternalContext delegate;

        public BasicExternalContext(ExternalContext delegate) {
            this.delegate = delegate;
        }

        public void dispatch(String path) throws IOException {

        }

        public String encodeActionURL(String url) {
            return null;
        }

        public String encodeNamespace(String name) {
            return null;
        }

        public String encodePartialActionURL(String viewId) {
            return null;
        }

        public String encodeResourceURL(String url) {
            return null;
        }
        
        public String encodeWebsocketURL(String url) {
            return null;
        }

        public Map<String, Object> getApplicationMap() {
            return null;
        }

        public String getAuthType() {
            return null;
        }

        public Object getContext() {
            return null;
        }

        public String getInitParameter(String name) {
            return null;
        }

        public Map getInitParameterMap() {
            return null;
        }

        public String getRemoteUser() {
            return null;
        }

        public Object getRequest() {
            return null;
        }

        public String getRequestContextPath() {
            return null;
        }

        public Map<String, Object> getRequestCookieMap() {
            return null;
        }

        public Map<String, String> getRequestHeaderMap() {
            return null;
        }

        public Map<String, String[]> getRequestHeaderValuesMap() {
            return null;
        }

        public Locale getRequestLocale() {
            return null;
        }

        public Iterator<Locale> getRequestLocales() {
            return null;
        }

        public Map<String, Object> getRequestMap() {
            return null;
        }

        public Map<String, String> getRequestParameterMap() {
            return null;
        }

        public Iterator<String> getRequestParameterNames() {
            return null;
        }

        public Map<String, String[]> getRequestParameterValuesMap() {
            return null;
        }

        public String getRequestPathInfo() {
            return null;
        }

        public String getRequestServletPath() {
            return null;
        }

        public URL getResource(String path) throws MalformedURLException {
            return null;
        }

        public InputStream getResourceAsStream(String path) {
            return null;
        }

        public Set<String> getResourcePaths(String path) {
            return null;
        }

        public Object getResponse() {
            return null;
        }

        public Object getSession(boolean create) {
            return null;
        }

        public Map<String, Object> getSessionMap() {
            return null;
        }

        public Principal getUserPrincipal() {
            return null;
        }

        public boolean isUserInRole(String role) {
            return false;
        }

        public void log(String message) {

        }

        public void log(String message, Throwable exception) {

        }

        public void redirect(String url) throws IOException {

        }

    }


    public static final class BasicFacesContextFactory extends FacesContextFactory {

        private FacesContextFactory delegate;


        // -------------------------------------------------------- Constructors


        public BasicFacesContextFactory(FacesContextFactory delegate) {

            this.delegate = delegate;

        }


        // ------------------------------------------- Methods from FacesWrapper

        
        @Override
        public FacesContextFactory getWrapped() {

            return delegate;

        }


        // ------------------------------------ Methods from FacesContextFactory


        public FacesContext getFacesContext(Object context,
                                            Object request,
                                            Object response,
                                            Lifecycle lifecycle)
              throws FacesException {

            FacesContext fcdelegate = delegate.getFacesContext(context, request, response, lifecycle);
            return new BasicFacesContext(fcdelegate);

        }

    } // END BasicFacesContextFactory



    public static final class BasicFacesContext extends FacesContext {

        FacesContext delegate;

        public BasicFacesContext(FacesContext delegate) {
            this.delegate = delegate;
        }

        public Application getApplication() {
            return null;
        }

        public Iterator<String> getClientIdsWithMessages() {
            return null;
        }

        public ExternalContext getExternalContext() {
            return delegate.getExternalContext();
        }

        public FacesMessage.Severity getMaximumSeverity() {
            return null;
        }

        public Iterator<FacesMessage> getMessages() {
            return null;
        }

        public Iterator<FacesMessage> getMessages(String clientId) {
            return null;
        }

        public RenderKit getRenderKit() {
            return null;
        }

        public boolean getRenderResponse() {
            return false;
        }

        public boolean getResponseComplete() {
            return false;
        }

        public ResponseStream getResponseStream() {
            return null;
        }

        public void setResponseStream(ResponseStream responseStream) {

        }

        public ResponseWriter getResponseWriter() {
            return null;
        }

        public void setResponseWriter(ResponseWriter responseWriter) {

        }

        public UIViewRoot getViewRoot() {
            return null;
        }

        public void setViewRoot(UIViewRoot root) {

        }

        public void addMessage(String clientId, FacesMessage message) {

        }

        public void release() {
            delegate.release();
        }

        public void renderResponse() {

        }

        public void responseComplete() {

        }
        
        boolean validationFailed;

        @Override
        public boolean isValidationFailed() {
            return validationFailed;
        }

        @Override
        public void validationFailed() {
            this.validationFailed = true;
        }
        
        

    } // END BasicFacesContext

    public static final class BasicApplicationFactory extends ApplicationFactory {

        private ApplicationFactory delegate;

        // -------------------------------------------------------- Constructors


        public BasicApplicationFactory(ApplicationFactory delegate) {

            this.delegate = delegate;

        }


        // ------------------------------------------- Methods from FacesWrapper


        @Override
        public ApplicationFactory getWrapped() {

            return delegate;

        }


        // ------------------------------------- Methods from ApplicationFactory


        public Application getApplication() {

            return new BasicApplication();
        }

        public void setApplication(Application application) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

    } // END BasicApplicationFactory


    public static final class BasicApplication extends Application {

        public ActionListener getActionListener() {
            return null;
        }

        public void setActionListener(ActionListener listener) {

        }

        public Locale getDefaultLocale() {
            return null;
        }

        public void setDefaultLocale(Locale locale) {

        }

        public String getDefaultRenderKitId() {
            return null;
        }

        public void setDefaultRenderKitId(String renderKitId) {

        }

        public String getMessageBundle() {
            return null;
        }

        public void setMessageBundle(String bundle) {

        }

        public NavigationHandler getNavigationHandler() {
            return null;
        }

        public void setNavigationHandler(NavigationHandler handler) {

        }

        public PropertyResolver getPropertyResolver() {
            return null;
        }

        public void setPropertyResolver(PropertyResolver resolver) {

        }

        public VariableResolver getVariableResolver() {
            return null;
        }

        public void setVariableResolver(VariableResolver resolver) {

        }

        public ViewHandler getViewHandler() {
            return null;
        }

        public void setViewHandler(ViewHandler handler) {

        }

        public StateManager getStateManager() {
            return null;
        }

        public void setStateManager(StateManager manager) {

        }

        public void addComponent(String componentType, String componentClass) {

        }

        public UIComponent createComponent(String componentType)
              throws FacesException {
            return null;
        }

        public UIComponent createComponent(ValueBinding componentBinding,
                                           FacesContext context,
                                           String componentType)
              throws FacesException {
            return null;
        }

        public Iterator<String> getComponentTypes() {
            return null;
        }

        public void addConverter(String converterId, String converterClass) {

        }

        public void addConverter(Class<?> targetClass, String converterClass) {

        }

        public Converter createConverter(String converterId) {
            return null;
        }

        public Converter createConverter(Class<?> targetClass) {
            return null;
        }

        public Iterator<String> getConverterIds() {
            return null;
        }

        public Iterator<Class<?>> getConverterTypes() {
            return null;
        }

        public MethodBinding createMethodBinding(String ref, Class<?>[] params)
              throws ReferenceSyntaxException {
            return null;
        }

        public Iterator<Locale> getSupportedLocales() {
            return null;
        }

        public void setSupportedLocales(Collection<Locale> locales) {

        }

        public void addValidator(String validatorId, String validatorClass) {

        }

        public Validator createValidator(String validatorId)
              throws FacesException {
            return null;
        }

        public Iterator<String> getValidatorIds() {
            return null;
        }

        public void addBehavior(String behaviorId, String behaviorClass) {

        }

        public Behavior createBehavior(String behaviorId)
              throws FacesException {
            return null;
        }

        public Iterator<String> getBehaviorIds() {
            return null;
        }

        public ValueBinding createValueBinding(String ref)
              throws ReferenceSyntaxException {
            return null;
        }

    } // END BasicApplication

}
