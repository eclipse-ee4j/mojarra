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

package org.eclipse.mojarra.test.issue6173;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;
import jakarta.inject.Named;

@Named
@RequestScoped
public class Issue6173 {

    private final List<String> rows = List.of("one", "two");
    private final List<SelectItem> items = List.of(
        new SelectItem("1", "one"),
        new SelectItemGroup("group", null, false, new SelectItem[] { new SelectItem("2", "two"), new SelectItem("3", "three") }));
    private List<String> checks;
    private String radio;
    private String grouped;

    public void addMessages() {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage("one"));
        context.addMessage(null, new FacesMessage("two"));
    }

    public List<String> getRows() {
        return rows;
    }

    public List<SelectItem> getItems() {
        return items;
    }

    public List<String> getChecks() {
        return checks;
    }

    public void setChecks(List<String> checks) {
        this.checks = checks;
    }

    public String getRadio() {
        return radio;
    }

    public void setRadio(String radio) {
        this.radio = radio;
    }

    public String getGrouped() {
        return grouped;
    }

    public void setGrouped(String grouped) {
        this.grouped = grouped;
    }
}
