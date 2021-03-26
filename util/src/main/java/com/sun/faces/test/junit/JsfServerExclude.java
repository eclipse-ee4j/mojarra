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

package com.sun.faces.test.junit;

public enum JsfServerExclude {

    GLASSFISH_5_0("Glassfish", "5.0"),
    GLASSFISH_4_1("Glassfish", "4.1"),
    GLASSFISH_4_0("Glassfish", "4.0"),
    GLASSFISH_3_1_2_2("Glassfish", "3.1.2.2"),
    TOMCAT_7_0_35("Tomcat", "7.0.35"),
    WEBLOGIC_12_3_1("Weblogic", "12.3.1.0"),
    WEBLOGIC_12_2_1("Weblogic", "12.2.1.0"),
    WEBLOGIC_12_1_4("Weblogic", "12.1.4.0"),
    WEBLOGIC_12_1_3("Weblogic", "12.1.3.0"),
    WEBLOGIC_12_1_2("Weblogic", "12.1.2.0"),
    WEBLOGIC_12_1_1("Weblogic", "12.1.1.0");

    /**
     * Constructor.
     *
     * @param version the version.
     */
    private JsfServerExclude(String name, String version) {
        this.name = name;
        this.version = version;
    }

    /**
     * To string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return name + ":" + version;
    }

    /**
     * From string.
     *
     * @param serverString the server string.
     * @return the JsfServerExclude
     */
    public static JsfServerExclude fromString(String serverString) {
        if (serverString != null) {
            JsfServerExclude[] excludes = JsfServerExclude.values();
            for (JsfServerExclude exclude : excludes) {
                if (serverString.contains(exclude.name) && serverString.contains(exclude.version)) {
                    return exclude;
                }
            }
        }
        return null;
    }

    /**
     * Stores the name.
     */
    private final String name;

    /**
     * Stores the version.
     */
    private final String version;
}
