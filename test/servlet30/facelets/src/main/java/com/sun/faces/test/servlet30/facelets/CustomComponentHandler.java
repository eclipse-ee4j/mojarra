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

package com.sun.faces.test.servlet30.facelets;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagConfig;

public class CustomComponentHandler extends ComponentHandler {

    public CustomComponentHandler(final TagConfig config) {
        super(new ComponentConfig() {

            @Override
            public String getComponentType() {
                return UIInput.COMPONENT_TYPE;
            }

            @Override
            public String getRendererType() {
                return "javax.faces.Text";
            }

            @Override
            public FaceletHandler getNextHandler() {
                return config.getNextHandler();
            }

            @Override
            public Tag getTag() {
                return config.getTag();
            }

            @Override
            public String getTagId() {
                return config.getTagId();
            }
            
        });
    }
    
    

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        ctx.getFacesContext().getAttributes().put("tagHandlerMessage", CustomComponentHandler.class.getSimpleName() +
                ".apply() called");
        
        super.apply(ctx, parent);
    }
    
    
    
}
