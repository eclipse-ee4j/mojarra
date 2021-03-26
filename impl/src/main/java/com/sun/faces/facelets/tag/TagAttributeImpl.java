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

package com.sun.faces.facelets.tag;

import static com.sun.faces.util.MessageUtils.ARGUMENTS_NOT_LEGAL_CC_ATTRS_EXPR;

import com.sun.faces.el.ELUtils;
import com.sun.faces.facelets.el.ContextualCompositeMethodExpression;
import com.sun.faces.facelets.el.ContextualCompositeValueExpression;
import com.sun.faces.facelets.el.ELText;
import com.sun.faces.facelets.el.TagMethodExpression;
import com.sun.faces.facelets.el.TagValueExpression;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.Util;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;
import jakarta.el.MethodExpression;
import jakarta.el.MethodInfo;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.view.Location;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;

/**
 * Representation of a Tag's attribute in a Facelet File
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public class TagAttributeImpl extends TagAttribute {

    private final boolean literal;

    private final String localName;

    private final Location location;

    private final String namespace;

    private final String qName;

    private final String value;

    private String string;

    private Tag tag;

    public TagAttributeImpl() {
        literal = false;
        localName = null;
        location = null;
        namespace = null;
        qName = null;
        value = null;
        string = null;
        tag = null;
    }

    public TagAttributeImpl(Location location, String ns, String localName, String qName, String value) {
        this.location = location;
        namespace = ns;
        this.localName = null == localName || 0 == localName.length() ? qName : localName;
        this.qName = qName;
        this.value = value;
        try {
            literal = ELText.isLiteral(this.value);
        } catch (ELException e) {
            throw new TagAttributeException(this, e);
        }
    }

    /**
     * <p class="changed_modified_2_3">
     * If literal,return {@link Boolean#valueOf(java.lang.String) Boolean.valueOf(java.lang.String)} passing our value,
     * otherwise call {@link #getObject(FaceletContext, Class) getObject(FaceletContext, Class)}.
     * </p>
     *
     * @see Boolean#valueOf(java.lang.String)
     * @see #getObject(FaceletContext, Class)
     * @param ctx FaceletContext to use
     * @return boolean value
     */
    @Override
    public boolean getBoolean(FaceletContext ctx) {
        if (literal) {
            return Boolean.valueOf(value);
        } else {
            Boolean bool = (Boolean) this.getObject(ctx, Boolean.class);
            if (bool == null) {
                bool = false;
            }
            return bool;
        }
    }

    /**
     * If literal, call {@link Integer#parseInt(java.lang.String) Integer.parseInt(String)}, otherwise call
     * {@link #getObject(FaceletContext, Class) getObject(FaceletContext, Class)}.
     *
     * @see Integer#parseInt(java.lang.String)
     * @see #getObject(FaceletContext, Class)
     * @param ctx FaceletContext to use
     * @return int value
     */
    @Override
    public int getInt(FaceletContext ctx) {
        if (literal) {
            return Integer.parseInt(value);
        } else {
            return ((Number) this.getObject(ctx, Integer.class)).intValue();
        }
    }

    /**
     * Local name of this attribute
     *
     * @return local name of this attribute
     */
    @Override
    public String getLocalName() {
        return localName;
    }

    /**
     * The location of this attribute in the FaceletContext
     *
     * @return the TagAttributeImpl's location
     */
    @Override
    public Location getLocation() {
        return location;
    }

    /**
     * Create a MethodExpression, using this attribute's value as the expression String.
     *
     * @see ExpressionFactory#createMethodExpression(jakarta.el.ELContext, java.lang.String, java.lang.Class,
     * java.lang.Class[])
     * @see MethodExpression
     * @param ctx FaceletContext to use
     * @param type expected return type
     * @param paramTypes parameter type
     * @return a MethodExpression instance
     */
    @Override
    public MethodExpression getMethodExpression(FaceletContext ctx, Class type, Class[] paramTypes) {

        MethodExpression result;

        try {
            ExpressionFactory f = ctx.getExpressionFactory();
            if (ELUtils.isCompositeComponentLookupWithArgs(value)) {
                String message = MessageUtils.getExceptionMessageString(ARGUMENTS_NOT_LEGAL_CC_ATTRS_EXPR);
                throw new TagAttributeException(this, message);
            }
            // Determine if this is a composite component attribute lookup.
            // If so, look for a MethodExpression under the attribute key
            if (ELUtils.isCompositeComponentMethodExprLookup(value)) {
                result = new AttributeLookupMethodExpression(getValueExpression(ctx, MethodExpression.class));
            } else if (ELUtils.isCompositeComponentExpr(value)) {
                MethodExpression delegate = new TagMethodExpression(this, f.createMethodExpression(ctx, value, type, paramTypes));
                result = new ContextualCompositeMethodExpression(getLocation(), delegate);
            } else {
                result = new TagMethodExpression(this, f.createMethodExpression(ctx, value, type, paramTypes));
            }
        } catch (Exception e) {
            if (e instanceof TagAttributeException) {
                throw (TagAttributeException) e;
            } else {
                throw new TagAttributeException(this, e);
            }
        }
        return result;
    }

    /**
     * The resolved Namespace for this attribute
     *
     * @return resolved Namespace
     */
    @Override
    public String getNamespace() {
        return namespace;
    }

    /**
     * Delegates to getObject with Object.class as a param
     *
     * @see #getObject(FaceletContext, Class)
     * @param ctx FaceletContext to use
     * @return Object representation of this attribute's value
     */
    @Override
    public Object getObject(FaceletContext ctx) {
        return this.getObject(ctx, Object.class);
    }

    /**
     * The qualified name for this attribute
     *
     * @return the qualified name for this attribute
     */
    @Override
    public String getQName() {
        return qName;
    }

    @Override
    public Tag getTag() {
        return tag;
    }

    @Override
    public void setTag(Tag tag) {
        this.tag = tag;
    }

    /**
     * Return the literal value of this attribute
     *
     * @return literal value
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * If literal, then return our value, otherwise delegate to getObject, passing String.class.
     *
     * @see #getObject(FaceletContext, Class)
     * @param ctx FaceletContext to use
     * @return String value of this attribute
     */
    @Override
    public String getValue(FaceletContext ctx) {
        if (literal) {
            return value;
        } else {
            return (String) this.getObject(ctx, String.class);
        }
    }

    /**
     * If literal, simply coerce our String literal value using an ExpressionFactory, otherwise create a ValueExpression and
     * evaluate it.
     *
     * @see ExpressionFactory#coerceToType(java.lang.Object, java.lang.Class)
     * @see ExpressionFactory#createValueExpression(jakarta.el.ELContext, java.lang.String, java.lang.Class)
     * @see ValueExpression
     * @param ctx FaceletContext to use
     * @param type expected return type
     * @return Object value of this attribute
     */
    @Override
    public Object getObject(FaceletContext ctx, Class type) {
        if (literal) {
            if (String.class.equals(type)) {
                return value;
            } else {
                try {
                    return ctx.getExpressionFactory().coerceToType(value, type);
                } catch (Exception e) {
                    throw new TagAttributeException(this, e);
                }
            }
        } else {
            ValueExpression ve = this.getValueExpression(ctx, type);
            try {
                return ve.getValue(ctx);
            } catch (Exception e) {
                throw new TagAttributeException(this, e);
            }
        }
    }

    /**
     * Create a ValueExpression, using this attribute's literal value and the passed expected type.
     *
     * @see ExpressionFactory#createValueExpression(jakarta.el.ELContext, java.lang.String, java.lang.Class)
     * @see ValueExpression
     * @param ctx FaceletContext to use
     * @param type expected return type
     * @return ValueExpression instance
     */
    @Override
    public ValueExpression getValueExpression(FaceletContext ctx, Class type) {
        return getValueExpression(ctx, value, type);
    }

    /**
     * If this TagAttributeImpl is literal (not #{..} or ${..})
     *
     * @return true if this attribute is literal
     */
    @Override
    public boolean isLiteral() {
        return literal;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (string == null) {
            string = location + " " + qName + "=\"" + value + "\"";
        }
        return string;
    }

    // --------------------------------------------------------- Private Methods

    public ValueExpression getValueExpression(FaceletContext ctx, String expr, Class type) {
        try {
            ExpressionFactory f = ctx.getExpressionFactory();
            ValueExpression delegate = f.createValueExpression(ctx, expr, type);
            if (ELUtils.isCompositeComponentExpr(expr)) {
                if (ELUtils.isCompositeComponentLookupWithArgs(expr)) {
                    String message = MessageUtils.getExceptionMessageString(ARGUMENTS_NOT_LEGAL_CC_ATTRS_EXPR);
                    throw new TagAttributeException(this, message);
                }
                return new TagValueExpression(this, new ContextualCompositeValueExpression(getLocation(), delegate));
            } else {
                return new TagValueExpression(this, delegate);
            }
        } catch (Exception e) {
            throw new TagAttributeException(this, e);
        }
    }

    // ---------------------------------------------------------- Nested Classes

    private static class AttributeLookupMethodExpression extends MethodExpression {

        private static final long serialVersionUID = -8983924930720420664L;
        private ValueExpression lookupExpression;

        public AttributeLookupMethodExpression(ValueExpression lookupExpression) {

            Util.notNull("lookupExpression", lookupExpression);
            this.lookupExpression = lookupExpression;

        }

        @SuppressWarnings({ "UnusedDeclaration" })
        public AttributeLookupMethodExpression() {
        } // for serialization

        @Override
        public MethodInfo getMethodInfo(ELContext elContext) {

            Util.notNull("elContext", elContext);
            Object result = lookupExpression.getValue(elContext);
            if (result != null && result instanceof MethodExpression) {
                return ((MethodExpression) result).getMethodInfo(elContext);
            }

            return null;

        }

        @Override
        public Object invoke(ELContext elContext, Object[] args) {

            Util.notNull("elContext", elContext);

            Object result = lookupExpression.getValue(elContext);
            if (result == null) {
                throw new FacesException(
                        "Unable to resolve composite component from using page using EL expression '" + lookupExpression.getExpressionString() + '\'');
            }
            if (!(result instanceof MethodExpression)) {
                throw new FacesException(
                        "Successfully resolved expression '" + lookupExpression.getExpressionString() + "', but the value is not a MethodExpression");
            }

            return ((MethodExpression) result).invoke(elContext, args);

        }

        @Override
        public String getExpressionString() {

            return lookupExpression.getExpressionString();

        }

        @Override
        public boolean equals(Object otherObj) {

            boolean result = false;
            if (otherObj instanceof AttributeLookupMethodExpression) {
                AttributeLookupMethodExpression other = (AttributeLookupMethodExpression) otherObj;
                result = lookupExpression.getExpressionString().equals(other.lookupExpression.getExpressionString());
            }
            return result;

        }

        @Override
        public boolean isLiteralText() {

            return lookupExpression.isLiteralText();

        }

        @Override
        public int hashCode() {

            return lookupExpression.hashCode();

        }

    } // END AttributeLookupMethodExpression
}
