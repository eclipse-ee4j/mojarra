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

import javax.faces.event.ValueChangeEvent;
import javax.faces.event.AbortProcessingException;
import javax.faces.context.FacesContext;

public class ValueChangeListenerBean extends Object {

    public ValueChangeListenerBean() {
    }

    protected String textAResult;

    public String getTextAResult() {
        return textAResult;
    }

    public void setTextAResult(String newTextAResult) {
        textAResult = newTextAResult;
    }

    protected String textBResult;

    public String getTextBResult() {
        return textBResult;
    }

    public void setTextBResult(String newTextBResult) {
        textBResult = newTextBResult;
    }

    public void textAChanged(ValueChangeEvent event) throws AbortProcessingException {
        setTextAResult("Received valueChangeEvent for textA: " + event.hashCode());
    }

    public void textBChanged(ValueChangeEvent event) throws AbortProcessingException {
        setTextBResult("Received valueChangeEvent for textB: " + event.hashCode());
    }

    public void valueChange(ValueChangeEvent event) throws AbortProcessingException {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.renderResponse();
    }

}
