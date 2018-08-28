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

// ButtonRenderer.java

package com.sun.faces.test.servlet30.renderkit;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.Util;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;
import javax.faces.render.Renderer;

import java.io.IOException;
import java.util.Map;

/**
 * <B>ButtonRenderer</B> is a class that renders the current value of <code>UICommand<code> as a
 * Button.
 */

public class ButtonRenderer extends Renderer {

    private static final Attribute[] ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.COMMANDBUTTON);

    public static final String CLEAR_HIDDEN_FIELD_FN_NAME = "clearFormHiddenParams";
    public static final String FORM_CLIENT_ID_ATTR = "com.sun.faces.FORM_CLIENT_ID_ATTR";

    //
    // Protected Constants
    //
    // Log instance for this class

    //
    // Class Variables
    //

    //
    // Instance Variables
    //

    // Attribute Instance Variables

    // Relationship Instance Variables

    //
    // Constructors and Initializers
    //

    //
    // Class methods
    //

    //
    // General Methods
    //

    //
    // Methods From Renderer
    //

    @Override
    public void decode(FacesContext context, UIComponent component) {
        if (context == null) {
            throw new NullPointerException(
                    MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context"));
        }
        if (component == null) {
            throw new NullPointerException(
                    MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "component"));
        }

        // If the component is disabled, do not change the value of the
        // component, since its state cannot be changed.
        if (Util.componentIsDisabledOrReadonly(component)) {
            return;
        }

        // Was our command the one that caused this submission?
        // we don' have to worry about getting the value from request parameter
        // because we just need to know if this command caused the submission. We
        // can get the command name by calling currentValue. This way we can
        // get around the IE bug.
        String clientId = component.getClientId(context);
        Map requestParameterMap = context.getExternalContext().getRequestParameterMap();
        String value = (String) requestParameterMap.get(clientId);
        if (value == null) {
            if (requestParameterMap.get(clientId + ".x") == null && requestParameterMap.get(clientId + ".y") == null) {
                return;
            }
        }

        String type = (String) component.getAttributes().get("type");
        if ((type != null) && (type.toLowerCase().equals("reset"))) {
            return;
        }
        ActionEvent actionEvent = new ActionEvent(component);
        component.queueEvent(actionEvent);

        return;
    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        if (context == null) {
            throw new NullPointerException(
                    MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context"));
        }
        if (component == null) {
            throw new NullPointerException(
                    MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "component"));
        }
        // suppress rendering if "rendered" property on the component is
        // false.
        if (!component.isRendered()) {
            return;
        }

        // Which button type (SUBMIT, RESET, or BUTTON) should we generate?
        String type = (String) component.getAttributes().get("type");
        String styleClass = null;
        if (type == null) {
            type = "submit";
            // This is needed in the decode method
            component.getAttributes().put("type", type);
        }

        ResponseWriter writer = context.getResponseWriter();
        assert (writer != null);

        String label = "";
        Object value = ((UICommand) component).getValue();
        if (value != null) {
            label = value.toString();
        }
        String imageSrc = (String) component.getAttributes().get("image");
        writer.startElement("input", component);
        writeIdAttributeIfNecessary(context, writer, component);
        String clientId = component.getClientId(context);
        if (imageSrc != null) {
            writer.writeAttribute("type", "image", "type");
            writer.writeURIAttribute("src", src(context, imageSrc), "image");
            writer.writeAttribute("name", clientId, "clientId");
        } else {
            writer.writeAttribute("type", type.toLowerCase(), "type");
            writer.writeAttribute("name", clientId, "clientId");
            writer.writeAttribute("value", label, "value");
        }

        // look up the clientId of the form in request scope to arrive the name of
        // the javascript function to invoke from the onclick event handler.
        // PENDING (visvan) we need to fix this dependency between the renderers.
        // This solution is only temporary.
        Map requestMap = context.getExternalContext().getRequestMap();
        String formClientId = (String) requestMap.get(FORM_CLIENT_ID_ATTR);

        StringBuffer sb = new StringBuffer();
        // call the javascript function that clears the all the hidden field
        // parameters in the form.
        sb.append(CLEAR_HIDDEN_FIELD_FN_NAME);
        if (formClientId != null) {
            sb.append("_").append(formClientId.replace(UINamingContainer.getSeparatorChar(context), '_'));
        }
        sb.append("(this.form.id);");
        // append user specified script for onclick if any.
        String onclickAttr = (String) component.getAttributes().get("onclick");
        if (onclickAttr != null && onclickAttr.length() != 0) {
            sb.append(onclickAttr);

        }
        writer.writeAttribute("onclick", sb.toString(), null);

        RenderKitUtils.renderPassThruAttributes(context, writer, component, ATTRIBUTES);
        RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer, component);

        if (null != (styleClass = (String) component.getAttributes().get("styleClass"))) {
            writer.writeAttribute("class", styleClass, "styleClass");
        }
        writer.endElement("input");
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        if (context == null) {
            throw new NullPointerException(
                    MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context"));
        }
        if (component == null) {
            throw new NullPointerException(
                    MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "component"));
        }
    }

    //
    // General Methods
    //

    private String src(FacesContext context, String value) {
        if (value == null) {
            return "";
        }
        value = context.getApplication().getViewHandler().getResourceURL(context, value);
        return (context.getExternalContext().encodeResourceURL(value));
    }

    private boolean shouldWriteIdAttribute(UIComponent component) {
        String id;
        return (null != (id = component.getId()) && !id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX));
    }

    private void writeIdAttributeIfNecessary(FacesContext context, ResponseWriter writer, UIComponent component) {
        String id;
        if (shouldWriteIdAttribute(component)) {
            try {
                writer.writeAttribute("id", component.getClientId(context), "id");
            } catch (IOException e) {
            }
        }
    }

} // end of class ButtonRenderer
