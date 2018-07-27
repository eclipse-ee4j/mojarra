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

package com.sun.faces.test;

import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class FooConverterBase implements Converter<Foo> {

    public static final Logger LOGGER = Logger.getAnonymousLogger();

    public FooConverterBase() {
        LOGGER.info("FooConverter ctor");
    }

    @Override
    public Foo getAsObject(FacesContext context, UIComponent component, String value) {
        return new Foo(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Foo value) {
        if (value != null) {
            return value.getName();
        }

        return "";
    }

}
