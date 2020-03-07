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

package com.sun.faces.config.manager.tasks;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.faces.config.manager.FacesConfigInfo;
import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.spi.AnnotationScanner;
import com.sun.faces.spi.InjectionProvider;

public final class ProvideMetadataToAnnotationScanTask {

    private static final Pattern JAR_PATTERN = Pattern.compile("(.*/(\\S*\\.jar)).*(/faces-config.xml|/*.\\.faces-config.xml)");

    private final DocumentInfo[] documentInfos;
    private final InjectionProvider containerConnector;

    private Set<URI> uris;
    private Set<String> jarNames;

    public ProvideMetadataToAnnotationScanTask(DocumentInfo[] documentInfos, InjectionProvider containerConnector) {
        this.documentInfos = documentInfos;
        this.containerConnector = containerConnector;
    }

    private void initializeIvars(Set<Class<?>> annotatedSet) {
        if (uris != null || jarNames != null) {
            return;
        }

        uris = new HashSet<>(documentInfos.length);
        jarNames = new HashSet<>(documentInfos.length);

        for (DocumentInfo docInfo : documentInfos) {

            URI sourceURI = docInfo.getSourceURI();
            Matcher jarMatcher = JAR_PATTERN.matcher(sourceURI == null ? "" : sourceURI.toString());

            if (jarMatcher.matches()) {
                String jarName = jarMatcher.group(2);
                if (!jarNames.contains(jarName)) {
                    FacesConfigInfo configInfo = new FacesConfigInfo(docInfo);
                    if (!configInfo.isMetadataComplete()) {
                        uris.add(sourceURI);
                        jarNames.add(jarName);
                    } else {
                        /*
                         * Because the container annotation scanning does not know anything about faces-config.xml metadata-complete the
                         * annotatedSet of classes will include classes that are not supposed to be included.
                         *
                         * The code below looks at the CodeSource of the class and determines whether or not it should be removed from the
                         * annotatedSet because the faces-config.xml that owns it has metadata-complete="true".
                         */
                        ArrayList<Class<?>> toRemove = new ArrayList<>(1);
                        String sourceURIString = sourceURI.toString();
                        if (annotatedSet != null) {
                            for (Class<?> clazz : annotatedSet) {
                                if (sourceURIString.contains(clazz.getProtectionDomain().getCodeSource().getLocation().toString())) {
                                    toRemove.add(clazz);
                                }
                            }
                            annotatedSet.removeAll(toRemove);
                        }
                    }
                }
            }
        }
    }

    public Set<URI> getAnnotationScanURIs(Set<Class<?>> annotatedSet) {
        initializeIvars(annotatedSet);

        return uris;

    }

    public Set<String> getJarNames(Set<Class<?>> annotatedSet) {
        initializeIvars(annotatedSet);

        return jarNames;
    }

    public AnnotationScanner getAnnotationScanner() {
        if (containerConnector instanceof AnnotationScanner) {
            return (AnnotationScanner) containerConnector;
        }

        return null;
    }
}
