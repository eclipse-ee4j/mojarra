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

package com.sun.faces.cactus;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

/**
 * $Id: TestingUtil.java,v 1.2 2005/10/26 02:24:06 edburns Exp $
 */
public class TestingUtil {

    public static Object invokePrivateMethod(String methodName,
                                             Class[] params,
                                             Object[] args,
                                             Class containingClass,
                                             Object invocationTarget) {
        try {
            Method method =
                containingClass.
                    getDeclaredMethod(methodName, params);
            method.setAccessible(true);
            return method.invoke(invocationTarget, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getPrivateField(String fieldName,
                                         Class containingClass,
                                         Object target) {
        try {
            Field field = containingClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    
    public static void setPrivateField(String fieldName,
                                       Class containingClass,
                                       Object target,
                                       Object value) {
        try {
            Field field = containingClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void setUnitTestModeEnabled(boolean newState) {
        try {
            // look up Switch class.
            Class switchClass = Class.forName("com.sun.faces.util.Util");
            // look up getSwitch method.
	    Class paramTypes[] = new Class[] { Boolean.TYPE };
            Method switchMethod = switchClass.getMethod("setUnitTestModeEnabled", 
                                                        paramTypes);
            // invoke the method and get an instance of Switch
	    Object params[] = new Object[] {  newState ? Boolean.TRUE : Boolean.FALSE };
            Object switchObj = switchMethod.invoke(null, params);
            
        } catch (Exception e) {            
        }	
    }
}
