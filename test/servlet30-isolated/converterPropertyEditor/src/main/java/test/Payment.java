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
 * Payment.java
 *
 * Created on 10 novembre 2005, 16.34
 *
 */

package test;

import java.math.BigDecimal;

public class Payment {

    private String value;
    private String label;

    public Payment() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString() {
        return "Payment[" + getLabel() + ": " + getValue() + "]";
    }

    public boolean equals(Object rhs) {
        if (!(rhs instanceof Payment)) {
            return false;
        }
        String rv = ((Payment) rhs).getValue();
        return getValue() == rv || getValue() != null &&
            getValue().equals(((Payment) rhs).getValue());
    }
}
