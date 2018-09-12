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

package com.sun.faces.application;

import javax.faces.context.FacesContext;

import com.sun.faces.util.LRUMap;
import com.sun.faces.util.RequestStateManager;

public class ResetUniqueRequestIdBean {

    public ResetUniqueRequestIdBean() {
    }

    protected String reset = "Unique Id Counter Has Been Reset";

    public String getReset() {
        FacesContext context = FacesContext.getCurrentInstance();
        LRUMap lruMap = new LRUMap(15);
        context.getExternalContext().getSessionMap().put(RequestStateManager.LOGICAL_VIEW_MAP, lruMap);
        StateManagerImpl stateManagerImpl = (StateManagerImpl) context.getApplication().getStateManager();
//        TestingUtil.setPrivateField("requestIdSerial",
//                                    StateManagerImpl.class,
//                                    stateManagerImpl,
//                                    ((char) -1));
        return reset;
    }

    public void setReset(String newReset) {
        reset = newReset;
    }

}
