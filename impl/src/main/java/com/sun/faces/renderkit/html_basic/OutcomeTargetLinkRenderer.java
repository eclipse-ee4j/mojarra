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
import java.util.Arrays;
import java.util.List;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.Util;

import jakarta.faces.application.NavigationCase;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

public class OutcomeTargetLinkRenderer extends OutcomeTargetRenderer {

    private static final Attribute[] ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.OUTCOMETARGETLINK);

    private static final String NO_NAV_CASE = OutcomeTargetLinkRenderer.class.getName() + "_NO_NAV_CASE";

    // Attributes that are to excluded from rendering for this renderer.
    private static final List<String> EXCLUDED_ATTRIBUTES = Arrays.asList("disabled");

    // --------------------------------------------------- Methods from Renderer

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }

        NavigationCase navCase = null;
        boolean failedToResolveNavigationCase = false;
        boolean disabled = Util.componentIsDisabled(component);

        if (!disabled) {
            navCase = getNavigationCase(context, component);
            if (navCase == null) {
                failedToResolveNavigationCase = true;
                context.getAttributes().put(NO_NAV_CASE, true);
            }

        }

        if (disabled || navCase == null) {
            renderAsDisabled(context, component, failedToResolveNavigationCase);
        } else {
            renderAsActive(context, navCase, component);
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
        String endElement = Util.componentIsDisabled(component) || context.getAttributes().remove(NO_NAV_CASE) != null ? "span" : "a";
        writer.endElement(endElement);

    }

    // ------------------------------------------------------- Protected Methods

    protected void renderAsDisabled(FacesContext context, UIComponent component, boolean failedToResolveNavigationCase) throws IOException {

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        writer.startElement("span", component);
        writeIdAndNameAttributes(context, writer, component);
        renderLinkCommonAttributes(writer, component);
        renderPassThruAttributes(context, writer, component, ATTRIBUTES, EXCLUDED_ATTRIBUTES);
        writeValue(writer, component);

        // shame that we can't put this in encodeEnd, but then we have to attempt to resolve the navigation case again
        if (failedToResolveNavigationCase) {
            if (!context.isProjectStage(ProjectStage.Production)) {
                writer.write(MessageUtils.getExceptionMessageString(MessageUtils.OUTCOME_TARGET_LINK_NO_MATCH));
            }
        }

    }

    protected void renderAsActive(FacesContext context, NavigationCase navCase, UIComponent component) throws IOException {

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        writer.startElement("a", component);
        writeIdAndNameAttributes(context, writer, component);

        String hrefVal = getEncodedTargetURL(context, component, navCase);
        hrefVal += getFragment(component);
        writer.writeURIAttribute("href", hrefVal, "outcome");

        renderLinkCommonAttributes(writer, component);
        renderPassThruAttributes(context, writer, component, ATTRIBUTES, null);
        writeValue(writer, component);

    }

    protected void writeIdAndNameAttributes(FacesContext context, ResponseWriter writer, UIComponent component) throws IOException {

        String writtenId = writeIdAttributeIfNecessary(context, writer, component);
        if (null != writtenId) {
            writer.writeAttribute("name", writtenId, "name");
        }

    }

    protected void writeValue(ResponseWriter writer, UIComponent component) throws IOException {

        writer.writeText(getLabel(component), component, null);
        writer.flush();

    }

    protected void renderLinkCommonAttributes(ResponseWriter writer, UIComponent component) throws IOException {

        // this is common to both link and button target renderers
        String styleClass = (String) component.getAttributes().get("styleClass");
        if (styleClass != null && styleClass.length() > 0) {
            writer.writeAttribute("class", styleClass, "styleClass");
        }

        // target/onclick should be pass through, but right now, due to command Link,
        // they all share the same base properties file which marks them as non
        // pass-through
        String target = (String) component.getAttributes().get("target");
        if (target != null && target.length() > 0) {
            writer.writeAttribute("target", target, "target");
        }

        String onclick = (String) component.getAttributes().get("onclick");
        if (onclick != null && onclick.length() > 0) {
            writer.writeAttribute("onclick", onclick, "onclick");
        }
    }

}
