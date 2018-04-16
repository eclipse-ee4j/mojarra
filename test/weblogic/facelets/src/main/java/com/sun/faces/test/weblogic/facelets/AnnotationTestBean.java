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

package com.sun.faces.test.weblogic.facelets;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;


public class AnnotationTestBean {
    
      public String getTestResult() {

        try {
            testAnnotatedComponentsWebInfClasses();
            return Boolean.TRUE.toString();
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.SEVERE,
                                            "AnnotationTestBean validation failure!",
                                            e);
            return Boolean.FALSE.toString();
        }
    }
    
    private void testAnnotatedComponentsWebInfClasses() throws Exception {
        FacesContext ctx = FacesContext.getCurrentInstance();
        Application app = ctx.getApplication();
        Validator v = app.createValidator("annotatedValidatorNoValue");
        
        assertNotNull(v);
        assertTrue(v instanceof AnnotatedValidatorNoValue);
        Set<String> defaultValidatorIds = app.getDefaultValidatorInfo().keySet();
        assertFalse(defaultValidatorIds.contains("AnnotatedValidatorNoValue"));
        String welcomeMessage = ((AnnotatedValidatorNoValue)v).getWelcomeMessage();
        assertTrue(welcomeMessage.equals("AnnotatedValidatorNoValue"));

        boolean exceptionThrown = false;
        v = null;
        try {
            v = app.createValidator("AnnotatedValidatorNoValue");
        }
        catch (FacesException fe) {
            assertTrue(null == v);
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

    } 
    
     private void assertNotNull(Object v) {
        if (v == null) {
            throw new RuntimeException();
        }
    }

    private void assertTrue(boolean t) {
        if (!t) {
            throw new RuntimeException();
        }
    }

    private void assertFalse(boolean t) {
        if (t) {
            throw new RuntimeException();
        }
    }

}
