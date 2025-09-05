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

// CheckboxRenderer.java

package com.sun.faces.renderkit.html_basic;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.ConverterException;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;

/**
 * <B>CheckboxRenderer</B> is a class that renders the current value of <code>UISelectBoolean</code> as a checkbox.
 */

public class CheckboxRenderer extends HtmlBasicInputRenderer {

    private static final Attribute[] ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.SELECTBOOLEANCHECKBOX);

    // ---------------------------------------------------------- Public Methods

    @Override
    public void decode(FacesContext context, UIComponent component) {

        rendererParamsNotNull(context, component);

        if (!shouldDecode(component)) {
            return;
        }

        String clientId = decodeBehaviors(context, component);

        if (clientId == null) {
            clientId = component.getClientId(context);
        }
        assert clientId != null;
        // Convert the new value

        Map<String, String> requestParameterMap = context.getExternalContext().getRequestParameterMap();
        boolean isChecked = isChecked(requestParameterMap.get(clientId));
        setSubmittedValue(component, isChecked);
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "new value after decoding: {0}", isChecked);
        }

    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

    }

    @Override
    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {

        return submittedValue instanceof Boolean ? submittedValue : Boolean.valueOf(submittedValue.toString());

    }

    // ------------------------------------------------------- Protected Methods

    @Override
    protected void getEndTextToRender(FacesContext context, UIComponent component, String currentValue) throws IOException {

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;
        String styleClass;

        writer.startElement("input", component);
        writeIdAttributeIfNecessary(context, writer, component);
        writer.writeAttribute("type", "checkbox", "type");
        writer.writeAttribute("name", component.getClientId(context), "clientId");

        if (Boolean.valueOf(currentValue)) {
            writer.writeAttribute("checked", Boolean.TRUE, "value");
        }
        if (null != (styleClass = (String) component.getAttributes().get("styleClass"))) {
            writer.writeAttribute("class", styleClass, "styleClass");
        }
        RenderKitUtils.renderPassThruAttributes(context, writer, component, ATTRIBUTES, getNonOnClickSelectBehaviors(component));
        RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer, component);

        writer.endElement("input");

        RenderKitUtils.renderSelectOnclickEventListener(context, component, null, false);

    }

    // --------------------------------------------------------- Private Methods

    /**
     * @param value the submitted value
     * @return "true" if the component was checked, otherise "false"
     */
    private static boolean isChecked(String value) {

        return "on".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);

    }

} // end of class CheckboxRenderer
