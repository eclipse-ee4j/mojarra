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

package com.sun.faces.test.javaee6web.flowtraversalcombinations;

import com.sun.faces.flow.FlowHandlerImpl;
import java.io.Serializable;
import java.lang.reflect.Method;
import javax.faces.context.FacesContext;
import javax.faces.flow.FlowHandler;
import javax.faces.flow.FlowScoped;
import javax.inject.Named;

@Named
@FlowScoped(value = "nested-flow-call-f1")
public class NestedFlowCallF1Bean implements Serializable {
    private int count = 0;
    
    public NestedFlowCallF1Bean() {
        count ++;
        count --;
    }
    
    public int getCount() {
        return this.count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public void addCount() {
        this.count++;
    }
    
    public int getFlowDequeSize() throws Exception {
        int result = 0;
        FacesContext context = FacesContext.getCurrentInstance();
        FlowHandler flowHandler = context.getApplication().getFlowHandler();
        Method flowDequeMethod = FlowHandlerImpl.class.getDeclaredMethod("getFlowStack", FacesContext.class);
        flowDequeMethod.setAccessible(true);
        Object flowDeque = flowDequeMethod.invoke(null, context);
        
        Class flowDequeClass = Class.forName("com.sun.faces.flow.FlowHandlerImpl$FlowDeque");
        Method flowDequeSizeMethod = flowDequeClass.getDeclaredMethod("getCurrentFlowDepth", (Class[]) null);
        flowDequeSizeMethod.setAccessible(true);
        result = (Integer) flowDequeSizeMethod.invoke(flowDeque, (Object[]) null);
        
        return result;
    }
}
