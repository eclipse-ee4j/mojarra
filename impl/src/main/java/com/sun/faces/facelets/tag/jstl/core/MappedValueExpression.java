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

import javax.el.ELContext;
import javax.el.ValueExpression;
import java.io.Serializable;
import java.util.Map;

/**
 * @author Jacob Hookom
 * @version $Id$
 */
public final class MappedValueExpression extends ValueExpression {

    private final static class Entry implements Map.Entry, Serializable {

        private static final long serialVersionUID = 4361498560718735987L;
        private final Map src;
        private final Object key;

        public Entry(Map src, Object key) {
            this.src = src;
            this.key = key;
        }

        @Override
        public Object getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return src.get(key);
        }

        @Override
        public Object setValue(Object value) {
            return src.put(key, value);
        }

    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final Object key;

    private final ValueExpression orig;

    /**
     * 
     */
    public MappedValueExpression(ValueExpression orig, Map.Entry entry) {
        this.orig = orig;
        this.key = entry.getKey();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.el.ValueExpression#getValue(javax.el.ELContext)
     */
    @Override
    public Object getValue(ELContext context) {
        Object base = this.orig.getValue(context);
        if (base != null) {
            context.setPropertyResolved(true);
            return new Entry((Map) base, key);
            
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.el.ValueExpression#setValue(javax.el.ELContext,
     *      java.lang.Object)
     */
    @Override
    public void setValue(ELContext context, Object value) {
        Object base = this.orig.getValue(context);
        if (base != null) {
            context.setPropertyResolved(false);
            context.getELResolver().setValue(context, base, key, value);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.el.ValueExpression#isReadOnly(javax.el.ELContext)
     */
    @Override
    public boolean isReadOnly(ELContext context) {
        Object base = this.orig.getValue(context);
        if (base != null) {
            context.setPropertyResolved(false);
            return context.getELResolver().isReadOnly(context, base, key);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.el.ValueExpression#getType(javax.el.ELContext)
     */
    @Override
    public Class getType(ELContext context) {
        Object base = this.orig.getValue(context);
        if (base != null) {
            context.setPropertyResolved(false);
            return context.getELResolver().getType(context, base, key);
        }
        return null;
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
        return 0;
    }

    /*
     * (non-Javadoc)eturn new Map.Entry<K, V>
     * 
     * @see javax.el.Expression#isLiteralText()
     */
    @Override
    public boolean isLiteralText() {
        return false;
    }

}
