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

package com.sun.faces.application;

import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.faces.component.UIComponent;
import javax.faces.component.UIGraphic;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.application.StateManager;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;

import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.cactus.TestingUtil;
import com.sun.faces.renderkit.ServerSideStateHelper;
import com.sun.faces.renderkit.RenderKitImpl;
import com.sun.faces.renderkit.html_basic.HtmlResponseWriter;
import com.sun.faces.util.Util;
import com.sun.faces.config.WebConfiguration;
import static com.sun.faces.config.WebConfiguration.WebContextInitParameter.StateSavingMethod;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.AutoCompleteOffOnViewState;
import com.sun.faces.context.FacesContextImpl;
import org.apache.cactus.WebRequest;



/**
 * This class tests the <code>StateManagerImpl</code> class
 * functionality.
 */
public class TestStateManagerImpl extends ServletFacesTestCase {


    //
    // Constructors/Initializers
    //
    public TestStateManagerImpl() {
        super("TestStateManagerImpl");
    }


    public TestStateManagerImpl(String name) {
        super(name);
    }
    
    // ------------------------------------------------------------ Test Methods


    
    // Verify saveSerializedView() throws IllegalStateException
    // if duplicate component id's are detected on non-transient 
    // components.
    public void testDuplicateIdDetection() throws Exception {

        FacesContext context = getFacesContext();

        // construct a view
        UIViewRoot root = Util.getViewHandler(getFacesContext()).createView(context, null);
        root.setViewId("/test");
        root.setId("root");
        root.setLocale(Locale.US);

        UIComponent comp1 = new UIInput();
        comp1.setId("comp1");

        UIComponent comp2 = new UIOutput();
        comp2.setId("comp2");

        UIComponent comp3 = new UIGraphic();
        comp3.setId("comp3");

        UIComponent facet1 = new UIOutput();
        facet1.setId("comp4");

        UIComponent facet2 = new UIOutput();
        facet2.setId("comp2");

        comp2.getFacets().put("facet1", facet1);
        comp2.getFacets().put("facet2", facet2);

        root.getChildren().add(comp1);
        root.getChildren().add(comp2);
        root.getChildren().add(comp3);

        context.setViewRoot(root);

        StateManagerImpl stateManager = (StateManagerImpl) context.getApplication()
            .getStateManager();

        boolean exceptionThrown = false;
        try {
            stateManager.saveView(context);
        } catch (IllegalStateException ise) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        
        // multiple componentns with a null ID should not
        // trigger an exception
        // construct a view
        root = Util.getViewHandler(getFacesContext()).createView(context, null); 
        root.setViewId("/test");
        root.setId("root");
        root.setLocale(Locale.US);

        comp1 = new UIInput();
        comp1.setId("comp1");

        comp2 = new UIOutput();
        comp2.setId(null);

        comp3 = new UIGraphic();
        comp3.setId(null);

        facet1 = new UIOutput();
        facet1.setId("comp4");

        facet2 = new UIOutput();
        facet2.setId("comp2");

        comp2.getFacets().put("facet1", facet1);
        comp2.getFacets().put("facet2", facet2);

        root.getChildren().add(comp1);
        root.getChildren().add(comp2);
        root.getChildren().add(comp3);

        context.setViewRoot(root);

        exceptionThrown = false;
        try {
            stateManager.saveView(context);
        } catch (IllegalStateException ise) {
            exceptionThrown = true;
        }
        assertTrue(!exceptionThrown);
        
        // transient components with duplicate ids should 
        // trigger an error condition
        // construct a view
        root = Util.getViewHandler(getFacesContext()).createView(context, null); 
        root.setViewId("/test");
        root.setId("root");
        root.setLocale(Locale.US);

        comp1 = new UIInput();
        comp1.setId("comp1");
        comp1.setTransient(true);

        comp2 = new UIOutput();
        comp2.setId("comp1");
        comp2.setTransient(true);

        comp3 = new UIGraphic();
        comp3.setId("comp3");

        facet1 = new UIOutput();
        facet1.setId("comp4");

        facet2 = new UIOutput();
        facet2.setId("comp2");

        comp2.getFacets().put("facet1", facet1);
        comp2.getFacets().put("facet2", facet2);

        root.getChildren().add(comp1);
        root.getChildren().add(comp2);
        root.getChildren().add(comp3);

        context.setViewRoot(root);

        exceptionThrown = false;
        try {
            stateManager.saveView(context);
        } catch (IllegalStateException ise) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }



    
    public void beginMultiWindowSaveServer(WebRequest theRequest) {
        theRequest.addParameter("javax.faces.ViewState", "j_id1:j_id2");
    }

    public void testMultiWindowSaveServer() throws Exception {
        StateManagerImpl wrapper =
            new StateManagerImpl() {
                public boolean isSavingStateInClient(FacesContext context) {
                    return false;
                }
            };

        FacesContext ctx = getFacesContext();
        ctx.getApplication().setStateManager(wrapper);
        
        // construct a view
        initView(ctx);

        UIViewRoot root = ctx.getViewRoot();
        root.getAttributes().put("checkThisValue", "checkThisValue");
        getFacesContext().setResponseWriter(new HtmlResponseWriter(new StringWriter(), "text/html", "UTF-8"));
        Object viewState = wrapper.saveView(getFacesContext());
        wrapper.writeState(getFacesContext(), viewState);
        
        // See that the Logical View and Actual View maps are correctly created
        Map sessionMap = ctx.getExternalContext().getSessionMap();
        assertTrue(sessionMap.containsKey(ServerSideStateHelper.LOGICAL_VIEW_MAP));
        assertTrue(((Map)sessionMap.get(ServerSideStateHelper.LOGICAL_VIEW_MAP)).containsKey("j_id1"));

        UIViewRoot newRoot = wrapper.restoreView(ctx, "test", "HTML_BASIC");
        assertNotNull(newRoot);
        assertEquals(root.getAttributes().get("checkThisValue"),
                     newRoot.getAttributes().get("checkThisValue"));
        
    }


    public void testGetViewStateServer() {

        // this exercise ResponseStateManager.getViewState() as well
        FacesContext ctx = getFacesContext();

        initView(ctx);

        String control = "j_id1:j_id2";
        String result = ctx.getApplication().getStateManager().getViewState(ctx);

        assertEquals(control, result);

    }

    public void testGetViewStateClient() throws Exception {

        // this exercise ResponseStateManager.getViewState() as well
        FacesContext ctx = getFacesContext();
        WebConfiguration webConfig =
              WebConfiguration.getInstance(ctx.getExternalContext());
        webConfig.overrideContextInitParameter(StateSavingMethod,
                                               StateManager.STATE_SAVING_METHOD_CLIENT);
        webConfig.overrideContextInitParameter(AutoCompleteOffOnViewState, false);

        // recreate the RenderKit so the change is picked up.
        RenderKit rk = new RenderKitImpl();
        TestingUtil.setPrivateField("lastRk",
                                    FacesContextImpl.class,
                                    ctx,
                                    rk);
        TestingUtil.setPrivateField("lastRkId",
                                    FacesContextImpl.class,
                                    ctx,
                                    RenderKitFactory.HTML_BASIC_RENDER_KIT);

        initView(ctx);

        // 
        // Cause the state to be written to the hidden field
        //
        StringWriter capture = new StringWriter();
        ResponseWriter writer = new HtmlResponseWriter(capture,
                                                       "text/html",
                                                       "UTF-8");
        ctx.setResponseWriter(writer);
        StateManager manager = ctx.getApplication().getStateManager();
        Object state = manager.saveView(ctx);
        manager.writeState(ctx, state);
        
        //
        // Extract the state from the hidden field
        //
        String rawResult = capture.toString();
        Pattern p = Pattern.compile("\\bvalue=\"(.+)\"");
        Matcher m = p.matcher(rawResult);
        assertTrue(m.find());
        String control = m.group(1);
        
        // 
        // Get the state as a string
        // 
        String result = manager.getViewState(ctx);

        assertEquals(control, result);

    }


    // --------------------------------------------------------- Private Methods


    private void initView(FacesContext ctx) {

        UIViewRoot root = Util.getViewHandler(getFacesContext()).createView(ctx, null);
        root.setViewId("/test");
        root.setId("root");
        root.setLocale(Locale.US);

        UIComponent comp1 = new UIInput();
        comp1.setId("comp1");

        UIComponent comp2 = new UIOutput();
        comp2.setId("comp2");

        UIComponent comp3 = new UIGraphic();
        comp3.setId("comp3");

        UIComponent facet1 = new UIOutput();
        facet1.setId("comp4");

        comp2.getFacets().put("facet1", facet1);

        root.getChildren().add(comp1);
        root.getChildren().add(comp2);
        root.getChildren().add(comp3);

        ctx.setViewRoot(root);

    }



}
