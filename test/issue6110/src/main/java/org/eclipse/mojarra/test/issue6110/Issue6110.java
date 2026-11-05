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
package org.eclipse.mojarra.test.issue6110;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Holds one model list per table, keyed by table ID. Each item value starts as the table ID plus the row index, and
 * for a nested table plus "-" and the inner row index.
 */
@Named
@ViewScoped
public class Issue6110 implements Serializable {

    private static final long serialVersionUID = 1L;
    static final int ROWS = 2;
    private static final List<String> TABLES = List.of("table", "tablePreserved", "repeat", "repeatPreserved");
    private static final List<String> NESTED_TABLES = List.of("nestedTable", "nestedTablePreserved", "nestedTableOuterPreserved",
            "nestedTableInnerPreserved", "nestedRepeat", "nestedRepeatPreserved", "nestedRepeatOuterPreserved",
            "nestedRepeatInnerPreserved");

    private final Map<String, List<?>> model = new HashMap<>();
    private String required;

    public Issue6110() {
        for (String table : TABLES) {
            model.put(table, items(table));
        }
        for (String table : NESTED_TABLES) {
            model.put(table, outers(table));
        }
    }

    public void addInfo() {
        addMessage(FacesMessage.SEVERITY_INFO);
    }

    public void addFatal() {
        addMessage(FacesMessage.SEVERITY_FATAL);
    }

    private static void addMessage(FacesMessage.Severity severity) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, severity.toString(), null));
    }

    public Map<String, List<?>> getModel() {
        return model;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    private static List<Item> items(String prefix) {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            items.add(new Item(prefix + i));
        }
        return items;
    }

    private static List<Outer> outers(String prefix) {
        List<Outer> outers = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            outers.add(new Outer(items(prefix + i + "-")));
        }
        return outers;
    }

    public static class Item implements Serializable {

        private static final long serialVersionUID = 1L;

        private String name;

        Item(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Outer implements Serializable {

        private static final long serialVersionUID = 1L;

        private final List<Item> items;

        Outer(List<Item> items) {
            this.items = items;
        }

        public List<Item> getItems() {
            return items;
        }
    }
}
