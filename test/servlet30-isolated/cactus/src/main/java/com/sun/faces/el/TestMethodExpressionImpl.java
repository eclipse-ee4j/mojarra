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

// TestMethodRef.java
package com.sun.faces.el;

import com.sun.faces.cactus.ServletFacesTestCase;

import javax.el.MethodExpression;
import javax.el.ELException;
import javax.el.MethodNotFoundException;
import javax.faces.el.PropertyNotFoundException;

/**
 * <B>TestMethodRef </B> is a class ... <p/><B>Lifetime And Scope </B>
 * <P>
 * 
 */

public class TestMethodExpressionImpl extends ServletFacesTestCase
{

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

    public TestMethodExpressionImpl()
    {
        super("TestMethodExpression");
    }

    public TestMethodExpressionImpl(String name)
    {
        super(name);
    }

    //
    // Class methods
    //

    //
    // General Methods
    //
    protected MethodExpression create(String ref, Class[] params) throws Exception
    {
        return (getFacesContext().getApplication().getExpressionFactory().
            createMethodExpression(getFacesContext().getELContext(),ref, null, params));
    }
    
    public void testNullReference() throws Exception
    {
        try
        {
            create(null, null);
            fail();
        }
        catch (ELException e) {}
        catch (Exception exception) {
            fail();
        }
    }
    
    public void testInvalidMethod() throws Exception
    {
        try
        {
            create("${foo > 1}", null);
            fail();
        }
        catch (ELException e) {}
        catch (Exception exeption) {
            fail();
        }
    }
    
    public void testLiteralReference() throws Exception
    {
        boolean exceptionThrown = false;
        try
        {
            create("some.method", null);
        }
        catch (NullPointerException ee) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    public void testInvalidTrailing() throws Exception
    {
        MethodExpression mb = this.create(
                "#{NewCustomerFormHandler.redLectroidsMmmm}", new Class[0]);

        boolean exceptionThrown = false;
        try
        {
            mb.invoke(getFacesContext().getELContext(), new Object[0]);
        }
        catch (MethodNotFoundException me)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        mb = this.create("#{nonexistentBean.redLectroidsMmmm}", new Class[0]);
       
        exceptionThrown = false;
        try
        {
            mb.invoke(getFacesContext().getELContext(), new Object[0]);
        }
        catch (PropertyNotFoundException ne)
        {
            exceptionThrown = true;
        }
        catch (ELException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

} // end of class TestMethodRef
