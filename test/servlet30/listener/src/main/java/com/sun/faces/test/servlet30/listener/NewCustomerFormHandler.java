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

import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named("newCustomer")
@RequestScoped
public class NewCustomerFormHandler {

    private String minimumAge;
    private String maximumAge = "65";
    private String nationality;
    private List<Integer> allowableValues = asList(10, 20, 60, null);
    private String[] firstNames = { "bob", "jerry" };
    private HashMap<String, Double> claimAmounts;

    public String loginRequired() {
        return "loginRequired";
    }

    public String getMinimumAge() {
        return minimumAge;
    }

    public void setMinimumAge(String minimumAge) {
        this.minimumAge = minimumAge;
    }

    public String getMaximumAge() {
        return maximumAge;
    }

    public void setMaximumAge(String maximumAge) {
        this.maximumAge = maximumAge;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public List getAllowableValues() {
        return allowableValues;
    }

    public void setAllowableValues(List allowableValues) {
        this.allowableValues = allowableValues;
    }

    public String[] getFirstNames() {
        return firstNames;
    }

    public void setFirstNames(String[] newNames) {
        firstNames = newNames;
    }

    public HashMap getClaimAmounts() {
        return claimAmounts;
    }

    public void setClaimAmounts(HashMap claimAmounts) {
        this.claimAmounts = claimAmounts;
    }

}
