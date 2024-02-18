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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.facelets.util.Classpath;
import com.sun.faces.spi.ConfigurationResourceProvider;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.servlet.ServletContext;

/**
 *
 */
public class MetaInfFacesConfigResourceProvider implements ConfigurationResourceProvider {

    /**
     * <p>
     * This <code>Pattern</code> will pick the the JAR file name if present within a URL.
     * </p>
     */
    private static final Pattern JAR_PATTERN = Pattern.compile(".*/(\\S*\\.jar).*");

    /**
     * <p>
     * The resource path for faces-config files included in the <code>META-INF</code> directory of JAR files.
     * </p>
     */
    private static final String META_INF_RESOURCES = "META-INF/faces-config.xml";

    private static final String WEB_INF_CLASSES = "/WEB-INF/classes/META-INF";

    private static final String FACES_CONFIG_EXTENSION = ".faces-config.xml";

    // ------------------------------ Methods From ConfigurationResourceProvider

    /**
     * @see com.sun.faces.spi.ConfigurationResourceProvider#getResources(jakarta.servlet.ServletContext)
     */
    @Override
    public Collection<URI> getResources(ServletContext context) {

        WebConfiguration webConfig = WebConfiguration.getInstance(context);
        String duplicateJarPattern = webConfig.getOptionValue(WebConfiguration.WebContextInitParameter.DuplicateJARPattern);
        Pattern duplicatePattern = null;
        if (duplicateJarPattern != null) {
            duplicatePattern = Pattern.compile(duplicateJarPattern);
        }
        SortedMap<String, Set<URI>> sortedJarMap = new TreeMap<>();
        // noinspection CollectionWithoutInitialCapacity
        List<URI> unsortedResourceList = new ArrayList<>();

        try {
            for (URI uri : loadURLs(context)) {

                String jarUrl = uri.toString();
                String jarName = null;
                Matcher m = JAR_PATTERN.matcher(jarUrl);
                if (m.matches()) {
                    jarName = m.group(1);
                }
                if (jarName != null) {
                    if (duplicatePattern != null) {
                        m = duplicatePattern.matcher(jarName);
                        if (m.matches()) {
                            jarName = m.group(1);
                        }
                    }

                    Set<URI> uris = sortedJarMap.computeIfAbsent(jarName, k -> new HashSet<>());
                    uris.add(uri);
                } else {
                    unsortedResourceList.add(0, uri);
                }
            }
        } catch (IOException e) {
            throw new FacesException(e);
        }
        // Load the sorted resources first:
        List<URI> result = new ArrayList<>(sortedJarMap.size() + unsortedResourceList.size());
        for (Map.Entry<String, Set<URI>> entry : sortedJarMap.entrySet()) {
            result.addAll(entry.getValue());
        }
        // Then load the unsorted resources
        result.addAll(unsortedResourceList);

        return result;

    }

    // --------------------------------------------------------- Private Methods

    private Collection<URI> loadURLs(ServletContext context) throws IOException {

        Set<URI> urls = new HashSet<>();
        try {
            for (Enumeration<URL> e = Util.getCurrentLoader(this).getResources(META_INF_RESOURCES); e.hasMoreElements();) {
                String urlString = e.nextElement().toExternalForm();
                urlString = urlString.replace(" ", "%20");
                urls.add(new URI(urlString));
            }
            URL[] urlArray = Classpath.search("META-INF/", FACES_CONFIG_EXTENSION);
            for (URL cur : urlArray) {
                String urlString = cur.toExternalForm();
                urlString = urlString.replace(" ", "%20");
                urls.add(new URI(urlString));
            }
            // special case for finding taglib files in WEB-INF/classes/META-INF
            Set<String> paths = context.getResourcePaths(WEB_INF_CLASSES);
            if (paths != null) {
                for (Object path : paths) {
                    String p = path.toString();
                    if (p.endsWith(FACES_CONFIG_EXTENSION)) {
                        String urlString = context.getResource(p).toExternalForm();
                        urlString = urlString.replace(" ", "%20");
                        urls.add(new URI(urlString));
                    }
                }
            }
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
        return urls;

    }
}
