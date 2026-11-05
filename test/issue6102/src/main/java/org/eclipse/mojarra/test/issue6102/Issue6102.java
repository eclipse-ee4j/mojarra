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
package org.eclipse.mojarra.test.issue6102;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named
@RequestScoped
public class Issue6102 {

    public void add(String id, int index) {
        var text = new HtmlOutputText();
        text.setId(id);
        text.setValue(id.toUpperCase());
        getTarget().getChildren().add(index, text);
    }

    public void addAndRemove(String id, int index, String removeId) {
        add(id, index);
        remove(removeId);
    }

    public void remove(String id) {
        getTarget().getChildren().remove(getTarget().findComponent(id));
    }

    private static UIComponent getTarget() {
        return FacesContext.getCurrentInstance().getViewRoot().findComponent("form:target");
    }
}
