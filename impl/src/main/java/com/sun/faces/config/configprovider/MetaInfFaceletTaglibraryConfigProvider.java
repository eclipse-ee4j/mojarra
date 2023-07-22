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

import static com.sun.faces.util.Util.getCurrentLoader;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.sun.faces.facelets.util.Classpath;
import com.sun.faces.spi.ConfigurationResourceProvider;

import jakarta.faces.FacesException;
import jakarta.servlet.ServletContext;

/**
 * This resource providers finds all .taglib.xml files in the system.
 *
 */
public class MetaInfFaceletTaglibraryConfigProvider implements ConfigurationResourceProvider {

    private static final String SUFFIX = ".taglib.xml";
    private static final String WEB_INF_CLASSES = "/WEB-INF/classes/META-INF";


    // -------------------------------------------- Methods from ConfigProcessor

    @Override
    public Collection<URI> getResources(ServletContext context) {

        try {
            List<URL> resourceURLs = new ArrayList<>(asList(Classpath.search(getCurrentLoader(this), "META-INF/", SUFFIX)));

            // Special case for finding taglib files in WEB-INF/classes/META-INF
            Set<String> paths = context.getResourcePaths(WEB_INF_CLASSES);
            if (paths != null) {
                for (String path : paths) {
                    if (path.endsWith(SUFFIX)) {
                        resourceURLs.add(context.getResource(path));
                    }
                }
            }

            return resourceURLs.stream()
                               .map( url -> transformToURI(url) )
                               .collect(toList());

        } catch (IOException ioe) {
            throw new FacesException("Error searching classpath from facelet-taglib documents", ioe);
        }
    }

    // --------------------------------------------------------- Private Methods

    private static URI transformToURI(URL url) {
        try {
            return new URI(url.toExternalForm().replace(" ", "%20"));
        } catch (URISyntaxException ex) {
            throw new FacesException(ex);
        }
    }

}
