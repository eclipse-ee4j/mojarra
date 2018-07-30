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

package com.sun.faces.test.servlet30.dynamic;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

@FacesComponent(value = "com.sun.faces.test.servlet30.dynamic.StableComponent")
public class StableComponent extends UIComponentBase implements SystemEventListener {

    //
    // Constructor - subscribes to PreRenderViewEvent(s)
    //

    public StableComponent() {
        setRendererType("component");
        FacesContext context = FacesContext.getCurrentInstance();
        UIViewRoot root = context.getViewRoot();
        root.subscribeToViewEvent(PreRenderViewEvent.class, this);
    }

    //
    // Public methods
    //

    @Override
    public String getFamily() {
        return "com.sun.faces.test.servlet30.dynamic";
    }

    @Override
    public boolean isListenerForSource(Object source) {
        return (source instanceof UIViewRoot);
    }

    //
    // Event processing method: Adds 3 input components.
    //

    @Override
    public void processEvent(SystemEvent event) throws AbortProcessingException {
        if (FacesContext.getCurrentInstance().getMaximumSeverity() != null) {
            return;
        }

        HtmlInputText inputText1 = new HtmlInputText();
        inputText1.setValue("1");
        getChildren().add(inputText1);

        HtmlInputText inputText2 = new HtmlInputText();
        inputText2.setValue("2");
        getChildren().add(inputText2);

        HtmlInputText inputText3 = new HtmlInputText();
        inputText3.setId("text3");
        inputText3.setRequired(true);
        getChildren().add(inputText3);
    }
}
