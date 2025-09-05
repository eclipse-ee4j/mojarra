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

package com.sun.faces.renderkit.html_basic;

import java.io.IOException;

import jakarta.faces.application.NavigationCase;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import com.sun.faces.RIConstants;
import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.Util;

public class OutcomeTargetButtonRenderer extends OutcomeTargetRenderer {

    private static final Attribute[] ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.OUTCOMETARGETBUTTON);

    // --------------------------------------------------- Methods from Renderer

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);
        if (!shouldEncode(component)) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        writer.startElement("input", component);
        writeIdAttributeIfNecessary(context, writer, component);

        String imageSrc = (String) component.getAttributes().get("image");
        if (imageSrc != null) {
            writer.writeAttribute("type", "image", "type");
            writer.writeURIAttribute("src", RenderKitUtils.getImageSource(context, component, "image"), "image");

            String alt = (String) component.getAttributes().get("alt");
            if (alt == null) {
                writer.writeAttribute("alt", alt, "alt");
            }
            else if (writer.getContentType().equals(RIConstants.XHTML_CONTENT_TYPE)) {
                writer.writeAttribute("alt", "", "alt"); // write out an empty alt as it is required by HTML spec.
            }
        } else {
            writer.writeAttribute("type", "button", "type");
        }

        String label = getLabel(component);

        // value should be used even for image type for accessibility (e.g., images disabled in browser)
        writer.writeAttribute("value", label, "value");

        String styleClass = (String) component.getAttributes().get("styleClass");
        if (styleClass != null && styleClass.length() > 0) {
            writer.writeAttribute("class", styleClass, "styleClass");
        }

        renderPassThruAttributes(context, writer, component, ATTRIBUTES, null);

        if (!Util.componentIsDisabled(component)) {
            NavigationCase navCase = getNavigationCase(context, component);

            if (navCase == null) {
                // QUESTION should this only be added in development mode?
                label += MessageUtils.getExceptionMessageString(MessageUtils.OUTCOME_TARGET_BUTTON_NO_MATCH);
                writer.writeAttribute("disabled", "true", "disabled");
            }
        }

        if (component.getChildCount() == 0) {
            writer.endElement("input");
        }

    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (component.getChildCount() > 0) {
            context.getResponseWriter().endElement("input");
        }

        if (!Util.componentIsDisabled(component)) {
            NavigationCase navCase = getNavigationCase(context, component);

            if (navCase != null) {
                String hrefVal = getEncodedTargetURL(context, component, navCase);
                hrefVal += getFragment(component);
                RenderKitUtils.addEventListener(context, component, null,"click", getOnclick(component, hrefVal));
            }
        }
    }

    // ------------------------------------------------------- Protected Methods

    protected String getOnclick(UIComponent component, String targetURI) {

        String onclick = (String) component.getAttributes().get("onclick");

        if (onclick != null) {
            onclick = onclick.trim();
            if (onclick.length() > 0 && !onclick.endsWith(";")) {
                onclick += "; ";
            }
        } else {
            onclick = "";
        }

        if (targetURI != null) {
            onclick += "window.location.href='" + targetURI + "'; ";
        }

        onclick += "return false;";

        return onclick;

    }

}
