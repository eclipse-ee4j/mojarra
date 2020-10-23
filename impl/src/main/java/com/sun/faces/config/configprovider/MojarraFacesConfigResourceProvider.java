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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sun.faces.spi.ConfigurationResourceProvider;

import jakarta.faces.FacesException;
import jakarta.servlet.ServletContext;

/**
 *
 */
public class MojarraFacesConfigResourceProvider implements ConfigurationResourceProvider {

    private static final String JSF_RI_CONFIG = "com/sun/faces/jsf-ri-runtime.xml";

    // ------------------------------ Methods from ConfigurationResourceProvider

    /**
     * @see ConfigurationResourceProvider#getResources(jakarta.servlet.ServletContext)
     */
    @Override
    public Collection<URI> getResources(ServletContext context) {

        List<URI> list = new ArrayList<>(1);
        // Don't use Util.getCurrentLoader(). This config resource should
        // be available from the same classloader that loaded this instance.
        // Doing so allows us to be more OSGi friendly.
        ClassLoader loader = this.getClass().getClassLoader();
        try {
            URL url = loader.getResource(JSF_RI_CONFIG);
            String urlStr = url.toExternalForm();
            if (urlStr.contains(" ")) {
                urlStr = urlStr.replaceAll(" ", "%20");
            }

            list.add(new URI(urlStr));
        } catch (URISyntaxException ex) {
            throw new FacesException(ex);
        }
        return list;

    }

}
