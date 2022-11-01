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

package com.sun.faces.spi;

import static java.util.logging.Level.WARNING;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.RIConstants;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.config.WebConfiguration.WebContextInitParameter;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;
import com.sun.faces.vendor.WebContainerInjectionProvider;

import jakarta.faces.context.ExternalContext;
import jakarta.servlet.ServletContext;

/**
 * <p>
 * A factory for creating <code>InjectionProvider</code> instances.
 * </p>
 */

public class InjectionProviderFactory {

    /**
     * <p>
     * Our no-op <code>InjectionProvider</code>.
     * </p>
     */
    private static final InjectionProvider NOOP_PROVIDER = new NoopInjectionProvider();

    private static final InjectionProvider GENERIC_WEB_PROVIDER = new WebContainerInjectionProvider();

    private static final String INJECTION_SERVICE = "META-INF/services/com.sun.faces.spi.injectionprovider";

    /**
     * <p>
     * The system property that will be checked for alternate <code>InjectionProvider</code> implementations.
     * </p>
     */
    private static final String INJECTION_PROVIDER_PROPERTY = RIConstants.FACES_PREFIX + "InjectionProvider";

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    private static final String[] EMPTY_ARRAY = new String[] {};

    /**
     * <p>
     * Creates a new instance of the class specified by the <code>com.sun.faces.InjectionProvider</code> system property. If
     * this propery is not defined, then a default, no-op, <code>InjectionProvider</code> will be returned.
     *
     * @param extContext ExteranlContext for the current request
     *
     * @return an implementation of the <code>InjectionProvider</code> interfaces
     */
    public static InjectionProvider createInstance(ExternalContext extContext) {

        String providerClass = findProviderClass(extContext);
        InjectionProvider provider = getProviderInstance(providerClass, extContext);

        if (!NoopInjectionProvider.class.equals(provider.getClass()) && !WebContainerInjectionProvider.class.equals(provider.getClass())) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "faces.spi.injection.provider_configured", new Object[] { provider.getClass().getName() });
            }
            return provider;
        } else if (WebContainerInjectionProvider.class.equals(provider.getClass())) {
            LOGGER.info("faces.core.injection.provider_generic_web_configured");
            return provider;
        } else {
            LOGGER.log(WARNING, "faces.spi.injection.no_injection");
            return provider;
        }

    }

    private static InjectionProvider getProviderInstance(String className, ExternalContext extContext) {

        InjectionProvider provider = NOOP_PROVIDER;
        if (className != null) {
            try {
                Class<?> clazz = Util.loadClass(className, InjectionProviderFactory.class);
                if (implementsInjectionProvider(clazz)) {
                    try {
                        Constructor ctor = clazz.getConstructor(ServletContext.class);
                        return (InjectionProvider) ctor.newInstance((ServletContext) extContext.getContext());
                    } catch (NoSuchMethodException nsme) {
                        return (InjectionProvider) clazz.getDeclaredConstructor().newInstance();
                    } catch (InvocationTargetException ite) {
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.log(Level.SEVERE, "faces.spi.injection.provider_cannot_instantiate", new Object[] { className });
                            LOGGER.log(Level.SEVERE, "", ite);
                        }
                    }
                } else {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE, "faces.spi.injection.provider_not_implemented", new Object[] { className });
                    }
                }
            } catch (ClassNotFoundException cnfe) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "faces.spi.injection.provider_not_found", new Object[] { className });
                }
            } catch (IllegalArgumentException | ReflectiveOperationException | SecurityException ie) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "faces.spi.injection.provider_cannot_instantiate", new Object[] { className });
                    LOGGER.log(Level.SEVERE, "", ie);
                }
            }
        }

        // We weren't able to find a configured provider - check
        // to see if the PostConstruct and PreDestroy annotations
        // are available. If they are, then default to the
        // WebContainerInjectionProvider, otherwise, use
        // NoopInjectionProvider
        if (NOOP_PROVIDER.equals(provider)) {
            try {
                if (Util.loadClass("jakarta.annotation.PostConstruct", null) != null && Util.loadClass("jakarta.annotation.PreDestroy", null) != null) {
                    provider = GENERIC_WEB_PROVIDER;
                }
            } catch (Exception e) {
                provider = NOOP_PROVIDER;
            }
        }

        return provider;

    }

    /**
     * <p>
     * Determine if the specified class implements the <code>InjectionProvider</code> interfaces.
     * </p>
     *
     * @param clazz the class in question
     * @return <code>true</code> if <code>clazz</code> implements the <code>InjectionProvider</code> interface
     */
    private static boolean implementsInjectionProvider(Class<?> clazz) {

        return InjectionProvider.class.isAssignableFrom(clazz);

    }

    /**
     * <p>
     * Determine if the specified class extends the <code>DiscoverableInjectionProvider</code> interfaces.
     * </p>
     *
     * @param clazz the class in question
     * @return <code>true</code> if <code>clazz</code> implements the <code>InjectionProvider</code> interface
     */
    private static boolean extendsDiscoverableInjectionProvider(Class<?> clazz) {

        return DiscoverableInjectionProvider.class.isAssignableFrom(clazz);

    }

    /**
     * <p>
     * Attempt to find an <code>InjectionProvider</code> based on the following algorithm:
     * </p>
     * <ul>
     * <li>Check for an explicit configuration within the web.xml using the key
     * <code>com.sun.faces.injectionProvider</code>. If found, return the value.</li>
     * <li>Check for a system property keyed by <code>com.sun.faces.InjectionProvider</code>. If found, return the
     * value.</li>
     * <li>Check for entries within <code>META-INF/services/com.sun.faces.injectionprovider</code>. If entries are found and
     * the entries extend <code>DiscoverableInjectionProvider</code>, invoke
     * <code>isInjectionFeatureAvailable(String)</code> passing in the configured delegate. If
     * <code>isInjectionFeatureAvailable(String)</code> returns <code>true</code> return the service entry.</li>
     * <li>If no <code>InjectionProviders are found, return <code>null</code></li> Tries to find a provider class in a web
     * context parameter. If not present it tries to find it as a System property. If still not found returns null.
     * <ul>
     *
     * @param extContext The ExternalContext for this request
     * @return The provider class name specified in the container configuration, or <code>null</code> if not found.
     */
    private static String findProviderClass(ExternalContext extContext) {

        WebConfiguration webConfig = WebConfiguration.getInstance(extContext);
        String provider = webConfig.getOptionValue(WebContextInitParameter.InjectionProviderClass);

        if (provider != null) {
            return provider;
        } else {
            provider = System.getProperty(INJECTION_PROVIDER_PROPERTY);
        }

        if (provider != null) {
            return provider;
        } else {
            String[] serviceEntries = getServiceEntries();
            if (serviceEntries.length > 0) {
                for (int i = 0; i < serviceEntries.length; i++) {
                    provider = getProviderFromEntry(extContext.getApplicationMap(), serviceEntries[i]);
                    if (provider != null) {
                        break;
                    }
                }
            } else {
                return provider;
            }
        }

        return provider;

    }

    private static String getProviderFromEntry(Map<String, Object> appMap, String entry) {

        if (entry == null) {
            return null;
        }

        String[] parts = Util.split(appMap, entry, ":");
        if (parts.length != 2) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "faces.spi.injection.invalid_service_entry", new Object[] { entry });
            }
            return null;
        }

        try {
            Class<?> clazz = Util.loadClass(parts[0], null);
            if (extendsDiscoverableInjectionProvider(clazz)) {
                if (DiscoverableInjectionProvider.isInjectionFeatureAvailable(parts[1])) {
                    return parts[0];
                }
            } else {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "faces.spi.injection.provider.entry_not_discoverable", new Object[] { parts[0] });
                }
                return null;
            }
        } catch (ClassNotFoundException cnfe) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "faces.spi.injection.provider_not_found", new Object[] { parts[0] });
            }
            return null;
        }

        return null;

    }

    private static String[] getServiceEntries() {

        List<String> results = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            return EMPTY_ARRAY;
        }

        Enumeration<URL> urls = null;
        try {
            urls = loader.getResources(INJECTION_SERVICE);
        } catch (IOException ioe) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, ioe.toString(), ioe);
            }
        }

        if (urls != null) {
            InputStream input = null;
            BufferedReader reader = null;
            while (urls.hasMoreElements()) {
                try {
                    if (results == null) {
                        results = new ArrayList<>();
                    }
                    URL url = urls.nextElement();
                    URLConnection conn = url.openConnection();
                    conn.setUseCaches(false);
                    input = conn.getInputStream();
                    if (input != null) {
                        try {
                            reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                        } catch (Exception e) {
                            // The DM_DEFAULT_ENCODING warning is acceptable here
                            // because we explicitly *want* to use the Java runtime's
                            // default encoding.
                            reader = new BufferedReader(new InputStreamReader(input));
                        }
                        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                            results.add(line.trim());
                        }
                    }
                } catch (Exception e) {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE, "faces.spi.provider.cannot_read_service", new Object[] { INJECTION_SERVICE });
                        LOGGER.log(Level.SEVERE, e.toString(), e);
                    }
                } finally {
                    if (input != null) {
                        try {
                            input.close();
                        } catch (Exception e) {
                            if (LOGGER.isLoggable(Level.FINEST)) {
                                LOGGER.log(Level.FINEST, "Closing stream", e);
                            }
                        }
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Exception e) {
                            if (LOGGER.isLoggable(Level.FINEST)) {
                                LOGGER.log(Level.FINEST, "Closing stream", e);
                            }
                        }
                    }
                }
            }
        }

        return results != null && !results.isEmpty() ? results.toArray(new String[results.size()]) : EMPTY_ARRAY;

    }

    /**
     * <p>
     * A no-op implementation of <code>InjectionProvider</code> which will be used when the #INJECTION_PROVIDER_PROPERTY is
     * not specified or is invalid.
     * </p>
     */
    private static final class NoopInjectionProvider implements InjectionProvider, AnnotationScanner {

        /**
         * <p>
         * This is a no-op.
         * </p>
         *
         * @param managedBean target ManagedBean
         */
        @Override
        public void inject(Object managedBean) {
        }

        @Override
        public Map<String, List<AnnotationScanner.ScannedAnnotation>> getAnnotatedClassesInCurrentModule(ServletContext extContext)
                throws InjectionProviderException {
            return Collections.emptyMap();
        }

        /**
         * <p>
         * This is a no-op.
         * </p>
         *
         * @param managedBean target ManagedBean
         */
        @Override
        public void invokePreDestroy(Object managedBean) {
        }

        /**
         * <p>
         * This is a no-op.
         * </p>
         *
         * @param managedBean target ManagedBean
         */
        @Override
        public void invokePostConstruct(Object managedBean) throws InjectionProviderException {
        }

    }

} // END InjectionProviderFactory
