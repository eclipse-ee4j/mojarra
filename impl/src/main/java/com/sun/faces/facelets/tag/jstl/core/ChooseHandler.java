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

package com.sun.faces.facelets.tag.jstl.core;

import com.sun.faces.facelets.tag.TagHandlerImpl;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Jacob Hookom
 */
public final class ChooseHandler extends TagHandlerImpl {
    
    private final ChooseOtherwiseHandler otherwise;
    private final ChooseWhenHandler[] when;
    
    public ChooseHandler(TagConfig config) {
        super(config);
        
        List whenList = new ArrayList();
        Iterator itr = this.findNextByType(ChooseWhenHandler.class);
        while (itr.hasNext()) {
            whenList.add(itr.next());
        }
        if (whenList.isEmpty()) {
            throw new TagException(this.tag, "Choose Tag must have one or more When Tags");
        }
        this.when = (ChooseWhenHandler[]) whenList.toArray(new ChooseWhenHandler[whenList.size()]);
        
        itr = this.findNextByType(ChooseOtherwiseHandler.class);
        if (itr.hasNext()) {
            this.otherwise = (ChooseOtherwiseHandler) itr.next();
        } else {
            this.otherwise = null;
        }
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException {
        for (int i = 0; i < this.when.length; i++) {
            if (this.when[i].isTestTrue(ctx)) {
                this.when[i].apply(ctx, parent);
                return;
            }
        }
        if (this.otherwise != null) {
            this.otherwise.apply(ctx, parent);
        }
    }

}
