/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.application.resource;

import java.util.Objects;

public final class ContractInfo {

    final String contract;

    public ContractInfo(String contract) {
        this.contract = contract;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ContractInfo)) {
            return false;
        }

        ContractInfo other = (ContractInfo) obj;

        return Objects.equals(contract, other.contract);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(contract);
    }

    @Override
    public String toString() {
        return contract;
    }

}
