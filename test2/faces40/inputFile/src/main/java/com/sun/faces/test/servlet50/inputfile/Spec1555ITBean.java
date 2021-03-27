/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

package com.sun.faces.test.servlet50.inputfile;

import java.nio.file.Paths;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;

@Named
@RequestScoped
public class Spec1555ITBean {

    private Part singleSelection;
    private List<Part> multipleSelection;

    public void submitSingleSelection() {
        addAsMessage("singleSelection", singleSelection);
    }

    public void submitMultipleSelection() {
        for (Part part : multipleSelection) {
            addAsMessage("multipleSelection", part);
        }
    }

    private static void addAsMessage(String field, Part part) {
        String name = Paths.get(part.getSubmittedFileName()).getFileName().toString();
        long size = part.getSize();

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("field: " + field + ", name: " + name + ", size: " + size));
    }

    public Part getSingleSelection() {
        return singleSelection;
    }

    public void setSingleSelection(Part singleSelection) {
        this.singleSelection = singleSelection;
    }

    public List<Part> getMultipleSelection() {
        return multipleSelection;
    }

    public void setMultipleSelection(List<Part> multipleSelection) {
        this.multipleSelection = multipleSelection;
    }
}
