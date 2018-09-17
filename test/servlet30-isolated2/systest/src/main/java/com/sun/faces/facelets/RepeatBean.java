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

package com.sun.faces.facelets;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;

@Named
@RequestScoped
public class RepeatBean {

    List<String> flavors;
    List<String> singleFlavor;

    public List<String> getFlavorsList() {
        if (null == flavors) {
            flavors = new ArrayList<String>();
            flavors.add("chocolate");
            flavors.add("vanilla");
            flavors.add("strawberry");
            flavors.add("chocolate peanut butter");
        }
        return flavors;
    }

    public List<String> getSingleFlavorList() {
        if (singleFlavor == null) {
            singleFlavor = new ArrayList<String>(1);
            singleFlavor.add("chocolate");
        }
        return singleFlavor;
    }

    public void setIndex(int index) {

        FacesContext ctx = FacesContext.getCurrentInstance();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Index: " + index, "Index: " + index);
        ctx.addMessage(null, msg);

    }

    public String[] getFlavorsArray() {
        String[] result = new String[getFlavorsList().size()];
        getFlavorsList().toArray(result);
        return result;
    }

}
