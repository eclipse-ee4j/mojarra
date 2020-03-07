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

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;

public class BehaviorHolderAttachedObjectTargetHandler extends AttachedObjectTargetHandler {

    public BehaviorHolderAttachedObjectTargetHandler(TagConfig config) {
        super(config);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.faces.facelets.tag.composite.AttachedObjectTargetHandler#newAttachedObjectTargetImpl()
     */
    @Override
    AttachedObjectTargetImpl newAttachedObjectTargetImpl() {
        BehaviorHolderAttachedObjectTargetImpl target = new BehaviorHolderAttachedObjectTargetImpl();
        TagAttribute event = getAttribute("event");
        FaceletContext ctx = null;

        if (null != event) {
            if (!event.isLiteral()) {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                ctx = (FaceletContext) facesContext.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
                String eventStr = (String) event.getValueExpression(ctx, String.class).getValue(ctx);
                target.setEvent(eventStr);
            } else {
                target.setEvent(event.getValue());
            }
        }
        TagAttribute defaultAttr = getAttribute("default");
        if (null != defaultAttr) {
            if (null == ctx) {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                ctx = (FaceletContext) facesContext.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
            }
            target.setDefaultEvent(defaultAttr.getBoolean(ctx));
        }
        return target;
    }

}
