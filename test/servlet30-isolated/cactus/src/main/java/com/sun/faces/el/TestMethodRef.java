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

import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.MethodNotFoundException;
import javax.faces.el.ReferenceSyntaxException;

/**
 * <B>TestMethodRef </B> is a class ... <p/><B>Lifetime And Scope </B>
 * <P>
 * 
 */

public class TestMethodRef extends ServletFacesTestCase
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

    public TestMethodRef()
    {
        super("TestMethodRef");
    }

    public TestMethodRef(String name)
    {
        super(name);
    }

    //
    // Class methods
    //

    //
    // General Methods
    //
    protected MethodBinding create(String ref, Class[] params) throws Exception
    {
        return (getFacesContext().getApplication().createMethodBinding(ref, params));
    }
    
    public void testNullReference() throws Exception
    {
        try
        {
            create(null, null);
            fail();
        }
        catch (NullPointerException npe) {}
        catch (Exception e) { fail("Should have thrown an NPE"); };
    }
    
    public void testInvalidMethod() throws Exception
    {
        try
        {
            create("#{foo > 1}", null);
            fail();
        }
        catch (ReferenceSyntaxException rse) {}
        catch (Exception e) { fail("Should have thrown a ReferenceSyntaxException"); }
    }
    
    public void testLiteralReference() throws Exception
    {
        try
        {
            create("some.method", null);
            fail();
        }
        catch (ReferenceSyntaxException rse) {}
        catch (Exception e) { fail("Should have thrown a ReferenceSyntaxException"); }
    }

    public void testInvalidTrailing() throws Exception
    {
        MethodBinding mb = this.create(
                "#{NewCustomerFormHandler.redLectroidsMmmm}", new Class[0]);

        boolean exceptionThrown = false;
        try
        {
            mb.invoke(getFacesContext(), new Object[0]);
        }
        catch (MethodNotFoundException e)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        mb = this.create("#{nonexistentBean.redLectroidsMmmm}", new Class[0]);
        
        // page 80 of the EL Spec, since nonexistentBean is null, the target
        // method is never reached and should catch a PropertyNotFoundException
        // and rethrow as a MethodNotFoundException
        exceptionThrown = false;
        try
        {
            mb.invoke(getFacesContext(), new Object[0]);
        }
        catch (MethodNotFoundException e)
        {
            exceptionThrown = true;
        }
        catch (EvaluationException e) {
            //TODO remove once adaptor is fixed
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

} // end of class TestMethodRef
