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

package com.sun.faces.renderkit.html_basic;

import java.io.IOException;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.html.HtmlInputFile;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import com.sun.faces.config.WebConfiguration;
import java.util.Map;

/**
 * <B>TextRenderer</B> is a class that renders the current value of
 * <code>UIInput<code> or <code>UIOutput<code> component as a input field or
 * static text.
 */
public class TextRenderer extends HtmlBasicInputRenderer {


    private static final Attribute[] INPUT_ATTRIBUTES =
          AttributeManager.getAttributes(AttributeManager.Key.INPUTTEXT);
    private static final Attribute[] OUTPUT_ATTRIBUTES = 
          AttributeManager.getAttributes(AttributeManager.Key.OUTPUTTEXT);


    // ---------------------------------------------------------- Public Methods


    @Override
    public void encodeBegin(FacesContext context, UIComponent component)
          throws IOException {

        rendererParamsNotNull(context, component);

    }

    // ------------------------------------------------------- Protected Methods


    @Override
    protected void getEndTextToRender(FacesContext context,
                                      UIComponent component,
                                      String currentValue)
          throws IOException {

        ResponseWriter writer = context.getResponseWriter();
        assert(writer != null);
        boolean shouldWriteIdAttribute = false;
        boolean isOutput = false;

        String style = (String) component.getAttributes().get("style");
        String styleClass = (String) component.getAttributes().get("styleClass");
        String dir = (String) component.getAttributes().get("dir");
        String lang = (String) component.getAttributes().get("lang");
        String title = (String) component.getAttributes().get("title");
        Map<String, Object> passthroughAttributes = component.getPassThroughAttributes(false);
        boolean hasPassthroughAttributes = null != passthroughAttributes && !passthroughAttributes.isEmpty();
        if (component instanceof UIInput) {
            writer.startElement("input", component);
            writeIdAttributeIfNecessary(context, writer, component);
            
            if (component instanceof HtmlInputFile) {
                writer.writeAttribute("type", "file", null);
            }
            else {
                writer.writeAttribute("type", "text", null);
            }
            writer.writeAttribute("name", (component.getClientId(context)),
                                  "clientId");

            // only output the autocomplete attribute if the value
            // is 'off' since its lack of presence will be interpreted
            // as 'on' by the browser
            if ("off".equals(component.getAttributes().get("autocomplete"))) {
                writer.writeAttribute("autocomplete",
                                      "off",
                                      "autocomplete");
            }

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
                                                    INPUT_ATTRIBUTES,
                                                    getNonOnChangeBehaviors(component));
            RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer, component);

            RenderKitUtils.renderOnchange(context, component, false);


            writer.endElement("input");

        } else if (isOutput = (component instanceof UIOutput)) {
            if (styleClass != null
                 || style != null
                 || dir != null
                 || lang != null
                 || title != null
                 || hasPassthroughAttributes
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

            }
            if (currentValue != null) {
                Object val = component.getAttributes().get("escape");
                if ((val != null) && Boolean.valueOf(val.toString())) {
                    writer.writeText(currentValue, component, "value");
                } else {
                    writer.write(currentValue);
                }
            }
        }
        if (isOutput && (styleClass != null
                 || style != null
                 || dir != null
                 || lang != null
                 || title != null
                 || hasPassthroughAttributes
                 || (shouldWriteIdAttribute))) {
            writer.endElement("span");
        }

    }


    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component)
          throws IOException {

        boolean renderChildren = WebConfiguration.getInstance()
                .isOptionEnabled(WebConfiguration.BooleanWebContextInitParameter.AllowTextChildren);

        if (!renderChildren) {
            return;
        }

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

    // The testcase for this class is TestRenderers_2.java

} // end of class TextRenderer


