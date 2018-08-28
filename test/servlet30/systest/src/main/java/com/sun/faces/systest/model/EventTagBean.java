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

package com.sun.faces.systest.model;

import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.component.ContextCallback;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIOutput;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;

/**
 *
 */
public class EventTagBean {

    public void beforeViewRender(ComponentSystemEvent event) {
        FacesContext context = FacesContext.getCurrentInstance();

        context.getExternalContext().getRequestMap().put("preRenderComponentMessage", event.getComponent().getClass() + " pre-render");
    }

    public void beforeEncode(ComponentSystemEvent event) {
        UIOutput output = (UIOutput) event.getComponent();
        output.setValue("The '" + event.getClass().getName() + "' event fired!");
    }

    public void beforeEncodeNoArg() {
        FacesContext context = FacesContext.getCurrentInstance();
        UIOutput output = (UIOutput) context.getViewRoot().findComponent("form:noArgTest");
//        UIOutput output = (UIOutput)event.getComponent();
        output.setValue("The no-arg event fired!");
    }

    public void postValidate(ComponentSystemEvent event) {
        FacesContext context = FacesContext.getCurrentInstance();
        final UIForm form = (UIForm) event.getComponent();
        final String[] clientIds = { "lesser", "greater" };
        final int[] values = new int[2];
        final boolean[] hasValues = new boolean[2];
        final List<FacesMessage> toAdd = new ArrayList<FacesMessage>();

        // Traverse the form and suck out the individual values
        for (int i = 0; i < clientIds.length; i++) {
            final int finalI = i;
            form.invokeOnComponent(context, clientIds[i], new ContextCallback() {

                @Override
                public void invokeContextCallback(FacesContext context, UIComponent target) {
                    Object value = ((ValueHolder) target).getValue();
                    try {
                        if (null != value) {
                            values[finalI] = Integer.parseInt(value.toString());
                            hasValues[finalI] = true;
                        } else {
                            hasValues[finalI] = false;
                            FacesMessage msg = new FacesMessage(clientIds[finalI] + " must have a value");
                            toAdd.add(msg);
                        }
                    } catch (NumberFormatException nfe) {
                        FacesMessage msg = new FacesMessage("unable to parse the number for field " + clientIds[finalI]);
                        toAdd.add(msg);
                    }

                }
            });
        }

        // case one, ensure both fields have a value
        if (!hasValues[0] || !hasValues[1]) {
            FacesMessage msg = new FacesMessage("both fields must have a value");
            toAdd.add(msg);
        } else {
            // case two, ensure lesser is lesser than greater
            if (!(values[0] < values[1])) {
                FacesMessage msg = new FacesMessage("lesser must be lesser than greater");
                toAdd.add(msg);
            }
        }

        // If we have any messages
        if (!toAdd.isEmpty()) {
            // add them so the user sees the message
            String formClientId = form.getClientId(context);
            for (FacesMessage cur : toAdd) {
                context.addMessage(formClientId, cur);
            }
            // skip remaining lifecycle phases
            context.renderResponse();
        }
    }

}
