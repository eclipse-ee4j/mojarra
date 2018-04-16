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

package com.sun.faces.test.javaee6web.flowswitchcall;

import javax.faces.flow.FlowScoped;
import java.io.Serializable;
import javax.inject.Named;

@Named
@FlowScoped("flow-a")
public class Flow_a_Bean implements Serializable {

    public String getName() {
        return "Flow_a_Bean";
    }

    public String getReturnValue() {
        return "/return1";
    }

    public boolean isSwitchA_Case01() {
        return false;
    }

    public boolean isSwitchA_Case02() {
        return false;
    }

    public boolean isSwitchA_Case03() {
        return true;
    }

    public boolean isSwitchB_Case01() {
        return false;
    }

    public boolean isSwitchB_Case02() {
        return true;
    }

    public boolean isSwitchB_Case03() {
        return false;
    }

    public boolean isSwitchC_Case01() {
        return false;
    }

    public boolean isSwitchC_Case02() {
        return false;
    }

    public boolean isSwitchC_Case03() {
        return false;
    }

    public String getDefaultOutcome() {
        return "switchC_result";
    }
}
