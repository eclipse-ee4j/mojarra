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

package com.sun.faces.config.configprovider;

import java.net.URI;
import java.util.Collection;

import jakarta.faces.annotation.FacesConfig.ContextParam;
import jakarta.servlet.ServletContext;

/**
 *
 */
public class WebFacesConfigResourceProvider extends BaseWebConfigResourceProvider {

    /**
     * <p>
     * The resource path for the faces configuration in the <code>WEB-INF</code> directory of an application.
     * </p>
     */
    private static final String WEB_INF_RESOURCE = "/WEB-INF/faces-config.xml";

    private static final String[] EXCLUDES = { WEB_INF_RESOURCE };
    private static final String SEPARATORS = ",|;";

    // ------------------------------ Methods from ConfigurationResourceProvider

    /**
     * @see com.sun.faces.spi.ConfigurationResourceProvider#getResources(jakarta.servlet.ServletContext)
     */
    @Override
    public Collection<URI> getResources(ServletContext context) {

        Collection<URI> urls = super.getResources(context);

        // Step 5, parse "/WEB-INF/faces-config.xml" if it exists
        URI webFacesConfig = getContextURLForPath(context, WEB_INF_RESOURCE);
        if (webFacesConfig != null) {
            urls.add(webFacesConfig);
        }

        // PENDING (rlubke,driscoll) this is a temporary measure to prevent
        // having to find the web-based configuration resources twice
        context.setAttribute("com.sun.faces.webresources", urls);

        return urls;
    }

    // ------------------------------ Methods from BaseWebConfigResourceProvider

    @Override
    protected ContextParam getParameter() {
        return ContextParam.CONFIG_FILES;
    }

    @Override
    protected String[] getExcludedResources() {
        return EXCLUDES;
    }

    @Override
    protected String getSeparatorRegex() {
        return SEPARATORS;
    }
}
