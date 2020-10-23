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

package com.sun.faces.test.servlet30.ajax;

import java.util.HashMap;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;
import javax.faces.context.PartialResponseWriter;
import javax.faces.FacesException;

@ManagedBean
@RequestScoped

public class UpdateBean {

    public String updateBodyTag() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext extContext = ctx.getExternalContext();
        if (ctx.getPartialViewContext().isAjaxRequest()) {
            try {
                extContext.setResponseContentType("text/xml");
                extContext.addResponseHeader("Cache-Control", "no-cache");
                PartialResponseWriter writer =
                    ctx.getPartialViewContext().getPartialResponseWriter();
                writer.startDocument();
                writer.startUpdate("jakarta.faces.ViewBody");
                writer.startElement("body",null);
                writer.writeAttribute("class","foo","class");
                writer.writeAttribute("title","fooTitle","title");
                writer.writeAttribute("lang","fooLang","lang");
                writer.startElement("span", null);
                writer.writeAttribute("id","status","id");
                writer.endElement("span");
                writer.endElement("body");
                writer.endUpdate();
                writer.startEval();
                writer.write("var body = document.getElementsByTagName('body')[0];");
                writer.write("document.getElementById('status').innerHTML='BODY CLASS:'+body.className+' BODY TITLE:'+body.title+' BODY LANG:'+body.lang");
                writer.endEval();
                writer.endDocument();
                writer.flush();
                ctx.responseComplete();
            } catch (Exception e) {
                throw new FacesException(e);
            }
        }
        return null;
    }

    public String updateValueAsAttributeName() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext extContext = ctx.getExternalContext();
        if (ctx.getPartialViewContext().isAjaxRequest()) {
            try {
                Map attrs = new HashMap();
                attrs.put("value", "");
                extContext.setResponseContentType("text/xml");
                extContext.addResponseHeader("Cache-Control", "no-cache");
                PartialResponseWriter writer =
                    ctx.getPartialViewContext().getPartialResponseWriter();
                writer.startDocument();
                writer.updateAttributes("form1:foo", attrs); 
                writer.startEval();
                writer.write("document.getElementById('form1:foo').value = 'bar';");
                writer.endEval();
                writer.endDocument();
                writer.flush();
                ctx.responseComplete();
            } catch (Exception e) {
                throw new FacesException(e);
            }
        }
        return null;
    }

    public String updateDisabledAsAttributeName() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext extContext = ctx.getExternalContext();
        if (ctx.getPartialViewContext().isAjaxRequest()) {
            try {
                Map attrs = new HashMap();
                attrs.put("disabled", "true");
                extContext.setResponseContentType("text/xml");
                extContext.addResponseHeader("Cache-Control", "no-cache");
                PartialResponseWriter writer =
                    ctx.getPartialViewContext().getPartialResponseWriter();
                writer.startDocument();
                writer.updateAttributes("form1:foo", attrs);
                writer.startEval();
                writer.write("document.getElementById('form1:foo').disabled = false;");
                writer.endEval();
                writer.endDocument();
                writer.flush();
                ctx.responseComplete();
            } catch (Exception e) {
                throw new FacesException(e);
            }
        }
        return null;
    }

    public String updateClassAsAttributeName() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext extContext = ctx.getExternalContext();
        if (ctx.getPartialViewContext().isAjaxRequest()) {
            try {
                Map attrs = new HashMap();
                attrs.put("class", "myclass");
                extContext.setResponseContentType("text/xml");
                extContext.addResponseHeader("Cache-Control", "no-cache");
                PartialResponseWriter writer =
                    ctx.getPartialViewContext().getPartialResponseWriter();
                writer.startDocument();
                writer.updateAttributes("form1:foo", attrs);
                writer.startEval();
                writer.write("document.getElementById('form1:foo').value = document.getElementById('form1:foo').className;");
                writer.endEval();
                writer.endDocument();
                writer.flush();
                ctx.responseComplete();
            } catch (Exception e) {
                throw new FacesException(e);
            }
        }
        return null;
    }

    public String updateCheckedAsAttributeName() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext extContext = ctx.getExternalContext();
        if (ctx.getPartialViewContext().isAjaxRequest()) {
            try {
                Map attrs = new HashMap();
                attrs.put("checked", "true");
                extContext.setResponseContentType("text/xml");
                extContext.addResponseHeader("Cache-Control", "no-cache");
                PartialResponseWriter writer =
                    ctx.getPartialViewContext().getPartialResponseWriter();
                writer.startDocument();
                writer.updateAttributes("form1:foo", attrs);
                writer.endDocument();
                writer.flush();
                ctx.responseComplete();
            } catch (Exception e) {
                throw new FacesException(e);
            }
        }
        return null;
    }

    public String updateReadonlyAsAttributeName() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext extContext = ctx.getExternalContext();
        if (ctx.getPartialViewContext().isAjaxRequest()) {
            try {
                Map attrs = new HashMap();
                attrs.put("readonly", "true");
                extContext.setResponseContentType("text/xml");
                extContext.addResponseHeader("Cache-Control", "no-cache");
                PartialResponseWriter writer =
                    ctx.getPartialViewContext().getPartialResponseWriter();
                writer.startDocument();
                writer.updateAttributes("form1:foo", attrs);
                writer.endDocument();
                writer.flush();
                ctx.responseComplete();
            } catch (Exception e) {
                throw new FacesException(e);
            }
        }
        return null;
    }

    public String updateOnAsAttributeName() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext extContext = ctx.getExternalContext();
        if (ctx.getPartialViewContext().isAjaxRequest()) {
            try {
                Map attrs = new HashMap();
                attrs.put("onclick", "document.getElementById('form1:out').innerHTML='ONCLICK CALLED';return;");
                extContext.setResponseContentType("text/xml");
                extContext.addResponseHeader("Cache-Control", "no-cache");
                PartialResponseWriter writer =
                    ctx.getPartialViewContext().getPartialResponseWriter();
                writer.startDocument();
                writer.updateAttributes("form1:button1", attrs);
                writer.endDocument();
                writer.flush();
                ctx.responseComplete();
            } catch (Exception e) {
                throw new FacesException(e);
            }
        }
        return null;
    }
}
