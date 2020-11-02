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

package com.sun.faces.config.manager.spi;

import static com.sun.faces.RIConstants.ANNOTATED_CLASSES;
import static com.sun.faces.config.WebConfiguration.WebContextInitParameter.AnnotationScanPackages;
import static com.sun.faces.config.manager.spi.AnnotationScanner.FACES_ANNOTATION_TYPE;
import static com.sun.faces.util.Util.isEmpty;
import static java.util.Arrays.stream;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.spi.AnnotationProvider;

import jakarta.servlet.ServletContext;

/**
 * This class is the default implementation of AnnotationProvider referenced by the AnnotationProviderFactory. Unless
 * someone manually provides one via in META-INF/services, this is the one that will actually be instantiated and
 * installed into Mojarra.
 *
 * <p>
 * This class just further filters the classes that have already been obtained by the ServletContainerInitializer and
 * stored in the ServletContext using the key <code>RIConstants.ANNOTATED_CLASSES</code>
 *
 */
public class FilterClassesFromFacesInitializerAnnotationProvider extends AnnotationProvider {

    public FilterClassesFromFacesInitializerAnnotationProvider(ServletContext servletContext) {
        super(servletContext);
    }

    // ---------------------------------------------------------- Public Methods

    /*
     * This will only be called if the InjectionProvider offered by the container implements
     * com.sun.faces.spi.AnnotationScanner.
     */
    public void setAnnotationScanner(com.sun.faces.spi.AnnotationScanner containerConnector, Set<String> jarNamesWithoutMetadataComplete) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Class<? extends Annotation>, Set<Class<?>>> getAnnotatedClasses(Set<URI> urls) {
        return createAnnotatedMap((Set<Class<?>>) servletContext.getAttribute(ANNOTATED_CLASSES));
    }

    // ---------------------------------------------------------- Private Methods

    /**
     * Go over the annotated set and converter it to a hash map.
     *
     * @param annotatedMap
     * @param annotatedSet
     */
    private Map<Class<? extends Annotation>, Set<Class<?>>> createAnnotatedMap(Set<Class<?>> annotatedSet) {
        HashMap<Class<? extends Annotation>, Set<Class<?>>> annotatedMap = new HashMap<>();

        if (isEmpty(annotatedSet)) {
            return annotatedMap;
        }

        WebConfiguration webConfig = WebConfiguration.getInstance();
        boolean annotationScanPackagesSet = webConfig.isSet(AnnotationScanPackages);

        String[] annotationScanPackages = annotationScanPackagesSet ? webConfig.getOptionValue(AnnotationScanPackages).split("\\s+") : null;

        Iterator<Class<?>> iterator = annotatedSet.iterator();
        while (iterator.hasNext()) {
            try {
                Class<?> clazz = iterator.next();

                stream(clazz.getAnnotations()).map(annotation -> annotation.annotationType())
                        .filter(annotationType -> FACES_ANNOTATION_TYPE.contains(annotationType)).forEach(annotationType -> {

                            Set<Class<?>> classes = annotatedMap.computeIfAbsent(annotationType, e -> new HashSet<>());

                            if (annotationScanPackagesSet) {
                                if (matchesAnnotationScanPackages(clazz, annotationScanPackages)) {
                                    classes.add(clazz);
                                }
                            } else {
                                classes.add(clazz);
                            }

                        });

            } catch (NoClassDefFoundError ncdfe) {
            }
        }

        return annotatedMap;
    }

    private boolean matchesAnnotationScanPackages(Class<?> clazz, String[] annotationScanPackages) {
        boolean result = false;

        for (int i = 0; i < annotationScanPackages.length; i++) {

            String classUrlString = clazz.getProtectionDomain().getCodeSource().getLocation().toString();
            String classPackageName = clazz.getPackage().getName();

            if (classUrlString.contains("WEB-INF/classes") && annotationScanPackages[i].equals("*")) {
                result = true;
            } else if (classPackageName.equals(annotationScanPackages[i])) {
                result = true;
            } else if (annotationScanPackages[i].startsWith("jar:")) {
                String jarName = annotationScanPackages[i].substring(4, annotationScanPackages[i].indexOf(":", 5));
                String jarPackageName = annotationScanPackages[i].substring(annotationScanPackages[i].lastIndexOf(":") + 1);
                if (jarName.equals("*")) {
                    if (jarPackageName.equals("*")) {
                        result = true;
                    } else if (jarPackageName.equals(classPackageName)) {
                        result = true;
                    }
                } else if (classUrlString.contains(jarName) && jarPackageName.equals("*")) {
                    result = true;
                } else if (classUrlString.contains(jarName) && jarPackageName.equals(classPackageName)) {
                    result = true;
                }
            }
        }

        return result;
    }
}
