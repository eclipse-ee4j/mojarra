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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class TestBeanConverter implements Converter {

        public Object getAsObject(FacesContext context, UIComponent component, String
value) {
                return new TestBean2(Integer.valueOf(value.substring(0, value.indexOf(","))),
value.substring(value.indexOf(",")+1));
        }

        public String getAsString(FacesContext context, UIComponent component, Object
value) {
                return ((TestBean2)value).id+","+((TestBean2)value).name;
        }
}
