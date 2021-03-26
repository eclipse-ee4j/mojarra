/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.faces.facelets.tag.IterationStatus;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;

/**
 * @author Jacob Hookom
 * @version $Id$
 */
public final class IterationStatusExpression extends ValueExpression {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final IterationStatus status;

    /**
     *
     */
    public IterationStatusExpression(IterationStatus status) {
        this.status = status;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.el.ValueExpression#getValue(jakarta.el.ELContext)
     */
    @Override
    public Object getValue(ELContext context) {
        return status;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.el.ValueExpression#setValue(jakarta.el.ELContext, java.lang.Object)
     */
    @Override
    public void setValue(ELContext context, Object value) {
        throw new UnsupportedOperationException("Cannot set IterationStatus");
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.el.ValueExpression#isReadOnly(jakarta.el.ELContext)
     */
    @Override
    public boolean isReadOnly(ELContext context) {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.el.ValueExpression#getType(jakarta.el.ELContext)
     */
    @Override
    public Class getType(ELContext context) {
        return IterationStatus.class;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.el.ValueExpression#getExpectedType()
     */
    @Override
    public Class getExpectedType() {
        return IterationStatus.class;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.el.Expression#getExpressionString()
     */
    @Override
    public String getExpressionString() {
        return toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IterationStatusExpression other = (IterationStatusExpression) obj;
        if (status != other.status && (status == null || !status.equals(other.status))) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.el.Expression#hashCode()
     */
    @Override
    public int hashCode() {
        return status.hashCode();
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.el.Expression#isLiteralText()
     */
    @Override
    public boolean isLiteralText() {
        return true;
    }

    @Override
    public String toString() {
        return status.toString();
    }

}
