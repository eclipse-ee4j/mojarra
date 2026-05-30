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

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

/**
 * Lightweight singleton used as a target of EL bean resolution on every page,
 * so CDI bean-resolution caching benefits show up in the numbers.
 */
@Named
@ApplicationScoped
public class AppConfig {

    private static final List<String> COUNTRIES = List.of("NL", "BE", "DE", "FR", "GB", "US", "JP");
    private static final List<String> CATEGORIES = List.of("Books", "Music", "Movies", "Games", "Other");

    public List<String> getCountries() {
        return COUNTRIES;
    }

    public List<String> getCategories() {
        return CATEGORIES;
    }

    public String getAppName() {
        return "Faces Perf Bench";
    }
}
