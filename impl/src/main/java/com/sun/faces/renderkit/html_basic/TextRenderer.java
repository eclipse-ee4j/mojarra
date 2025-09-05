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

// TextRenderer.java

package com.sun.faces.renderkit.html_basic;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.html.HtmlInputFile;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;

/**
 * <B>TextRenderer</B> is a class that renders the current value of <code>UIInput</code> or <code>UIOutput</code>
 * component as a input field or static text.
 */
public class TextRenderer extends HtmlBasicInputRenderer {

    private static final Attribute[] INPUTTEXT_ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.INPUTTEXT);
    private static final Attribute[] INPUTFILE_ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.INPUTFILE);
    private static final Attribute[] OUTPUT_ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.OUTPUTTEXT);

    private static final Map<String, String> RECOMMENDED_COMPONENTS_BY_DISCOMMENDED_TYPES = mapRecommendedComponentsByDiscommendedTypes();

    private static Map<String, String> mapRecommendedComponentsByDiscommendedTypes() {
        Map<String, String> map = new HashMap<>(9);
        map.put("hidden", "<h:inputHidden>");
        map.put("password", "<h:inputSecret>");
        map.put("checkbox", "<h:selectBooleanCheckbox> or <h:selectManyCheckbox>");
        map.put("radio", "<h:selectOneRadio>");
        map.put("file", "<h:inputFile>");
        map.put("submit", "<h:commandButton>");
        map.put("image", "<h:commandButton type=\"image\">");
        map.put("reset", "<h:commandButton type=\"reset\">");
        map.put("button", "<h:commandButton type=\"button\"> or <h:button>");
        return Collections.unmodifiableMap(map);
    }

    // ---------------------------------------------------------- Public Methods

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        rendererParamsNotNull(context, component);
    }

    // ------------------------------------------------------- Protected Methods

    @Override
    protected void getEndTextToRender(FacesContext context, UIComponent component, String currentValue) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;
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
                String accept = ((HtmlInputFile) component).getAccept();

                if (accept != null) {
                    writer.writeAttribute("accept", accept, "accept");
                }
            } else if (component instanceof HtmlInputText) {
                String type = ((HtmlInputText) component).getType();

                if (context.isProjectStage(ProjectStage.Development)) {
                    String recommendedComponent = RECOMMENDED_COMPONENTS_BY_DISCOMMENDED_TYPES.get(type.trim().toLowerCase());

                    if (recommendedComponent != null) {
                        String message = "<h:inputText type=\"" + type + "\"> is discommended, you should instead use " + recommendedComponent;
                        context.addMessage(component.getClientId(context), new FacesMessage(FacesMessage.SEVERITY_WARN, message, message));
                    }
                }

                writer.writeAttribute("type", type, null);
            } else {
                writer.writeAttribute("type", "text", null);
            }
            writer.writeAttribute("name", component.getClientId(context), "clientId");

            // only output the autocomplete attribute if the value
            // is 'off' since its lack of presence will be interpreted
            // as 'on' by the browser
            if ("off".equals(component.getAttributes().get("autocomplete"))) {
                writer.writeAttribute("autocomplete", "off", "autocomplete");
            }

            // render default text specified
            if (currentValue != null) {
                writer.writeAttribute("value", currentValue, "value");
            }
            if (null != styleClass) {
                writer.writeAttribute("class", styleClass, "styleClass");
            }

            // style is rendered as a passthru attribute
            Attribute[] attributes = component instanceof HtmlInputFile ? INPUTFILE_ATTRIBUTES : INPUTTEXT_ATTRIBUTES;
            RenderKitUtils.renderPassThruAttributes(context, writer, component, attributes, getNonOnChangeBehaviors(component));
            RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer, component);

            writer.endElement("input");

            RenderKitUtils.renderOnchangeEventListener(context, component, hasPassthroughAttributes);

        } else if (isOutput = component instanceof UIOutput) {
            if (styleClass != null || style != null || dir != null || lang != null || title != null || hasPassthroughAttributes
                    || (shouldWriteIdAttribute = shouldWriteIdAttribute(component))) {
                writer.startElement("span", component);
                writeIdAttributeIfNecessary(context, writer, component);
                if (null != styleClass) {
                    writer.writeAttribute("class", styleClass, "styleClass");
                }
                // style is rendered as a passthru attribute
                RenderKitUtils.renderPassThruAttributes(context, writer, component, OUTPUT_ATTRIBUTES);

            }
            if (currentValue != null) {
                Object val = component.getAttributes().get("escape");
                if (val != null && Boolean.valueOf(val.toString())) {
                    writer.writeText(currentValue, component, "value");
                } else {
                    writer.write(currentValue);
                }
            }
        }
        if (isOutput && (styleClass != null || style != null || dir != null || lang != null || title != null || hasPassthroughAttributes
                || shouldWriteIdAttribute)) {
            writer.endElement("span");
        }

    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        boolean renderChildren = WebConfiguration.getInstance().isOptionEnabled(WebConfiguration.BooleanWebContextInitParameter.AllowTextChildren);

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

}
