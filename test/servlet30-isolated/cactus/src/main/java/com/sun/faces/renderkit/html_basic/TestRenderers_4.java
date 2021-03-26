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

// TestRenderers_4.java

package com.sun.faces.renderkit.html_basic;

import com.sun.faces.cactus.JspFacesTestCase;
import com.sun.faces.RIConstants;
import com.sun.faces.util.Util;
import org.apache.cactus.WebRequest;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContextFactory;

import java.io.IOException;
import java.util.Locale;

/**
 * Test encode and decode methods in Renderer classes.
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 */

public class TestRenderers_4 extends JspFacesTestCase {

    //
    // Protected Constants
    //

    public boolean sendWriterToFile() {
        return true;
    }


    public String getExpectedOutputFilename() {
        return "CorrectRenderersResponse_4";
    }

    //
    // Class Variables
    //

    //
    // Instance Variables
    //
    private FacesContextFactory facesContextFactory = null;

    // Attribute Instance Variables
    // Relationship Instance Variables
    //
    // Constructors and Initializers    
    //

    public TestRenderers_4() {
        super("TestRenderers_4");
    }


    public TestRenderers_4(String name) {
        super(name);
    }

    //
    // Class methods
    //

    //
    // Methods from TestCase
    //
    public void setUp() {
        super.setUp();
        UIViewRoot page = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        page.setViewId("viewId");
        page.setLocale(Locale.US);
        getFacesContext().setViewRoot(page);
        Object view = 
	    Util.getStateManager(getFacesContext()).saveSerializedView(getFacesContext());
	getFacesContext().getExternalContext().getRequestMap().put(RIConstants.SAVED_STATE, view);
        assertTrue(null != getFacesContext().getResponseWriter());
    }


    public void beginRenderers(WebRequest theRequest) {

    }


    public void testRenderers() {

        try {
            // create a dummy root for the tree.
            UIViewRoot root = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
            root.setLocale(Locale.US);
            root.setId("root");

            testGridRenderer(root);

            root = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
            root.setId("root");
            root.setLocale(Locale.US);
            testGridRendererWithNonRenderedChildren(root);

            getFacesContext().getResponseWriter().close();
            assertTrue(verifyExpectedOutput());
        } catch (Throwable t) {
            t.printStackTrace();
            assertTrue(false);
            return;
        }
    }


    public void testGridRenderer(UIComponent root)
        throws IOException {
        System.out.println("Testing GridRenderer");
        GridRenderer gridRenderer = null;
        UIPanel
            panel = null,
            headerGroup = null,
            footerGroup = null;
        UIOutput
            header1 = null,
            header2 = null,
            footer1 = null,
            footer2 = null,
            body1 = null,
            body2 = null;

        panel = new UIPanel();
        root.getChildren().add(panel);

        headerGroup = new UIPanel();
        headerGroup.setId("header");
        headerGroup.setRendererType("jakarta.faces.Group");
        header1 = new UIOutput();
        header1.setValue("header1 ");
        headerGroup.getChildren().add(header1);
        header2 = new UIOutput();
        header2.setValue("header2 ");
        headerGroup.getChildren().add(header2);
        panel.getFacets().put("header", headerGroup);

        footerGroup = new UIPanel();
        footerGroup.setId("footer");
        footerGroup.setRendererType("jakarta.faces.Group");
        footer1 = new UIOutput();
        footer1.setValue("footer1 ");
        footerGroup.getChildren().add(footer1);
        footer2 = new UIOutput();
        footer2.setValue("footer2 ");
        footerGroup.getChildren().add(footer2);
        panel.getFacets().put("footer", footerGroup);

        body1 = new UIOutput();
        body1.setValue("body1");
        panel.getChildren().add(body1);

        body2 = new UIOutput();
        body2.setValue("body2");
        panel.getChildren().add(body2);

        gridRenderer = new GridRenderer();

        System.out.println("    Testing encodeBegin method... ");
        gridRenderer.encodeBegin(getFacesContext(), panel);
        gridRenderer.encodeChildren(getFacesContext(), panel);
        gridRenderer.encodeEnd(getFacesContext(), panel);

    }


    public void testGridRendererWithNonRenderedChildren(UIComponent root)
        throws IOException {
        System.out.println("Testing GridRenderer");
        GridRenderer gridRenderer = null;
        UIPanel
            panel = null,
            headerGroup = null,
            footerGroup = null;
        UIOutput
            header1 = null,
            header2 = null,
            footer1 = null,
            footer2 = null,
            body1 = null,
            body2 = null;

        panel = new UIPanel();
        root.getChildren().add(panel);

        // the header should not be rendered
        headerGroup = new UIPanel();
        headerGroup.setRendered(false);
        headerGroup.setId("header");
        headerGroup.setRendererType("jakarta.faces.Group");
        header1 = new UIOutput();
        header1.setValue("header1 ");
        headerGroup.getChildren().add(header1);
        header2 = new UIOutput();
        header2.setValue("header2 ");
        headerGroup.getChildren().add(header2);
        panel.getFacets().put("header", headerGroup);

        footerGroup = new UIPanel();
        footerGroup.setId("footer");
        footerGroup.setRendererType("jakarta.faces.Group");
        footer1 = new UIOutput();
        footer1.setValue("footer1 ");
        footerGroup.getChildren().add(footer1);
        footer2 = new UIOutput();
        footer2.setValue("footer2 ");
        footerGroup.getChildren().add(footer2);
        panel.getFacets().put("footer", footerGroup);

        // this child should not be rendered
        body1 = new UIOutput();
        body1.setRendered(false);
        body1.setValue("body1");
        panel.getChildren().add(body1);

        body2 = new UIOutput();
        body2.setValue("body2");
        panel.getChildren().add(body2);

        gridRenderer = new GridRenderer();

        System.out.println("    Testing encodeBegin method... ");
        gridRenderer.encodeBegin(getFacesContext(), panel);
        gridRenderer.encodeChildren(getFacesContext(), panel);
        gridRenderer.encodeEnd(getFacesContext(), panel);

    }


} // end of class TestRenderers_4
