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

/*
 * PaymentConverter.java
 *
 * Created on 10 novembre 2005, 20.25
 *
 */

package test;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

public class PaymentConverter  implements Converter {

    public String getAsString(FacesContext context, UIComponent component,
            Object object) throws ConverterException {
        System.out.println("CONVERTER CALLED!!!!!!!! getAsString(component=" +
            component + ", object=" + object + ")");

        if (context == null || component == null) {
            throw new NullPointerException();
        }
        if(object == null) {
            return null;
        }
        if (object instanceof String) {
            return (String) object;
        }
        Payment payment = (Payment) object;
        return payment.getValue();
    }

    public Object getAsObject(FacesContext context, UIComponent component,
            String value) throws ConverterException {
        System.out.println("CONVERTER CALLED!!!!!!!! getAsObject(component=" +
            component + ", id=" + (component != null ? component.getId() : "null") +
            ", value=" + value + ")");

        if (context == null || component == null) {
            throw new NullPointerException();
        }
        if (value == null || value.equals("")) {
            return null;
        }

        Payment p = new Payment();
        p.setLabel("credit card " + value);
        p.setValue(value);

        return p;
    }
}
