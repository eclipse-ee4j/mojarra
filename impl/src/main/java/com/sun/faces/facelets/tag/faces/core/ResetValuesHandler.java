/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.facelets.tag.faces.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.faces.component.ActionSource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;
import jakarta.faces.view.ActionSourceAttachedObjectHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;

public final class ResetValuesHandler extends ActionListenerHandlerBase implements ActionSourceAttachedObjectHandler {

    private final TagAttribute render;

    // Pattern used for execute/render string splitting
    private static Pattern SPLIT_PATTERN = Pattern.compile(" ");

    private final static class LazyActionListener implements ActionListener, Serializable {
        Collection<String> render;

        private static final long serialVersionUID = -5676209243297546166L;

        public LazyActionListener(Collection<String> render) {
            this.render = new ArrayList<>(render);
        }

        @Override
        public void processAction(ActionEvent event) throws AbortProcessingException {
            FacesContext context = FacesContext.getCurrentInstance();
            UIViewRoot root = context.getViewRoot();
            root.resetValues(context, render);
        }
    }

    /**
     * @param config
     */
    public ResetValuesHandler(TagConfig config) {
        super(config);
        render = getAttribute("render");
    }

    @Override
    public void applyAttachedObject(FacesContext context, UIComponent parent) {
        FaceletContext ctx = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
        ActionSource as = (ActionSource) parent;
        String renderStr = (String) render.getObject(ctx, String.class);
        ActionListener listener = new LazyActionListener(toList(renderStr));
        as.addActionListener(listener);
    }

    // Converts the specified object to a List<String>
    private static List<String> toList(String strValue) {

        // If the value contains no spaces, we can optimize.
        // This is worthwhile, since the execute/render lists
        // will often only contain a single value.
        if (strValue.indexOf(' ') == -1) {
            return Collections.singletonList(strValue);
        }

        // We're stuck splitting up the string.
        String[] values = SPLIT_PATTERN.split(strValue);
        if (values == null || values.length == 0) {
            return null;
        }

        // Note that we could create a Set out of the values if
        // we care about removing duplicates. However, the
        // presence of duplicates does not real harm. They will
        // be consolidated during the partial view traversal. So,
        // just create an list - garbage in, garbage out.
        return Collections.unmodifiableList(Arrays.asList(values));

    }

}
