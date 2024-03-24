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

import static com.sun.faces.RIConstants.ANNOTATED_CLASSES;
import static com.sun.faces.RIConstants.ERROR_PAGE_PRESENT_KEY_NAME;
import static com.sun.faces.RIConstants.FACES_SERVLET_MAPPINGS;
import static com.sun.faces.RIConstants.FACES_SERVLET_REGISTRATION;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableLazyBeanValidation;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableThreading;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.ForceLoadFacesConfigFiles;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.VerifyFacesConfigObjects;
import static com.sun.faces.context.SessionMap.createMutex;
import static com.sun.faces.context.SessionMap.removeMutex;
import static com.sun.faces.push.WebsocketEndpoint.URI_TEMPLATE;
import static java.lang.Boolean.TRUE;
import static java.text.MessageFormat.format;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.WebappLifecycleListener;
import com.sun.faces.el.ELContextImpl;
import com.sun.faces.push.WebsocketEndpoint;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MojarraThreadFactory;
import com.sun.faces.util.ReflectionUtils;
import com.sun.faces.util.Timer;
import com.sun.faces.util.Util;

import jakarta.el.ELManager;
import jakarta.faces.FactoryFinder;
import jakarta.faces.annotation.FacesConfig.ContextParam;
import jakarta.faces.application.Application;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PreDestroyApplicationEvent;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpointConfig;

/**
 * Parse all relevant Faces configuration resources, and configure the Mojarra runtime
 * environment.
 */
public class ConfigureListener implements ServletRequestListener, HttpSessionListener, ServletContextListener {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    private ScheduledThreadPoolExecutor webResourcePool;

    protected WebappLifecycleListener webAppListener;
    protected WebConfiguration webConfig;

    // ------------------------------------------ ServletContextListener Methods

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();

        Timer timer = Timer.getInstance();
        if (timer != null) {
            timer.startTiming();
        }

        ConfigManager configManager = ConfigManager.getInstance(servletContext);
        if (configManager == null) {
            configManager = ConfigManager.createInstance(servletContext);
        }

        if (configManager.hasBeenInitialized(servletContext)) {
            return;
        }

        InitFacesContext initFacesContext = new InitFacesContext(servletContext);
        Util.getCdiBeanManager(initFacesContext); // #5232 Fail fast when CDI is really not available.

        LOGGER.log(FINE, () -> format("ConfigureListener.contextInitialized({0})", servletContext.getContextPath()));

        webConfig = WebConfiguration.getInstance(servletContext);

        // Check to see if the FacesServlet is present in the
        // web.xml. If it is, perform faces configuration as normal,
        // otherwise, simply return.
        Object facesServletRegistration = servletContext.getAttribute(FACES_SERVLET_REGISTRATION); // If found by FacesInitializer.

        WebXmlProcessor webXmlProcessor = new WebXmlProcessor(servletContext);
        if (facesServletRegistration == null) {
            if (!webXmlProcessor.isFacesServletPresent()) {
                if (!webConfig.isOptionEnabled(ForceLoadFacesConfigFiles)) {
                    LOGGER.log(FINE, "No FacesServlet found in deployment descriptor - bypassing configuration");

                    WebConfiguration.clear(servletContext);
                    configManager.destroy(servletContext, initFacesContext);
                    ConfigManager.removeInstance(servletContext);
                    initFacesContext.release();

                    return;
                }
            } else {
                LOGGER.log(FINE, "FacesServlet found in deployment descriptor - processing configuration.");
            }
        }

        // Do not override if already defined
        if (!webConfig.isSet(WebConfiguration.BooleanWebContextInitParameter.EnableDistributable)) {
            webConfig.setOptionEnabled(WebConfiguration.BooleanWebContextInitParameter.EnableDistributable, webXmlProcessor.isDistributablePresent());
        }
        if (webConfig.isOptionEnabled(WebConfiguration.BooleanWebContextInitParameter.EnableDistributable)) {
            servletContext.setAttribute(WebConfiguration.BooleanWebContextInitParameter.EnableDistributable.getQualifiedName(), TRUE);
        }

        // Bootstrap of faces required
        webAppListener = new WebappLifecycleListener(servletContext);
        webAppListener.contextInitialized(servletContextEvent);
        ReflectionUtils.initCache(Thread.currentThread().getContextClassLoader());
        Throwable caughtThrowable = null;

        try {
            if (LOGGER.isLoggable(INFO)) {
                LOGGER.log(INFO, "faces.config.listener.version", servletContext.getContextPath());
            }

            if (webConfig.isOptionEnabled(VerifyFacesConfigObjects)) {
                LOGGER.warning(
                    "JSF1059: WARNING!  The com.sun.faces.verifyObjects feature is to aid developers not using tools.  " +
                    "It shouldn't be enabled if using an IDE, or if this application is being deployed for production as it " +
                    "will impact application start times.");

                // If we're verifying, force bean validation to occur at startup as well
                webConfig.overrideContextInitParameter(EnableLazyBeanValidation, false);
                Verifier.setCurrentInstance(new Verifier());
            }

            configManager.initialize(servletContext, initFacesContext);

            if (shouldInitConfigMonitoring()) {
                initConfigMonitoring(servletContext);
            }

            // Step 7, verify that all the configured factories are available
            // and optionally that configured objects can be created.
            Verifier verifier = Verifier.getCurrentInstance();
            if (verifier != null && !verifier.isApplicationValid() && LOGGER.isLoggable(SEVERE)) {
                LOGGER.severe("faces.config.verifyobjects.failures_detected");
                StringBuilder sb = new StringBuilder(128);
                for (String msg : verifier.getMessages()) {
                    sb.append(msg).append('\n');
                }
                LOGGER.severe(sb.toString());
            }

            ApplicationAssociate associate = ApplicationAssociate.getInstance(servletContext);
            if (associate != null) {
                associate.setExpressionFactory(ELManager.getExpressionFactory());
            }

            initFacesContext.setELContext(new ELContextImpl(initFacesContext));

            if (associate != null) {
                associate.setContextName(servletContext.getContextPath());

                boolean isErrorPagePresent = webXmlProcessor.isErrorPagePresent();
                associate.setErrorPagePresent(isErrorPagePresent);
                servletContext.setAttribute(ERROR_PAGE_PRESENT_KEY_NAME, isErrorPagePresent);
            }

            // Register websocket endpoint if explicitly enabled.
            // Note: websocket channel filter is registered in FacesInitializer.
            if (ContextParam.ENABLE_WEBSOCKET_ENDPOINT.isSet(initFacesContext)) {
                ServerContainer serverContainer = (ServerContainer) servletContext.getAttribute(ServerContainer.class.getName());

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
            servletContextEvent.getServletContext().removeAttribute(ANNOTATED_CLASSES);
            servletContextEvent.getServletContext().removeAttribute(FACES_SERVLET_MAPPINGS);

            Verifier.setCurrentInstance(null);

            LOGGER.log(FINE, "faces.config.listener.version.complete");

            if (timer != null) {
                timer.stopTiming();
                timer.logResult("Initialization of context " + servletContext.getContextPath());
            }

            if (caughtThrowable != null) {
                throw new RuntimeException(caughtThrowable);
            }

            // Bug 20458755: The InitFacesContext was not being cleaned up, resulting in
            // a partially constructed FacesContext being made available
            // to other code that re-uses this Thread at init time.
            initFacesContext.releaseCurrentInstance();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        ConfigManager configManager = ConfigManager.getInstance(context);

        // The additional check for a WebConfiguration instance was added at the request of JBoss
        if (configManager == null && WebConfiguration.getInstanceWithoutCreating(context) != null) {
            LOGGER.log(WARNING,
               "Unexpected state during contextDestroyed: no ConfigManager instance in current ServletContext but one is expected to exist.");
        }

        InitFacesContext initContext = null;
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext instanceof InitFacesContext) {
                initContext = (InitFacesContext) facesContext;
            } else {
                initContext = new InitFacesContext(context);
            }

            if (webAppListener != null) {
                webAppListener.contextDestroyed(sce);
                webAppListener = null;
            }

            if (webResourcePool != null) {
                webResourcePool.shutdownNow();
            }

            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, "ConfigureListener.contextDestroyed({0})", context.getServletContextName());
            }

            if (configManager == null || !configManager.hasBeenInitialized(context)) {
                return;
            }

            ApplicationAssociate associate = ApplicationAssociate.getInstance(context);
            if (associate != null) {
                associate.setExpressionFactory(ELManager.getExpressionFactory());
            }

            initContext.setELContext(new ELContextImpl(initContext));
            Application application = initContext.getApplication();

            application.publishEvent(initContext, PreDestroyApplicationEvent.class, Application.class, application);

        } catch (Exception e) {
            LOGGER.log(SEVERE, "Unexpected exception when attempting to tear down the Mojarra runtime", e);
        } finally {
            ApplicationAssociate.clearInstance(context);
            ApplicationAssociate.setCurrentInstance(null);

            // Release the initialization mark on this web application
            if (configManager != null) {
                configManager.destroy(context, initContext);
                ConfigManager.removeInstance(context);
            } else if (WebConfiguration.getInstanceWithoutCreating(context) != null && LOGGER.isLoggable(WARNING)) {
                LOGGER.log(WARNING, "Unexpected state during contextDestroyed: no ConfigManager instance in current ServletContext but one is expected to exist.");
            }

            FactoryFinder.releaseFactories();
            ReflectionUtils.clearCache(Thread.currentThread().getContextClassLoader());
            WebConfiguration.clear(context);
            if (initContext != null) {
                initContext.release();
            }
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
        createMutex(event.getSession());

        if (webAppListener != null) {
            webAppListener.sessionCreated(event);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        removeMutex(event.getSession());

        if (webAppListener != null) {
            webAppListener.sessionDestroyed(event);
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
        return ContextParam.PROJECT_STAGE.getValue(FacesContext.getCurrentInstance()) == ProjectStage.Development;
    }

    /**
     * This method will be invoked {@link WebConfigResourceMonitor} when changes to any of the faces-config.xml files
     * included in WEB-INF are modified.
     */
    private void reload(ServletContext servletContext) {
        LOGGER.log(INFO, () -> format("Reloading Faces configuration for context {0}", servletContext.getContextPath()));

        // tear down the application
        try {
            // this will only be true in the automated test usage scenario
            if (webAppListener != null) {
                List<HttpSession> sessions = webAppListener.getActiveSessions();
                if (sessions != null) {
                    for (HttpSession session : sessions) {
                        if (LOGGER.isLoggable(INFO)) {
                            LOGGER.log(INFO, "Invalidating Session {0}", session.getId());
                        }
                        session.invalidate();
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
                LOGGER.log(SEVERE, "Unexpected state during reload: no ConfigManager instance in current ServletContext but one is expected to exist.");
            }

            initContext.release();
            ReflectionUtils.clearCache(Thread.currentThread().getContextClassLoader());
            WebConfiguration.clear(servletContext);
        }

        // Bring the application back up.
        // No verification will be performed either to make this light weight.

        // init a new WebAppLifecycleListener so that the cached ApplicationAssociate
        // is removed.
        webAppListener = new WebappLifecycleListener(servletContext);

        InitFacesContext initContext = new InitFacesContext(servletContext);
        ReflectionUtils.initCache(Thread.currentThread().getContextClassLoader());

        try {
            ConfigManager configManager = ConfigManager.createInstance(servletContext);
            if (configManager != null) {
                configManager.initialize(servletContext, initContext);
            } else {
                LOGGER.log(SEVERE, "Unexpected state during reload: no ConfigManager instance in current ServletContext but one is expected to exist.");
            }

            ApplicationAssociate associate = ApplicationAssociate.getInstance(servletContext);

            if (associate != null) {
                Boolean errorPagePresent = (Boolean) servletContext.getAttribute(ERROR_PAGE_PRESENT_KEY_NAME);
                if (errorPagePresent != null) {
                    associate.setErrorPagePresent(errorPagePresent);
                    associate.setContextName(servletContext.getContextPath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            initContext.release();
        }

        LOGGER.log(INFO, () -> format("Reload complete. ({0})", servletContext.getContextPath()));
    }


    // ----------------------------------------------------------- Inner classes

    /**
     * <p>
     * Processes a web application's deployment descriptor looking for a reference to
     * <code>jakarta.faces.webapp.FacesServlet</code>.
     * </p>
     */
    private static class WebXmlProcessor {

        private static final String WEB_XML_PATH = "/WEB-INF/web.xml";
        private static final String WEB_FRAGMENT_PATH = "META-INF/web-fragment.xml";

        private boolean facesServletPresent;
        private boolean errorPagePresent;
        private boolean distributablePresent;

        /**
         * <p>
         * When instantiated, the web.xml of the current application will be scanned looking for a references to the
         * <code>FacesServlet</code>. <code>isFacesServletPresent()</code> will return the appropriate value based on the scan.
         * </p>
         *
         * @param context the <code>ServletContext</code> for the application of interest
         */
        WebXmlProcessor(ServletContext context) {
            if (context != null) {
                scanForFacesServlet(context);
            }
        }

        /**
         * @return <code>true</code> if the <code>WebXmlProcessor</code> detected a <code>FacesServlet</code> entry, otherwise
         * return <code>false</code>.
         * </p>
         */
        boolean isFacesServletPresent() {
            return facesServletPresent;
        }

        /**
         * @return <code>true</code> if <code>WEB-INF/web.xml</code> contains a <code>&lt;error-page&gt;</code> element.
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
         * <p>
         * Parse the web.xml for the current application and scan for a FacesServlet entry, if found, set the
         * <code>facesServletPresent</code> property to true.
         *
         * @param context the ServletContext instance for this application
         */
        private void scanForFacesServlet(ServletContext context) {
            InputStream in = context.getResourceAsStream(WEB_XML_PATH);
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
                            LOGGER.log(FINEST, "Closing stream", ioe);
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
                            factory.newSAXParser().parse(fragmentStream, new WebXmlHandler());
                        } catch (IOException | ParserConfigurationException | SAXException e) {
                            warnProcessingError(e, context);
                            facesServletPresent = true;
                            return;
                        } finally {
                            if (fragmentStream != null) {
                                try {
                                    fragmentStream.close();
                                } catch (IOException ioe) {
                                    LOGGER.log(WARNING, "Exception whil scanning for FacesServlet", ioe);
                                }
                            }
                        }
                    }
                }
            }

        } // END scanForFacesServlet

        /**
         * <p>
         * Return a <code>SAXParserFactory</code> instance that is non-validating and is namespace aware.
         * </p>
         *
         * @return configured <code>SAXParserFactory</code>
         */
        private SAXParserFactory getConfiguredFactory() {
            SAXParserFactory factory = Util.createSAXParserFactory();
            factory.setValidating(false);
            factory.setNamespaceAware(true);

            return factory;
        }

        private void warnProcessingError(Exception e, ServletContext servletContext) {
            LOGGER.log(WARNING,format("JSF1078: Unable to process deployment descriptor for context ({0}).", servletContext.getContextPath() ), e);
        }

        /**
         * <p>
         * A simple SAX handler to process the elements of interested within a web application's deployment descriptor.
         * </p>
         */
        private class WebXmlHandler extends DefaultHandler {

            private static final String ERROR_PAGE = "error-page";
            private static final String SERVLET_CLASS = "servlet-class";
            private static final String FACES_SERVLET = "jakarta.faces.webapp.FacesServlet";

            private boolean servletClassFound;
            private StringBuffer content;

            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
                return new InputSource(new StringReader(""));
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if (!errorPagePresent && ERROR_PAGE.equals(localName)) {
                    errorPagePresent = true;
                    return;
                }

                if (!facesServletPresent) {
                    if (SERVLET_CLASS.equals(localName)) {
                        servletClassFound = true;
                        content = new StringBuffer();
                    } else {
                        servletClassFound = false;
                    }
                }

                if ("distributable".equals(localName)) {
                    distributablePresent = true;
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                if (servletClassFound && !facesServletPresent) {
                    content.append(ch, start, length);
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                if (servletClassFound && !facesServletPresent && FACES_SERVLET.equals(content.toString().trim())) {
                    facesServletPresent = true;
                }
            }

        } // END WebXmlHandler

    } // END WebXmlProcessor


    private class WebConfigResourceMonitor implements Runnable {

        private List<Monitor> monitors;
        private final ServletContext servletContext;

        // -------------------------------------------------------- Constructors

        public WebConfigResourceMonitor(ServletContext servletContext, Collection<URI> uris) {
            this.servletContext = servletContext;
            for (URI uri : uris) {
                if (monitors == null) {
                    monitors = new ArrayList<>(uris.size());
                }

                try {
                    monitors.add(new Monitor(uri));
                } catch (IOException ioe) {
                    LOGGER.log(SEVERE, () -> "Unable to setup resource monitor for " + uri.toString() + ".  Resource will not be monitored for changes.");
                    LOGGER.log(FINE, ioe, ioe::toString);
                }
            }

        }

        // ----------------------------------------------- Methods from Runnable

        /**
         * PENDING javadocs
         */
        @Override
        public void run() {
            boolean reloaded = false;
            for (Iterator<Monitor> i = monitors.iterator(); i.hasNext();) {
                Monitor m = i.next();
                try {
                    if (m.hasBeenModified()) {
                        if (!reloaded) {
                            reloaded = true;
                        }
                    }
                } catch (IOException ioe) {
                    if (LOGGER.isLoggable(SEVERE)) {
                        LOGGER.severe("Unable to access url " + m.uri.toString() + ".  Monitoring for this resource will no longer occur.");
                    }
                    if (LOGGER.isLoggable(FINE)) {
                        LOGGER.log(FINE, ioe.toString(), ioe);
                    }
                    i.remove();
                }
            }
            if (reloaded) {
                reload(servletContext);
            }
        }

        // ------------------------------------------------------- Inner Classes

        private class Monitor {

            private final URI uri;
            private long timestamp = -1;

            // ---------------------------------------------------- Constructors

            Monitor(URI uri) throws IOException {
                this.uri = uri;
                timestamp = getLastModified();
                if (LOGGER.isLoggable(INFO)) {
                    LOGGER.log(INFO, "Monitoring {0} for modifications", uri.toURL().toExternalForm());
                }

            }

            // ----------------------------------------- Package Private Methods

            boolean hasBeenModified() throws IOException {
                long temp = getLastModified();
                if (timestamp < temp) {
                    timestamp = temp;
                    if (LOGGER.isLoggable(INFO)) {
                        LOGGER.log(INFO, "{0} changed!", uri.toURL().toExternalForm());
                    }
                    return true;
                }

                return false;
            }

            // ------------------------------------------------- Private Methods

            private long getLastModified() throws IOException {
                InputStream in = null;
                try {
                    URLConnection connection = uri.toURL().openConnection();
                    connection.connect();
                    in = connection.getInputStream();
                    return connection.getLastModified();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException ignored) {
                            LOGGER.log(FINEST, "Exception while closing stream", ignored);
                        }
                    }
                }

            }

        } // END Monitor

    } // END WebConfigResourceMonitor

}
