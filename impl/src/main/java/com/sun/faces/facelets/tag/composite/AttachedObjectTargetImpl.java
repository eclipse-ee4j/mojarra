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

package com.sun.faces.facelets.tag.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.faces.util.Util;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.AttachedObjectTarget;

public class AttachedObjectTargetImpl implements AttachedObjectTarget {

    private String name = null;

    @Override
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    @Override
    public List<UIComponent> getTargets(UIComponent topLevelComponent) {
        assert null != name;

        List<UIComponent> result;
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (null != targetsList) {
            String targetsListStr = (String) targetsList.getValue(ctx.getELContext());
            Map<String, Object> appMap = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
            String[] targetArray = Util.split(appMap, targetsListStr, " ");
            result = new ArrayList<>(targetArray.length);
            for (int i = 0, len = targetArray.length; i < len; i++) {
                UIComponent comp = topLevelComponent.findComponent(augmentSearchId(ctx, topLevelComponent, targetArray[i]));
                if (null != comp) {
                    result.add(comp);
                }
            }
        } else {
            result = new ArrayList<>(1);
            UIComponent comp = topLevelComponent.findComponent(augmentSearchId(ctx, topLevelComponent, name));
            if (null != comp) {
                result.add(comp);
            }
        }
        return result;

    }

    private ValueExpression targetsList;

    void setTargetsList(ValueExpression targetsList) {
        this.targetsList = targetsList;
    }
    
    ValueExpression getTargetsList() {
        return targetsList;
    }

    // if the current composite component ID is the same as the target ID,
    // we'll need to make the ID passed to findComponent be a combination
    // of the two so we find the correct component. If we don't do this,
    // we end with a StackOverFlowException as 'c' will be what is found
    // and not the child of 'c'.
    private String augmentSearchId(FacesContext ctx, UIComponent c, String targetId) {

        if (targetId.equals(c.getId())) {
            return targetId + UINamingContainer.getSeparatorChar(ctx) + targetId;
        }
        return targetId;

    }

}
