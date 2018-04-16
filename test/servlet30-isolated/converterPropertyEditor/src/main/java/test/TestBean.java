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
 *
 * Created on 10 agosto 2006, 16.45
 */

package test;

import javax.faces.model.SelectItem;


/**
 *
 * @author agori
 */
public class TestBean {

    private Payment payment;
    private Payment[] payments = new Payment[0];

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Payment[] getPayments() {
        return payments;
    }

    public void setPayments(Payment[] payments) {
        System.out.println(this + " setting payments to " + java.util.Arrays.asList(payments));
        this.payments = payments;
    }
}
