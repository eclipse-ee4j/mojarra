/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Contributors to Eclipse Foundation.
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

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.Facelet;
import jakarta.faces.view.facelets.FaceletCache;

public final class MDSFaceletCache extends FaceletCache<Facelet> {

    private final FaceletCache<Facelet> _defaultFaceletCache;

    public MDSFaceletCache(FaceletCache<Facelet> defaultFaceletCache) {
        _defaultFaceletCache = defaultFaceletCache;
    }

    @Override
    public void setCacheFactories(MemberFactory faceletFactory, MemberFactory viewMetadataFaceletFactory) {
        _defaultFaceletCache.setCacheFactories(faceletFactory, viewMetadataFaceletFactory);
    }

    @Override
    public Facelet getFacelet(URL url) throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().getRequestMap().put("message", "" + System.currentTimeMillis());

        return _defaultFaceletCache.getFacelet(url);
    }

    @Override
    public boolean isFaceletCached(URL url) {
        return _defaultFaceletCache.isFaceletCached(url);
    }

    @Override
    public Facelet getViewMetadataFacelet(URL url) throws IOException {
        return _defaultFaceletCache.getViewMetadataFacelet(url);
    }

    @Override
    public boolean isViewMetadataFaceletCached(URL url) {
        return _defaultFaceletCache.isViewMetadataFaceletCached(url);
    }

}
