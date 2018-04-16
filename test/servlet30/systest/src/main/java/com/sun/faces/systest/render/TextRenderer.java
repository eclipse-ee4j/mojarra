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

// TextRenderer.java

package com.sun.faces.systest.render;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.render.Renderer;

import java.io.IOException;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.MessageFactory;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.Util;


/**
 * <B>TextRenderer</B> is a class that renders the current value of
 * <code>UIInput<code> or <code>UIOutput<code> component as a input field or
 * static text.
 */
public class TextRenderer extends Renderer {

    private static final Attribute[] INPUT_ATTRIBUTES =
              AttributeManager.getAttributes(AttributeManager.Key.INPUTTEXT);
        private static final Attribute[] OUTPUT_ATTRIBUTES =
              AttributeManager.getAttributes(AttributeManager.Key.OUTPUTTEXT);


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

    public TextRenderer() {
        super();
    }

    //
    // Class methods
    //

    //
    // General Methods
    //

    //
    // Methods From Renderer
    //

    public void encodeBegin(FacesContext context, UIComponent component)
        throws IOException {
        if (context == null) {
            throw new NullPointerException(
                MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context"));
        }
        if (component == null) {
            throw new NullPointerException(
                MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "component"));
        }
    }

    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {

        String currentValue = null;
        ResponseWriter writer = null;
        String styleClass = null;

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

        writer = context.getResponseWriter();
        assert (writer != null);

        currentValue = getCurrentValue(context, component);
        getEndTextToRender(context, component, currentValue);
    }

    protected String getCurrentValue(FacesContext context, UIComponent component) {

        if (component instanceof UIInput) {
            Object submittedValue = ((UIInput) component).getSubmittedValue();
            if (submittedValue != null) {
                return (String) submittedValue;
            }
        }

        String currentValue = null;
        Object currentObj = getValue(component);
        if (currentObj != null) {
            currentValue = getFormattedValue(context, component, currentObj);
        }
        return currentValue;
    }

    protected void getEndTextToRender(FacesContext context,
                                      UIComponent component, String currentValue)
        throws IOException {

        ResponseWriter writer = context.getResponseWriter();
        assert (writer != null);
        boolean
            shouldWriteIdAttribute = false,
            isOutput = false;

        String style = (String) component.getAttributes().get("style");
        String styleClass = (String) component.getAttributes().get("styleClass");
        String dir = (String) component.getAttributes().get("dir");
        String lang = (String) component.getAttributes().get("lang");
        String title = (String) component.getAttributes().get("title");
        if (component instanceof UIInput) {
            writer.startElement("input", component);
            writeIdAttributeIfNecessary(context, writer, component);
            writer.writeAttribute("type", "text", null);
            writer.writeAttribute("name", (component.getClientId(context)),
                                  "clientId");

            // render default text specified
            if (currentValue != null) {
                writer.writeAttribute("value", currentValue, "value");
            }
            if (null != styleClass) {
                writer.writeAttribute("class", styleClass, "styleClass");
            }

            // style is rendered as a passthur attribute
            RenderKitUtils.renderPassThruAttributes(context,
                  writer,
                                                    component,
                                                    INPUT_ATTRIBUTES);
            RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer, component);

            writer.endElement("input");

        } else if (isOutput = (component instanceof UIOutput)) {
            if (styleClass != null
                 || style != null
                 || dir != null
                 || lang != null
                 || title != null
                 || (shouldWriteIdAttribute = shouldWriteIdAttribute(component))) {
                writer.startElement("span", component);
                writeIdAttributeIfNecessary(context, writer, component);
                if (null != styleClass) {
                    writer.writeAttribute("class", styleClass, "styleClass");
                }
                // style is rendered as a passthru attribute
                RenderKitUtils.renderPassThruAttributes(context,
                      writer,
                                                        component,
                                                        OUTPUT_ATTRIBUTES);
                RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer, component);

            }
            if (currentValue != null) {
                Object val = null;
                boolean escape = true;
                if (null != (val = component.getAttributes().get("escape"))) {
                    if (val instanceof Boolean) {
                        escape = ((Boolean) val).booleanValue();
                    } else if (val instanceof String) {
                        try {
                            escape =
                                Boolean.valueOf((String) val).booleanValue();
                        } catch (Throwable e) {
                        }
                    }
                }
                if (escape) {
                    writer.writeText(currentValue, "value");
                } else {
                    writer.write(currentValue);
                }
                writer.writeText(" FROM THE CUSTOM RENDERER", null);
            }
        }
        if (isOutput && (styleClass != null
                 || style != null
                 || dir != null
                 || lang != null
                 || title != null
                 || (shouldWriteIdAttribute))) {
            writer.endElement("span");
        }
    }

    protected Object getValue(UIComponent component) {
        if (component instanceof ValueHolder) {
            Object value = ((ValueHolder) component).getValue();
            return value;
        }

        return null;
    }

    protected String getFormattedValue(FacesContext context, UIComponent component,
                                       Object currentValue)
        throws ConverterException {

        String result = null;
        // formatting is supported only for components that support
        // converting value attributes.
        if (!(component instanceof ValueHolder)) {
            if (currentValue != null) {
                result = currentValue.toString();
            }
            return result;
        }

        Converter converter = null;

        // If there is a converter attribute, use it to to ask application
        // instance for a converter with this identifer.

        if (component instanceof ValueHolder) {
            converter = ((ValueHolder) component).getConverter();
        }

        // if value is null and no converter attribute is specified, then
        // return a zero length String.
        if (converter == null && currentValue == null) {
            return "";
        }

        if (converter == null) {
            // Do not look for "by-type" converters for Strings
            if (currentValue instanceof String) {
                return (String) currentValue;
            }

            // if converter attribute set, try to acquire a converter
            // using its class type.

            Class converterType = currentValue.getClass();
            converter = Util.getConverterForClass(converterType, context);

            // if there is no default converter available for this identifier,
            // assume the model type to be String.
            if (converter == null && currentValue != null) {
                result = currentValue.toString();
                return result;
            }
        }

        if (converter != null) {
            result = converter.getAsString(context, component, currentValue);

            return result;
        } else {
            // throw converter exception if no converter can be
            // identified
	    Object [] params = {
		currentValue,
		"null Converter"
	    };

            throw new ConverterException(MessageFactory.getMessage(
                context, MessageUtils.CONVERSION_ERROR_MESSAGE_ID, params));
        }
    }
    private boolean shouldWriteIdAttribute(UIComponent component) {
        String id;
        return (null != (id = component.getId()) &&
            !id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX));
    }

    private void writeIdAttributeIfNecessary(FacesContext context,
                                               ResponseWriter writer,
                                               UIComponent component) {
        String id;
        if (shouldWriteIdAttribute(component)) {
            try {
                writer.writeAttribute("id", component.getClientId(context),
                                      "id");
            } catch (IOException e) {
            }
        }
    }


    // The testcase for this class is TestRenderers_2.java

} // end of class TextRenderer


