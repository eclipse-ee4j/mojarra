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

package com.sun.faces.component;

import java.io.IOException;
import java.util.Map;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

@FacesComponent(value="dynamicAdd")
public class DynamicAdd extends UINamingContainer implements SystemEventListener {
  
  private boolean facetRequired = true;

    public boolean isFacetRequired() {
        return facetRequired;
    }

    public void setFacetRequired(boolean facetRequired) {
        this.facetRequired = facetRequired;
    }

    public DynamicAdd() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        Map<String, Object> viewMap = ctx.getViewRoot().getViewMap();
        // increment the counter
        viewMap.put("dynamicAdd", null == viewMap.get("dynamicAdd") ?
            (Integer) 1 : ((Integer)viewMap.get("dynamicAdd")) + 1);
        this.setId("dynamic" + viewMap.get("dynamicAdd").toString());

        ctx.getViewRoot().subscribeToViewEvent(PreRenderViewEvent.class, (SystemEventListener) this);
    }

    public void processEvent(SystemEvent se) throws AbortProcessingException {
        FacesContext ctx = FacesContext.getCurrentInstance();
        UIComponent source = (UIComponent) se.getSource();
        String id = source.getClientId(ctx);
        if (source.equals(ctx.getViewRoot())) {
            Map<String, Object> viewMap = ctx.getViewRoot().getViewMap();
            Integer numAddedSoFar = (Integer) viewMap.get("dynamicAdd");
            if (numAddedSoFar < 5) {
                DynamicAdd dynamic = (DynamicAdd) ctx.getApplication().createComponent("dynamicAdd");
                dynamic.setFacetRequired(this.isFacetRequired());
                this.getChildren().add(dynamic);
            }
        }
    }

    public boolean isListenerForSource(Object o) {
        return o instanceof UIViewRoot;
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
      // conditionally create dynamic component facets
      if (facetRequired && null == getFacet("dynamicAddFacet")) {
        getFacets().put("dynamicAddFacet", new HtmlPanelGroup());
      }
        Map<Object, Object> contextMap = context.getAttributes();
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("ul", this);
        writer.startElement("p", this);
        writer.write("Dynamic Component " + this.getId());
        
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        Map<Object, Object> contextMap = context.getAttributes();
        ResponseWriter writer = context.getResponseWriter();
        writer.endElement("p");
        writer.endElement("ul");

    }

    @Override
    public void processDecodes(FacesContext context) {
      // conditionally recreate the dynamic component facet before process decode
      if (facetRequired && null == getFacet("dynamicAddFacet")) {
        getFacets().put("dynamicAddFacet", new HtmlPanelGroup());
      }
      
      // TODO Auto-generated method stub
      super.processDecodes(context);
    }



}
