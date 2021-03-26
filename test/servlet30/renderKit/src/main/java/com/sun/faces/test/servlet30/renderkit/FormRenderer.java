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

// FormRenderer.java

package com.sun.faces.test.servlet30.renderkit;

import com.sun.faces.RIConstants;
import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.MessageUtils;


import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <B>FormRenderer</B> is a class that renders a <code>UIForm<code> as a Form.
 */

public class FormRenderer extends Renderer {

    private static final Attribute[] ATTRIBUTES =
          AttributeManager.getAttributes(AttributeManager.Key.FORMFORM);

    public static final String SCRIPT_ELEMENT = "script";
    public static final String SCRIPT_TYPE = "type";
    public static final String CLEAR_HIDDEN_FIELD_FN_NAME = 
         "clearFormHiddenParams";
    public static final String FORM_CLIENT_ID_ATTR = 
         "com.sun.faces.FORM_CLIENT_ID_ATTR";

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

    public FormRenderer() {
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

    public void decode(FacesContext context, UIComponent component) {
        // Was our form the one that was submitted?  If so, we need to set
        // the indicator accordingly..
        //
        String clientId = component.getClientId(context);
        Map requestParameterMap = context.getExternalContext()
            .getRequestParameterMap();
        if (requestParameterMap.containsKey(clientId)) {
            ((UIForm) component).setSubmitted(true);
        } else {
            ((UIForm) component).setSubmitted(false);
        }
    }


    public void encodeBegin(FacesContext context, UIComponent component)
        throws IOException {
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
        ResponseWriter writer = context.getResponseWriter();
        assert (writer != null);
        // since method and action are rendered here they are not added
        // to the pass through attributes in Util class.
        writer.startElement("form", component);
        writer.writeAttribute("id", component.getClientId(context), "clientId");
        writer.writeAttribute("method", "post", null);
        writer.writeAttribute("action", getActionStr(context), null);
        if (null != (styleClass = (String)
            component.getAttributes().get("styleClass"))) {
            writer.writeAttribute("class", styleClass, "styleClass");
        }
        String acceptcharset = null;
        if (null != (acceptcharset = (String)
            component.getAttributes().get("acceptcharset"))) {
            writer.writeAttribute("accept-charset", acceptcharset, 
                    "acceptcharset");
        }
        
        RenderKitUtils.renderPassThruAttributes(context,
              writer,
                                                component,
                                                ATTRIBUTES);       
        writer.writeText("\n", null);
        
        // store the clientId of the form in request scope. This will be used
        // by the commandLinkRenderer and ButtonRenderer to arrive the name of
        // the javascript function to invoke from the onclick event handler.
        // PENDING (visvan) we need to fix this dependency between the renderers.
        // This solution is only temporary.
        Map requestMap =context.getExternalContext().getRequestMap();
        requestMap.put(FORM_CLIENT_ID_ATTR, component.getClientId(context));
    }


    /**
     * <p>Return the value to be rendered as the <code>action</code> attribute
     * of the form generated for this component.</p>
     *
     * @param context FacesContext for the response we are creating
     */
    private String getActionStr(FacesContext context) {
        String viewId = context.getViewRoot().getViewId();
        String actionURL =
            context.getApplication().getViewHandler().
            getActionURL(context, viewId);
        return (context.getExternalContext().encodeActionURL(actionURL));
    }


    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {
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

        context.getApplication().getViewHandler().writeState(context);

        // Render the end tag for form
        ResponseWriter writer = context.getResponseWriter();
        assert (writer != null);

        // this hidden field will be checked in the decode method to determine if
        // this form has been submitted.
        //
        writer.startElement("input", component);
        writer.writeAttribute("type", "hidden", "type");
        writer.writeAttribute("name", component.getClientId(context),
                              "clientId");
        writer.writeAttribute("value", component.getClientId(context), "value");
        writer.endElement("input");

        renderNeededHiddenFields(context, component);
        writer.endElement("form");
        
        Map requestMap = context.getExternalContext().getRequestMap();
        String formClientId = (String)requestMap.put(FORM_CLIENT_ID_ATTR, null);
    }


    /**
     * <p>Render any need hidden fields.</p>
     */
    private static void renderNeededHiddenFields(FacesContext context,
                                                 UIComponent component)
        throws IOException {

        ResponseWriter writer = context.getResponseWriter();
        Map map = getHiddenFieldMap(context, false);
        if (map != null) {
            Iterator entries = map.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                if (Boolean.TRUE.equals(entry.getValue())) {
                    writer.startElement("input", component);
                    writer.writeAttribute("type", "hidden", null);
                    writer.writeAttribute("name", entry.getKey(), null);
                    writer.endElement("input");
                }
            }
                
            // Clear the hidden field map
            Map requestMap = context.getExternalContext().getRequestMap();
            requestMap.put(HIDDEN_FIELD_KEY, null);
        }
        String formTarget = (String) component.getAttributes().get("target");
        renderClearHiddenParamsJavaScript(context,
                                          writer,
                                          map,
                                          formTarget,
                                          component.getClientId(context));
    }


    /**
     * <p>Remember that we will need a new hidden field.</p>
     */
    public static void addNeededHiddenField(FacesContext context,
                                            String clientId) {
        Map map = getHiddenFieldMap(context, true);
        if (!map.containsKey(clientId)) {
            map.put(clientId, Boolean.TRUE);
        }
    }


    /**
     * <p>Note that a hidden field has already been rendered.</p>
     */
    public static void addRenderedHiddenField(FacesContext context,
                                              String clientId) {
        Map map = getHiddenFieldMap(context, true);
        map.put(clientId, Boolean.FALSE);
    }


    private static Map getHiddenFieldMap(FacesContext context,
                                         boolean createIfNew) {
        Map requestMap = context.getExternalContext().getRequestMap();
        Map map = (Map) requestMap.get(HIDDEN_FIELD_KEY);
        if (map == null) {
            if (createIfNew) {
                map = new HashMap();
                requestMap.put(HIDDEN_FIELD_KEY, map);
            }
        }

        return map;
    }

    /**
     * Generates a JavaScript function to clear all the hidden fields
     * associated with a form and reset the target attribute if necessary.
     */
    private static void renderClearHiddenParamsJavaScript(FacesContext ctx,
                                                          ResponseWriter writer,
                                                          Map formParams,
                                                          String formTarget,
                                                          String formName)
    throws IOException {
            
         // clear all the hidden field parameters in the form represented by
         // formName.
         writer.write("\n");
         writer.startElement(SCRIPT_ELEMENT, null);
         writer.writeAttribute(SCRIPT_TYPE, "text/javascript", null);
         writer.write("\n<!--");
         writer.write("\nfunction ");
         String functionName = (CLEAR_HIDDEN_FIELD_FN_NAME + "_" + formName.replace(UINamingContainer.getSeparatorChar(ctx), '_'));
         writer.write(functionName);
         writer.write("(curFormName) {");
         writer.write("\n  var curForm = document.forms[curFormName];"); 
         if (formParams != null) {
            Iterator entries = formParams.entrySet().iterator();
            // clear only the hidden fields rendered by the form.
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                if (Boolean.TRUE.equals(entry.getValue())) {
                    writer.write("\n curForm.elements['"); 
                    writer.write((String) entry.getKey());
                    writer.write("'].value = null;");
                }
            }
         }
         // clear form target attribute if its present
         if (formTarget != null && formTarget.length() > 0) {
             writer.write("\n  curForm.target=");
             writer.write("'");
             writer.write(formTarget);
             writer.write("';");
         }
         writer.write("\n}");
         writer.write("\n//-->\n");
         writer.endElement(SCRIPT_ELEMENT);
         writer.write("\n");
         
     }

    private static final String HIDDEN_FIELD_KEY =
        RIConstants.FACES_PREFIX + "FormHiddenFieldMap";

} // end of class FormRenderer
