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

package com.sun.faces.facelets.tag.faces;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.BehaviorHandler;
import jakarta.faces.view.facelets.TagHandler;

/**
 * <p class="changed_added_2_0">
 * This class holds collection of {@link BehaviorHandler} instances, attached to the composite component. Descendant
 * components from that composite uses that collection to substitute actual instance
 * </p>
 *
 * @author asmirnov@exadel.com
 *
 */
public class AttachedBehaviors implements Serializable {

    private static final long serialVersionUID = 228672860891514377L;
    private Map<String, TagHandler> behaviors = new HashMap<>();
    public static final String COMPOSITE_BEHAVIORS_KEY = "jakarta.faces.view.ClientBehaviors";

    public void add(String eventName, TagHandler owner) {
        behaviors.put(eventName, owner);
    }

    public TagHandler get(String value) {
        return behaviors.get(value);
    }

    public static AttachedBehaviors getAttachedBehaviorsHandler(UIComponent component) {
        Map<String, Object> attributes = component.getAttributes();
        AttachedBehaviors handler = (AttachedBehaviors) attributes.get(AttachedBehaviors.COMPOSITE_BEHAVIORS_KEY);
        if (null == handler) {
            handler = new AttachedBehaviors();
            attributes.put(AttachedBehaviors.COMPOSITE_BEHAVIORS_KEY, handler);
        }
        return handler;
    }

}
