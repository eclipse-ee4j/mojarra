/*
 * Copyright (c) Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.test.issue6184;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.component.UIOutput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.inject.Named;

/**
 * Backs a view whose content is relocated by composite:insertFacet.
 */
@Named
@RequestScoped
public class Issue6184 {

    private boolean downloaded;

    /**
     * Adds a component resource to the head on the initial request, after the view is built, so that postbacks restore
     * a view whose head differs from the one built from the Facelet.
     */
    public void addHeadResource() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (!context.isPostback()) {
            UIOutput script = new UIOutput();
            script.setRendererType("jakarta.faces.resource.Script");
            script.getAttributes().put("library", "jakarta.faces");
            script.getAttributes().put("name", "faces.js");
            context.getViewRoot().addComponentResource(context, script, "head");
        }
    }

    /**
     * Resolves the client id of the event source while it is still at the position where it was built.
     */
    public void readClientId(ComponentSystemEvent event) {
        event.getComponent().getClientId();
    }

    /**
     * Marks that the relocated command link invoked its action listener.
     */
    public void download() {
        downloaded = true;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public List<String> getRows() {
        return List.of("first", "second");
    }
}
