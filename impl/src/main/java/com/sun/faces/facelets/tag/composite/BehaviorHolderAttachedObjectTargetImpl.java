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

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.BehaviorHolderAttachedObjectTarget;

public class BehaviorHolderAttachedObjectTargetImpl extends AttachedObjectTargetImpl implements BehaviorHolderAttachedObjectTarget {

    private String event;

    private boolean defaultEvent;

    /**
     * <p class="changed_added_2_0">
     * </p>
     */
    public BehaviorHolderAttachedObjectTargetImpl() {

    }

    /**
     * <p class="changed_added_2_0">
     * </p>
     *
     * @return the event
     */
    public String getEvent() {
        return event;
    }

    /**
     * <p class="changed_added_2_0">
     * </p>
     *
     * @param event the event to set
     */
    public void setEvent(String event) {
        this.event = event;
    }

    /**
     * <p class="changed_added_2_0">
     * </p>
     *
     * @return the defaultEvent
     */
    @Override
    public boolean isDefaultEvent() {
        return defaultEvent;
    }

    /**
     * <p class="changed_added_2_0">
     * </p>
     *
     * @param defaultEvent the defaultEvent to set
     */
    public void setDefaultEvent(boolean defaultEvent) {
        this.defaultEvent = defaultEvent;
    }

    @Override
    public List<UIComponent> getTargets(UIComponent topLevelComponent) {
        List<UIComponent> targets = super.getTargets(topLevelComponent);
        List<UIComponent> wrappedTargets = new ArrayList<>(targets.size());
        for (UIComponent component : targets) {
            wrappedTargets.add(new BehaviorHolderWrapper(component, getName(), getEvent(), super.getTargetsList()));
        }
        return wrappedTargets;
    }
    
}
