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

// TestVariableResolverImpl.java

package com.sun.faces.el;

import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.TestBean;
import com.sun.faces.mgbean.ManagedBeanInfo;
import com.sun.faces.mgbean.BeanManager;
import com.sun.faces.cactus.TestBean.InnerBean;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.util.Util;

import javax.faces.FactoryFinder;
import javax.faces.application.ApplicationFactory;
import javax.faces.component.UIViewRoot;
import javax.faces.el.VariableResolver;
import java.util.Locale;
import javax.faces.application.Application;


/**
 * <B>TestVariableResolverImpl</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestVariableResolverImpl extends ServletFacesTestCase {

//
// Protected Constants
//

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

    public TestVariableResolverImpl() {
        super("TestFacesContext");
    }


    public TestVariableResolverImpl(String name) {
        super(name);
    }
//
// Class methods
//

//
// Methods from TestCase
//

//
// General Methods
//

    public void testScopedLookup() {
        TestBean testBean = new TestBean();
        InnerBean newInner, oldInner = new InnerBean();
        testBean.setInner(oldInner);
        VariableResolver variableResolver = 
            getFacesContext().getApplication().getVariableResolver();
        Object result = null;
        getFacesContext().getExternalContext().getSessionMap().remove(
            "TestBean");

        //
        // Get tests
        //

        // application
        getFacesContext().getExternalContext().getApplicationMap().put(
            "TestBean",
            testBean);
        result = variableResolver.resolveVariable(getFacesContext(),
                                                  "TestBean");
        assertTrue(result == testBean);
        getFacesContext().getExternalContext().getApplicationMap().remove(
            "TestBean");
        // session
        getFacesContext().getExternalContext().getSessionMap().put("TestBean",
                                                                   testBean);
        result = variableResolver.resolveVariable(getFacesContext(),
                                                  "TestBean");
        assertTrue(result == testBean);
        getFacesContext().getExternalContext().getSessionMap().remove(
            "TestBean");

        // session
        getFacesContext().getExternalContext().getRequestMap().put("TestBean",
                                                                   testBean);

        result = variableResolver.resolveVariable(getFacesContext(),
                                                  "TestBean");
        assertTrue(result == testBean);
        getFacesContext().getExternalContext().getRequestMap().remove(
            "TestBean");

    }


    public void testImplicitObjects() {
        VariableResolver variableResolver = 
            getFacesContext().getApplication().getVariableResolver();
        Object result = null;

        //
        // test scope maps
        //

        // ApplicationMap
        assertTrue(variableResolver.resolveVariable(getFacesContext(),
                                                    "applicationScope") ==
                   getFacesContext().getExternalContext().getApplicationMap());

        // SessionMap
        assertTrue(variableResolver.resolveVariable(getFacesContext(),
                                                    "sessionScope") ==
                   getFacesContext().getExternalContext().getSessionMap());

        // RequestMap
        assertTrue(variableResolver.resolveVariable(getFacesContext(),
                                                    "requestScope") ==
                   getFacesContext().getExternalContext().getRequestMap());

        //
        // test request objects
        //

        // cookie
        assertTrue(variableResolver.resolveVariable(getFacesContext(),
                                                    "cookie") ==
                   getFacesContext().getExternalContext().getRequestCookieMap());

        // header
        assertTrue(variableResolver.resolveVariable(getFacesContext(),
                                                    "header") ==
                   getFacesContext().getExternalContext().getRequestHeaderMap());

        // headerValues
        assertTrue(
            variableResolver.resolveVariable(getFacesContext(),
                                             "headerValues") ==
            getFacesContext().getExternalContext().getRequestHeaderValuesMap());

        // parameter
        assertTrue(variableResolver.resolveVariable(getFacesContext(),
                                                    "param") ==
                   getFacesContext().getExternalContext()
                   .getRequestParameterMap());

        // parameterValues
        assertTrue(
            variableResolver.resolveVariable(getFacesContext(),
                                             "paramValues") ==
            getFacesContext().getExternalContext()
            .getRequestParameterValuesMap());

        //
        // misc
        //

        // initParameter
        assertTrue(variableResolver.resolveVariable(getFacesContext(),
                                                    "initParam") ==
                   getFacesContext().getExternalContext().getInitParameterMap());


        // facesContext
        assertTrue(variableResolver.resolveVariable(getFacesContext(),
                                                    "facesContext") ==
                   getFacesContext());

        // tree
        // create a dummy root for the tree.
        UIViewRoot page = Util.getViewHandler(getFacesContext()).createView(getFacesContext(), null);
        page.setId("root");
        page.setViewId("newTree");
        page.setLocale(Locale.US);
        getFacesContext().setViewRoot(page);

        assertTrue(variableResolver.resolveVariable(getFacesContext(),
                                                    "view") ==
                   getFacesContext().getViewRoot());


    }


    // Negative tests (should throw exceptions)
    public void testNegative() throws Exception {
        VariableResolver variableResolver = 
            getFacesContext().getApplication().getVariableResolver();

        Object value = null;

        // ---------- NullPointerException Returns ----------

        try {
            value = variableResolver.resolveVariable(getFacesContext(), null);
             fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            ; // Expected result
        } 
        
        try {
            value = variableResolver.resolveVariable(null, "foo");
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            ; // Expected result
        } 

        try {
            value = variableResolver.resolveVariable(null, null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            ; // Expected result
        } 

    }


    /**
     * This test verifies that if the variable resolver does not find a
     * managed bean it tries to instantiate it if it was added to the
     * application's managed bean factory list.
     */
    public void testManagedBean() throws Exception {
        String beanName = "com.sun.faces.TestBean";

        ManagedBeanInfo beanInfo = new ManagedBeanInfo(beanName,
                                                       beanName,
                                                       "session",
                                                       null,
                                                       null,
                                                       null,
                                                       null);

        ApplicationFactory aFactory = (ApplicationFactory) FactoryFinder.getFactory(
            FactoryFinder.APPLICATION_FACTORY);
        Application application = (Application) aFactory.getApplication();
        ApplicationAssociate associate = ApplicationAssociate.getCurrentInstance();
        BeanManager manager = associate.getBeanManager();
        manager.register(beanInfo);

        VariableResolver variableResolver = application.getVariableResolver();

        Object result = variableResolver.resolveVariable(getFacesContext(),
                                                         beanName);

        assertTrue(result instanceof TestBean);
    }

} // end of class TestVariableResolverImpl
