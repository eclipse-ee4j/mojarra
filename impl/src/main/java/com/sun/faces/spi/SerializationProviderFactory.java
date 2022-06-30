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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.RIConstants;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.config.WebConfiguration.WebContextInitParameter;
import com.sun.faces.renderkit.ApplicationObjectInputStream;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.faces.context.ExternalContext;

/**
 * <p>
 * A factory for creating <code>SerializationProvider</code> instances.
 * </p>
 */
public class SerializationProviderFactory {

    /**
     * <p>
     * Our default <code>SerializationProvider</code>.
     * </p>
     */
    private static final SerializationProvider JAVA_PROVIDER = new SerializationProviderFactory.JavaSerializationProvider();

    /**
     * <p>
     * The system property that will be checked for alternate <code>SerializationProvider</code> implementations.
     * </p>
     */
    private static final String SERIALIZATION_PROVIDER_PROPERTY = RIConstants.FACES_PREFIX + "SerializationProvider";

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    /**
     * <p>
     * Creates a new instance of the class specified by the <code>com.sun.faces.InjectionProvider</code> system property. If
     * this propery is not defined, then a default, no-op, <code>InjectionProvider</code> will be returned.
     *
     * @param extContext the ExternalContext for this application
     * @return an implementation of the <code>InjectionProvider</code> interfaces
     */
    public static SerializationProvider createInstance(ExternalContext extContext) {
        String providerClass = findProviderClass(extContext);

        SerializationProvider provider = getProviderInstance(providerClass);

        if (provider.getClass() != JavaSerializationProvider.class) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "faces.spi.serialization.provider_configured", new Object[] { provider.getClass().getName() });
            }
        }
        return provider;

    }

    private static SerializationProvider getProviderInstance(String className) {
        SerializationProvider provider = JAVA_PROVIDER;
        if (className != null) {
            try {
                Class<?> clazz = Util.loadClass(className, SerializationProviderFactory.class);
                if (implementsSerializationProvider(clazz)) {
                    provider = (SerializationProvider) clazz.getDeclaredConstructor().newInstance();
                } else {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE, "faces.spi.serialization.provider_not_implemented", new Object[] { className });
                    }
                }
            } catch (ClassNotFoundException cnfe) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "faces.spi.serialization.provider_not_found", new Object[] { className });
                }
            } catch (IllegalArgumentException | ReflectiveOperationException | SecurityException ie) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "faces.spi.serialization.provider_cannot_instantiate", new Object[] { className });
                    LOGGER.log(Level.SEVERE, "", ie);
                }
            }
        }

        return provider;
    }

    /**
     * <p>
     * Determine if the specified class implements the <code>SerializationProvider</code> interfaces.
     * </p>
     *
     * @param clazz the class in question
     * @return <code>true</code> if <code>clazz</code> implements the <code>SerializationProvider</code> interface
     */
    private static boolean implementsSerializationProvider(Class<?> clazz) {
        return SerializationProvider.class.isAssignableFrom(clazz);
    }

    /**
     * Tries to find a provider class in a web context parameter. If not present it tries to find it as a System property.
     * If still not found returns null.
     *
     * @param extContext The ExternalContext for this request
     * @return The provider class name specified in the container configuration, or <code>null</code> if not found.
     */
    private static String findProviderClass(ExternalContext extContext) {

        WebConfiguration webConfig = WebConfiguration.getInstance(extContext);

        String provider = webConfig.getOptionValue(WebContextInitParameter.SerializationProviderClass);

        if (provider != null) {
            return provider;
        } else {
            return System.getProperty(SERIALIZATION_PROVIDER_PROPERTY);
        }
    }

    /**
     * <p>
     * An implementation of <code>SerializationProvider</code> which uses standard Java serialization.
     * </p>
     */
    private static final class JavaSerializationProvider implements SerializationProvider {

        /**
         * <p>
         * Creates a new <code>ObjectOutputStream</code> wrapping the specified <code>destination</code>.
         * </p>
         *
         * @param destination the destination of the serialized Object(s)
         *
         * @return an <code>ObjectOutputStream</code>
         */
        @Override
        public ObjectOutputStream createObjectOutputStream(OutputStream destination) throws IOException {
            return new ObjectOutputStream(destination);
        }

        /**
         * <p>
         * Creates a new <code>ObjectInputStream</code> wrapping the specified <code>source</code>.
         * </p>
         *
         * @param source the source stream from which to read the Object(s) from
         *
         * @return an <code>ObjectInputStream</code>
         */
        @Override
        public ObjectInputStream createObjectInputStream(InputStream source) throws IOException {
            return new ApplicationObjectInputStream(source);
        }
    }

} // END InjectionProviderFactory
