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

package com.sun.faces.facelets.tag.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sun.faces.facelets.util.DevTools;
import com.sun.faces.facelets.util.FastWriter;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.renderkit.html_basic.ScriptRenderer;

import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Jacob Hookom
 */
public final class UIDebug extends UIComponentBase {

    public final static String COMPONENT_TYPE = "facelets.ui.Debug";
    public final static String COMPONENT_FAMILY = "facelets";
    private static long nextId = System.currentTimeMillis();
    private final static String KEY = "facelets.ui.DebugOutput";
    public final static String DEFAULT_HOTKEY = "D";
    private String hotkey = DEFAULT_HOTKEY;

    public UIDebug() {
        super();
        setTransient(true);
        setRendererType(null);
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    @Override
    public List getChildren() {
        return new ArrayList() {

            private static final long serialVersionUID = 2156130539926052013L;

            @Override
            public boolean add(Object o) {
                throw new IllegalStateException("<ui:debug> does not support children");
            }

            @Override
            public void add(int index, Object o) {
                throw new IllegalStateException("<ui:debug> does not support children");
            }
        };
    }

    @Override
    public void encodeBegin(FacesContext facesContext) throws IOException {

        if (isRendered()) {
            pushComponentToEL(facesContext, this);
            String actionId = facesContext.getApplication().getViewHandler().getActionURL(facesContext, facesContext.getViewRoot().getViewId());

            StringBuilder sb = new StringBuilder(512);
            sb.append("//<![CDATA[\n");
            sb.append("function faceletsDebugWindow(URL) {");
            sb.append("day = new Date();");
            sb.append("id = day.getTime();");
            sb.append(
                    "eval(\"page\" + id + \" = window.open(URL, '\" + id + \"', 'toolbar=0,scrollbars=1,location=0,statusbar=0,menubar=0,resizable=1,width=800,height=600,left = 240,top = 212');\"); };");
            sb.append("(function() { if (typeof faceletsDebug === 'undefined') { var faceletsDebug = false; } if (!faceletsDebug) {");
            sb.append("var faceletsOrigKeyup = document.onkeyup;");
            sb.append("document.onkeyup = function(e) { if (window.event) e = window.event; if (String.fromCharCode(e.keyCode) == '")
              .append(getHotkey())
              .append("' & e.shiftKey & e.ctrlKey) faceletsDebugWindow('");
            sb.append(actionId);
            sb.append(actionId.indexOf('?') == -1 ? '?' : '&');
            sb.append(KEY);
            sb.append('=');
            sb.append(writeDebugOutput(facesContext));
            sb.append("'); faceletsDebug = true; if (faceletsOrigKeyup) faceletsOrigKeyup(e); };\n");
            sb.append("}})();");
            sb.append("//]]>\n");

            ResponseWriter writer = facesContext.getResponseWriter();
            writer.startElement("span", this);
            writer.writeAttribute("id", getClientId(facesContext), "id");
            writer.startElement("script", this);

            if (!RenderKitUtils.isOutputHtml5Doctype(facesContext)) {
                writer.writeAttribute("type", ScriptRenderer.DEFAULT_CONTENT_TYPE, "type");
            }

            writer.writeText(sb.toString(), this, null);
            writer.endElement("script");
            writer.endElement("span");
        }
    }

    private static String writeDebugOutput(FacesContext faces) throws IOException {
        FastWriter fw = new FastWriter();
        DevTools.debugHtml(fw, faces);

        Map session = faces.getExternalContext().getSessionMap();
        Map debugs = (Map) session.get(KEY);
        if (debugs == null) {
            debugs = new LinkedHashMap() {

                private static final long serialVersionUID = 2541609242499547693L;

                @Override
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > 5;
                }
            };
        }
        session.put(KEY, debugs);
        String id = String.valueOf(nextId++);
        debugs.put(id, fw.toString());
        return id;
    }

    private static String fetchDebugOutput(FacesContext faces, String id) {
        Map session = faces.getExternalContext().getSessionMap();
        Map debugs = (Map) session.get(KEY);
        if (debugs != null) {
            return (String) debugs.get(id);
        }
        return null;
    }

    public static boolean debugRequest(FacesContext faces) {
        String id = faces.getExternalContext().getRequestParameterMap().get(KEY);
        if (id != null) {
            Object resp = faces.getExternalContext().getResponse();
            if (!faces.getResponseComplete() && resp instanceof HttpServletResponse) {
                try {
                    HttpServletResponse httpResp = (HttpServletResponse) resp;
                    String page = fetchDebugOutput(faces, id);
                    if (page != null) {
                        httpResp.setContentType("text/html");
                        httpResp.getWriter().write(page);
                    } else {
                        httpResp.setContentType("text/plain");
                        httpResp.getWriter().write("No Debug Output Available");
                    }
                    httpResp.flushBuffer();
                    faces.responseComplete();
                } catch (IOException e) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public String getHotkey() {
        return hotkey;
    }

    public void setHotkey(String hotkey) {
        this.hotkey = hotkey != null ? hotkey.toUpperCase() : "";
    }
}
