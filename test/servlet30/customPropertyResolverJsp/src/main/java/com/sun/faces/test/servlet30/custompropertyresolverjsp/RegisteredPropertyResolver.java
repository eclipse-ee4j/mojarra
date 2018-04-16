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

package com.sun.faces.test.servlet30.custompropertyresolverjsp;

import javax.faces.el.PropertyResolver;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;


public class RegisteredPropertyResolver extends PropertyResolver { 

     private PropertyResolver delegate;

    public RegisteredPropertyResolver(PropertyResolver delegate) {
        this.delegate = delegate;
    }

    public Object getValue(Object object, Object object1) throws EvaluationException, PropertyNotFoundException {
        System.out.println(this.getClass().getName() + ".getValue(Object,Object) called");
        return delegate.getValue(object, object1);
    }

    public Object getValue(Object object, int i) throws EvaluationException, PropertyNotFoundException {
        System.out.println(this.getClass().getName() + ".getValue(Object,int) called");
        return delegate.getValue(object, i);
    }

    public void setValue(Object object, Object object1, Object object2) throws EvaluationException, PropertyNotFoundException {
        System.out.println(this.getClass().getName() + ".setValue(Object,Object) called");
        delegate.setValue(object, object1, object2);
    }

    public void setValue(Object object, int i, Object object1) throws EvaluationException, PropertyNotFoundException {
        System.out.println(this.getClass().getName() + ".setValue(Object,int) called");
        delegate.setValue(object, i, object1);
    }

    public boolean isReadOnly(Object object, Object object1) throws EvaluationException, PropertyNotFoundException {
        System.out.println(this.getClass().getName() + ".isReadOnly(Object,Object) called");
        return delegate.isReadOnly(object, object1);
    }

    public boolean isReadOnly(Object object, int i) throws EvaluationException, PropertyNotFoundException {
        System.out.println(this.getClass().getName() + ".isReadOnly(Object,int) called");
        return delegate.isReadOnly(object, i);
    }

    public Class getType(Object object, Object object1) throws EvaluationException, PropertyNotFoundException {
        System.out.println(this.getClass().getName() + ".getType(Object,Object) called");
        return delegate.getType(object, object1);
    }

    public Class getType(Object object, int i) throws EvaluationException, PropertyNotFoundException {
        System.out.println(this.getClass().getName() + ".getValue(Object,int) called");
        return delegate.getType(object, i);
    }
}
