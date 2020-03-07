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
import static java.lang.System.arraycopy;
import static java.util.Collections.emptyList;

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
 *
 */
public class MetaInfFaceletTaglibraryConfigProvider implements ConfigurationResourceProvider {

    private static final String SUFFIX = ".taglib.xml";
    private static final String WEB_INF_CLASSES = "/WEB-INF/classes/META-INF";

    /**
     * Array of taglib.xml files included with Facelets 1.1.x. If they are on the classpath, we don't want to process them.
     */
    private static final String[] FACELET_CONFIG_FILES = { "META-INF/jsf-core.taglib.xml", "META-INF/jsf-html.taglib.xml", "META-INF/jsf-ui.taglib.xml",
            "META-INF/jstl-core.taglib.xml", "META-INF/jstl-fn.taglib.xml" };

    private static final String[] BUILT_IN_TAGLIB_XML_FILES = { "META-INF/mojarra_ext.taglib.xml"

    };

    // -------------------------------------------- Methods from ConfigProcessor

    @Override
    public Collection<URI> getResources(ServletContext context) {

        try {
            URL[] externalTaglibUrls = Classpath.search(getCurrentLoader(this), "META-INF/", SUFFIX);
            URL[] builtInTaglibUrls = new URL[BUILT_IN_TAGLIB_XML_FILES.length];
            ClassLoader runtimeClassLoader = this.getClass().getClassLoader();

            for (int i = 0; i < BUILT_IN_TAGLIB_XML_FILES.length; i++) {
                builtInTaglibUrls[i] = runtimeClassLoader.getResource(BUILT_IN_TAGLIB_XML_FILES[i]);
            }

            URL[] urls = new URL[externalTaglibUrls.length + builtInTaglibUrls.length];
            arraycopy(externalTaglibUrls, 0, urls, 0, externalTaglibUrls.length);
            arraycopy(builtInTaglibUrls, 0, urls, externalTaglibUrls.length, builtInTaglibUrls.length);

            // Perform some 'correctness' checking. If the user has
            // removed the FaceletViewHandler from their configuration,
            // but has left the jsf-facelets.jar in the classpath, we
            // need to ignore the default configuration resouces from
            // that JAR.
            List<URI> urlsList = pruneURLs(urls);

            // Special case for finding taglib files in WEB-INF/classes/META-INF
            Set<String> paths = context.getResourcePaths(WEB_INF_CLASSES);
            if (paths != null) {
                for (String path : paths) {
                    if (path.endsWith(".taglib.xml")) {
                        try {
                            urlsList.add(new URI(context.getResource(path).toExternalForm().replaceAll(" ", "%20")));
                        } catch (URISyntaxException ex) {
                            throw new FacesException(ex);
                        }
                    }
                }
            }

            return urlsList;
        } catch (IOException ioe) {
            throw new FacesException("Error searching classpath from facelet-taglib documents", ioe);
        }

    }

    // --------------------------------------------------------- Private Methods

    private List<URI> pruneURLs(URL[] urls) {

        List<URI> ret = null;
        if (urls != null && urls.length > 0) {
            for (URL url : urls) {
                String u = url.toString();
                boolean found = false;
                for (String excludeName : FACELET_CONFIG_FILES) {
                    if (u.contains(excludeName)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    if (ret == null) {
                        ret = new ArrayList<>();
                    }

                    try {
                        ret.add(new URI(url.toExternalForm().replaceAll(" ", "%20")));
                    } catch (URISyntaxException ex) {
                        throw new FacesException(ex);
                    }
                }
            }
        }

        if (ret == null) {
            ret = emptyList();
        }

        return ret;
    }

}
