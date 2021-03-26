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

package com.sun.faces.test.servlet30.composite;

import java.beans.BeanInfo;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;

/**
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
@ManagedBean(name = "issue2176Bean")
@SessionScoped
public class Issue2176Bean implements Serializable {

    /**
     * Stores the previos count.
     */
    private int previousCount = -1;

    /**
     * Get the status.
     */
    public String getStatus() {
        String result = "SUCCESS";
        final List<Integer> seenCount = new ArrayList<Integer>();
        seenCount.add(0);
        VisitContext visitContext = VisitContext.createVisitContext(FacesContext.getCurrentInstance());
        FacesContext.getCurrentInstance().getViewRoot().visitTree(visitContext, new VisitCallback() {

            public VisitResult visit(VisitContext context, UIComponent target) {
                if ("jakarta.faces.Composite".equals(target.getRendererType())) {
                    BeanInfo beanInfo = (BeanInfo) target.getAttributes().get(UIComponent.BEANINFO_KEY);
                    Collection<String> ids = (Collection<String>) beanInfo.getBeanDescriptor().getValue(UIComponent.ATTRS_WITH_DECLARED_DEFAULT_VALUES);

                    int count = 0;
                    if (ids != null) {
                        for (String id : ids) {
                            count++;
                        }
                    }
                    
                    seenCount.set(0, Integer.valueOf(seenCount.get(0).intValue() + count));
                }
                return VisitResult.ACCEPT;
            }
        });
        int observedCount = (Integer) seenCount.get(0);
        if (previousCount != -1 && observedCount > previousCount) {
            result = "FAILED";
        }
        previousCount = observedCount;
        return result;
    }
}
