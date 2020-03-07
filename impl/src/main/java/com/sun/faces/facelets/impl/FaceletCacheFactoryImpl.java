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

package com.sun.faces.facelets.impl;

import com.sun.faces.config.WebConfiguration;

import jakarta.faces.view.facelets.FaceletCache;
import jakarta.faces.view.facelets.FaceletCacheFactory;

/**
 * Default implementation of {@link FaceletCacheFactory}.
 */
public class FaceletCacheFactoryImpl extends FaceletCacheFactory {

    public FaceletCacheFactoryImpl() {
        super(null);
    }

    @Override
    public FaceletCache getFaceletCache() {
        WebConfiguration webConfig = WebConfiguration.getInstance();
        String refreshPeriod = webConfig.getOptionValue(WebConfiguration.WebContextInitParameter.FaceletsDefaultRefreshPeriod);
        long period = Long.parseLong(refreshPeriod) * 1000;
        FaceletCache<DefaultFacelet> result = new DefaultFaceletCache(period);
        return result;

    }

}
