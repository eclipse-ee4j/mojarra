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

package com.sun.faces.test.javaee6web.el;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;

@Named(value = "exceptionSwallowedBean")
@SessionScoped
public class ExceptionSwallowedBean implements Serializable {

    private String property;

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void actionWithException() throws AbortProcessingException {
        throw new IllegalStateException();
    }

    public void actionWithAbort() throws AbortProcessingException {
        throw new AbortProcessingException();
    }

    public void throwException(ValueChangeEvent vce) throws AbortProcessingException {
        throw new NullPointerException();
    }
}
