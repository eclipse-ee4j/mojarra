<%--

    Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Public License v. 2.0, which is available at
    http://www.eclipse.org/legal/epl-2.0.

    This Source Code may also be made available under the following Secondary
    Licenses when the conditions for such availability set forth in the
    Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
    version 2 with the GNU Classpath Exception, which is available at
    https://www.gnu.org/software/classpath/license.html.

    SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

--%>

<%@ page contentType="text/html"
%><%@ page import="java.util.Iterator"
%><%@ page import="java.util.Map"
%><%@ page import="jakarta.faces.FactoryFinder"
%><%@ page import="jakarta.faces.context.FacesContext"
%><%@ page import="jakarta.faces.render.RenderKit"
%><%@ page import="jakarta.faces.render.RenderKitFactory"
%><%@ page import="jakarta.faces.render.Renderer"
%><%

// This test goes through the config system to test the loading of 
// the default renderkit information as well as a custom renderkit
// consisting of one renderer.

    // Initialize list of Renderer types
    //
    String families[] = {
      "jakarta.faces.Command",
      "jakarta.faces.Command",
      "jakarta.faces.Data",
      "jakarta.faces.Form",
      "jakarta.faces.Graphic",
      "jakarta.faces.Input",
      "jakarta.faces.Input",
      "jakarta.faces.Input",
      "jakarta.faces.Input",
      "jakarta.faces.Message",
      "jakarta.faces.Messages",
      "jakarta.faces.Output",
      "jakarta.faces.Output",
      "jakarta.faces.Output",
      "jakarta.faces.Output",
      "jakarta.faces.Panel",
      "jakarta.faces.Panel",
      "jakarta.faces.SelectBoolean",
      "jakarta.faces.SelectMany",
      "jakarta.faces.SelectMany",
      "jakarta.faces.SelectMany",
      "jakarta.faces.SelectOne",
      "jakarta.faces.SelectOne",
      "jakarta.faces.SelectOne"
    };

    String defaultList[] = {
      "jakarta.faces.Button",
      "jakarta.faces.Link",
      "jakarta.faces.Table",
      "jakarta.faces.Form",
      "jakarta.faces.Image",
      "jakarta.faces.Hidden",
      "jakarta.faces.Secret",
      "jakarta.faces.Text",
      "jakarta.faces.Textarea",
      "jakarta.faces.Message",
      "jakarta.faces.Messages",
      "jakarta.faces.Format",
      "jakarta.faces.Label",
      "jakarta.faces.Link",
      "jakarta.faces.Text",
      "jakarta.faces.Grid",
      "jakarta.faces.Group",
      "jakarta.faces.Checkbox",
      "jakarta.faces.Checkbox",
      "jakarta.faces.Listbox",
      "jakarta.faces.Menu",
      "jakarta.faces.Listbox",
      "jakarta.faces.Menu",
      "jakarta.faces.Radio"
      };

    String customFamilies[] = {"SysTest"};
    String customList[] = {"Text"};

    // Acquire RenderKits and check RenderKitId(s)
    //
    RenderKitFactory renderKitFactory = (RenderKitFactory)
        FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
    Iterator renderKitIds = renderKitFactory.getRenderKitIds();

    boolean foundDefault = false; 
    boolean foundCustom= false; 
    while (renderKitIds.hasNext()) {
        String renderKitId = (String)renderKitIds.next();
        if (renderKitId.equals(RenderKitFactory.HTML_BASIC_RENDER_KIT)) {
            foundDefault = true;
        } else if (renderKitId.equals("CUSTOM")) {
            foundCustom = true;
        }
    }
    if (!foundDefault || !foundCustom) {
        out.println("/renderkit01.jsp FAILED - all renderkit ids not found");
	return;
    }

    // Check Renderers For Each RenderKit
    //
    while (renderKitIds.hasNext()) {
        String renderKitId = (String)renderKitIds.next();
	RenderKit rKit = renderKitFactory.getRenderKit(null, renderKitId);
	if (rKit == null) {
	    out.println("/renderkit01.jsp FAILED - renderkit not found");
	    return;
	}
	Renderer renderer = null;
	if (renderKitId.equals(RenderKitFactory.HTML_BASIC_RENDER_KIT)) {
	    for (int i=0; i<defaultList.length; i++) {
	        try {
	            renderer = rKit.getRenderer(families[i], defaultList[i]);
	        } catch (IllegalArgumentException ia) {
	            out.println("/renderkit01.jsp FAILED - renderer not found for type:"+
		        defaultList[i]+" in renderkit 'DEFAULT'");
		    return;
	        }
	    }
	} else if (renderKitId.equals("CUSTOM")) {
	    for (int i=0; i<customList.length; i++) {
	        try {
	            renderer = rKit.getRenderer(customFamilies[i],
                                                customList[i]);
	        } catch (IllegalArgumentException ia) {
	            out.println("/renderkit01.jsp FAILED - renderer not found for type:"+
		        customList[i]+" in renderkit 'CUSTOM'");
		    return;
	        }
	    }
	}
    }

    // All tests passed
    //
    out.println("/renderkit01.jsp PASSED");
%>
