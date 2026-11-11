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

package org.eclipse.mojarra.test.issue6161;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.inject.Named;

@Named
@RequestScoped
public class Issue6161 {

    private final List<String> events = new ArrayList<>();
    private final List<Row> rows = List.of(new Row(), new Row());
    private String plain;
    private String composite;

    public void validate(FacesContext context, UIComponent component, Object value) {
        events.add("validate:" + component.getClientId(context));
    }

    public void valueChanged(ValueChangeEvent event) {
        events.add("change:" + event.getComponent().getClientId());
    }

    public void action(ActionEvent event) {
        events.add("action:" + event.getComponent().getClientId());
    }

    public String getEvents() {
        return String.join(" ", events);
    }

    public List<Row> getRows() {
        return rows;
    }

    public String getPlain() {
        return plain;
    }

    public void setPlain(String plain) {
        this.plain = plain;
    }

    public String getComposite() {
        return composite;
    }

    public void setComposite(String composite) {
        this.composite = composite;
    }

    public static class Row {

        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
