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

package com.sun.faces.application;

import static com.sun.faces.util.Util.coalesce;
import static com.sun.faces.util.Util.getCurrentLoader;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * <p>
 * Contains an application level resource bundle name and its associated descriptions, if any.
 * </p>
 */
public class ApplicationResourceBundle {

    public static final String DEFAULT_KEY = "DEFAULT";

    private final String baseName;
    private final Map<String, String> displayNames;
    private final Map<String, String> descriptions;
    private volatile Map<Locale, ResourceBundle> resources;

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Constructs a new ApplicationResourceBundle
     * </p>
     *
     * @param baseName the base name of the <code>ResourceBundle</code>
     * @param displayNames any display names that were associated with the resource bundle definition in the configuration
     * resource
     * @param descriptions any descriptions that were associated with the resource bundle definition in the configuration
     * resource
     */
    public ApplicationResourceBundle(String baseName, Map<String, String> displayNames, Map<String, String> descriptions) {

        if (baseName == null) {
            throw new IllegalArgumentException();
        }
        this.baseName = baseName;
        this.displayNames = displayNames;
        this.descriptions = descriptions;
        resources = new HashMap<>(4, 1.0f);
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * @return the base name of the <code>ResourceBundle</code> associated with this <code>ApplicationResourceBundle</code>
     * instance
     */
    public String getBaseName() {
        return baseName;
    }

    /**
     * @param locale a <code>Locale</code>
     * @return return the <code>ResourceBundle</code> associated with the specified </code>locale</code>
     */
    public ResourceBundle getResourceBundle(Locale locale) {

        if (locale == null) {
            locale = Locale.getDefault();
        }

        ResourceBundle bundle = resources.get(locale);
        if (bundle == null) {
            ClassLoader loader = getCurrentLoader(this);
            synchronized (this) {
                bundle = resources.get(locale);
                if (bundle == null) {
                    bundle = ResourceBundle.getBundle(baseName, locale, loader);
                    resources.put(locale, bundle);
                }
            }
        }

        return bundle;
    }

    /**
     * @param locale a <code>Locale</code>
     * @return a text of a <code>display-name</code> element associated with the specified </code>locale</code>
     */
    public String getDisplayName(Locale locale) {

        String displayName = null;
        if (displayNames != null) {
            displayName = queryMap(locale, displayNames);
        }

        return coalesce(displayName, "");
    }

    /**
     * @param locale a <code>Locale</code>
     * @return a text of a <code>description</code> element associated with the specified </code>locale</code>
     */
    public String getDescription(Locale locale) {

        String description = null;
        if (descriptions != null) {
            description = queryMap(locale, descriptions);
        }

        return coalesce(description, "");
    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>
     * Lookup and return the text for the specified <code>Locale</code> from within the specified <code>Map</code>.
     * </p>
     *
     * @param locale <code>Locale</code> if interest
     * @param map a map containing localized text keyed by <code>Locale</code>
     * @return localized text, if any
     */
    private String queryMap(Locale locale, Map<String, String> map) {

        if (locale == null) {
            return map.get(DEFAULT_KEY);
        }

        String description = map.get(locale.toString());

        if (description == null) {
            return map.get(DEFAULT_KEY);
        }

        return null;
    }

}
