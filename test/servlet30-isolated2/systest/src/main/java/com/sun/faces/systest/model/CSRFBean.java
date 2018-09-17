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

/**
 * <p>
 * Test JavaBean for CSRF application.
 * </p>
 */
public class CSRFBean {

    private String amount = "1000";
    private String account = "37665";
    private String destAccount;
    private String transferAmount;

    public String getAmount() {
        return amount;
    }

    public String getAccount() {
        return account;
    }

    public String getTransferAmount() {
        return transferAmount;
    }

    public String getDestAccount() {
        return destAccount;
    }

    public void setTransferAmount(String transferAmount) {
        this.transferAmount = transferAmount;
    }

    public void setDestAccount(String destAccount) {
        this.destAccount = destAccount;
    }

    public void transfer() {
        int intAmount = Integer.valueOf(amount).intValue();
        int intTransferAmount = Integer.valueOf(transferAmount).intValue();
        intAmount = intAmount - intTransferAmount;
        amount = String.valueOf(intAmount);
    }
}
