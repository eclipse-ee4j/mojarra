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
package org.glassfish.mojarra.test.impl.issue5970;

import java.io.Serializable;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class Issue5970ListenerBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private int count = 1;

    /**
     * Adds as many inputs as the count says, each with an id of its own, to a target which holds none yet.
     */
    public void addInputs(ComponentSystemEvent event) {
        UIComponent target = event.getComponent();

        if (target.getChildCount() > 0) {
            return;
        }

        for (int i = 0; i < count; i++) {
            HtmlInputText input = new HtmlInputText();
            input.setId("in" + i);
            target.getChildren().add(input);
        }
    }

    public void increment() {
        count++;
    }

    public void decrement() {
        count--;
    }

}
