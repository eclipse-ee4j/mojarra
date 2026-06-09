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
package org.eclipse.mojarra.test.perf.beans;

import java.io.Serializable;
import java.util.List;

import jakarta.faces.application.Application;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Backs {@code dynamic-toggle-ajax.xhtml}. Each {@link #toggle()} adds or removes an input subtree under the
 * in-view {@code container} via {@code getChildren().add()/clear()} in the action. Because the mutation happens
 * <em>after</em> the view's initial state was marked, it drives Mojarra's dynamic add/remove path — the
 * {@code StateContext} dynamic-action tracking, the {@code DYNAMIC_COMPONENT} marker, and full-state-save/restore
 * of the dynamic subtree — which no structurally-static scenario reaches. This is the scenario for benchmarking
 * that path.
 * <p>
 * The branch is chosen from the live tree, not a flag: on Mojarra the dynamic subtree is replayed on restore, so
 * toggles alternate add/remove; MyFaces does not persist it across postback the same way, so it re-adds each
 * request. Both are valid — the divergence is itself the cross-impl observation — and neither errors.
 */
@Named
@ViewScoped
public class DynamicToggleBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int FIELD_COUNT = 820;

    public String toggle() {
        FacesContext context = FacesContext.getCurrentInstance();
        UIComponent container = context.getViewRoot().findComponent("form:container");
        List<UIComponent> children = container.getChildren();

        if (children.isEmpty()) {
            Application application = context.getApplication();
            for (int i = 1; i <= FIELD_COUNT; i++) {
                HtmlInputText input = (HtmlInputText) application.createComponent(HtmlInputText.COMPONENT_TYPE);
                input.setId("field" + i);
                children.add(input);
            }
        }
        else {
            children.clear();
        }

        return null;
    }
}
