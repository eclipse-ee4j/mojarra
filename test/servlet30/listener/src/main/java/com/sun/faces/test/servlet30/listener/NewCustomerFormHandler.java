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

package com.sun.faces.test.servlet30.listener;

import java.util.HashMap;
import java.util.List;

public class NewCustomerFormHandler {

    public NewCustomerFormHandler() {
    }

    public String loginRequired() {
        return "loginRequired";
    }

    private String minimumAge;

    public String getMinimumAge() {
        return minimumAge;
    }

    public void setMinimumAge(String minimumAge) {
        this.minimumAge = minimumAge;
    }

    private String maximumAge;

    public String getMaximumAge() {
        return maximumAge;
    }

    public void setMaximumAge(String maximumAge) {
        this.maximumAge = maximumAge;
    }

    private String nationality;

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    private List allowableValues;

    public List getAllowableValues() {
        return allowableValues;
    }

    public void setAllowableValues(List allowableValues) {
        this.allowableValues = allowableValues;
    }

    private String[] firstNames = { "bob", "jerry" };

    public String[] getFirstNames() {
        return firstNames;
    }

    public void setFirstNames(String[] newNames) {
        firstNames = newNames;
    }

    private HashMap claimAmounts;

    public HashMap getClaimAmounts() {
        return claimAmounts;
    }

    public void setClaimAmounts(HashMap claimAmounts) {
        this.claimAmounts = claimAmounts;
    }

}
