/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates.
 * Copyright (c) 2018 Payara Services Limited.
 * All rights reserved.
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

package com.sun.faces.test.servlet30.composite;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Named;

/**
 * A session scoped bean used in some tests for composite components
 *
 * @author Manfred Riem (manfred.riem@oracle.com)
 */

@Named
@RequestScoped
public class ConverterBean {

    public Converter<Object> getConverter() {
        return new TestConverter();
    }

    public static class TestConverter implements Converter<Object> {

        @Override
        public Object getAsObject(FacesContext context, UIComponent component, String value) {
            return value;
        }

        @Override
        public String getAsString(FacesContext context, UIComponent component, Object value) {
            String cid = component.getClientId(context);

            context.addMessage(cid,
                    new FacesMessage(SEVERITY_INFO,
                    "Converter Invoked : " + cid,
                    "Converter Invoked : " + cid));

            return value.toString();
        }
    }
}
