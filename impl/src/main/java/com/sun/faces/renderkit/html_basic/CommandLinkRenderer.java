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

// CommandLinkRenderer.java

package com.sun.faces.renderkit.html_basic;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.ActionEvent;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;

/**
 * <b>CommandLinkRenderer</b> is a class that renders the current value of <code>UICommand</code> as a HyperLink that
 * acts like a Button.
 */

public class CommandLinkRenderer extends LinkRenderer {

    private static final Attribute[] ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.COMMANDLINK);

    // ---------------------------------------------------------- Public Methods

    @Override
    public void decode(FacesContext context, UIComponent component) {

        rendererParamsNotNull(context, component);

        if (!shouldDecode(component)) {
            return;
        }

        String clientId = decodeBehaviors(context, component);

        if (wasClicked(context, component, clientId)) {
            component.queueEvent(new ActionEvent(context, component));
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("This commandLink resulted in form submission " + " ActionEvent queued.");

            }
        }

    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }

        boolean componentDisabled = Boolean.TRUE.equals(component.getAttributes().get("disabled"));

        if (componentDisabled) {
            renderAsDisabled(context, component);
        } else {
            RenderKitUtils.renderFacesJsIfNecessary(context);
            renderAsActive(context, component);
        }

    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncodeChildren(component)) {
            return;
        }

        if (component.getChildCount() > 0) {
            for (UIComponent kid : component.getChildren()) {
                encodeRecursive(context, kid);
            }
        }

    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        if (Boolean.TRUE.equals(component.getAttributes().get("disabled"))) {
            writer.endElement("span");
        } else {
            writer.endElement("a");
        }
    }

    @Override
    public boolean getRendersChildren() {

        return true;

    }

    // ------------------------------------------------------- Protected Methods

    @Override
    protected Object getValue(UIComponent component) {

        return ((UICommand) component).getValue();

    }

    /*
     * Render the necessary Javascript for the link. Note that much of this code is shared with
     * CommandButtonRenderer.renderOnClick RELEASE_PENDING: Consolidate this code into a utility method, if possible.
     */
    @Override
    protected void renderAsActive(FacesContext context, UIComponent command) throws IOException {

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        // make link act as if it's a button using javascript
        writer.startElement("a", command);
        writeIdAttributeIfNecessary(context, writer, command);
        writer.writeAttribute("href", "#", "href");
        RenderKitUtils.renderPassThruAttributes(context, writer, command, ATTRIBUTES, getNonOnClickBehaviors(command));

        RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer, command);

        writeCommonLinkAttributes(writer, command);

        // render the current value as link text.
        writeValue(command, writer);
        writer.flush();

        String target = (String) command.getAttributes().get("target");
        if (target != null) {
            target = target.trim();
        } else {
            target = "";
        }

        Collection<ClientBehaviorContext.Parameter> params = getBehaviorParameters(command);
        RenderKitUtils.renderOnclickEventListener(context, command, params, target, true);
    }

    // --------------------------------------------------------- Private Methods

    private static boolean wasClicked(FacesContext context, UIComponent component, String clientId) {

        Map<String, String> requestParamMap = context.getExternalContext().getRequestParameterMap();

        if (clientId == null) {
            clientId = component.getClientId(context);
        }

        // Fire an action event if we've had a traditional (non-Ajax)
        // postback, or if we've had a partial or behavior-based postback.
        return requestParamMap.containsKey(clientId) || RenderKitUtils.isPartialOrBehaviorAction(context, clientId);
    }

    // Returns the Behaviors map, but only if it contains some entry other
    // than those handled by renderOnclick(). This helps us optimize
    // renderPassThruAttributes() in the very common case where the
    // link only contains an "action" (or "click") Behavior. In that
    // we pass a null Behaviors map into renderPassThruAttributes(),
    // which allows us to take a more optimized code path.
    private static Map<String, List<ClientBehavior>> getNonOnClickBehaviors(UIComponent component) {

        return getPassThruBehaviors(component, "click", "action");
    }

} // end of class CommandLinkRenderer
