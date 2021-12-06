/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.faces.view.facelets;

import java.io.IOException;
import java.net.URL;

/**
 * <p class="changed_added_2_1">
 * <span class="changed_modified_2_3">This</span> API defines the facility by which the Facelets
 * {@link jakarta.faces.view.ViewDeclarationLanguage} creates and caches instances of Facelets.
 * </p>
 *
 * <p class="changed_added_2_1">
 * The cache handles two different kinds of Facelets: View Facelets and View Metadata Facelets. The former is the usual
 * Facelet that provides for the construction of a <code>UIComponent</code> tree. This kind of Facelet is accessed via
 * the {@link #getFacelet} and {@link #isFaceletCached} methods. The latter is a special kind of Facelet that
 * corresponds to {@link jakarta.faces.view.ViewDeclarationLanguage#getViewMetadata}. This kind of Facelet is accessed
 * via the {@link #getViewMetadataFacelet} and {@link #isViewMetadataFaceletCached} methods.
 * </p>
 *
 * @since 2.1
 */
public abstract class FaceletCache<V> {

    private MemberFactory<V> memberFactory;
    private MemberFactory<V> viewMetadataMemberFactory;

    /**
     * <p class="changed_added_2_1">
     * Factory interface for creating Facelet or View Metadata Facelet instances.
     * </p>
     */
    public interface MemberFactory<V> {

        /**
         * <p class="changed_added_2_1">
         * Create a Facelet or View Metadata Facelet (depending on the type of factory this is) for the argument URL.
         * </p>
         *
         * @param key the <code>URL</code> that will be used as the key for the instance being created.
         *
         * @throws NullPointerException if argument <code>key</code> is <code>null</code>.
         *
         * @throws IOException if unable to load a file necessary to respond to service this method.
         *
         * @return the newly created {@code Facelet} or View Metadata {@code Facelet}
         */
        V newInstance(final URL key) throws IOException;
    }

    /**
     * <p class="changed_added_2_1">
     * Returns a cached Facelet instance. If no instance is available, it will be created using the Facelet
     * {@link MemberFactory} and stored in the cache.
     * </p>
     *
     * @param url <code>URL</code> for the Facelet being retrieved
     *
     * @throws NullPointerException if argument <code>url</code> is <code>null</code>.
     *
     * @throws IOException if unable to load a file necessary to respond to service this method.
     *
     * @return a cached or new {@code Facelet}
     *
     */
    public abstract V getFacelet(URL url) throws IOException;

    /**
     *
     * <p class="changed_added_2_1">
     * Determines whether a cached Facelet instance exists for this URL. Returns true if a cached instance exists, false
     * otherwise
     * </p>
     *
     * @param url <code>URL</code> for the Facelet
     *
     * @throws NullPointerException if argument <code>url</code> is <code>null</code>.
     *
     * @return true if a cached instance exists, false otherwise
     *
     */
    public abstract boolean isFaceletCached(URL url);

    /**
     * <p class="changed_added_2_1">
     * Returns a cached View Metadata Facelet instance. If no instance is available, it will be created using the View
     * Metadata Facelet {@link MemberFactory} and stored in the cache.
     * </p>
     *
     * @param url <code>URL</code> for the View Metadata Facelet being retrieved
     *
     * @throws NullPointerException if argument <code>url</code> is <code>null</code>.
     *
     * @throws IOException if unable to load a file necessary to respond to service this method.
     *
     * @return a cached or new View Metadata {@code Facelet} instance
     *
     */
    public abstract V getViewMetadataFacelet(URL url) throws IOException;

    /**
     * <p class="changed_added_2_1">
     * Determines whether a cached View Metadata Facelet instance exists for this URL. Returns true if a cached instance
     * exists, false otherwise
     * </p>
     *
     * @param url <code>URL</code> for the View Metadata Facelet
     *
     * @throws NullPointerException if argument <code>url</code> is <code>null</code>.
     *
     * @return true if a cached instance exists, false otherwise
     */
    public abstract boolean isViewMetadataFaceletCached(URL url);

    /**
     *
     * <p class="changed_added_2_3">
     * This must be called by the runtime at startup time, before any requests are serviced, and allows for the
     * <code>FaceletCache</code> implementation to provide the {@link MemberFactory} instances that will be used to create
     * instances of Facelets and View Metadata Facelets.
     * </p>
     *
     * @param faceletFactory the {@link MemberFactory} instance that will be used to create instances of Facelets.
     *
     * @param viewMetadataFaceletFactory the {@link MemberFactory} instance that will be used to create instances of
     * metadata Facelets.
     *
     * @throws NullPointerException if either argument is <code>null</code>
     *
     * @since 2.3
     */
    public void setCacheFactories(MemberFactory<V> faceletFactory, MemberFactory<V> viewMetadataFaceletFactory) {
        if (null == faceletFactory || null == viewMetadataFaceletFactory) {
            throw new NullPointerException("Neither faceletFactory no viewMetadataFaceletFactory may be null.");
        }

        this.memberFactory = faceletFactory;
        this.viewMetadataMemberFactory = viewMetadataFaceletFactory;
    }

    /**
     * <p class="changed_added_2_1">
     * Returns the {@link MemberFactory} passed to {@link #setCacheFactories} for the purpose of creating Facelet instance.
     * </p>
     *
     * @return the {@link MemberFactory} passed to {@link #setCacheFactories} for the purpose of creating Facelet instance.
     */
    protected MemberFactory<V> getMemberFactory() {
        return memberFactory;
    }

    /**
     * <p class="changed_added_2_1">
     * Returns the {@link MemberFactory} passed to {@link #setCacheFactories} for the purpose of creating View Metadata
     * Facelet instance.
     * </p>
     *
     * @return the {@link MemberFactory} passed to {@link #setCacheFactories} for the purpose of creating View Metadata
     * Facelet instance.
     */
    protected MemberFactory<V> getMetadataMemberFactory() {
        return viewMetadataMemberFactory;
    }

}
