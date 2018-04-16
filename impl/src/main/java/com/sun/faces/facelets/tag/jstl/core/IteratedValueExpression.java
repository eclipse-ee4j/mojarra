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

package com.sun.faces.facelets.tag.jstl.core;

import java.util.Collection;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.PropertyNotWritableException;
import javax.el.ValueExpression;

public final class IteratedValueExpression extends ValueExpression {

    private static final long serialVersionUID = 1L;

    private ValueExpression orig;

    private int start;
    private int index;

    public IteratedValueExpression(ValueExpression orig, int start, int index) {
        this.orig = orig;
        this.start = start;
        this.index = index;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.el.ValueExpression#getValue(javax.el.ELContext)
     */
    @Override
    public Object getValue(ELContext context) {
        Collection collection = (Collection) orig.getValue(context);
        Iterator iterator = collection.iterator();
        Object result = null;
        int i = start;
        if (i != 0) {
            while(i != 0) {
                result = iterator.next();
                if (!iterator.hasNext()) {
                    throw new ELException("Unable to position start");
                }
                i--;
            }
        } else {
            result = iterator.next();
        }
        while(i < index) {
            if (!iterator.hasNext()) {
                throw new ELException("Unable to get given value");
            }
            i++;
            result = iterator.next();
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.el.ValueExpression#setValue(javax.el.ELContext,
     *      java.lang.Object)
     */
    @Override
    public void setValue(ELContext context, Object value) {
        context.setPropertyResolved(false);
        throw new PropertyNotWritableException();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.el.ValueExpression#isReadOnly(javax.el.ELContext)
     */
    @Override
    public boolean isReadOnly(ELContext context) {
        context.setPropertyResolved(false);
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.el.ValueExpression#getType(javax.el.ELContext)
     */
    @Override
    public Class getType(ELContext context) {
        context.setPropertyResolved(false);
        return Object.class;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.el.ValueExpression#getExpectedType()
     */
    @Override
    public Class getExpectedType() {
        return Object.class;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.el.Expression#getExpressionString()
     */
    @Override
    public String getExpressionString() {
        return this.orig.getExpressionString();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.el.Expression#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return this.orig.equals(obj);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.el.Expression#hashCode()
     */
    @Override
    public int hashCode() {
        return this.orig.hashCode();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.el.Expression#isLiteralText()
     */
    @Override
    public boolean isLiteralText() {
        return false;
    }

}
