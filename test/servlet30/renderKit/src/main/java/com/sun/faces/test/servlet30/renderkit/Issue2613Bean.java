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

package com.sun.faces.test.servlet30.renderkit;

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

@ManagedBean(name = "issue2613Bean")
@SessionScoped
public class Issue2613Bean implements Serializable {

    private List<String> parameterNames = null;
    private int parameterCount = 0;

    public List<String> getParameterNames() {
        FacesContext context = FacesContext.getCurrentInstance();
        parameterNames = Collections.list((Enumeration<String>)((
            HttpServletRequest)context.getExternalContext().getRequest()).getParameterNames());
        Collections.sort(parameterNames);
        return parameterNames;
    }

    public void setParameterNames(List<String> parameterNames) {
        this.parameterNames = parameterNames;
    }

    public int getParameterCount() {
        parameterCount = parameterNames.size();
        return parameterCount;
    } 

    public void setParameterCount(int parameterCount) {
        this.parameterCount = parameterCount;
    }
}
