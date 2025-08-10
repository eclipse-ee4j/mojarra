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

// CommandScriptRenderer.java

package com.sun.faces.renderkit.html_basic;

import java.io.IOException;
import java.util.logging.Level;
import java.util.regex.Pattern;

import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.html.HtmlCommandScript;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.PhaseId;

import com.sun.faces.renderkit.RenderKitUtils;

/**
 * <b>CommandScriptRenderer</b> is a class that renders the current value of <code>UICommand</code> as a Script that acts
 * like an Ajax Button.
 */
public class CommandScriptRenderer extends HtmlBasicRenderer {

    private static final Pattern PATTERN_NAME = Pattern.compile("[$a-z_](\\.?[$\\w])*", Pattern.CASE_INSENSITIVE);

    @Override
    public void decode(FacesContext context, UIComponent component) {
        rendererParamsNotNull(context, component);

        if (!shouldDecode(component)) {
            return;
        }

        String clientId = component.getClientId(context);

        if (RenderKitUtils.isPartialOrBehaviorAction(context, clientId)) {
            UICommand command = (UICommand) component;
            ActionEvent event = new ActionEvent(command);
            event.setPhaseId(command.isImmediate() ? PhaseId.APPLY_REQUEST_VALUES : PhaseId.INVOKE_APPLICATION);
            command.queueEvent(event);

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("This commandScript resulted in form submission ActionEvent queued.");
            }
        }
    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }

        HtmlCommandScript commandScript = (HtmlCommandScript) component;
        String clientId = commandScript.getClientId(context);

        if (RenderKitUtils.getForm(commandScript, context) == null) {
            throw new IllegalArgumentException("commandScript ID " + clientId + " must be placed in UIForm");
        }

        String name = commandScript.getName();

        if (name == null || !PATTERN_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("commandScript ID " + clientId + " has an illegal name: '" + name + "'");
        }

        RenderKitUtils.renderFacesJsIfNecessary(context);

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        writer.startElement("span", commandScript);
        writer.writeAttribute("id", clientId, "id");

        RenderKitUtils.renderFunction(context, component, getBehaviorParameters(commandScript), clientId);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        writer.endElement("span");
    }

    @Override
    public boolean getRendersChildren() {
        return false;
    }

}
