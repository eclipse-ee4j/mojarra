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
import jakarta.el.MethodExpression;
import jakarta.el.MethodInfo;
import jakarta.el.MethodNotFoundException;
import jakarta.el.PropertyNotFoundException;
import jakarta.faces.view.facelets.TagAttribute;

/**
 *
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public final class TagMethodExpression extends MethodExpression implements Externalizable {

    private static final long serialVersionUID = 1L;

    private String attr;
    private MethodExpression orig;

    public TagMethodExpression() {
        super();
    }

    public TagMethodExpression(TagAttribute attr, MethodExpression orig) {
        this.attr = attr.toString();
        this.orig = orig;
    }

    @Override
    public MethodInfo getMethodInfo(ELContext context) {
        try {
            return orig.getMethodInfo(context);
        } catch (PropertyNotFoundException pnfe) {
            throw new PropertyNotFoundException(attr + ": " + pnfe.getMessage(), pnfe.getCause());
        } catch (MethodNotFoundException mnfe) {
            throw new MethodNotFoundException(attr + ": " + mnfe.getMessage(), mnfe.getCause());
        } catch (ELException e) {
            throw new ELException(attr + ": " + e.getMessage(), e.getCause());
        }
    }

    @Override
    public Object invoke(ELContext context, Object[] params) {
        try {
            return orig.invoke(context, params);
        } catch (PropertyNotFoundException pnfe) {
            throw new PropertyNotFoundException(attr + ": " + pnfe.getMessage(), pnfe.getCause());
        } catch (MethodNotFoundException mnfe) {
            throw new MethodNotFoundException(attr + ": " + mnfe.getMessage(), mnfe.getCause());
        } catch (ELException e) {
            throw new ELException(attr + ": " + e.getMessage(), e.getCause());
        }
    }

    @Override
    public String getExpressionString() {
        return orig.getExpressionString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TagMethodExpression that = (TagMethodExpression) o;

        if (attr != null ? !attr.equals(that.attr) : that.attr != null) {
            return false;
        }
        if (orig != null ? !orig.equals(that.orig) : that.orig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = attr != null ? attr.hashCode() : 0;
        result = 31 * result + (orig != null ? orig.hashCode() : 0);
        return result;
    }

    @Override
    public boolean isLiteralText() {
        return orig.isLiteralText();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(orig);
        out.writeUTF(attr);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        orig = (MethodExpression) in.readObject();
        attr = in.readUTF();
    }

    @Override
    public String toString() {
        return attr + ": " + orig;
    }
}
