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

package com.sun.faces.test.servlet30.factory;

import javax.faces.view.facelets.FaceletCache;
import javax.faces.view.facelets.FaceletCacheFactory;

public class CustomFaceletCacheFactory extends FaceletCacheFactory {

    private boolean oneArgCtorCalled = false;
    private FaceletCache _cache;
    private FaceletCacheFactory _wrapped;

    public boolean isOneArgCtorCalled() {
        return oneArgCtorCalled;
    }

    @Override
    public FaceletCacheFactory getWrapped() {
        return _wrapped;
    }

    public CustomFaceletCacheFactory() {
        super();
        oneArgCtorCalled = false;
    }

    public CustomFaceletCacheFactory(FaceletCacheFactory wrapped) {
        oneArgCtorCalled = true;
        _wrapped = wrapped;
    }

    @Override
    public FaceletCache getFaceletCache() {
        FaceletCacheFactory wrapped = getWrapped();

        if (_cache == null) {
            _cache = wrapped.getFaceletCache();
        }

        return _cache;
    }
}
