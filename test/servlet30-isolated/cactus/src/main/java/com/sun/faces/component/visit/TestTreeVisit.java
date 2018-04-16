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

package com.sun.faces.component.visit;

import com.sun.faces.TestFormVisit;
import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.context.PartialViewContextImpl;

import com.sun.faces.util.Util;
import java.util.HashSet;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlForm;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.event.PhaseId;

import org.apache.cactus.WebRequest;


public class TestTreeVisit extends ServletFacesTestCase {

//
// Protected Constants
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

    public TestTreeVisit() {
        super("TestTreeVisit.java");
    }


    public TestTreeVisit(String name) {
        super(name);
    }

//
// Class methods
//

//
// General Methods
//
    private void buildTree() {
	FacesContext context = getFacesContext();
        UIViewRoot root = Util.getViewHandler(context).createView(context, null);
        root.setId("root");
        context.setViewRoot(root);

        HtmlForm form = new HtmlForm();
        form.setId("form");
        root.getChildren().add(form);

        buildPanel(form, "panel0");
        buildPanel(form, "panel1");

    }

    private void buildPanel(HtmlForm form, String panelId) {
        UIComponent
                panel,
                input0,
                input1,
                button0,
                button1;

        panel = new HtmlPanelGrid();
        panel.setId(panelId);
        form.getChildren().add(panel);

        input0 = new HtmlInputText();
        input0.setId("input0");
        panel.getChildren().add(input0);

        input1 = new HtmlInputText();
        input1.setId("input1");
        panel.getChildren().add(input1);

        button0 = new HtmlCommandButton();
        button0.setId("button0");
        panel.getChildren().add(button0);

        button1 = new HtmlCommandButton();
        button1.setId("button1");
        panel.getChildren().add(button1);

    }

    public void testFullTraversal() throws Exception {
        
        buildTree();
        UIViewRoot root = getFacesContext().getViewRoot();
        final StringBuilder builder = new StringBuilder();

        root.visitTree(VisitContext.createVisitContext(getFacesContext()),
                new VisitCallback() {
                    public VisitResult visit(VisitContext context,
                            UIComponent target) {
                        builder.append(target.getClientId(context.getFacesContext()) + " ");
                        return VisitResult.ACCEPT;
                    }
                });
        System.out.println(builder);
        String result = builder.toString().trim();
        assertEquals(result, "root form form:panel0 form:input0 form:input1 form:button0 form:button1 form:panel1 form:input0 form:input1 form:button0 form:button1");

    }

    public void testSpecificIdTraversal() throws Exception {
        buildTree();
        UIViewRoot root = getFacesContext().getViewRoot();
        final StringBuilder builder = new StringBuilder();

        HashSet ids = new HashSet();
        ids.add("form:panel0");
        root.visitTree(VisitContext.createVisitContext(getFacesContext(),
                ids, null),
                new VisitCallback() {
                    public VisitResult visit(VisitContext context,
                            UIComponent target) {
                        builder.append(target.getClientId(context.getFacesContext()) + " ");
                        return VisitResult.ACCEPT;
                    }
                });
        System.out.println(builder);
        String result = builder.toString().trim();
        assertEquals(result, "form:panel0");

    }
    public void beginPartialTraversal(WebRequest req) {
        req.addParameter(PartialViewContext.PARTIAL_EXECUTE_PARAM_NAME, "form");
    }

    public void testPartialTraversal() throws Exception {
        FacesContext context = getFacesContext();
        UIViewRoot root = Util.getViewHandler(context).createView(context, null);
        root.setId("root");
        context.setViewRoot(root);
        TestFormVisit form  = new TestFormVisit();
        form.setId("form");
        root.getChildren().add(form);

        PartialViewContextImpl pvContext = new PartialViewContextImpl(context);
        pvContext.processPartial(PhaseId.APPLY_REQUEST_VALUES);
        if (context.getAttributes().get("VisitHint.EXECUTE_LIFECYCLE") != null) {
            System.out.println("YESSSSSS");
        } else {
            System.out.println("NOOOOOOOO");
        }
        assertNotNull(context.getAttributes().remove("VisitHint.EXECUTE_LIFECYCLE"));
    }





    // PENDING make sure UIData and UIRepeat are tested.

} // end of class TestTreeVisit
