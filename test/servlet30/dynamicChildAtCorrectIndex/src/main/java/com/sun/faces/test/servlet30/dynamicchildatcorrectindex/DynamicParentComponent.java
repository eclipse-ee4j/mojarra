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

package com.sun.faces.test.servlet30.dynamicchildatcorrectindex;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

@FacesComponent(value = "com.sun.faces.test.servlet30.dynamicchildatcorrectindex.DynamicParentComponent")
public class DynamicParentComponent extends UIComponentBase implements SystemEventListener {

    /**
     * Constructor.
     */
    public DynamicParentComponent() {
        setRendererType("com.sun.faces.test.servlet30.dynamicchildatcorrectindex.DynamicParentComponentRenderer");

        FacesContext context = FacesContext.getCurrentInstance();
        UIViewRoot root = context.getViewRoot();

        if (!context.isPostback()) {
            root.subscribeToViewEvent(PreRenderViewEvent.class, this);
        }
    }

    /**
     * Get the component family.
     *
     * @return the component family.
     */
    public String getFamily() {
        return "com.sun.faces.test.agnostic.statesaving.basic.DynamicParentComponent";
    }

    /**
     * Is listener for the given source.
     *
     * @param source the source object.
     * @return true if we are listening, false otherwise.
     */
    public boolean isListenerForSource(Object source) {
        return (source instanceof UIViewRoot);
    }

    /**
     * Process the system event.
     *
     * <p> Here we'll add a child in between static text. If dynamic component
     * state saving works properly then upon redisplay it should have first
     * static, then dynamic and then static text. </p>
     *
     * @param event the system event.
     * @throws AbortProcessingException when processing should be aborted.
     */
    public void processEvent(SystemEvent event)
            throws AbortProcessingException {
        FacesContext context = FacesContext.getCurrentInstance();
        HtmlOutputText outputText = (HtmlOutputText) context.getApplication().createComponent("jakarta.faces.HtmlOutputText");
        outputText.setValue("Dynamic Text");
        outputText.setEscape(false);
        getChildren().add(1, outputText);
    }
}
