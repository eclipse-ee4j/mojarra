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

package com.sun.faces.systest.model;

import javax.faces.event.ValueChangeListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.AbortProcessingException;
import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;

/**
 * Leveraged by systest/web/valueChangeListener02.jsp
 */
public class TestValueChangeListener implements ValueChangeListener {

    @Override
    public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {

        FacesContext ctx = FacesContext.getCurrentInstance();
        String value = event.getOldValue() + "";
        if (value.length() == 0) {
            value = "[empty string]";
        }
        FacesMessage msg1 = new FacesMessage("old value: " + value);

        value = event.getNewValue() + "";
        if (value.length() == 0) {
            value = "[empty string]";
        }

        FacesMessage msg2 = new FacesMessage("new value: " + value);
        ctx.addMessage(null, msg1);
        ctx.addMessage(null, msg2);
    }

}
