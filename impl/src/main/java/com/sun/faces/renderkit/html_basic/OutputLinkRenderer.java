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

// OutputLinkRenderer.java

package com.sun.faces.renderkit.html_basic;

import static com.sun.faces.util.Util.componentIsDisabled;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Level.FINE;

import java.io.IOException;
import java.net.URLEncoder;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

/**
 * <B>OutputLinkRenderer</B> is a class ...
 *
 * <B>Lifetime And Scope</B>
 *
 */
public class OutputLinkRenderer extends LinkRenderer {

    private static final Attribute[] ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.OUTPUTLINK);

    // ---------------------------------------------------------- Public Methods

    @Override
    public void decode(FacesContext context, UIComponent component) {
        rendererParamsNotNull(context, component);

        if (shouldDecode(component)) {
            decodeBehaviors(context, component);
        }
    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        rendererParamsNotNull(context, component);

        UIOutput output = (UIOutput) component;
        boolean componentDisabled = false;
        if (output.getAttributes().get("disabled") != null) {
            if (output.getAttributes().get("disabled").equals(Boolean.TRUE)) {
                componentDisabled = true;
            }
        }
        if (componentDisabled) {
            renderAsDisabled(context, output);
        } else {
            renderAsActive(context, output);
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
            // Write Anchor inline elements
            // Done writing Anchor element
            writer.endElement("a");
        }

    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    // ------------------------------------------------------- Protected Methods

    protected String getFragment(UIComponent component) {

        String fragment = (String) component.getAttributes().get("fragment");
        fragment = fragment != null ? fragment.trim() : "";
        if (fragment.length() > 0) {
            fragment = "#" + fragment;
        }
        return fragment;

    }

    @Override
    protected Object getValue(UIComponent component) {
        if (componentIsDisabled(component)) {
            return null;
        }

        return ((UIOutput) component).getValue();
    }

    @Override
    protected void renderAsActive(FacesContext context, UIComponent component) throws IOException {
        String hrefVal = getCurrentValue(context, component);
        if (logger.isLoggable(FINE)) {
            logger.fine("Value to be rendered " + hrefVal);
        }

        // suppress rendering if "rendered" property on the output is
        // false
        if (!component.isRendered()) {
            if (logger.isLoggable(FINE)) {
                logger.fine("End encoding component " + component.getId() + " since " + "rendered attribute is set to false ");
            }
            return;
        }
        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;
        writer.startElement("a", component);
        String writtenId = writeIdAttributeIfNecessary(context, writer, component);
        if (null != writtenId) {
            writer.writeAttribute("name", writtenId, "name");
        }
        // render an empty value for href if it is not specified
        if (null == hrefVal || 0 == hrefVal.length()) {
            hrefVal = "";
        }

        // Write Anchor attributes

        Param[] paramList = getParamList(component);
        StringBuilder sb = new StringBuilder();
        sb.append(hrefVal);
        boolean paramWritten = hrefVal.indexOf('?') > 0;

        for (Param param : paramList) {
            String pn = param.name;
            if (pn != null && pn.length() != 0) {
                String pv = param.value;
                sb.append(paramWritten ? '&' : '?');
                sb.append(URLEncoder.encode(pn, UTF_8));
                sb.append('=');
                if (pv != null && pv.length() != 0) {
                    sb.append(URLEncoder.encode(pv, UTF_8));
                }
                paramWritten = true;
            }
        }
        sb.append(getFragment(component));
        writer.writeURIAttribute("href", context.getExternalContext().encodeResourceURL(sb.toString()), "href");
        RenderKitUtils.renderPassThruAttributes(context, writer, component, ATTRIBUTES);
        RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer, component);

        String target = (String) component.getAttributes().get("target");
        if (target != null && target.trim().length() != 0) {
            writer.writeAttribute("target", target, "target");
        }

        writeCommonLinkAttributes(writer, component);

        writer.flush();

    }

}
