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

// ButtonRenderer.java

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

import com.sun.faces.RIConstants;
import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;

/**
 * <B>ButtonRenderer</B> is a class that renders the current value of <code>UICommand</code> as a Button.
 */

public class ButtonRenderer extends HtmlBasicRenderer {

    private static final Attribute[] ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.COMMANDBUTTON);

    // ---------------------------------------------------------- Public Methods

    @Override
    public void decode(FacesContext context, UIComponent component) {

        rendererParamsNotNull(context, component);

        if (!shouldDecode(component)) {
            return;
        }

        String clientId = decodeBehaviors(context, component);

        if (wasClicked(context, component, clientId) && !isReset(component)) {
            component.queueEvent(new ActionEvent(context, component));

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("This command resulted in form submission " + " ActionEvent queued.");
                logger.log(Level.FINE, "End decoding component {0}", component.getId());
            }
        }

    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }

        // Which button type (SUBMIT, RESET, or BUTTON) should we generate?
        String type = getButtonType(component);

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        String label = "";
        Object value = ((UICommand) component).getValue();
        if (value != null) {
            label = value.toString();
        }

        /*
         * If we have any parameters and the button type is submit or button, then render Javascript to use later.
         * RELEASE_PENDING this logic is slightly wrong - we should buffer the user onclick, and use it later. Leaving it for
         * when we decide how to do script injection.
         */

        String imageSrc = (String) component.getAttributes().get("image");
        writer.startElement("input", component);
        writeIdAttributeIfNecessary(context, writer, component);
        String clientId = component.getClientId(context);
        if (imageSrc != null) {
            writer.writeAttribute("type", "image", "type");
            writer.writeURIAttribute("src", RenderKitUtils.getImageSource(context, component, "image"), "image");
            writer.writeAttribute("name", clientId, "clientId");

            String alt = (String) component.getAttributes().get("alt");
            if (alt != null) {
                writer.writeAttribute("alt", alt, "alt");
            }
            else if (writer.getContentType().equals(RIConstants.XHTML_CONTENT_TYPE)) {
                writer.writeAttribute("alt", "", "alt"); // write out an empty alt as it is required by HTML spec.
            }
        } else {
            writer.writeAttribute("type", type, "type");
            writer.writeAttribute("name", clientId, "clientId");
            writer.writeAttribute("value", label, "value");
        }

        RenderKitUtils.renderPassThruAttributes(context, writer, component, ATTRIBUTES, getNonOnClickBehaviors(component));

        RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer, component);

        String styleClass = (String) component.getAttributes().get("styleClass");
        if (styleClass != null && styleClass.length() > 0) {
            writer.writeAttribute("class", styleClass, "styleClass");
        }

        // PENDING(edburns): Prior to i_spec_1111, this element
        // was rendered unconditionally

        if (component.getChildCount() == 0) {
            writer.endElement("input");
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        // PENDING(edburns): Prior to i_spec_1111, this element
        // was rendered unconditionally

        if (component.getChildCount() > 0) {
            context.getResponseWriter().endElement("input");
        }

        String type = getButtonType(component);
        Collection<ClientBehaviorContext.Parameter> params = getBehaviorParameters(component);

        if (!params.isEmpty() && (type.equals("submit") || type.equals("button"))) {
            RenderKitUtils.renderFacesJsIfNecessary(context);
        }

        RenderKitUtils.renderOnclickEventListener(context, component, params, null, false);
    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>
     * Determine if this component was activated on the client side.
     * </p>
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param component the component of interest
     * @param clientId the client id, if it has been retrieved, otherwise null
     * @return <code>true</code> if this component was in fact activated, otherwise <code>false</code>
     */
    private static boolean wasClicked(FacesContext context, UIComponent component, String clientId) {

        if (clientId == null) {
            clientId = component.getClientId(context);
        }

        if (context.getPartialViewContext().isAjaxRequest()) {
            return RenderKitUtils.isPartialOrBehaviorAction(context, clientId);
        } else {
            Map<String, String> requestParameterMap = context.getExternalContext().getRequestParameterMap();

            if (requestParameterMap.get(clientId) == null) {
                // Check to see whether we've got an action event from button of type="image"
                StringBuilder builder = new StringBuilder(clientId);
                String xValue = builder.append(".x").toString();
                builder.setLength(clientId.length());
                String yValue = builder.append(".y").toString();
                return requestParameterMap.get(xValue) != null && requestParameterMap.get(yValue) != null;
            }
            return true;
        }
    }

    /**
     * @param component the component of interest
     * @return <code>true</code> if the button represents a <code>reset</code> button, otherwise <code>false</code>
     */
    private static boolean isReset(UIComponent component) {

        return "reset".equals(component.getAttributes().get("type"));

    }

    /**
     * <p>
     * If the component's type attribute is null or not equal to <code>reset</code>, <code>submit</code> or
     * <code>button</code>, default to <code>submit</code>.
     *
     * @param component the component of interest
     * @return the type for this button
     */
    private static String getButtonType(UIComponent component) {

        String type = (String) component.getAttributes().get("type");
        if (type == null || !"reset".equals(type) && !"submit".equals(type) && !"button".equals(type)) {
            type = "submit";
            // This is needed in the decode method
            component.getAttributes().put("type", type);
        }
        return type;

    }

    // Returns the Behaviors map, but only if it contains some entry other
    // than those handled by renderOnclick(). This helps us optimize
    // renderPassThruAttributes() in the very common case where the
    // button only contains an "action" (or "click") Behavior. In that
    // we pass a null Behaviors map into renderPassThruAttributes(),
    // which allows us to take a more optimized code path.
    private static Map<String, List<ClientBehavior>> getNonOnClickBehaviors(UIComponent component) {

        return getPassThruBehaviors(component, "click", "action");
    }

} // end of class ButtonRenderer
