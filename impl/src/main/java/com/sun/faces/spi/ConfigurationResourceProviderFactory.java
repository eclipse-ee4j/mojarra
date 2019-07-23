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

package com.sun.faces.spi;


import java.util.List;

import static com.sun.faces.spi.ServiceFactoryUtils.getProviderFromEntry;
import static com.sun.faces.spi.ServiceFactoryUtils.getServiceEntries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;

import com.sun.faces.util.FacesLogger;

/**
 * Factory class for creating <code>ConfigurationResourceProvider</code> instances
 * using the Java services discovery mechanism.
 */
public class ConfigurationResourceProviderFactory {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    public enum ProviderType {

        /**
         * ConfigurationResourceProvider type for configuration resources that follow the faces-config DTD/Schema.
         */
        FacesConfig(FacesConfigResourceProvider.SERVICES_KEY),

        /**
         * ConfigurationResourceProvider type for configuration resources that follow the Facelet taglib DTD/Schema.
         */
        FaceletConfig(FaceletConfigResourceProvider.SERVICES_KEY);

        String servicesKey;

        ProviderType(String servicesKey) {
            this.servicesKey = servicesKey;
        }

    }


    // ---------------------------------------------------------- Public Methods


    /**
     * @param providerType the type of providers that should be discovered and instantiated.
     *
     * @return an array of all <code>ConfigurationResourceProviders discovered that
     *  match the specified <code>ProviderType</code>.
     */
    public static ConfigurationResourceProvider[] createProviders(ProviderType providerType) {

        String[] serviceEntries = getServiceEntries(providerType.servicesKey);
        List<ConfigurationResourceProvider> providers = new ArrayList<>();
        
        if (serviceEntries.length > 0) {
            for (String serviceEntry : serviceEntries) {
                try {
                    ConfigurationResourceProvider provider = (ConfigurationResourceProvider) getProviderFromEntry(serviceEntry, null, null);
                    
                    if (provider != null) {
                        if (ProviderType.FacesConfig == providerType) {
                            if (!(provider instanceof FacesConfigResourceProvider)) {
                                throw new IllegalStateException("Expected ConfigurationResourceProvider type to be an instance of FacesConfigResourceProvider");
                            }
                        } else {
                            if (!(provider instanceof FaceletConfigResourceProvider)) {
                                throw new IllegalStateException("Expected ConfigurationResourceProvider type to be an instance of FaceletConfigResourceProvider");
                            }
                        }
                        providers.add(provider);
                    }
                } catch (ClassCastException cce) {
                    // we are going to ignore these for now.
                } catch (FacesException e) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, e.toString(), e);
                    }
                }
            }
        } else {

            ServiceLoader serviceLoader;

            switch (providerType) {
                case FacesConfig:
                    serviceLoader = ServiceLoader.load(FacesConfigResourceProvider.class);
                break;
                case FaceletConfig:
                    serviceLoader = ServiceLoader.load(FaceletConfigResourceProvider.class);
                break;
                default:
                    throw new UnsupportedOperationException(providerType.servicesKey +
                        " cannot be loaded via ServiceLoader API.");
            }

            Iterator iterator = serviceLoader.iterator();

            while (iterator.hasNext()) {
                providers.add((ConfigurationResourceProvider) iterator.next());
            }
        }

        return providers.toArray(new ConfigurationResourceProvider[providers.size()]);
    }
}
