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

package com.sun.faces.test.javaee6web.ajax;

import jakarta.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.AjaxBehaviorListener;

@Model
public class ProgrammaticClientBehaviorBean implements AjaxBehaviorListener {

    private HtmlInputText text;

    public HtmlInputText getText() {
        return text;
    }

    public void setText(HtmlInputText text) {
        this.text = text;
    }

    @Override
    public void processAjaxBehavior(AjaxBehaviorEvent event)
            throws AbortProcessingException {
    }

    @PostConstruct
    public void init() {
        HtmlInputText inputText = (HtmlInputText) FacesContext
                .getCurrentInstance().getApplication()
                .createComponent(HtmlInputText.COMPONENT_TYPE);
        AjaxBehavior ajaxBehavior = (AjaxBehavior) FacesContext
                .getCurrentInstance().getApplication()
                .createBehavior(AjaxBehavior.BEHAVIOR_ID);
        inputText.setId("input");
        ajaxBehavior.addAjaxBehaviorListener(this);
        ajaxBehavior.setTransient(true);
        ajaxBehavior.setDisabled(false);
        inputText.addClientBehavior("focus", ajaxBehavior);
        text = inputText;
    }
}
