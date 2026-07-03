/*
 * Copyright (c) Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.test.perf.beans;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

/**
 * Lightweight singleton used as a target of EL bean resolution on every page,
 * so CDI bean-resolution caching benefits show up in the numbers.
 */
@Named
@ApplicationScoped
public class AppConfig {

    // Map-valued select items (label -> value) exercise SelectItemsIterator's Map path, distinct from the
    // List<String> path that CATEGORIES takes. A code/name country map is the idiomatic real-world case.
    private static final Map<String, String> COUNTRIES = new LinkedHashMap<>();
    static {
        COUNTRIES.put("Netherlands", "NL");
        COUNTRIES.put("Belgium", "BE");
        COUNTRIES.put("Germany", "DE");
        COUNTRIES.put("France", "FR");
        COUNTRIES.put("United Kingdom", "GB");
        COUNTRIES.put("United States", "US");
        COUNTRIES.put("Japan", "JP");
    }
    private static final List<String> CATEGORIES = List.of("Books", "Music", "Movies", "Games", "Other");
    private static final List<String> LANGUAGES = List.of("NL", "EN", "DE", "FR", "JP");
    private static final List<String> TIMEZONES =
            List.of("Europe/Amsterdam", "Europe/London", "America/New_York", "Asia/Tokyo");
    private static final List<String> THEMES = List.of("Light", "Dark", "System");

    public Map<String, String> getCountries() {
        return COUNTRIES;
    }

    public List<String> getCategories() {
        return CATEGORIES;
    }

    public List<String> getLanguages() {
        return LANGUAGES;
    }

    public List<String> getTimezones() {
        return TIMEZONES;
    }

    public List<String> getThemes() {
        return THEMES;
    }

    public String getAppName() {
        return "Faces Perf Bench";
    }
}
