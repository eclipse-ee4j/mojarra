/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.systest.component;

import com.sun.faces.event.UIAddComponent;
import java.util.HashMap;
import java.util.Map;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIForm;
import javax.faces.component.UINamingContainer;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewDeclarationLanguage;

@Named
@RequestScoped
public class Issue599Bean {

    public String getResult() {
        FacesContext context = FacesContext.getCurrentInstance();
        Application app = context.getApplication();
        ViewHandler vh = app.getViewHandler();
        ViewDeclarationLanguage vdl = vh.getViewDeclarationLanguage(context, context.getViewRoot().getViewId());

        // Can I create a simple h:form with prependId="false"?
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put("prependId", "false");
        UIForm form = (UIForm) vdl.createComponent(context, "http://java.sun.com/jsf/html", "form", attrs);

        if (form.isPrependId()) {
            throw new IllegalStateException("I asked for a form to be created" + " with prependId false, but that attr is not set.");
        }

        attrs.clear();

        // Can I create a composite component in the default ResourceLibrary?
        UINamingContainer cc = (UINamingContainer) vdl.createComponent(context, "http://java.sun.com/jsf/composite/i_spec_599_composite",
                "i_spec_599_composite", attrs);
        attrs = cc.getAttributes();
        if (!attrs.containsKey("customAttr")) {
            throw new IllegalArgumentException(
                    "I asked for a composite component" + " with a known default attribute, but that attr is not set.");
        }

        if (!"customAttrValue".equals(attrs.get("customAttr"))) {
            throw new IllegalArgumentException("I asked for a composite component" + " with a known default attribute"
                    + " but the value of that attr is not as expected.");
        }

        // Can I create a component coming from a custom taglib?
        attrs = new HashMap<String, Object>();
        UIAddComponent ac = (UIAddComponent) vdl.createComponent(context, "http://testcomponent", "addcomponent", attrs);
        if (!"com.sun.faces.event".equals(ac.getFamily())) {
            throw new IllegalArgumentException(
                    "I asked for a component" + " with a known family" + " but the value of that family is not as expected.");

        }

        // Can I create a composite component coming from a custom
        // taglib?
        cc = (UINamingContainer) vdl.createComponent(context, "i_spec_599_composite_taglib", "i_spec_599_composite_taglib", attrs);
        attrs = cc.getAttributes();
        if (!attrs.containsKey("customAttr2")) {
            throw new IllegalArgumentException(
                    "I asked for a composite component" + " with a known default attribute, but that attr is not set.");
        }

        if (!"customAttrValue2".equals(attrs.get("customAttr2"))) {
            throw new IllegalArgumentException("I asked for a composite component" + " with a known default attribute"
                    + " but the value of that attr is not as expected.");
        }

        return "success";
    }

}
