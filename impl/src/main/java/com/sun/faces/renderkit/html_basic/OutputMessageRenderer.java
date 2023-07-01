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

// OutputMessageRenderer.java

package com.sun.faces.renderkit.html_basic;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.faces.renderkit.RenderKitUtils;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIParameter;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

/** <B>OutputMessageRenderer</B> is a class that renderes UIOutput */

public class OutputMessageRenderer extends HtmlBasicInputRenderer {

    // ---------------------------------------------------------- Public Methods

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }

        String currentValue = getCurrentValue(context, component);
        // If null, do not output anything - return.
        if (null == currentValue) {
            return;
        }
        int childCount = component.getChildCount();
        List<Object> parameterList;

        if (childCount > 0) {
            parameterList = new ArrayList<>(childCount);
            // get UIParameter children...

            for (UIComponent kid : component.getChildren()) {
                // PENDING(rogerk) ignore if child is not UIParameter?
                if (!(kid instanceof UIParameter)) {
                    continue;
                }

                parameterList.add(((UIParameter) kid).getValue());
            }
        } else {
            parameterList = Collections.emptyList();
        }

        // If at least one substitution parameter was specified,
        // use the string as a MessageFormat instance.
        String message;
        if (parameterList.size() > 0) {
            MessageFormat fmt = new MessageFormat(currentValue, context.getViewRoot().getLocale());
            StringBuffer buf = new StringBuffer(currentValue.length() * 2);
            fmt.format(parameterList.toArray(new Object[parameterList.size()]), buf, null);
            message = buf.toString();
        } else {
            message = currentValue;
        }

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        String style = (String) component.getAttributes().get("style");
        String styleClass = (String) component.getAttributes().get("styleClass");
        String lang = (String) component.getAttributes().get("lang");
        String dir = (String) component.getAttributes().get("dir");
        String title = (String) component.getAttributes().get("title");
        boolean wroteSpan = false;
        if (styleClass != null || style != null || dir != null || lang != null || title != null || shouldWriteIdAttribute(component)) {
            writer.startElement("span", component);
            writeIdAttributeIfNecessary(context, writer, component);
            wroteSpan = true;

            if (style != null) {
                writer.writeAttribute("style", style, "style");
            }
            if (null != styleClass) {
                writer.writeAttribute("class", styleClass, "styleClass");
            }
            if (dir != null) {
                writer.writeAttribute("dir", dir, "dir");
            }
            if (lang != null) {
                writer.writeAttribute(RenderKitUtils.prefixAttribute("lang", writer), lang, "lang");
            }
            if (title != null) {
                writer.writeAttribute("title", title, "title");
            }
        }

        Object val = component.getAttributes().get("escape");
        boolean escape = val != null && Boolean.valueOf(val.toString());

        if (escape) {
            writer.writeText(message, component, "value");
        } else {
            writer.write(message);
        }
        if (wroteSpan) {
            writer.endElement("span");
        }

    }

} // end of class OutputMessageRenderer
