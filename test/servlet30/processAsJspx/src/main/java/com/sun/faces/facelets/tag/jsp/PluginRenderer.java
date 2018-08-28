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

package com.sun.faces.facelets.tag.jsp;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

@FacesRenderer(componentFamily = "javax.faces.Output", rendererType = "jsp.Plugin")
public class PluginRenderer extends Renderer {

    final String[] passthruAttrs = { "name", "width", "height", "hspace", "vspace", "align" };
    final String[] pluginSkipAttrs = { "code", "codebase", "com.sun.faces.facelets.MARK_ID", "com.sun.faces.facelets.APPLIED", "jreversion",
            "type" };

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        Map<String, Object> attrs = component.getAttributes();
        ResponseWriter out = context.getResponseWriter();
        out.startElement("OBJECT", component);
        // write out the classid
        out.writeAttribute("classid", "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93", "classid");
        // write out the passthru attrs
        for (String attrName : passthruAttrs) {
            if (attrs.containsKey(attrName)) {
                out.writeAttribute(attrName, attrs.get(attrName), attrName);
            }
        }
        // write out the codebase
        out.writeAttribute("codebase", "http://java.sun.com/products/plugin/1.2.2/jinstall-1_2_2-win.cab#Version=1,2,2,0", "codebase");
        // write out <PARAM> elements for the attrs that need to be prefixed by
        // the string "java_"
        writeRequiredParamFromAttrs(context, component, attrs, out, "code", "java_");
        writeRequiredParamFromAttrs(context, component, attrs, out, "codebase", "java_");
        // write out the type attribute
        String jreversion = attrs.containsKey("jreversion") ? attrs.get("jreversion").toString() : "1.2";
        jreversion = "application/x-java-applet;version=" + jreversion;
        writeParam(context, component, out, "type", jreversion);
        // write out the remaining attributes to the plugin element as params
        Set<String> attrKeys = attrs.keySet();
        for (String cur : attrKeys) {
            // skip if it's a passthru attr
            boolean skip = false;
            for (int i = 0; i < passthruAttrs.length; i++) {
                if (passthruAttrs[i].equals(cur)) {
                    skip = true;
                    break;
                }
            }
            for (int i = 0; i < pluginSkipAttrs.length; i++) {
                if (pluginSkipAttrs[i].equals(cur)) {
                    skip = true;
                    break;
                }
            }
            if (skip) {
                continue;
            }
            writeParamFromAttrs(context, component, attrs, out, cur, "");
        }
        // write out the nested <jsp:params>
        Map<String, ValueExpression> params = ParamHandler.getParams(context, component);
        Set<String> paramKeys = params.keySet();
        ELContext elc = context.getELContext();
        for (String cur : paramKeys) {
            writeParam(context, component, out, cur, params.get(cur).getValue(elc).toString());
        }

        // now do the whole thing again, just a little bit differently,
        // and in a <COMMENT> element.
        out.startElement("COMMENT", component);
        out.startElement("EMBED", component);
        out.writeAttribute("type", jreversion, "type");
        // write out the passthru attrs
        for (String attrName : passthruAttrs) {
            if (attrs.containsKey(attrName)) {
                out.writeAttribute(attrName, attrs.get(attrName), attrName);
            }
        }
        out.writeAttribute("pluginspage", "http://java.sun.com/products/plugin/", "pluginspage");
        out.writeAttribute("java_code", attrs.get("code"), "java_code");
        out.writeAttribute("java_codebase", attrs.get("codebase"), "java_codebase");
        // write out the remaining attributes to the plugin element as attributes on emebed
        for (String cur : attrKeys) {
            // skip if it's a passthru attr
            boolean skip = false;
            for (int i = 0; i < passthruAttrs.length; i++) {
                if (passthruAttrs[i].equals(cur)) {
                    skip = true;
                    break;
                }
            }
            for (int i = 0; i < pluginSkipAttrs.length; i++) {
                if (pluginSkipAttrs[i].equals(cur)) {
                    skip = true;
                    break;
                }
            }
            if (skip) {
                continue;
            }
            out.writeAttribute(cur, attrs.get(cur), cur);
        }
        // write out the nested params as attributes this time
        for (String cur : paramKeys) {
            out.writeAttribute(cur, params.get(cur).getValue(elc).toString(), cur);
        }

        out.endElement("EMBED");
        out.startElement("NOEMBED", component);
        if (component.getChildCount() > 0) {
            Iterator<UIComponent> kids = component.getChildren().iterator();
            while (kids.hasNext()) {
                UIComponent kid = kids.next();
                kid.encodeAll(context);
            }
        }
        out.endElement("NOEMBED");
        out.endElement("COMMENT");
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter out = context.getResponseWriter();
        out.endElement("OBJECT");

    }

    private void writeRequiredParamFromAttrs(FacesContext context, UIComponent component, Map<String, Object> attrs, ResponseWriter out,
            String attrName, String prefix) throws IOException {
        if (!attrs.containsKey(attrName)) {
            throw new IOException("plugin must have a " + attrName + " attribute");
        }
        this.writeParamFromAttrs(context, component, attrs, out, attrName, prefix);
    }

    private void writeParamFromAttrs(FacesContext context, UIComponent component, Map<String, Object> attrs, ResponseWriter out,
            String attrName, String prefix) throws IOException {
        out.startElement("PARAM", component);
        out.writeAttribute("name", prefix + attrName, "name");
        out.writeAttribute("value", attrs.get(attrName).toString(), "value");
        out.endElement("PARAM");
    }

    private void writeParam(FacesContext context, UIComponent component, ResponseWriter out, String attrName, String attrValue)
            throws IOException {
        out.startElement("PARAM", component);
        out.writeAttribute("name", attrName, "name");
        out.writeAttribute("value", attrValue, "value");
        out.endElement("PARAM");
    }

}
