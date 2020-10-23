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

package com.sun.faces.facelets;

import java.io.IOException;
import java.net.URL;

import com.sun.faces.facelets.FaceletCache.InstanceFactory;

import jakarta.faces.view.facelets.FaceletCache;

public class PrivateApiFaceletCacheAdapter<V> extends FaceletCache<V> {

    private com.sun.faces.facelets.FaceletCache<V> privateApi;

    public PrivateApiFaceletCacheAdapter(com.sun.faces.facelets.FaceletCache<V> privateApi) {
        this.privateApi = privateApi;
    }

    @Override
    public V getFacelet(URL url) throws IOException {
        return privateApi.getFacelet(url);
    }

    @Override
    public V getViewMetadataFacelet(URL url) throws IOException {
        return privateApi.getMetadataFacelet(url);
    }

    @Override
    public boolean isFaceletCached(URL url) {
        return privateApi.isFaceletCached(url);
    }

    @Override
    public boolean isViewMetadataFaceletCached(URL url) {
        return privateApi.isMetadataFaceletCached(url);
    }

    private MemberFactory<V> memberFactory;
    private MemberFactory<V> metadataMemberFactory;

    @Override
    public void setMemberFactories(final MemberFactory<V> faceletFactory, final MemberFactory<V> viewMetadataFaceletFactory) {
        InstanceFactory<V> instanceFactory = key -> faceletFactory.newInstance(key);
        InstanceFactory<V> metadataInstanceFactory = key -> viewMetadataFaceletFactory.newInstance(key);

        privateApi.init(instanceFactory, metadataInstanceFactory);
    }

    @Override
    public MemberFactory<V> getMemberFactory() {
        return memberFactory;
    }

    @Override
    public MemberFactory<V> getMetadataMemberFactory() {
        return metadataMemberFactory;
    }

}
