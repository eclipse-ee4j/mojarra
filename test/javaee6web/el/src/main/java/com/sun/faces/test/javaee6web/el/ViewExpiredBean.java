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

package com.sun.faces.test.javaee6web.el;

import java.io.Serializable;
import java.util.Map;
import jakarta.annotation.PreDestroy;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@Named(value = "viewExpiredBean")
@ViewScoped
public class ViewExpiredBean implements Serializable {

    @PreDestroy
    public void destroy() {
        Map<String, Object> applicationMap = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
        if (!applicationMap.containsKey("count")) {
            applicationMap.put("count", 1);
        } else {
            int count = ((Integer) applicationMap.get("count")) + 1;
            applicationMap.put("count", count);
        }
    }

    public Integer getCount() {
        return (Integer) FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get("count");
    }
}
