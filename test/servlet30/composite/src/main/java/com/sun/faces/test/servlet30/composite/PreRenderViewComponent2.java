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

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.PreRenderComponentEvent;

@FacesComponent("com.sun.faces.test.servlet30.composite.PreRenderViewComponent2")
public class PreRenderViewComponent2 extends PreRenderViewComponentBase
        implements NamingContainer, ComponentSystemEventListener {

    public static final String COMPONENT_FAMILY = "jakarta.faces.NamingContainer";

    public PreRenderViewComponent2() {
        super();
        FacesContext ctx = FacesContext.getCurrentInstance();
        subscribeToEvent(PreRenderComponentEvent.class, this);
    }

    @Override
    public String getFamily() {
        return (COMPONENT_FAMILY);
    }

    @Override
    public void processEvent(ComponentSystemEvent e) throws AbortProcessingException {
        if (e instanceof PreRenderComponentEvent) {
            this.processPreRenderViewEvent((PreRenderComponentEvent) e);
        }
    }

    private void processPreRenderViewEvent(PreRenderComponentEvent e) throws AbortProcessingException {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (!ctx.isPostback()) {
            UIComponent parent = this.findComponent("controls");
            ExpressionFactory ef = ctx.getApplication().getExpressionFactory();
            HtmlOutputText itemCheck = new HtmlOutputText();
            boolean itemIsNull = (this.getAttributes().get("item") == null);
            itemCheck.setValue("Item Attribute is null: " + itemIsNull);
            HtmlInputText txt = new HtmlInputText();
            txt.setId("txt");
            ValueExpression ve = ef.createValueExpression(ctx.getELContext(), "#{cc.attrs.item.text}", java.lang.String.class);
            txt.setValueExpression("value", ve);
            parent.getChildren().add(txt);
            parent.getChildren().add(itemCheck);
        }
    }
}
