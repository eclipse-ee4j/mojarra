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

package com.sun.faces.test.servlet30.localeconfig;

import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.util.Iterator;
import java.util.Locale;

import static org.junit.Assert.*;

@ManagedBean
@SessionScoped
public class ApplicationConfigBean {

    private String title = "Test Application Config";

    public String getTitle() {
        return title;
    }

    public ApplicationConfigBean() {

    }

    public String getLocaleConfigPositive() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application app = fc.getApplication();

        Locale locale = app.getDefaultLocale();
        assertNotNull("Can't get default locale from Application", locale);
        assertEquals(Locale.US, locale);

        Iterator iter;
        int j = 0, len = 0;
        boolean found = false;
        String[][] expected = {
            {"de", "DE"},
            {"en", "US"},
            {"fr", "FR"},
            {"ps", "PS"}
        };
        len = expected.length;

        // test that the supported locales are a superset of the
        // expected locales
        for (j = 0; j < len; j++) {
            assertNotNull("Can't get supportedLocales from Application",
                    iter = app.getSupportedLocales());
            found = false;
            while (iter.hasNext()) {
                locale = (Locale) iter.next();
                if (expected[j][0].equals(locale.getLanguage())
                        && expected[j][1].equals(locale.getCountry())) {
                    found = true;
                }
            }
            assertTrue("Can't find expected locale " + expected[j][0] + "_"
                    + expected[j][1] + " in supported-locales list",
                    found);
        }

        return "SUCCESS";
    }

    private String status = "";

    public String getStatus() {
        return status;
    }
}
