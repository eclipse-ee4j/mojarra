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

public enum JsfVersion {

    JSF_2_2_0_M01("2.2.0-m01"),
    JSF_2_2_0_M02("2.2.0-m02"),
    JSF_2_2_0_M03("2.2.0-m03"),
    JSF_2_2_0_M04("2.2.0-m04"),
    JSF_2_2_0_M05("2.2.0-m05"),
    JSF_2_2_0_M06("2.2.0-m06"),
    JSF_2_2_0_M07("2.2.0-m07"),
    JSF_2_2_0_M08("2.2.0-m08"),
    JSF_2_2_0_M09("2.2.0-m09"),
    JSF_2_2_0_M10("2.2.0-m10"),
    JSF_2_2_0_M11("2.2.0-m11"),
    JSF_2_2_0_M12("2.2.0-m12"),
    JSF_2_2_0_M13("2.2.0-m13"),
    JSF_2_2_0_M14("2.2.0-m14"),
    JSF_2_2_0_M15("2.2.0-m15"),
    JSF_2_2_0("2.2.0"),
    JSF_2_2_1("2.2.1"),
    JSF_2_2_2("2.2.2"),
    JSF_2_2_3("2.2.3"),
    JSF_2_2_4("2.2.4"),
    JSF_2_2_5("2.2.5"),
    JSF_2_2_6("2.2.6"),
    JSF_2_2_7("2.2.7"),
    JSF_2_2_8("2.2.8"),
    JSF_2_2_8_01("2.2.8-01"),
    JSF_2_2_8_02("2.2.8-02"),
    JSF_2_2_8_03("2.2.8-03"),
    JSF_2_2_8_04("2.2.8-04"),
    JSF_2_2_8_05("2.2.8-05"),
    JSF_2_2_8_06("2.2.8-06"),
    JSF_2_2_8_07("2.2.8-07"),
    JSF_2_2_9("2.2.9"),
    JSF_2_2_10("2.2.10"),
    JSF_2_2_11("2.2.11"),
    JSF_2_2_12("2.2.12"),
    JSF_2_2_13("2.2.13"),
    JSF_2_2_14("2.2.14"),

    JSF_2_3_0_M01("2.3.0-m01"),
    JSF_2_3_0_M02("2.3.0-m02"),
    JSF_2_3_0_M03("2.3.0-m03"),
    JSF_2_3_0_M04("2.3.0-m04"),
    JSF_2_3_0_M05("2.3.0-m05"),
    JSF_2_3_0_M06("2.3.0-m06"),
    JSF_2_3_0_M07("2.3.0-m07"),
    JSF_2_3_0_M08("2.3.0-m08"),
    JSF_2_3_0_M09("2.3.0-m09"),
    JSF_2_3_0_M10("2.3.0-m10"),
    
    JSF_2_3_0("2.3.0");

    /**
     * Constructor.
     *
     * @param version the version.
     */
    private JsfVersion(String version) {
        this.version = version;

    }

    /**
     * To string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return version;
    }

    /**
     * From string.
     *
     * @param version the JSF version.
     * @return the JsfVersion
     */
    public static JsfVersion fromString(String version) {
        JsfVersion[] versions = JsfVersion.values();
        for (JsfVersion current : versions) {
            if (current.toString().equals(version)) {
                return current;
            }
        }
        throw new IllegalArgumentException("Unable to determine JSF version");
    }
    /**
     * Stores the version.
     */
    private final String version;
}
