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

import static com.sun.faces.config.WebConfiguration.WebContextInitParameter.AnnotationScanPackages;
import static java.util.logging.Level.WARNING;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.spi.AnnotationProvider;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.behavior.FacesBehavior;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.event.NamedEvent;
import jakarta.faces.render.FacesBehaviorRenderer;
import jakarta.faces.render.FacesRenderer;
import jakarta.faces.validator.FacesValidator;
import jakarta.servlet.ServletContext;

/**
 * This class is responsible for ensuring that the class file bytes of classes contained within the web application are
 * scanned for any of the known Faces configuration Annotations:
 * <ul>
 * <li>jakarta.faces.component.FacesBehavior</li>
 * <li>jakarta.faces.render.FacesBehaviorRenderer</li>
 * <li>jakarta.faces.component.FacesComponent</li>
 * <li>jakarta.faces.convert.FacesConverter</li>
 * <li>jakarta.faces.validator.FacesValidator</li>
 * <li>jakarta.faces.render.FacesRenderer</li>
 * <li>jakarta.faces.event.NamedEvent</li>
 * <li>jakarta.faces.view.facelets.FaceletsResourceResolver</li>
 * </ul>
 */
public abstract class AnnotationScanner extends AnnotationProvider {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();
    private static final String WILDCARD = "*";

    protected static final Set<String> FACES_ANNOTATIONS = Set.of(
            "Ljakarta/faces/component/FacesComponent;", "Ljakarta/faces/convert/FacesConverter;",
            "Ljakarta/faces/validator/FacesValidator;", "Ljakarta/faces/render/FacesRenderer;",
            "Ljakarta/faces/event/NamedEvent;", "Ljakarta/faces/component/behavior/FacesBehavior;",
            "Ljakarta/faces/render/FacesBehaviorRenderer;",

            "jakarta.faces.component.FacesComponent", "jakarta.faces.convert.FacesConverter",
            "jakarta.faces.validator.FacesValidator", "jakarta.faces.render.FacesRenderer",
            "jakarta.faces.event.NamedEvent", "jakarta.faces.component.behavior.FacesBehavior",
            "jakarta.faces.render.FacesBehaviorRenderer");

    protected static final Set<Class<? extends Annotation>> FACES_ANNOTATION_TYPE = Set.of(
            FacesComponent.class, FacesConverter.class,
            FacesValidator.class, FacesRenderer.class,
            NamedEvent.class, FacesBehavior.class,
            FacesBehaviorRenderer.class);

    private boolean isAnnotationScanPackagesSet;
    private String[] webInfClassesPackages;
    private Map<String, String[]> classpathPackages;

    /**
     * Creates a new <code>AnnotationScanner</code> instance.
     *
     * @param sc the <code>ServletContext</code> for the application to be scanned
     */
    public AnnotationScanner(ServletContext sc) {
        super(sc);

        WebConfiguration webConfig = WebConfiguration.getInstance(sc);
        initializeAnnotationScanPackages(sc, webConfig);
    }

    private void initializeAnnotationScanPackages(ServletContext sc, WebConfiguration webConfig) {
        if (!webConfig.isSet(AnnotationScanPackages)) {
            return;
        }

        isAnnotationScanPackagesSet = true;
        classpathPackages = new HashMap<>(4);
        webInfClassesPackages = new String[0];
        String[] options = webConfig.getOptionValue(AnnotationScanPackages, "\\s+");
        List<String> packages = new ArrayList<>(4);
        for (String option : options) {
            if (option.length() == 0) {
                continue;
            }
            if (option.startsWith("jar:")) {
                String[] parts = Util.split(sc, option, ":");
                if (parts.length != 3) {
                    if (LOGGER.isLoggable(WARNING)) {
                        LOGGER.log(WARNING, "faces.annotation.scanner.configuration.invalid",
                                new String[] { AnnotationScanPackages.getQualifiedName(), option });
                    }
                } else {
                    if (WILDCARD.equals(parts[1]) && !classpathPackages.containsKey(WILDCARD)) {
                        classpathPackages.clear();
                        classpathPackages.put(WILDCARD, normalizeJarPackages(Util.split(sc, parts[2], ",")));
                    } else if (WILDCARD.equals(parts[1]) && classpathPackages.containsKey(WILDCARD)) {
                        if (LOGGER.isLoggable(WARNING)) {
                            LOGGER.log(WARNING, "faces.annotation.scanner.configuration.duplicate.wildcard",
                                    new String[] { AnnotationScanPackages.getQualifiedName(), option });
                        }
                    } else {
                        if (!classpathPackages.containsKey(WILDCARD)) {
                            classpathPackages.put(parts[1], normalizeJarPackages(Util.split(sc, parts[2], ",")));
                        }
                    }
                }
            } else {
                if (WILDCARD.equals(option) && !packages.contains(WILDCARD)) {
                    packages.clear();
                    packages.add(WILDCARD);
                } else {
                    if (!packages.contains(WILDCARD)) {
                        packages.add(option);
                    }
                }
            }
        }
        webInfClassesPackages = packages.toArray(new String[packages.size()]);
    }

    private String[] normalizeJarPackages(String[] packages) {
        if (packages.length == 0) {
            return packages;
        }

        List<String> normalizedPackages = new ArrayList<>(packages.length);
        for (String pkg : packages) {
            if (WILDCARD.equals(pkg)) {
                normalizedPackages.clear();
                normalizedPackages.add(WILDCARD);
                break;
            } else {
                normalizedPackages.add(pkg);
            }
        }

        return normalizedPackages.toArray(new String[normalizedPackages.size()]);
    }

    // --------------------------------------------------------- Protected Methods

    protected boolean processJar(String entry) {
        return classpathPackages == null || classpathPackages.containsKey(entry) || classpathPackages.containsKey(WILDCARD);
    }

    /**
     * @param candidate the class that should be processed
     * @return <code>true</code> if the class should be processed further, otherwise, <code>false</code>
     */
    protected boolean processClass(String candidate) {
        return processClass(candidate, webInfClassesPackages);
    }

    protected boolean processClass(String candidate, String[] packages) {
        if (packages == null) {
            return true;
        }

        for (String packageName : packages) {
            if (candidate.startsWith(packageName) || WILDCARD.equals(packageName)) {
                return true;
            }
        }

        return false;
    }

    protected Map<Class<? extends Annotation>, Set<Class<?>>> processClassList(Set<String> classList) {

        Map<Class<? extends Annotation>, Set<Class<?>>> annotatedClasses = null;
        if (classList.size() > 0) {
            annotatedClasses = new HashMap<>(6, 1.0f);
            for (String className : classList) {
                try {
                    Class<?> clazz = Util.loadClass(className, this);
                    Annotation[] annotations = clazz.getAnnotations();
                    for (Annotation annotation : annotations) {
                        Class<? extends Annotation> annoType = annotation.annotationType();
                        if (FACES_ANNOTATION_TYPE.contains(annoType)) {
                            Set<Class<?>> classes = annotatedClasses.computeIfAbsent(annoType, k -> new HashSet<>());
                            classes.add(clazz);
                        }
                    }
                } catch (ClassNotFoundException cnfe) {
                    // shouldn't happen..
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE, "Unable to load annotated class: {0}", className);
                        LOGGER.log(Level.SEVERE, "", cnfe);
                    }
                } catch (NoClassDefFoundError ncdfe) {
                    // this is more likely
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE, "Unable to load annotated class: {0}, reason: {1}", new Object[] { className, ncdfe.toString() });
                    }
                }
            }
        }

        return annotatedClasses != null ? annotatedClasses : Collections.emptyMap();
    }

    protected boolean isAnnotationScanPackagesSet() {
        return isAnnotationScanPackagesSet;
    }

    protected Map<String, String[]> getClasspathPackages() {
        return classpathPackages;
    }

    protected String[] getWebInfClassesPackages() {
        return webInfClassesPackages;
    }

}
