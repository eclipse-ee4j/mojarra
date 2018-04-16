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

// TestApplicationImpl.java

package com.sun.faces.application;

import com.sun.faces.RIConstants;
import com.sun.faces.TestComponent;
import com.sun.faces.TestForm;
import com.sun.faces.cactus.JspFacesTestCase;
import com.sun.faces.cactus.TestingUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.application.StateManager;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.IntegerConverter;
import javax.faces.el.PropertyResolver;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;
import javax.faces.el.VariableResolver;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.ListenerFor;
import javax.faces.event.ListenersFor;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.event.PreRenderComponentEvent;

/**
 * <B>TestApplicationImpl</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 */

public class TestApplicationImpl extends JspFacesTestCase {

//
// Protected Constants
//
    public static final String HANDLED_ACTIONEVENT1 = "handledValueEvent1";
    public static final String HANDLED_ACTIONEVENT2 = "handledValueEvent2";

//
// Class Variables
//

//
// Instance Variables
//
    private Application application = null;

// Attribute Instance Variables

// Relationship Instance Variables

//
// Constructors and Initializers    
//

    public TestApplicationImpl() {
        super("TestApplicationImpl");
    }


    public TestApplicationImpl(String name) {
        super(name);
    }
//
// Class methods
//

//
// General Methods
//

    public void setUp() {
        super.setUp();
        ApplicationFactory aFactory =
            (ApplicationFactory) FactoryFinder.getFactory(
                FactoryFinder.APPLICATION_FACTORY);
        application = (Application) aFactory.getApplication();
    }


    public void testAccessors() {

        assertTrue(application.getELResolver() != null);
        assertTrue(application.getExpressionFactory() != null);
        
        // 1. Verify "getActionListener" returns the same ActionListener
        //    instance if called multiple times.
        //
        ActionListener actionListener1 = new ValidActionListener();
        application.setActionListener(actionListener1);
        ActionListener actionListener2 = application.getActionListener();
        ActionListener actionListener3 = application.getActionListener();
        assertTrue((actionListener1 == actionListener2) &&
                   (actionListener1 == actionListener3));

        // 2. Verify "getNavigationHandler" returns the same NavigationHandler
        //    instance if called multiple times.
        //
        NavigationHandler navigationHandler1 = new NavigationHandlerImpl();
        application.setNavigationHandler(navigationHandler1);
        NavigationHandler navigationHandler2 = application.getNavigationHandler();
        NavigationHandler navigationHandler3 = application.getNavigationHandler();
        assertTrue((navigationHandler1 == navigationHandler2) &&
                   (navigationHandler1 == navigationHandler3));

        // 3. Verify "getPropertyResolver" returns the same PropertyResolver
        //    instance if called multiple times.
        //
        PropertyResolver propertyResolver1 = application.getPropertyResolver();
        PropertyResolver propertyResolver2 = application.getPropertyResolver();
        PropertyResolver propertyResolver3 = application.getPropertyResolver();
        assertTrue((propertyResolver1 == propertyResolver2) &&
                   (propertyResolver1 == propertyResolver3));

        // 4. Verify "getVariableResolver" returns the same VariableResolver
        //    instance if called multiple times.
        //
        VariableResolver variableResolver1 = application.getVariableResolver();
        VariableResolver variableResolver2 = application.getVariableResolver();
        VariableResolver variableResolver3 = application.getVariableResolver();
        assertTrue((variableResolver1 == variableResolver2) &&
                   (variableResolver1 == variableResolver3));

        // 5. Verify "getStateManager" returns the same StateManager
        //    instance if called multiple times.
        //
        StateManager stateManager1 = new StateManagerImpl();
        application.setStateManager(stateManager1);
        StateManager stateManager2 = application.getStateManager();
        StateManager stateManager3 = application.getStateManager();
        assertTrue((stateManager1 == stateManager2) &&
                   (stateManager1 == stateManager3));
    }


    public void testExceptions() {
        boolean thrown;

        // 1. Verify NullPointer exception which occurs when attempting
        //    to set a null ActionListener
        //
        thrown = false;
        try {
            application.setActionListener(null);
        } catch (NullPointerException e) {
            thrown = true;
        }
        assertTrue(thrown);

        // 3. Verify NullPointer exception which occurs when attempting
        //    to set a null NavigationHandler
        //
        thrown = false;
        try {
            application.setNavigationHandler(null);
        } catch (NullPointerException e) {
            thrown = true;
        }
        assertTrue(thrown);

        // 4. Verify NPE occurs when attempting to set
        // a null PropertyResolver
        thrown = false;
        try {
            application.setPropertyResolver(null);
        } catch (NullPointerException npe) {
            thrown = true;
        }
        assertTrue(thrown);

        // 5. Verify NPE occurs when attempting to set
        // a null VariableResolver
        thrown = false;
        try {
            application.setVariableResolver(null);
        } catch (NullPointerException npe) {
            thrown = true;
        }
        assertTrue(thrown);

        // 5. Verify ISE occurs when attempting to set
        // a VariableResolver after a request has been processed
        ApplicationAssociate associate =
             ApplicationAssociate.getInstance(
                  getFacesContext().getExternalContext());
        associate.setRequestServiced();
        thrown = false;
        try {
            application.setVariableResolver(application.getVariableResolver());
        } catch (IllegalStateException e) {
            thrown = true;
        }
        assertTrue(thrown);

        // 6. Verify ISE occurs when attempting to set
        // a PropertyResolver after a request has been processed
        thrown = false;
        try {
            application.setPropertyResolver(application.getPropertyResolver());
        } catch (IllegalStateException e) {
            thrown = true;
        }
        assertTrue(thrown);

        // 7. Verify NullPointer exception which occurs when attempting
        //    to get a ValueBinding with a null ref
        //
        thrown = false;
        try {
            application.createValueBinding(null);
        } catch (Exception e) {
            thrown = true;
        }
        assertTrue(thrown);

        // 8.Verify NullPointerException occurs when attempting to pass a
        // null VariableResolver
        //
        thrown = false;
        try {
            application.setVariableResolver(null);
        } catch (NullPointerException e) {
            thrown = true;
        }
        assertTrue(thrown);
       
        // 9. Verify NullPointer exception which occurs when attempting
        //    to set a null StateManager
        //
        thrown = false;
        try {
            application.setStateManager(null);
        } catch (NullPointerException e) {
            thrown = true;
        }
        assertTrue(thrown);

        thrown = false;
        try {
            application.createValueBinding("improperexpression");
        } catch (ReferenceSyntaxException e) {
            thrown = true;
        }
        assertFalse(thrown);

        thrown = false;
        try {
            application.createValueBinding("improper expression");
        } catch (ReferenceSyntaxException e) {
            thrown = true;
        }
        assertFalse(thrown);

        thrown = false;
        try {
            application.createValueBinding("improper\texpression");
        } catch (ReferenceSyntaxException e) {
            thrown = true;
        }
        assertFalse(thrown);

        thrown = false;
        try {
            application.createValueBinding("improper\rexpression");
        } catch (ReferenceSyntaxException e) {
            thrown = true;
        }
        assertFalse(thrown);

        thrown = false;
        try {
            application.createValueBinding("improper\nexpression");
        } catch (ReferenceSyntaxException e) {
            thrown = true;
        }
        assertFalse(thrown);

        thrown = false;
        try {
            application.createValueBinding("#improperexpression");
        } catch (ReferenceSyntaxException e) {
            thrown = true;
        }
        assertFalse(thrown);

        thrown = false;
        try {
            application.createValueBinding("#{improperexpression");
        } catch (ReferenceSyntaxException e) {
            thrown = true;
        }
        assertTrue(thrown);

        thrown = false;
        try {
            application.createValueBinding("improperexpression}");
        } catch (ReferenceSyntaxException e) {
            thrown = true;
        }
        assertFalse(thrown);

        thrown = false;
        try {
            application.createValueBinding("{improperexpression}");
        } catch (ReferenceSyntaxException e) {
            thrown = true;
        }
        assertFalse(thrown);

        thrown = false;
        try {
            application.createValueBinding("improperexpression}#");
        } catch (ReferenceSyntaxException e) {
            thrown = true;
        } 
        assertFalse(thrown);


        thrown = false;
        try {
            application.createValueBinding("#{proper[\"a key\"]}");
        } catch (ReferenceSyntaxException e) {
            thrown = true;
        }
        assertFalse(thrown);

        try {
            application.createValueBinding("#{proper[\"a { } key\"]}");
        } catch (ReferenceSyntaxException e) {
            thrown = true;
        }
        assertFalse(thrown);

        try {
            application.createValueBinding("bean.a{indentifer");
        } catch (ReferenceSyntaxException e) {
            thrown = true;
        }
        assertFalse(thrown);

        thrown = false;
        try {
            application.createValueBinding("bean['invalid'");
        } catch (ReferenceSyntaxException e) {
            thrown = true;
        }
        assertFalse(thrown);

        thrown = false;
        try {
            application.createValueBinding("bean[[\"invalid\"]].foo");
        } catch (ReferenceSyntaxException e) {
            thrown = true;
        }
        assertFalse(thrown);

        thrown = false;
        try {
            application.createValueBinding("#{bean[\"[a\"]}");
        } catch (ReferenceSyntaxException e) {
            thrown = true;
        }
        assertFalse(thrown);

        try {
            application.createValueBinding("#{bean[\".a\"]}");
        } catch (ReferenceSyntaxException e) {
            thrown = true;
        }
        assertFalse(thrown);
    }


    public class InvalidActionListener implements ActionListener {

        public void processAction(ActionEvent event) {
            System.setProperty(HANDLED_ACTIONEVENT1, HANDLED_ACTIONEVENT1);
        }
    }

    public class ValidActionListener implements ActionListener {

        public void processAction(ActionEvent event) {
            System.setProperty(HANDLED_ACTIONEVENT2, HANDLED_ACTIONEVENT2);
        }
    }

    //
    // Test Config related methods
    //

    public void testAddComponentPositive() {
        TestComponent
            newTestComponent = null,
            testComponent = new TestComponent();


        application.addComponent(testComponent.getComponentType(),
                                 "com.sun.faces.TestComponent");
        assertTrue(
            null !=
            (newTestComponent =
             (TestComponent)
            application.createComponent(testComponent.getComponentType())));
        assertTrue(newTestComponent != testComponent);

    }


    public void testCreateComponentExtension() {
        application.addComponent(TestForm.COMPONENT_TYPE,
                                 TestForm.class.getName());
        UIComponent c = application.createComponent(TestForm.COMPONENT_TYPE);
        assertTrue(c != null);
    }


    public void testGetComponentWithRefNegative() {
        ValueBinding valueBinding = null;
        boolean exceptionThrown = false;
        UIComponent result = null;
        getFacesContext().getExternalContext().getSessionMap().put("TAIBean",
                                                                   this);
        assertTrue(null != (valueBinding =
                            application.createValueBinding(
                                "#{sessionScope.TAIBean}")));

        try {
            result = application.createComponent(valueBinding, getFacesContext(),
                                                 "notreached");
            assertTrue(false);
        } catch (FacesException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }
    
    public void testGetComponentExpressionRefNegative() throws ELException{
        ValueExpression valueBinding = null;
        boolean exceptionThrown = false;
        UIComponent result = null;
        getFacesContext().getExternalContext().getSessionMap().put("TAIBean",
                                                                   this);
        assertTrue(null != (valueBinding =
                            application.getExpressionFactory().createValueExpression(
                            getFacesContext().getELContext(), "#{sessionScope.TAIBean}", Object.class)));

        try {
            result = application.createComponent(valueBinding, getFacesContext(),
                                                 "notreached");
            assertTrue(false);
        } catch (FacesException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        
        // make sure FacesException is thrown when a bogus ValueExpression is
        // passed to createComponent. JSF RI Issue 162
        assertTrue(null != (valueBinding =
                            application.getExpressionFactory().createValueExpression(
                            getFacesContext().getELContext(), "#{a.b}", Object.class)));

        try {
            result = application.createComponent(valueBinding, getFacesContext(),
                                                 "notreached");
            assertTrue(false);
        } catch (FacesException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }


    public void testSetViewHandlerException() throws Exception {
        // RELEASE_PENDING - FIX.  There seems to be a problem
        // with the test framework exposing two different applicationassociate
        // instances.  As such, the flag denoting that a request has
        // been processed is never flagged and thus this test fails.
        /*
        ViewHandler handler = new ViewHandlerImpl();
        UIViewRoot root = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        root.setViewId("/view");
        root.setId("id");
        root.setLocale(Locale.US);
        getFacesContext().setViewRoot(root);

        boolean exceptionThrown = false;
        try {
            application.setViewHandler(handler);
        } catch (IllegalStateException ise) {
            exceptionThrown = true;
        }
        assertTrue(!exceptionThrown);

        try {
            handler.renderView(getFacesContext(),
                               getFacesContext().getViewRoot());
            application.setViewHandler(handler);
        } catch (IllegalStateException ise) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        // and test setting the StateManager too.
        exceptionThrown = false;
        try {
            application.setStateManager(new StateManagerImpl());
        } catch (IllegalStateException ise) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        */
    }

    // Ensure ApplicationImpl.setDefaultLocale(null) throws NPE
    public void testSetDefaultLocaleNPE() throws Exception {
        try {
            application.setDefaultLocale(null);
            assertTrue(false);
        } catch (NullPointerException npe) {
            ; // we're ok
        }
    }
    
    public void testResourceBundle() throws Exception {
        ResourceBundle rb = null;
        UIViewRoot root = new UIViewRoot();
        root.setLocale(Locale.ENGLISH);
        getFacesContext().setViewRoot(root);
       
        // negative test, non-existant rb
        rb = application.getResourceBundle(getFacesContext(), "bogusName");
        
        assertNull(rb);
        
        // basic test, existing rb
        rb = application.getResourceBundle(getFacesContext(), "testResourceBundle");
        
        assertNotNull(rb);
        
        String value = rb.getString("value1");
        assertEquals("Jerry", value);
        
        // switch locale to German
        getFacesContext().getViewRoot().setLocale(Locale.GERMAN);
        rb = application.getResourceBundle(getFacesContext(), "testResourceBundle");
        
        assertNotNull(rb);
        
        value = rb.getString("value1");
        assertEquals("Bernhard", value);
        
        // switch to a different rb
        rb = application.getResourceBundle(getFacesContext(), "testResourceBundle2");
        
        assertNotNull(rb);
        value = rb.getString("label");
        assertEquals("Abflug", value);
        
    }
    
    public void testLegacyPropertyResolversWithUnifiedEL() {
      
        ValueExpression ve1 = application.getExpressionFactory().
            createValueExpression(getFacesContext().getELContext(),
                "#{mixedBean.customPRTest1}", Object.class);
        Object result = ve1.getValue(getFacesContext().getELContext());     
        assertTrue(result.equals("TestPropertyResolver"));
        
        ValueExpression ve2 = application.getExpressionFactory().
            createValueExpression(getFacesContext().getELContext(),
                "#{mixedBean.customPRTest2}", Object.class);
        result = ve2.getValue(getFacesContext().getELContext());      
        assertTrue(result.equals("PropertyResolverTestImpl"));
    }
    
    public void testLegacyVariableResolversWithUnifiedEL() {
      
        ValueExpression ve1 = application.getExpressionFactory().
            createValueExpression(getFacesContext().getELContext(),
                "#{customVRTest1}", Object.class);
        Object result = ve1.getValue(getFacesContext().getELContext());        
        assertTrue(result.equals("TestVariableResolver"));
        
        ValueExpression ve2 = application.getExpressionFactory().
            createValueExpression(getFacesContext().getELContext(),
                "#{customVRTest2}", Object.class);
        result = ve2.getValue(getFacesContext().getELContext());      
        assertTrue(result.equals("TestOldVariableResolver"));
    }

    public void testConverterUpdate() {        

        FacesContext context = getFacesContext();
        Application app = context.getApplication();

        Converter intConverter = application.createConverter("javax.faces.Integer");
        Converter intConverter2 = application.createConverter(Integer.TYPE);
        Converter intConverter3 = application.createConverter(Integer.class);

        assertTrue(IntegerConverter.class.equals(intConverter.getClass())
             && IntegerConverter.class.equals(intConverter2.getClass())
             && IntegerConverter.class.equals(intConverter3.getClass()));

        app.addConverter("javax.faces.Integer", CustomIntConverter.class.getName());

        intConverter = application.createConverter("javax.faces.Integer");
        intConverter2 = application.createConverter(Integer.TYPE);
        intConverter3 = application.createConverter(Integer.class);

        assertTrue(CustomIntConverter.class.equals(intConverter.getClass())
             && CustomIntConverter.class.equals(intConverter2.getClass())
             && CustomIntConverter.class.equals(intConverter3.getClass()));

        app.addConverter(Integer.TYPE, IntegerConverter.class.getName());

        intConverter = application.createConverter("javax.faces.Integer");
        intConverter2 = application.createConverter(Integer.TYPE);
        intConverter3 = application.createConverter(Integer.class);

        assertTrue(IntegerConverter.class.equals(intConverter.getClass())
             && IntegerConverter.class.equals(intConverter2.getClass())
             && IntegerConverter.class.equals(intConverter3.getClass()));

        app.addConverter(Integer.class, CustomIntConverter.class.getName());

        intConverter = application.createConverter("javax.faces.Integer");
        intConverter2 = application.createConverter(Integer.TYPE);
        intConverter3 = application.createConverter(Integer.class);

        assertTrue(CustomIntConverter.class.equals(intConverter.getClass())
             && CustomIntConverter.class.equals(intConverter2.getClass())
             && CustomIntConverter.class.equals(intConverter3.getClass()));

        // reset to the standard converter
        app.addConverter("javax.faces.Integer", IntegerConverter.class.getName());
    }


    public void testComponentAnnotatations() throws Exception {

        Application application = getFacesContext().getApplication();
        application.addComponent("CustomInput", CustomOutput.class.getName());
        CustomOutput c = (CustomOutput) application.createComponent("CustomInput");
        CustomOutput c2 = (CustomOutput) application.createComponent("CustomInput");
        UIViewRoot root = getFacesContext().getViewRoot();
        root.getChildren().add(c);
        root.getChildren().add(c2);
        assertTrue(c.getEvent() instanceof PostAddToViewEvent);
        assertTrue(c2.getEvent() instanceof PostAddToViewEvent);
        List<UIComponent> headComponents = root.getComponentResources(getFacesContext(), "head");
        System.out.println(headComponents.toString());
        assertTrue(headComponents.size() == 1);
        assertTrue(headComponents.get(0) instanceof UIOutput);
        assertTrue("test".equals(headComponents.get(0).getAttributes().get("library")));
        List<UIComponent> bodyComponents = root.getComponentResources(getFacesContext(), "body");
        assertTrue(bodyComponents.size() == 1);
        assertTrue(bodyComponents.get(0) instanceof UIOutput);
        assertTrue("test.js".equals(bodyComponents.get(0).getAttributes().get("name")));
        assertTrue("body".equals(bodyComponents.get(0).getAttributes().get("target")));

        application.addComponent("CustomInput2", CustomOutput2.class.getName());
        CustomOutput2 c3 = (CustomOutput2) application.createComponent("CustomInput2");
        root.getChildren().add(c3);
        assertTrue(c3.getEvent() instanceof PostAddToViewEvent);
        c3.reset();
        c3.encodeAll(getFacesContext());
        assertTrue(c3.getEvent() instanceof PreRenderComponentEvent);

    }


    public void testEvaluateExpressionGet() {

        FacesContext ctx = getFacesContext();
        ExternalContext extCtx = ctx.getExternalContext();
        Application app = getFacesContext().getApplication();

        extCtx.getRequestMap().put("date", new Date());
        Date d = app.evaluateExpressionGet(ctx, "#{requestScope.date}", Date.class);
        assertNotNull(d);
        extCtx.getRequestMap().put("list", new ArrayList());
        List l = app.evaluateExpressionGet(ctx, "#{requestScope.list}", List.class);
        assertNotNull(l);
        Object o = app.evaluateExpressionGet(ctx, "#{requestScope.list}", Object.class);
        assertNotNull(o);

    }


    // ---------------------------------------------------------- Public Methods
    
    public static void clearResourceBundlesFromAssociate(ApplicationImpl application) {
        ApplicationAssociate associate = (ApplicationAssociate)
            TestingUtil.invokePrivateMethod("getAssociate",
                                            RIConstants.EMPTY_CLASS_ARGS,
                                            RIConstants.EMPTY_METH_ARGS,
                                            ApplicationImpl.class,
                                            application);       
        if (null != associate) {
            Map resourceBundles = (Map) 
                TestingUtil.getPrivateField("resourceBundles",
                                            ApplicationAssociate.class,
                                            associate);
            if (null != resourceBundles) {
                resourceBundles.clear();
            }
        }
    }


    // ----------------------------------------------------------- Inner Classes

    public static class CustomIntConverter implements Converter {

        private IntegerConverter delegate = new IntegerConverter();

        public Object getAsObject(FacesContext context, UIComponent component, String value) {
            return delegate.getAsObject(context, component, value);
        }

        public String getAsString(FacesContext context, UIComponent component, Object value) {
            return delegate.getAsString(context, component, value);
        }
    }

    @ListenerFor(systemEventClass=PostAddToViewEvent.class,
                 sourceClass= CustomOutput.class)
    @ResourceDependencies({
        @ResourceDependency(name="#{'test.js'}",library="test",target="#{'body'}"),
        @ResourceDependency(name="test.css",library="#{'test'}")
    })
    public static final class CustomOutput
          extends UIOutput
          implements ComponentSystemEventListener {

        private boolean processEventInvoked;
        private ComponentSystemEvent event;

        public void processEvent(ComponentSystemEvent event)
        throws AbortProcessingException {
            processEventInvoked = true;
            this.event = event;
        }

        public void reset() {
            processEventInvoked = false;
            event = null;
        }

        public boolean isProcessEventInvoked() {
            return processEventInvoked;
        }

        public ComponentSystemEvent getEvent() {
            return event;
        }
    }

    @ListenersFor({
        @ListenerFor(systemEventClass = PostAddToViewEvent.class,
                     sourceClass = CustomOutput.class),
        @ListenerFor(systemEventClass = PreRenderComponentEvent.class,
                     sourceClass = CustomOutput.class)
    })
    public static final class CustomOutput2
          extends UIOutput
          implements ComponentSystemEventListener {

        private boolean processEventInvoked;
        private ComponentSystemEvent event;

        public void processEvent(ComponentSystemEvent event)
              throws AbortProcessingException {
            processEventInvoked = true;
            this.event = event;
        }

        public void reset() {
            processEventInvoked = false;
            event = null;
        }

        public boolean isProcessEventInvoked() {
            return processEventInvoked;
        }

        public ComponentSystemEvent getEvent() {
            return event;
        }
    }


} // end of class TestApplicationImpl
