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

package com.sun.faces.systest.model.ajax;

import javax.faces.bean.RequestScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialResponseWriter;
import javax.faces.context.ExternalContext;
import javax.faces.FacesException;

@ManagedBean
@RequestScoped
public class InsertDeleteBean {

    public String insertBefore() {

        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext extContext = ctx.getExternalContext();
        if (ctx.getPartialViewContext().isAjaxRequest()) {
            try {
                extContext.setResponseContentType("text/xml");
                extContext.addResponseHeader("Cache-Control", "no-cache");
                PartialResponseWriter writer =
                      ctx.getPartialViewContext().getPartialResponseWriter();
                writer.startDocument();
                
                writer.startInsertBefore("hr");
                writer.writeAttribute("id", "h2before", "id");
                writer.startElement("h2", null);
                writer.writeText("BEFORE", null, null);
                writer.endElement("h2");
                writer.endInsert();
                
                writer.startInsertBefore("tablecenter");
                writer.writeAttribute("id", "trbefore", "id");
                writer.startElement("tr", null);
                writer.writeAttribute("id", "trbefore", "id");
                writer.startElement("td", null);
                writer.writeText("BEFORE", null, null);
                writer.endElement("td");
                writer.endElement("tr");
                writer.endInsert();
                
                writer.endDocument();
                writer.flush();
                ctx.responseComplete();
            } catch (Exception e) {
                throw new FacesException(e);
            }
        }
        return null;

    }

    public String insertAfter() {

        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext extContext = ctx.getExternalContext();
        if (ctx.getPartialViewContext().isAjaxRequest()) {
            try {
                PartialResponseWriter writer =
                      ctx.getPartialViewContext().getPartialResponseWriter();
                extContext.setResponseContentType("text/xml");
                extContext.addResponseHeader("Cache-Control", "no-cache");
                writer.startDocument();

                writer.startInsertAfter("hr");
                writer.startElement("h2", null);
                writer.writeAttribute("id", "h2after", "id");
                writer.writeText("AFTER", null, null);
                writer.endElement("h2");
                writer.endInsert();

                writer.startInsertAfter("tablecenter");
                writer.writeAttribute("id", "trafter", "id");
                writer.startElement("tr", null);
                writer.writeAttribute("id", "trafter", "id");
                writer.startElement("td", null);
                writer.writeText("AFTER", null, null);
                writer.endElement("td");
                writer.endElement("tr");
                writer.endInsert();

                writer.endDocument();
                writer.flush();
                ctx.responseComplete();
            } catch (Exception e) {
                throw new FacesException(e);
            }
        }
        return null;

    }


    public String removeBefore() {

        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext extContext = ctx.getExternalContext();
        if (ctx.getPartialViewContext().isAjaxRequest()) {
            try {
                PartialResponseWriter writer =
                      ctx.getPartialViewContext().getPartialResponseWriter();
                extContext.setResponseContentType("text/xml");
                extContext.addResponseHeader("Cache-Control", "no-cache");
                writer.startDocument();
                writer.delete("h2before");
                writer.delete("trbefore");
                writer.endDocument();
                writer.flush();
                ctx.responseComplete();
            } catch (Exception e) {
                throw new FacesException(e);
            }
        }
        return null;

    }

    public String removeAfter() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext extContext = ctx.getExternalContext();
        if (ctx.getPartialViewContext().isAjaxRequest()) {
            try {
                PartialResponseWriter writer =
                      ctx.getPartialViewContext().getPartialResponseWriter();
                extContext.setResponseContentType("text/xml");
                extContext.addResponseHeader("Cache-Control", "no-cache");
                writer.startDocument();
                writer.delete("h2after");
                writer.delete("trafter");
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
