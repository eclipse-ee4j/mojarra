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

package com.sun.faces.systest;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 * <p>
 * Test implementation of {@link Converter}.
 * </p>
 */
public class TestConverter01 implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String newValue) throws ConverterException {
        context.addMessage(component.getClientId(context),
                new FacesMessage(FacesMessage.SEVERITY_ERROR, component.getId() + " was converted to Object", null));

        return newValue;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
        context.addMessage(component.getClientId(context),
                new FacesMessage(FacesMessage.SEVERITY_ERROR, component.getId() + " was converted to String", null));

        return (value.toString());
    }
}
