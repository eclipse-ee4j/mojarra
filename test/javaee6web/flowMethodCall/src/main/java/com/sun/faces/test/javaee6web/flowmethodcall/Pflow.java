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

package com.sun.faces.test.javaee6web.flowmethodcall;

import java.io.Serializable;
import javax.faces.flow.FlowScoped;
import javax.inject.Named;

@Named
@FlowScoped(value = "method-call-start-node")
public class Pflow implements Serializable {

    private static final long serialVersionUID = -8810904955483635554L;

    public String firstPage() {
        return "myFirstViewNode";
    }
    public String methodCall02() {
        return "method-call-03";
    }
    
    public String methodCall03() {
        return "method-call-04";
    }
    
    public String methodCall04() {
        return "viewNodeAtEndOfMethodCalls";
    }
    
    public String callFlowCallNode() {
        return "call-switch-start-node";
    }

    public String callSwitchNode() {
        return "switchA";
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
    


}
