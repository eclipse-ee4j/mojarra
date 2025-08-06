/*
 * Copyright (c) Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GPL-2.0 with Classpath-exception-2.0 which
 * is available at https://openjdk.java.net/legal/gplv2+ce.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 or Apache-2.0
 */
package org.eclipse.mojarra.test.issue5594;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class Issue5594 implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private FacesContext context;

    @PostConstruct
    public void postConstruct() {
        addMessage("postConstruct");
    }

    public void preRenderView(ComponentSystemEvent event) {
        addMessage("preRenderView");
    }

    public void submit() {
        var viewAttributes = context.getViewRoot().getAttributes();
        var counter = (AtomicInteger) viewAttributes.get("counter");

        if (counter == null) {
            counter = new AtomicInteger();
            viewAttributes.put("counter", counter);
        }

        addMessage("submit" + counter.incrementAndGet());
    }

    private void addMessage(String message) {
        context.addMessage(null, new FacesMessage(message));
    }

}