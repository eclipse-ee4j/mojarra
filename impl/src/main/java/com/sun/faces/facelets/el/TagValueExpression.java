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

package com.sun.faces.facelets.el;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.PropertyNotWritableException;
import jakarta.el.ValueExpression;
import jakarta.el.ValueReference;
import jakarta.faces.view.facelets.TagAttribute;

/**
 *
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public final class TagValueExpression extends ValueExpression implements Externalizable {

    private static final long serialVersionUID = 1L;

    private ValueExpression wrapped;
    private String tagAttribute;

    public TagValueExpression() {
        super();
    }

    public TagValueExpression(TagAttribute tagAttribute, ValueExpression wrapped) {
        this.tagAttribute = tagAttribute.toString();
        this.wrapped = wrapped;
    }

    @Override
    public Class<?> getExpectedType() {
        return wrapped.getExpectedType();
    }

    @Override
    public Class<?> getType(ELContext context) {
        try {
            return wrapped.getType(context);
        } catch (PropertyNotFoundException pnfe) {
            throw new PropertyNotFoundException(tagAttribute + ": " + pnfe.getMessage(), pnfe);
        } catch (ELException e) {
            throw new ELException(tagAttribute + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Object getValue(ELContext context) {
        try {
            return wrapped.getValue(context);
        } catch (PropertyNotFoundException pnfe) {
            throw new PropertyNotFoundException(tagAttribute + ": " + pnfe.getMessage(), pnfe);
        } catch (ELException e) {
            throw new ELException(tagAttribute + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context) {
        try {
            return wrapped.isReadOnly(context);
        } catch (PropertyNotFoundException pnfe) {
            throw new PropertyNotFoundException(tagAttribute + ": " + pnfe.getMessage(), pnfe);
        } catch (ELException e) {
            throw new ELException(tagAttribute + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void setValue(ELContext context, Object value) {
        try {
            wrapped.setValue(context, value);
        } catch (PropertyNotFoundException pnfe) {
            throw new PropertyNotFoundException(tagAttribute + ": " + pnfe.getMessage(), pnfe);
        } catch (PropertyNotWritableException pnwe) {
            throw new PropertyNotWritableException(tagAttribute + ": " + pnwe.getMessage(), pnwe);
        } catch (ELException e) {
            throw new ELException(tagAttribute + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TagValueExpression that = (TagValueExpression) o;

        if (tagAttribute != null ? !tagAttribute.equals(that.tagAttribute) : that.tagAttribute != null) {
            return false;
        }
        if (wrapped != null ? !wrapped.equals(that.wrapped) : that.wrapped != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = wrapped != null ? wrapped.hashCode() : 0;
        result = 31 * result + (tagAttribute != null ? tagAttribute.hashCode() : 0);
        return result;
    }

    @Override
    public String getExpressionString() {
        return wrapped.getExpressionString();
    }

    @Override
    public boolean isLiteralText() {
        return wrapped.isLiteralText();
    }

    @Override
    public ValueReference getValueReference(ELContext context) {
        return wrapped.getValueReference(context);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        wrapped = (ValueExpression) in.readObject();
        tagAttribute = in.readUTF();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(wrapped);
        out.writeUTF(tagAttribute);
    }

    public ValueExpression getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return tagAttribute;
    }
}
