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
package org.eclipse.mojarra.test.issue6130;

import java.io.Serializable;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class Issue6130 implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean show = true;

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public void remove(String id) {
        UIComponent target = getComponent("form:target");
        target.getChildren().remove(target.findComponent(id));
    }

    public void removeAndHide(String id) {
        remove(id);
        show = false;
    }

    public void moveToOther(String id) {
        getComponent("form:other").getChildren().add(getComponent("form:target").findComponent(id));
    }

    private static UIComponent getComponent(String clientId) {
        return FacesContext.getCurrentInstance().getViewRoot().findComponent(clientId);
    }
}
