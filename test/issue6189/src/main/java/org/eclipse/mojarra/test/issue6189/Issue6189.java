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
package org.eclipse.mojarra.test.issue6189;

import java.io.IOException;
import java.io.Serializable;

import jakarta.faces.application.ViewHandler;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Backs a view whose include follows the current step, and builds another view on demand.
 */
@Named
@ViewScoped
public class Issue6189 implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String OTHER_VIEW_ID = "/WEB-INF/other.xhtml";

    private static final int STEPS = 3;

    private int step = 1;

    /**
     * Advances to the next step.
     */
    public void next() {
        step = step % STEPS + 1;
    }

    /**
     * Advances to the next step and builds another view, as done to render a mail template.
     *
     * @throws IOException when the other view cannot be built
     */
    public void nextAndBuildOtherView() throws IOException {
        next();
        FacesContext context = FacesContext.getCurrentInstance();
        ViewHandler viewHandler = context.getApplication().getViewHandler();
        viewHandler.getViewDeclarationLanguage(context, OTHER_VIEW_ID).buildView(context, viewHandler.createView(context, OTHER_VIEW_ID));
    }

    public int getStep() {
        return step;
    }
}
