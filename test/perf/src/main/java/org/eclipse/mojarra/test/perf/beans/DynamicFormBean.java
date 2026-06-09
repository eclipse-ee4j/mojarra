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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.Application;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.component.html.HtmlOutputLabel;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.inject.Named;

/**
 * Backs {@code dynamic-form-ajax.xhtml} — the idiomatic "dynamic components" pattern: a request-scoped bean holds
 * the container via {@code binding}, and an {@code f:event type="postAddToView"} listener {@link #build builds} its
 * input subtree programmatically each time the view is (re)built. Unlike an add/remove-after-build toggle, the tree
 * structure is identical on every request, so the ajax postback behaves exactly like {@code form-inputs-ajax} and
 * is portable across implementations (their add/remove-after-build state handling differs).
 */
@Named
@RequestScoped
public class DynamicFormBean {

    private static final int FIELD_COUNT = 570;

    private UIComponent container;
    private final Map<String, String> values = new HashMap<>();

    /**
     * {@code postAddToView} listener: programmatically populates the bound container with {@value #FIELD_COUNT}
     * labelled inputs. Runs on the initial build and on every postback's view refresh.
     */
    public void build(ComponentSystemEvent event) {
        UIComponent group = event.getComponent();
        List<UIComponent> children = group.getChildren();
        if (!children.isEmpty()) {
            return; // the view refresh can re-fire postAddToView; build the subtree only once per request
        }

        FacesContext context = FacesContext.getCurrentInstance();
        Application application = context.getApplication();
        for (int i = 1; i <= FIELD_COUNT; i++) {
            HtmlOutputLabel label = (HtmlOutputLabel) application.createComponent(HtmlOutputLabel.COMPONENT_TYPE);
            label.setFor("field" + i);
            label.setValue("Field " + i);

            HtmlInputText input = (HtmlInputText) application.createComponent(HtmlInputText.COMPONENT_TYPE);
            input.setId("field" + i);
            input.setValueExpression("value", application.getExpressionFactory().createValueExpression(
                    context.getELContext(), "#{dynamicFormBean.values['field" + i + "']}", String.class));

            children.add(label);
            children.add(input);
        }
    }

    public UIComponent getContainer() {
        return container;
    }

    public void setContainer(UIComponent container) {
        this.container = container;
    }

    public Map<String, String> getValues() {
        return values;
    }
}
