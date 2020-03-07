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

// LabelRenderer.java

package com.sun.faces.renderkit.html_basic;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;

import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.search.SearchExpressionContext;
import jakarta.faces.component.search.SearchExpressionHint;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

/**
 * <p>
 * <B>LabelRenderer</B> renders Label element.
 * <p>
 * .
 */
public class LabelRenderer extends HtmlBasicInputRenderer {

    private static final Attribute[] ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.OUTPUTLABEL);

    private static final String RENDER_END_ELEMENT = "com.sun.faces.RENDER_END_ELEMENT";

    // ---------------------------------------------------------- Public Methods

    private static final Set<SearchExpressionHint> EXPRESSION_HINTS = EnumSet.of(SearchExpressionHint.RESOLVE_SINGLE_COMPONENT,
            SearchExpressionHint.IGNORE_NO_RESULT);

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        String forClientId = null;
        String forValue = (String) component.getAttributes().get("for");
        if (forValue != null) {
            SearchExpressionContext searchExpressionContext = SearchExpressionContext.createSearchExpressionContext(context, component, EXPRESSION_HINTS, null);

            forClientId = context.getApplication().getSearchExpressionHandler().resolveClientId(searchExpressionContext, forValue);

            if (forClientId == null) {
                // it could that the component hasn't been created yet. So
                // construct the clientId for component.
                forClientId = getForComponentClientId(component, context, forValue);
            }
        }

        // set a temporary attribute on the component to indicate that
        // label end element needs to be rendered.
        component.getAttributes().put(RENDER_END_ELEMENT, "yes");
        writer.startElement("label", component);
        writeIdAttributeIfNecessary(context, writer, component);
        if (forClientId != null) {
            writer.writeAttribute("for", forClientId, "for");
        }

        RenderKitUtils.renderPassThruAttributes(context, writer, component, ATTRIBUTES);
        String styleClass = (String) component.getAttributes().get("styleClass");
        if (null != styleClass) {
            writer.writeAttribute("class", styleClass, "styleClass");
        }

        // render the curentValue as label text if specified.
        String value = getCurrentValue(context, component);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Value to be rendered " + value);
        }
        if (value != null && value.length() != 0) {
            Object val = component.getAttributes().get("escape");
            boolean escape = val != null && Boolean.valueOf(val.toString());

            if (escape) {
                writer.writeText(value, component, "value");
            } else {
                writer.write(value);
            }
        }
        writer.flush();

    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }

        // render label end element if RENDER_END_ELEMENT is set.
        String render = (String) component.getAttributes().get(RENDER_END_ELEMENT);
        if ("yes".equals(render)) {
            component.getAttributes().remove(RENDER_END_ELEMENT);
            ResponseWriter writer = context.getResponseWriter();
            assert writer != null;
            writer.endElement("label");
        }

    }

    // ------------------------------------------------------- Private Methods

    /**
     * Builds and returns the clientId of the component that is represented by the forValue. Since the component has not
     * been created yet, invoking <code>getClientId(context)</code> is not possible.
     *
     * @param component UIComponent that represents the label
     * @param context FacesContext for this request
     * @param forValue String representing the "id" of the component that this label represents.
     *
     * @return String clientId of the component represented by the forValue.
     */
    protected String getForComponentClientId(UIComponent component, FacesContext context, String forValue) {

        String result = null;
        // ASSUMPTION: The component for which this acts as the label
        // as well ths label component are part of the same form.
        // locate the nearest NamingContainer and get its clientId.
        UIComponent parent = component.getParent();
        while (parent != null) {
            if (parent instanceof NamingContainer) {
                break;
            }
            parent = parent.getParent();
        }
        if (parent == null) {
            return result;
        }
        String parentClientId = parent.getClientId(context);
        // prepend the clientId of the nearest container to the forValue.
        result = parentClientId + UINamingContainer.getSeparatorChar(context) + forValue;
        return result;

    }

    // The testcase for this class is TestRenderResponsePhase.java

} // end of class LabelRenderer
