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

package com.sun.faces.application.resource;

import static com.sun.faces.util.Util.notNull;
import static java.util.logging.Level.FINE;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.config.WebConfiguration.WebContextInitParameter;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MultiKeyConcurrentHashMap;

import jakarta.servlet.ServletContext;

/**
 * <p>
 * This is the caching mechanism for caching ResourceInfo instances to offset the cost of looking up the resource.
 * </p>
 *
 * <p>
 * This cache uses a background thread to check for modifications to the underlying webapp or JAR files containing
 * resources. This check is periodic, configurable via context init param
 * <code>com.sun.faces.resourceUpdateCheckPeriod</code>. Through this config option, the cache can also be made static
 * or completely disabled. If the value of of this option is <code>0</code>, then no check will be made making the cache
 * static. If value of this option is <code>less than 0</code>, then no caching will be perfomed. Otherwise, the value
 * of the option will be the number of minutes between modification checks.
 * </p>
 */
public class ResourceCache {

    private static final Logger LOGGER = FacesLogger.RESOURCE.getLogger();

    /**
     * The <code>ResourceInfo<code> cache.
     */
    private MultiKeyConcurrentHashMap<Object, ResourceInfoCheckPeriodProxy> resourceCache;

    /**
     * Resource check period in minutes.
     */
    private long checkPeriod;

    // ------------------------------------------------------------ Constructors

    /**
     * Constructs a new ResourceCache.
     */
    public ResourceCache() {
        this(WebConfiguration.getInstance());
    }

    private ResourceCache(WebConfiguration config) {
        this(getCheckPeriod(config));

        if (LOGGER.isLoggable(FINE)) {
            ServletContext sc = config.getServletContext();
            LOGGER.log(FINE, "ResourceCache constructed for {0}.  Check period is {1} minutes.",
                    new Object[] { getServletContextIdentifier(sc), checkPeriod });
        }
    }

    // this one is for unit tests
    ResourceCache(long period) {
        checkPeriod = period != -1 ? period * 1000L * 60L : -1;
        resourceCache = new MultiKeyConcurrentHashMap<>(30);
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * Add the {@link ResourceInfo} to the internal cache.
     *
     * @param info resource metadata
     *
     * @param contracts the contracts
     * @return previous value associated with specified key, or null if there was no mapping for key
     */
    public ResourceInfo add(ResourceInfo info, List<String> contracts) {
        notNull("info", info);

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE, "Caching ResourceInfo: {0}", info.toString());
        }
        ResourceInfoCheckPeriodProxy proxy = resourceCache.putIfAbsent(info.name, info.libraryName, info.localePrefix, new ArrayList(contracts),
                new ResourceInfoCheckPeriodProxy(info, checkPeriod));
        return proxy != null ? proxy.getResourceInfo() : null;

    }

    /**
     * @param name the resource name
     * @param libraryName the library name
     * @param localePrefix the locale prefix
     * @param contracts the contracts
     * @return the {@link ResourceInfo} associated with <code>key</code> if any.
     */
    public ResourceInfo get(String name, String libraryName, String localePrefix, List<String> contracts) {
        notNull("name", name);

        ResourceInfoCheckPeriodProxy proxy = resourceCache.get(name, libraryName, localePrefix, contracts);
        if (proxy != null && proxy.needsRefreshed()) {
            resourceCache.remove(name, libraryName, localePrefix, contracts);
            return null;
        }
        
        return proxy != null ? proxy.getResourceInfo() : null;
    }

    /**
     * <p>
     * Empty the cache.
     * </p>
     */
    public void clear() {
        resourceCache.clear();
        LOGGER.log(FINE, "Cache Cleared");
    }

    // --------------------------------------------------------- Private Methods

    private static Long getCheckPeriod(WebConfiguration webConfig) {
        String val = webConfig.getOptionValue(WebContextInitParameter.ResourceUpdateCheckPeriod);
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException nfe) {
            return Long.parseLong(WebContextInitParameter.ResourceUpdateCheckPeriod.getDefaultValue());
        }
    }

    private static String getServletContextIdentifier(ServletContext context) {
        return context.getContextPath();
    }

    // ---------------------------------------------------------- Nested Classes

    private static final class ResourceInfoCheckPeriodProxy {

        private final ResourceInfo resourceInfo;
        private Long checkTime;

        // -------------------------------------------------------- Constructors

        public ResourceInfoCheckPeriodProxy(ResourceInfo resourceInfo, long checkPeriod) {

            this.resourceInfo = resourceInfo;
            if (checkPeriod != -1L && !(resourceInfo.getHelper() instanceof ClasspathResourceHelper)) {
                checkTime = System.currentTimeMillis() + checkPeriod;
            }
        }

        private boolean needsRefreshed() {
            return checkTime != null && checkTime < System.currentTimeMillis();
        }

        private ResourceInfo getResourceInfo() {
            return resourceInfo;
        }

    } // END ResourceInfoCheckPeriodProxy

}
