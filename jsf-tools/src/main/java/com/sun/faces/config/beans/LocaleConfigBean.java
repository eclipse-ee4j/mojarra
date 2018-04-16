/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.config.beans;


import java.util.Set;
import java.util.TreeSet;


/**
 * <p>Configuration bean for <code>&lt;locale-config&gt; element.</p>
 */

public class LocaleConfigBean {


    // -------------------------------------------------------------- Properties


    private String defaultLocale;
    public String getDefaultLocale() { return defaultLocale; }
    public void setDefaultLocale(String defaultLocale)
    { this.defaultLocale = defaultLocale; }


    // ------------------------------------------- SupportedLocaleHolder Methods


    private Set<String> supportedLocales = new TreeSet<String>();


    public void addSupportedLocale(String supportedLocale) {
        if (!supportedLocales.contains(supportedLocale)) {
            supportedLocales.add(supportedLocale);
        }
    }


    public String[] getSupportedLocales() {
        String results[] = new String[supportedLocales.size()];
        return (supportedLocales.toArray(results));
    }


    public void removeSupportedLocale(String supportedLocale) {
        supportedLocales.remove(supportedLocale);
    }


    // ----------------------------------------------------------------- Methods


}
