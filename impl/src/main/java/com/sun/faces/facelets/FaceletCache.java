/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.facelets;

import java.io.IOException;
import java.net.URL;

@Deprecated
public abstract class FaceletCache<V> {

    /**
     * Factory interface for creating Facelets.
     */
    @Deprecated
    public interface InstanceFactory<V> {
        V newInstance(final URL key) throws IOException;
    }

    /**
     * Retrieves a cached Facelet
     *
     * @param url URL for the Facelet being retrieved
     * @return cached Facelet instance, If no instance is available, it will be created using the Facelet InstanceFactory
     * and stored in the cache
     */
    public abstract V getFacelet(URL url) throws IOException;

    /**
     * Determines whether a cached Facelet instance exists for this URL
     *
     * @param url URL for the Facelet
     * @return true if a cached instance exists, false otherwise
     */
    public abstract boolean isFaceletCached(URL url);

    /**
     * Retrieves a cached Metadata Facelet
     *
     * @param url URL for the Metadata Facelet being retrieved
     * @return cached Metadata Facelet instance, If no instance is available, it will be created using the Metadata Facelet
     * InstanceFactory and stored in the cache
     */
    public abstract V getMetadataFacelet(URL url) throws IOException;

    /**
     * Determines whether a cached Metadata Facelet instance exists for this URL
     *
     * @param url URL for the Metadata Facelet
     * @return true if a cached instance exists, false otherwise
     */
    public abstract boolean isMetadataFaceletCached(URL url);

    /**
     * Initializes this cache instance.
     *
     * @param faceletFactory <code>InstanceFactory</code> for creating Facelet instances
     * @param metafaceletFactory <code>InstanceFactory</code> for creating Metadata Facelet instances
     */
    public final void init(InstanceFactory<V> faceletFactory, InstanceFactory<V> metafaceletFactory) {
        _faceletFactory = faceletFactory;
        _metafaceletFactory = metafaceletFactory;
    }

    /**
     * Retrieves InstanceFactory for creating Facelets
     *
     * @return factory for creating Facelets
     */
    protected final InstanceFactory<V> getFaceletInstanceFactory() {
        return _faceletFactory;
    }

    /**
     * Retrieves InstanceFactory for creating Metadata Facelets
     *
     * @return factory for creating MetadataFacelets
     */
    protected final InstanceFactory<V> getMetadataFaceletInstanceFactory() {
        return _metafaceletFactory;
    }

    private InstanceFactory<V> _faceletFactory;
    private InstanceFactory<V> _metafaceletFactory;
}
