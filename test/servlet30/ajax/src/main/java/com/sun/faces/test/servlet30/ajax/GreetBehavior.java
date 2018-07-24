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

package com.sun.faces.test.servlet30.ajax;

import javax.faces.component.behavior.ClientBehaviorBase;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.component.behavior.FacesBehavior;
import javax.faces.context.FacesContext;

/**
 * <p>
 * A trivial Behavior implementation that shows a greeting to the user when invoked.
 * </p>
 */
@FacesBehavior(value = "custom.behavior.Greet")
public class GreetBehavior extends ClientBehaviorBase {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getScript(ClientBehaviorContext behaviorContext) {

        String name = (this.name == null) ? "World" : this.name;

        StringBuilder builder = new StringBuilder(19 + name.length());
        builder.append("alert('Hello, ");
        builder.append(name);
        builder.append("!');");

        return builder.toString();
    }

    @Override
    public Object saveState(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }
        if (initialStateMarked()) {
            Object superState = super.saveState(context);
            if (superState == null) {
                return null;
            } else {
                return new Object[] { superState };
            }
        } else {
            Object[] values = new Object[2];

            values[0] = super.saveState(context);
            values[1] = name;

            return values;
        }
    }

    @Override
    public void restoreState(FacesContext context, Object state) {

        if (context == null) {
            throw new NullPointerException();
        }

        if (state == null) {
            return;
        }
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        if (values.length == 2) {
            name = (String) values[1];
        }
    }


}
