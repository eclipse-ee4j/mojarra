/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.javaee8.ajax;

import jakarta.enterprise.inject.Model;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.context.FacesContext;

@Model
public class Spec1423 {

    /*
    @PostConstruct
    public void init() {
        FacesContext.getCurrentInstance().getViewRoot().subscribeToViewEvent(PreRenderViewEvent.class, new SystemEventListener() {
            @Override
            public boolean isListenerForSource(Object source) {
                return source instanceof UIViewRoot;
            }

            @Override
            public void processEvent(SystemEvent event) throws AbortProcessingException {
                UIViewRoot view = (UIViewRoot) event.getSource();

                for (String target : new String[] { "head", "body" }) {
                    for (UIComponent resource : view.getComponentResources(event.getFacesContext(), target)) {
                        Map<String, Object> attributes = resource.getAttributes();
                        System.out.println(target + " " + attributes.get("library") + ":" + attributes.get("name"));
                    }
                }
            }
        });
    }
    */

    public void addComponentResourceToHead() {
        UIOutput script = new UIOutput();
        script.setId("addedViaHead");
        script.setRendererType("jakarta.faces.resource.Script");
        script.getAttributes().put("library", "spec1423");
        script.getAttributes().put("name", "addedViaHead.js");
        FacesContext context = FacesContext.getCurrentInstance();
        context.getViewRoot().addComponentResource(context, script, "head");
    }

    public void addComponentResourceToBody() {
        UIOutput script = new UIOutput();
        script.setId("addedViaBody");
        script.setRendererType("jakarta.faces.resource.Script");
        script.getAttributes().put("library", "spec1423");
        script.getAttributes().put("name", "addedViaBody.js");
        FacesContext context = FacesContext.getCurrentInstance();
        context.getViewRoot().addComponentResource(context, script, "body");
    }

    public void addComponentProgrammatically() {
        FacesContext context = FacesContext.getCurrentInstance();
        UIComponent parent = UIComponent.getCurrentComponent(context).findComponent("parent");

        if (parent.getChildCount() == 0) {
            parent.getChildren().add(context.getApplication().createComponent(Spec1423ComponentAddedProgrammatically.COMPONENT_TYPE));
        }
    }

    private String include = "spec1423includeWithoutComponent.xhtml";

    public void addComponentViaInclude() {
        include = "spec1423includeWithComponent.xhtml";
    }

    public String getInclude() {
        return include;
    }

}
