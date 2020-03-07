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

// FormRenderer.java

package com.sun.faces.renderkit.html_basic;

import static com.sun.faces.renderkit.RenderKitUtils.getParameterName;

import java.io.IOException;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter;
import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

/** <B>FormRenderer</B> is a class that renders a <code>UIForm<code> as a Form. */

public class FormRenderer extends HtmlBasicRenderer {

    private static final Attribute[] ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.FORMFORM);

    private boolean writeStateAtEnd;

    // ------------------------------------------------------------ Constructors

    public FormRenderer() {
        WebConfiguration webConfig = WebConfiguration.getInstance();
        writeStateAtEnd = webConfig.isOptionEnabled(BooleanWebContextInitParameter.WriteStateAtFormEnd);
    }

    // ---------------------------------------------------------- Public Methods

    @Override
    public void decode(FacesContext context, UIComponent component) {

        rendererParamsNotNull(context, component);

        String clientId = decodeBehaviors(context, component);

        if (clientId == null) {
            clientId = component.getClientId(context);
        }

        // Was our form the one that was submitted? If so, we need to set
        // the indicator accordingly..
        Map<String, String> requestParameterMap = context.getExternalContext().getRequestParameterMap();
        if (requestParameterMap.containsKey(clientId)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "UIForm with client ID {0}, submitted", clientId);
            }
            ((UIForm) component).setSubmitted(true);
        } else {
            ((UIForm) component).setSubmitted(false);
        }

    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;
        String clientId = component.getClientId(context);
        // since method and action are rendered here they are not added
        // to the pass through attributes in Util class.
        writer.write('\n');
        writer.startElement("form", component);
        writer.writeAttribute("id", clientId, "clientId");
        writer.writeAttribute("name", clientId, "name");
        writer.writeAttribute("method", "post", null);
        writer.writeAttribute("action", getActionStr(context), null);
        String styleClass = (String) component.getAttributes().get("styleClass");
        if (styleClass != null) {
            writer.writeAttribute("class", styleClass, "styleClass");
        }
        String acceptcharset = (String) component.getAttributes().get("acceptcharset");
        if (acceptcharset != null) {
            writer.writeAttribute("accept-charset", acceptcharset, "acceptcharset");
        }

        RenderKitUtils.renderPassThruAttributes(context, writer, component, ATTRIBUTES);
        writer.writeText("\n", component, null);

        // this hidden field will be checked in the decode method to
        // determine if this form has been submitted.
        writer.startElement("input", null);
        writer.writeAttribute("type", "hidden", "type");
        writer.writeAttribute("name", clientId, "clientId");
        writer.writeAttribute("value", clientId, "value");
        writer.endElement("input");
        writer.write('\n');

        UIViewRoot viewRoot = context.getViewRoot();

        // Write out special hhidden field for partial submits
        String viewId = viewRoot.getViewId();
        String actionURL = context.getApplication().getViewHandler().getActionURL(context, viewId);
        ExternalContext externalContext = context.getExternalContext();
        String encodedActionURL = externalContext.encodeActionURL(actionURL);
        String encodedPartialActionURL = externalContext.encodePartialActionURL(actionURL);
        if (encodedPartialActionURL != null && !encodedPartialActionURL.equals(encodedActionURL)) {
            writer.startElement("input", null);
            writer.writeAttribute("type", "hidden", "type");
            writer.writeAttribute("name", getParameterName(context, "jakarta.faces.encodedURL"), null);
            writer.writeAttribute("value", encodedPartialActionURL, "value");
            writer.endElement("input");
            writer.write('\n');
        }

        if (!writeStateAtEnd) {
            context.getApplication().getViewHandler().writeState(context);
            writer.write('\n');
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);
        if (!shouldEncode(component)) {
            return;
        }

        // Render the end tag for form
        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        // Render ay resources that have been targeted for this form.

        UIViewRoot viewRoot = context.getViewRoot();
        ListIterator iter = viewRoot.getComponentResources(context, "form").listIterator();
        while (iter.hasNext()) {
            UIComponent resource = (UIComponent) iter.next();
            resource.encodeAll(context);
        }

        // Render the end tag for form
        if (writeStateAtEnd) {
            context.getApplication().getViewHandler().writeState(context);
        }

        writer.writeText("\n", component, null);
        writer.endElement("form");

    }

    // --------------------------------------------------------- Private Methods

    /**
     * @param context FacesContext for the response we are creating
     *
     * @return Return the value to be rendered as the <code>action</code> attribute of the form generated for this
     * component.
     */
    private static String getActionStr(FacesContext context) {

        String viewId = context.getViewRoot().getViewId();
        String actionURL = context.getApplication().getViewHandler().getActionURL(context, viewId);
        return context.getExternalContext().encodeActionURL(actionURL);

    }

} // end of class FormRenderer
