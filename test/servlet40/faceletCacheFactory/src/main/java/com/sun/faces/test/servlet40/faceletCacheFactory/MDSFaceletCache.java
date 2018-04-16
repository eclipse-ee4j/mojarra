/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.servlet40.faceletCacheFactory;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.Facelet;
import javax.faces.view.facelets.FaceletCache;

public final class MDSFaceletCache
        extends FaceletCache {

    public MDSFaceletCache(FaceletCache defaultFaceletCache) {
        _defaultFaceletCache = defaultFaceletCache;
    }

    @Override
    public void setCacheFactories(MemberFactory faceletFactory, MemberFactory viewMetadataFaceletFactory) {
        _defaultFaceletCache.setCacheFactories(faceletFactory, viewMetadataFaceletFactory);
    }

    @Override
    public Object getFacelet(URL url)
            throws IOException {
        if (_isMDSUrl(url)) {
            return _getFacelet(url);
        } else {
            FacesContext context = FacesContext.getCurrentInstance();
            context.getExternalContext().getRequestMap().put("message", "" + System.currentTimeMillis());
            return _defaultFaceletCache.getFacelet(url);
        }
    }

    @Override
    public boolean isFaceletCached(URL url) {
        if (_isMDSUrl(url)) {
            // only way to know if it is cached is to try and get one, we might end up creating the facelet,
            //  but then it is fine because we cache it
            return _getFacelet(url) != null;
        } else {
            return _defaultFaceletCache.isFaceletCached(url);
        }
    }

    @Override
    public Object getViewMetadataFacelet(URL url)
            throws IOException {
        if (_isMDSUrl(url)) {
            return _getViewMetadataFacelet(url);
        } else {
            return _defaultFaceletCache.getViewMetadataFacelet(url);
        }
    }

    @Override
    public boolean isViewMetadataFaceletCached(URL url) {
        if (_isMDSUrl(url)) {
            // only way to know if it is cached is to try and get one, we might
            // end up creating the facelet, but then it is fine because we 
            // cache it
            return _getViewMetadataFacelet(url) != null;
        } else {
            return _defaultFaceletCache.isViewMetadataFaceletCached(url);
        }
    }

    private Object _getFacelet(URL url) {
        return _getOrCreateFacelet(url, Key._FACELET_KEY, _getFaceletFactory());
    }

    private Object _getViewMetadataFacelet(URL url) {
        return _getOrCreateFacelet(url, Key._METADATA_FACELET_KEY, _getMetadataFaceletFactory());
    }

    private Object _getOrCreateFacelet(
            URL url,
            Key key,
            ClientObjectFactory faceletFactory) {
        if (null == url) {
            throw new NullPointerException("The supplied url is null");
        }

        throw new IllegalStateException("Unimplemented");
    }

    private ClientObjectFactory _getFaceletFactory() {
        if (_faceletFactory == null) {
            _faceletFactory = new ADFFaceletsClientObjectFactory(getMemberFactory());
        }

        return _faceletFactory;
    }

    private ClientObjectFactory _getMetadataFaceletFactory() {
        if (_metadataFaceletFactory == null) {
            _metadataFaceletFactory = new ADFFaceletsClientObjectFactory(getMetadataMemberFactory());
        }

        return _metadataFaceletFactory;
    }

    private boolean _isMDSUrl(URL url) {
        return false;
    }

    /**
     * Implementation of ClientObjectFactory that MDS will callback. This
     * implementation delegates to appropriate Facelet instance factory.
     */
    private static class ADFFaceletsClientObjectFactory implements ClientObjectFactory {

        public ADFFaceletsClientObjectFactory(MemberFactory<Facelet> faceletFactory) {
            _faceletFactory = faceletFactory;
        }

        public Object createClientObject(Object mdsObject, Object factoryContext) {
            try {
                return _faceletFactory.newInstance((URL) factoryContext);
            } catch (IOException ioe) {
                _LOG.log(Level.FINE, "FACELET_CREATION_ERROR", ioe);
                return null;
            }
        }

        private final MemberFactory<Facelet> _faceletFactory;
    }

    static private enum Key {

        _FACELET_KEY("facelet"),
        _METADATA_FACELET_KEY("metadataFacelet");

        Key(String keyString) {
            this._keyString = keyString;
        }

        public String getKeyString() {
            return _keyString;
        }

        private final String _keyString;
    };

    private volatile ClientObjectFactory _faceletFactory;
    private volatile ClientObjectFactory _metadataFaceletFactory;

    private static interface ClientObjectFactory {

    }

    private final FaceletCache _defaultFaceletCache;

    // defined in oracle.mds.internal.MDSConstants.MDS_PROTOCOL_FACELETS, 
    // temporarily declare it here until we get it in API package from MDS team.
    //
    private static final Logger _LOG = Logger.getLogger("javax.enterprise.resource.webcontainer.jsf.facelets.factory", "com.sun.faces.LogStrings");
}
