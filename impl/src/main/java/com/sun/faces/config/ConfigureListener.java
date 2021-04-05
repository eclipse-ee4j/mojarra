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

import static com.sun.faces.RIConstants.ANNOTATED_CLASSES;
import static com.sun.faces.RIConstants.ERROR_PAGE_PRESENT_KEY_NAME;
import static com.sun.faces.RIConstants.FACES_INITIALIZER_MAPPINGS_ADDED;
import static com.sun.faces.RIConstants.FACES_SERVLET_MAPPINGS;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableLazyBeanValidation;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableThreading;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableWebsocketEndpoint;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.ForceLoadFacesConfigFiles;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.VerifyFacesConfigObjects;
import static com.sun.faces.config.WebConfiguration.WebContextInitParameter.JavaxFacesProjectStage;
import static com.sun.faces.el.ELUtils.getDefaultExpressionFactory;
import static com.sun.faces.push.WebsocketEndpoint.URI_TEMPLATE;
import static java.lang.Boolean.TRUE;
import static java.text.MessageFormat.format;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.ProjectStage;
import javax.faces.context.FacesContext;
import javax.faces.event.PreDestroyApplicationEvent;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.WebappLifecycleListener;
import com.sun.faces.config.WebConfiguration.WebContextInitParameter;
import com.sun.faces.el.ChainTypeCompositeELResolver;
import com.sun.faces.el.ELContextImpl;
import com.sun.faces.el.ELContextListenerImpl;
import com.sun.faces.el.ELUtils;
import com.sun.faces.el.FacesCompositeELResolver;
import com.sun.faces.mgbean.BeanBuilder;
import com.sun.faces.mgbean.BeanManager;
import com.sun.faces.push.WebsocketEndpoint;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.MojarraThreadFactory;
import com.sun.faces.util.ReflectionUtils;
import com.sun.faces.util.Timer;
import com.sun.faces.util.Util;

/**
 * <p>Parse all relevant JavaServer Faces configuration resources, and
 * configure the Reference Implementation runtime environment.</p>
 * <p/>
 */
public class ConfigureListener implements ServletRequestListener, HttpSessionListener, ServletRequestAttributeListener, HttpSessionAttributeListener, ServletContextAttributeListener, ServletContextListener {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    private ScheduledThreadPoolExecutor webResourcePool;

    protected WebappLifecycleListener webAppListener;
    protected WebConfiguration webConfig;


    // ------------------------------------------ ServletContextListener Methods


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        
        Timer timer = Timer.getInstance();
        if (timer != null) {
            timer.startTiming();
        }
        
        ConfigManager configManager = ConfigManager.getInstance(context);
        if (configManager == null) {
            configManager = ConfigManager.createInstance(context);
        }
        
        if (configManager.hasBeenInitialized(context)) {
            return;
        }

        InitFacesContext initContext = new InitFacesContext(context);

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE, format(
                    "ConfigureListener.contextInitialized({0})",
                    getServletContextIdentifier(context)));
        }

        webConfig = WebConfiguration.getInstance(context);

        // Check to see if the FacesServlet is present in the
        // web.xml.   If it is, perform faces configuration as normal,
        // otherwise, simply return.
        Object mappingsAdded = context.getAttribute(FACES_INITIALIZER_MAPPINGS_ADDED);
        if (mappingsAdded != null) {
            context.removeAttribute(FACES_INITIALIZER_MAPPINGS_ADDED);
        }

        WebXmlProcessor webXmlProcessor = new WebXmlProcessor(context);
        if (mappingsAdded == null) {
            if (!webXmlProcessor.isFacesServletPresent()) {
                if (!webConfig.isOptionEnabled(ForceLoadFacesConfigFiles)) {
                    LOGGER.log(FINE, "No FacesServlet found in deployment descriptor - bypassing configuration");
                    
                    WebConfiguration.clear(context);
                    configManager.destroy(context, initContext);
                    ConfigManager.removeInstance(context);
                    InitFacesContext.cleanupInitMaps(context);
                    
                    return;
                }
            } else {
                LOGGER.log(FINE, "FacesServlet found in deployment descriptor - processing configuration.");
            }
        }
        
        if (webXmlProcessor.isDistributablePresent()) {
            webConfig.setOptionEnabled(WebConfiguration.BooleanWebContextInitParameter.EnableDistributable, true);
            context.setAttribute(WebConfiguration.BooleanWebContextInitParameter.EnableDistributable.getQualifiedName(), TRUE);
        }

        // Bootstrap of faces required
        webAppListener = new WebappLifecycleListener(context);
        webAppListener.contextInitialized(sce);
        ReflectionUtils.initCache(Thread.currentThread().getContextClassLoader());
        Throwable caughtThrowable = null;

        try {

            if (LOGGER.isLoggable(INFO)) {
                LOGGER.log(INFO, "jsf.config.listener.version", getServletContextIdentifier(context));
            }

            if (webConfig.isOptionEnabled(VerifyFacesConfigObjects)) {
                LOGGER.warning("jsf.config.verifyobjects.development_only");
                
                // If we're verifying, force bean validation to occur at startup as well
                webConfig.overrideContextInitParameter(EnableLazyBeanValidation, false);
                Verifier.setCurrentInstance(new Verifier());
            }
            
            configManager.initialize(context, initContext);
            
            if (shouldInitConfigMonitoring()) {
                initConfigMonitoring(context);
            }

            // Step 7, verify that all the configured factories are available
            // and optionally that configured objects can be created. 
            Verifier verifier = Verifier.getCurrentInstance();
            if (verifier != null && !verifier.isApplicationValid() && LOGGER.isLoggable(SEVERE)) {
                LOGGER.severe("jsf.config.verifyobjects.failures_detected");
                StringBuilder sb = new StringBuilder(128);
                for (String msg : verifier.getMessages()) {
                    sb.append(msg).append('\n');
                }
                LOGGER.severe(sb.toString());
            }
            
            registerELResolverAndListenerWithJsp(context, false);
            ApplicationAssociate associate = ApplicationAssociate.getInstance(context);
            ELContext elContext = new ELContextImpl(initContext.getApplication().getELResolver());
            elContext.putContext(FacesContext.class, initContext);
            ExpressionFactory exFactory = getDefaultExpressionFactory(associate, initContext);
            if (exFactory != null) {
                elContext.putContext(ExpressionFactory.class, exFactory);
            }
            
            initContext.setELContext(elContext);
            
            if (associate != null) {
                associate.setContextName(getServletContextIdentifier(context));
                BeanManager manager = associate.getBeanManager();
                List<String> eagerBeans = manager.getEagerBeanNames();
                if (!eagerBeans.isEmpty()) {
                    for (String name : eagerBeans) {
                        manager.create(name, initContext);
                    }
                }
                
                boolean isErrorPagePresent = webXmlProcessor.isErrorPagePresent();
                associate.setErrorPagePresent(isErrorPagePresent);
                context.setAttribute(ERROR_PAGE_PRESENT_KEY_NAME, isErrorPagePresent);
            }

            // Register websocket endpoint if explicitly enabled.
            // Note: websocket channel filter is registered in FacesInitializer.
            if (webConfig.isOptionEnabled(EnableWebsocketEndpoint)) {
                ServerContainer serverContainer = (ServerContainer) context.getAttribute(ServerContainer.class.getName());

                if (serverContainer == null) {
                    throw new UnsupportedOperationException(
                            "Cannot enable f:websocket." + 
                            " The current websocket container implementation does not support programmatically registering a container-provided endpoint.");
                }

                serverContainer.addEndpoint(ServerEndpointConfig.Builder.create(WebsocketEndpoint.class, URI_TEMPLATE).build());
            }

            webConfig.doPostBringupActions();
            configManager.publishPostConfigEvent();

        } catch (Throwable t) {
            LOGGER.log(SEVERE, "Critical error during deployment: ", t);
            caughtThrowable = t;

        } finally {
            sce.getServletContext().removeAttribute(ANNOTATED_CLASSES);
            sce.getServletContext().removeAttribute(FACES_SERVLET_MAPPINGS);

            Verifier.setCurrentInstance(null);
            
            LOGGER.log(FINE, "jsf.config.listener.version.complete");
            
            if (timer != null) {
                timer.stopTiming();
                timer.logResult("Initialization of context " + getServletContextIdentifier(context));
            }
            
            if (caughtThrowable != null) {
                throw new RuntimeException(caughtThrowable);
            }
            
            // Bug 20458755: The InitFacesContext was not being cleaned up, resulting in
            // a partially constructed FacesContext being made available
            // to other code that re-uses this Thread at init time.
            initContext.releaseCurrentInstance();
        }
    }


    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        
        ConfigManager configManager = ConfigManager.getInstance(context);
        
        // The additional check for a WebConfiguration instance was added at the request of JBoss
        if (configManager == null && WebConfiguration.getInstanceWithoutCreating(context) != null) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "Unexpected state during contextDestroyed: no ConfigManager instance in current ServletContext but one is expected to exist.");
            }
        }

        InitFacesContext initContext = null;
        try {
            initContext = getInitFacesContext(context);
            if (initContext == null) {
                initContext = new InitFacesContext(context);
            } else {
                InitFacesContext.getThreadInitContextMap().put(Thread.currentThread(), initContext);
            }

            if (webAppListener != null) {
                webAppListener.contextDestroyed(sce);
                webAppListener = null;
            }
            
            if (webResourcePool != null) {
                webResourcePool.shutdownNow();
            }
            
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE,
                           "ConfigureListener.contextDestroyed({0})",
                           context.getServletContextName());
            }

            if (configManager == null || !configManager.hasBeenInitialized(context)) {
                return;
            }
            
            ELContext elContext = new ELContextImpl(initContext.getApplication().getELResolver());
            elContext.putContext(FacesContext.class, initContext);
            ExpressionFactory exFactory = ELUtils.getDefaultExpressionFactory(initContext);
            if (null != exFactory) {
                elContext.putContext(ExpressionFactory.class, exFactory);
            }
            
            initContext.setELContext(elContext);
            Application application = initContext.getApplication();
            
            application.publishEvent(
                initContext,
                PreDestroyApplicationEvent.class,
                Application.class,
                application);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected exception when attempting to tear down the Mojarra runtime", e);
        } finally {
            ApplicationAssociate.clearInstance(context);
            ApplicationAssociate.setCurrentInstance(null);
            
            // Release the initialization mark on this web application
            if( configManager != null ) {
              configManager.destroy(context, initContext);
              ConfigManager.removeInstance(context);
            } else if (WebConfiguration.getInstanceWithoutCreating(context) != null && LOGGER.isLoggable(WARNING)) {
                LOGGER.log(WARNING, "Unexpected state during contextDestroyed: no ConfigManager instance in current ServletContext but one is expected to exist.");
            }
            FactoryFinder.releaseFactories();
            ReflectionUtils.clearCache(Thread.currentThread().getContextClassLoader());
            WebConfiguration.clear(context);
            InitFacesContext.cleanupInitMaps(context);
        }

    }


    // ------------------------------------- Methods from ServletRequestListener


    @Override
    public void requestDestroyed(ServletRequestEvent event) {
        if (webAppListener != null) {
            webAppListener.requestDestroyed(event);
        }
    }


    @Override
    public void requestInitialized(ServletRequestEvent event) {
        if (webAppListener != null) {
            webAppListener.requestInitialized(event);
        }
    }


    // ----------------------------------------- Methods from HttpSessionListener


    @Override
    public void sessionCreated(HttpSessionEvent event) {
        if (webAppListener != null) {
            webAppListener.sessionCreated(event);
        }
    }


    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        if (webAppListener != null) {
            webAppListener.sessionDestroyed(event);
        }
    }


    // ---------------------------- Methods from ServletRequestAttributeListener


    @Override
    public void attributeAdded(ServletRequestAttributeEvent event) {
        // ignored
    }


    @Override
    public void attributeRemoved(ServletRequestAttributeEvent event) {
        if (webAppListener != null) {
            webAppListener.attributeRemoved(event);
        }
    }


    @Override
    public void attributeReplaced(ServletRequestAttributeEvent event) {
        if (webAppListener != null) {
            webAppListener.attributeReplaced(event);
        }
    }


    // ------------------------------- Methods from HttpSessionAttributeListener


    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        // ignored
    }


    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        if (webAppListener != null) {
            webAppListener.attributeRemoved(event);
        }
    }


    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
        if (webAppListener != null) {
            webAppListener.attributeReplaced(event);
        }
    }


    // ---------------------------- Methods from ServletContextAttributeListener


    @Override
    public void attributeAdded(ServletContextAttributeEvent event) {
        // ignored
    }

    @Override
    public void attributeRemoved(ServletContextAttributeEvent event) {
        if (webAppListener != null) {
            webAppListener.attributeRemoved(event);
        }
    }

    @Override
    public void attributeReplaced(ServletContextAttributeEvent event) {
        if (webAppListener != null) {
            webAppListener.attributeReplaced(event);
        }
    }


    // --------------------------------------------------------- Private Methods

    private boolean shouldInitConfigMonitoring() {

        boolean development = isDevModeEnabled();
        boolean threadingOptionSpecified = webConfig.isSet(EnableThreading);
        
        if (development && !threadingOptionSpecified) {
            return true;
        }
        
        return development && threadingOptionSpecified && webConfig.isOptionEnabled(EnableThreading);
    }

    private void initConfigMonitoring(ServletContext context) {

        @SuppressWarnings("unchecked")
        Collection<URI> webURIs = (Collection<URI>) context.getAttribute("com.sun.faces.webresources");
        
        if (isDevModeEnabled() && webURIs != null && !webURIs.isEmpty()) {
            webResourcePool = new ScheduledThreadPoolExecutor(1, new MojarraThreadFactory("WebResourceMonitor"));
            webResourcePool.scheduleAtFixedRate(new WebConfigResourceMonitor(context, webURIs), 2000, 2000, TimeUnit.MILLISECONDS);
        }

        context.removeAttribute("com.sun.faces.webresources");
    }

    private boolean isDevModeEnabled() {
        // interrogate the init parameter directly vs looking up the application
        return "Development".equals(webConfig.getOptionValue(JavaxFacesProjectStage));
    }

    /**
     * This method will be invoked {@link WebConfigResourceMonitor} when
     * changes to any of the faces-config.xml files included in WEB-INF
     * are modified.
     */
    private void reload(ServletContext servletContext) {

        if (LOGGER.isLoggable(INFO)) {
            LOGGER.log(INFO, "Reloading JSF configuration for context {0}", getServletContextIdentifier(servletContext));
        }
        
        // tear down the application
        try {
            // this will only be true in the automated test usage scenario
            if (null != webAppListener) {
                List<HttpSession> sessions = webAppListener.getActiveSessions();
                if (sessions != null) {
                    for (HttpSession session : sessions) {
                        if (LOGGER.isLoggable(Level.INFO)) {
                            LOGGER.log(Level.INFO,
                                    "Invalidating Session {0}",
                                    session.getId());
                        }
                        session.invalidate();
                    }
                }
            }
            ApplicationAssociate associate = ApplicationAssociate.getInstance(servletContext);
            if (associate != null) {
                BeanManager manager = associate.getBeanManager();
                for (Map.Entry<String, BeanBuilder> entry : manager.getRegisteredBeans().entrySet()) {
                    String name = entry.getKey();
                    BeanBuilder bean = entry.getValue();
                    if (ELUtils.Scope.APPLICATION.toString().equals(bean.getScope())) {
                        if (LOGGER.isLoggable(Level.INFO)) {
                            LOGGER.log(Level.INFO,
                                    "Removing application scoped managed bean: {0}",
                                    name);
                        }
                        servletContext.removeAttribute(name);
                    }

                }
            }
            // Release any allocated application resources
            FactoryFinder.releaseFactories();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FacesContext initContext = new InitFacesContext(servletContext);
            ApplicationAssociate.clearInstance(initContext.getExternalContext());
            ApplicationAssociate.setCurrentInstance(null);
            
            // Release the initialization mark on this web application
            ConfigManager configManager = ConfigManager.getInstance(servletContext);
            
            if (configManager != null) {
                configManager.destroy(servletContext, initContext);
                ConfigManager.removeInstance(servletContext);
            } else {
                if (LOGGER.isLoggable(SEVERE)) {
                    LOGGER.log(SEVERE,
                            "Unexpected state during reload: no ConfigManager instance in current ServletContext but one is expected to exist.");
                }
            }
            
            initContext.release();
            ReflectionUtils.clearCache(Thread.currentThread().getContextClassLoader());
            WebConfiguration.clear(servletContext);
        }

        // Bring the application back up, avoid re-registration of certain JSP
        // artifacts.  No verification will be performed either to make this
        // light weight.

        // init a new WebAppLifecycleListener so that the cached ApplicationAssociate
        // is removed.
        webAppListener = new WebappLifecycleListener(servletContext);

        InitFacesContext initContext = new InitFacesContext(servletContext);
        ReflectionUtils.initCache(Thread.currentThread().getContextClassLoader());

        try {
            ConfigManager configManager = ConfigManager.createInstance(servletContext);
            if (null != configManager) {
                configManager.initialize(servletContext, initContext);
            } else {
                LOGGER.log(SEVERE, "Unexpected state during reload: no ConfigManager instance in current ServletContext but one is expected to exist.");
            }

            registerELResolverAndListenerWithJsp(servletContext, true);
            ApplicationAssociate associate = ApplicationAssociate.getInstance(servletContext);
            
            if (associate != null) {
                Boolean errorPagePresent = (Boolean) servletContext.getAttribute(ERROR_PAGE_PRESENT_KEY_NAME);
                if (errorPagePresent != null) {
                    associate.setErrorPagePresent(errorPagePresent);
                    associate.setContextName(getServletContextIdentifier(servletContext));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            initContext.release();
        }

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO,
                    "Reload complete.",
                    getServletContextIdentifier(servletContext));
        }

    }


    private static String getServletContextIdentifier(ServletContext context) {
        if (context.getMajorVersion() == 2 && context.getMinorVersion() < 5) {
            return context.getServletContextName();
        } else {
            try {
                return context.getContextPath();
            } catch(AbstractMethodError error){
                return context.getServletContextName();
            }
        }
    }


    private static boolean isJspTwoOne(ServletContext context) {

        // The following try/catch is a hack to work around
        // a bug in Tomcat 6 where JspFactory.getDefaultFactory() will
        // return null unless JspRuntimeContext has been loaded.
        try {
            Class.forName("org.apache.jasper.compiler.JspRuntimeContext");
        } catch (ClassNotFoundException ignored) {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, "Dected JSP 2.1", ignored);
            }
        }

        if (JspFactory.getDefaultFactory() == null) {
            return false;
        }
        try {
            JspFactory.class.getMethod("getJspApplicationContext",
                    ServletContext.class);
        } catch (NoSuchMethodException | SecurityException e) {
            return false;
        }
        try {
            JspFactory.getDefaultFactory().getJspApplicationContext(context);
        } catch (Throwable e) {
            return false;
        }
        return true;

    }

    public void registerELResolverAndListenerWithJsp(ServletContext context, boolean reloaded) {

        if (webConfig.isSet(WebContextInitParameter.ExpressionFactory)
                || !isJspTwoOne(context)) {

            // first try to load a factory defined in web.xml
            if (!installExpressionFactory(context,
                    webConfig.getOptionValue(
                            WebContextInitParameter.ExpressionFactory))) {

                throw new ConfigurationException(
                        MessageUtils.getExceptionMessageString(
                                MessageUtils.INCORRECT_JSP_VERSION_ID,
                                WebContextInitParameter.ExpressionFactory.getDefaultValue(),
                                WebContextInitParameter.ExpressionFactory.getQualifiedName()));

            }

        } else {

            // JSP 2.1 specific check
            if (JspFactory.getDefaultFactory().getJspApplicationContext(context) == null) {
                return;
            }

            // register an empty resolver for now. It will be populated after the 
            // first request is serviced.
            FacesCompositeELResolver compositeELResolverForJsp =
                    new ChainTypeCompositeELResolver(FacesCompositeELResolver.ELResolverChainType.JSP);
            ApplicationAssociate associate =
                    ApplicationAssociate.getInstance(context);
            if (associate != null) {
                associate.setFacesELResolverForJsp(compositeELResolverForJsp);
            }

            // get JspApplicationContext.
            JspApplicationContext jspAppContext = JspFactory.getDefaultFactory()
                    .getJspApplicationContext(context);

            // cache the ExpressionFactory instance in ApplicationAssociate
            if (associate != null) {
                associate.setExpressionFactory(jspAppContext.getExpressionFactory());
            }

            // register compositeELResolver with JSP
            try {
                jspAppContext.addELResolver(compositeELResolverForJsp);
            }
            catch (IllegalStateException e) {
                ApplicationFactory factory = (ApplicationFactory)
                        FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
                Application app = factory.getApplication();
                if (app.getProjectStage() != ProjectStage.UnitTest && !reloaded) {
                    throw e;
                }
            }

            // register JSF ELContextListenerImpl with Jsp
            ELContextListenerImpl elContextListener = new ELContextListenerImpl();
            jspAppContext.addELContextListener(elContextListener);
        }
    }

    private boolean installExpressionFactory(ServletContext sc,
                                             String elFactoryType) {

        if (elFactoryType == null) {
            return false;
        }
        try {
            ExpressionFactory factory = (ExpressionFactory)
                    Util.loadClass(elFactoryType, this).newInstance();
            ApplicationAssociate associate =
                    ApplicationAssociate.getInstance(sc);
            if (associate != null) {
                associate.setExpressionFactory(factory);
            }
            return true;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.severe(MessageFormat.format("Unable to instantiate ExpressionFactory ''{0}''",
                        elFactoryType));
            }
            return false;
        }

    }

    private InitFacesContext getInitFacesContext(ServletContext context) {
        Map initContextServletContext = InitFacesContext.getInitContextServletContextMap();
        Set entries = initContextServletContext.entrySet();
        InitFacesContext initContext = null;
        for (Iterator iterator1 = entries.iterator(); iterator1.hasNext();) {
            Map.Entry entry1 = (Map.Entry)iterator1.next();
            Object initContextKey = entry1.getKey();
            Object value1 = entry1.getValue();
            if (context == value1) {
                initContext =  (InitFacesContext)initContextKey;
                break;
            }
        }
        return initContext;
    }

    // ----------------------------------------------------------- Inner classes


    /**
     * <p>Processes a web application's deployment descriptor looking
     * for a reference to <code>javax.faces.webapp.FacesServlet</code>.</p>
     */
    private static class WebXmlProcessor {

        private static final String WEB_XML_PATH = "/WEB-INF/web.xml";
        private static final String WEB_FRAGMENT_PATH = "META-INF/web-fragment.xml";

        private boolean facesServletPresent;
        private boolean errorPagePresent;
        private boolean distributablePresent;


        /**
         * <p>When instantiated, the web.xml of the current application
         * will be scanned looking for a references to the
         * <code>FacesServlet</code>.  <code>isFacesServletPresent()</code>
         * will return the appropriate value based on the scan.</p>
         *
         * @param context the <code>ServletContext</code> for the application
         *                of interest
         */
        WebXmlProcessor(ServletContext context) {

            if (context != null) {
                scanForFacesServlet(context);
            }

        } // END WebXmlProcessor


        /**
         * @return <code>true</code> if the <code>WebXmlProcessor</code>
         *         detected a <code>FacesServlet</code> entry, otherwise return
         *         <code>false</code>.</p>
         */
        boolean isFacesServletPresent() {

            return facesServletPresent;

        } // END isFacesServletPresent


        /**
         * @return <code>true</code> if <code>WEB-INF/web.xml</code> contains
         *         a <code>&lt;error-page&gt;</code> element.
         */
        boolean isErrorPagePresent() {

            return errorPagePresent;

        }

        /*
         * return true if <distributable /> is present in the web.xml or a fragment.
         * 
         */
        public boolean isDistributablePresent() {
            return distributablePresent;
        }
        
        


        /**
         * <p>Parse the web.xml for the current application and scan
         * for a FacesServlet entry, if found, set the
         * <code>facesServletPresent</code> property to true.
         *
         * @param context the ServletContext instance for this application
         */
        private void scanForFacesServlet(ServletContext context) {
            InputStream in = context.getResourceAsStream(WEB_XML_PATH);
            if (in == null) {
                if (context.getMajorVersion() < 3) {
                    throw new ConfigurationException("no web.xml present");
                }
            }
            SAXParserFactory factory = getConfiguredFactory();
            if (in != null) {
                try {
                    SAXParser parser = factory.newSAXParser();
                    parser.parse(in, new WebXmlHandler());
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    warnProcessingError(e, context);
                    facesServletPresent = true;
                    return;
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception ioe) {
                            if (LOGGER.isLoggable(Level.FINEST)) {
                                LOGGER.log(Level.FINEST, "Closing stream", ioe);
                            }
                        }
                    }
                }
            }
            if (!facesServletPresent && context.getMajorVersion() >= 3) {
                ClassLoader cl = Util.getCurrentLoader(this);
                Enumeration<URL> urls;
                try {
                    urls = cl.getResources(WEB_FRAGMENT_PATH);
                } catch (IOException ioe) {
                    throw new ConfigurationException(ioe);
                }
                if (urls != null) {
                    while (urls.hasMoreElements() && !facesServletPresent) {
                        InputStream fragmentStream = null;
                        try {
                            URL url = urls.nextElement();
                            URLConnection conn = url.openConnection();
                            conn.setUseCaches(false);
                            fragmentStream = conn.getInputStream();
                            SAXParser parser = factory.newSAXParser();
                            parser.parse(fragmentStream, new WebXmlHandler());
                        } catch (IOException | ParserConfigurationException | SAXException e) {
                            warnProcessingError(e, context);
                            facesServletPresent = true;
                            return;
                        } finally {
                            if (fragmentStream != null) {
                                try {
                                    fragmentStream.close();
                                } catch (IOException ioe) {
                                    if (LOGGER.isLoggable(Level.WARNING)) {
                                        LOGGER.log(Level.WARNING,
                                                "Exception whil scanning for FacesServlet", ioe);                                
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } // END scanForFacesServlet

        /**
         * <p>Return a <code>SAXParserFactory</code> instance that is
         * non-validating and is namespace aware.</p>
         *
         * @return configured <code>SAXParserFactory</code>
         */
        private SAXParserFactory getConfiguredFactory() {

            SAXParserFactory factory = Util.createSAXParserFactory();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            return factory;

        } // END getConfiguredFactory


        private void warnProcessingError(Exception e, ServletContext sc) {

            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING,
                        MessageFormat.format(
                                "jsf.configuration.web.xml.parse.failed",
                                getServletContextIdentifier(sc)),
                        e);
            }

        }


        /**
         * <p>A simple SAX handler to process the elements of interested
         * within a web application's deployment descriptor.</p>
         */
        private class WebXmlHandler extends DefaultHandler {

            private static final String ERROR_PAGE = "error-page";
            private static final String SERVLET_CLASS = "servlet-class";
            private static final String FACES_SERVLET =
                    "javax.faces.webapp.FacesServlet";

            private boolean servletClassFound;
            @SuppressWarnings({"StringBufferField"})
            private StringBuffer content;

            @Override
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException {

                return new InputSource(new StringReader(""));

            } // END resolveEntity


            @Override
            public void startElement(String uri, String localName,
                                     String qName, Attributes attributes)
                    throws SAXException {

                if (!errorPagePresent && ERROR_PAGE.equals(localName)) {
                    errorPagePresent = true;
                    return;
                }
                if (!facesServletPresent) {
                    if (SERVLET_CLASS.equals(localName)) {
                        servletClassFound = true;
                        //noinspection StringBufferWithoutInitialCapacity
                        content = new StringBuffer();
                    } else {
                        servletClassFound = false;
                    }
                }
                if ("distributable".equals(localName)) {
                    distributablePresent = true;
                }


            } // END startElement


            @Override
            public void characters(char[] ch, int start, int length)
                    throws SAXException {

                if (servletClassFound && !facesServletPresent) {
                    content.append(ch, start, length);
                }

            } // END characters


            @Override
            public void endElement(String uri, String localName, String qName)
                    throws SAXException {

                if (servletClassFound && !facesServletPresent && 
                    FACES_SERVLET.equals(content.toString().trim())) {
                    facesServletPresent = true;
                }

            } // END endElement

        } // END WebXmlHandler

    } // END WebXmlProcessor


    private class WebConfigResourceMonitor implements Runnable {

        private List<Monitor> monitors;
        private ServletContext sc;

        // -------------------------------------------------------- Constructors


        public WebConfigResourceMonitor(ServletContext sc, Collection<URI> uris) {

            assert (uris != null);
            this.sc = sc;
            for (URI uri : uris) {
                if (monitors == null) {
                    monitors = new ArrayList<>(uris.size());
                }
                try {
                    Monitor m = new Monitor(uri);
                    monitors.add(m);
                } catch (IOException ioe) {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.severe("Unable to setup resource monitor for "
                                      + uri.toString()
                                      + ".  Resource will not be monitored for changes.");
                    }
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE,
                                   ioe.toString(),
                                   ioe);
                    }
                }
            }

        }


        // ----------------------------------------------- Methods from Runnable

        /**
         * PENDING javadocs
         */
        @Override
        public void run() {

            assert (monitors != null);
            boolean reloaded = false;
            for (Iterator<Monitor> i = monitors.iterator(); i.hasNext(); ) {
                Monitor m = i.next();
                try {
                    if (m.hasBeenModified()) {
                        if (!reloaded) {
                            reloaded = true;
                        }
                    }
                } catch (IOException ioe) {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.severe("Unable to access url "
                                      + m.uri.toString()
                                      + ".  Monitoring for this resource will no longer occur.");
                    }
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE,
                                   ioe.toString(),
                                   ioe);
                    }
                    i.remove();
                }
            }
            if (reloaded) {
                reload(sc);
            }

        }


        // ------------------------------------------------------- Inner Classes


        private class Monitor {

            private URI uri;
            private long timestamp = -1;

            // ---------------------------------------------------- Constructors


            Monitor(URI uri) throws IOException {

                this.uri = uri;
                this.timestamp = getLastModified();
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.log(Level.INFO,
                            "Monitoring {0} for modifications",
                            uri.toURL().toExternalForm());
                }

            }


            // ----------------------------------------- Package Private Methods


            boolean hasBeenModified() throws IOException {
                long temp = getLastModified();
                if (timestamp < temp) {
                    timestamp = temp;
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.log(Level.INFO,
                                "{0} changed!",
                                uri.toURL().toExternalForm());
                    }
                    return true;
                }
                return false;

            }


            // ------------------------------------------------- Private Methods


            private long getLastModified() throws IOException {

                InputStream in = null;
                try {
                    URLConnection conn = uri.toURL().openConnection();
                    conn.connect();
                    in = conn.getInputStream();
                    return conn.getLastModified();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException ignored) {
                            if (LOGGER.isLoggable(Level.FINEST)) {
                                LOGGER.log(Level.FINEST,
                                        "Exception while closing stream", ignored);
                            }
                        }
                    }
                }

            }

        } // END Monitor

    } // END WebConfigResourceMonitor

}

