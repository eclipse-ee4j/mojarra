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

import com.sun.faces.facelets.tag.TagHandlerImpl;

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.*;

import jakarta.el.ValueExpression;

import java.io.IOException;
import java.util.Iterator;

/**
 * Simplified implementation of c:set
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public class SetHandler extends TagHandlerImpl {

    private final TagAttribute var;

    private final TagAttribute value;

    private final TagAttribute target;

    private final TagAttribute property;

    private final TagAttribute scope;

    public SetHandler(TagConfig config) {
        super(config);
        this.value = this.getAttribute("value");
        this.var = this.getAttribute("var");
        this.target = this.getAttribute("target");
        this.property = this.getAttribute("property");
        this.scope = this.getAttribute("scope");

    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {

        StringBuilder bodyValue = new StringBuilder();

        Iterator iter = TagHandlerImpl.findNextByType(this.nextHandler, TextHandler.class);
        while (iter.hasNext()) {
            TextHandler text = (TextHandler) iter.next();
            bodyValue.append(text.getText(ctx));
        }

        // true if either a value in body or value attr
        boolean valSet = bodyValue.length() > 0 || (value != null && value.getValue().length() > 0);

        // Apply precedence algorithm for attributes. The JstlCoreTLV doesn't
        // seem to enforce much in the way of this, so I edburns needs to check
        // with an authority on the matter, probabyl Kin-Man Chung

        ValueExpression veObj;
        ValueExpression lhs;
        String expr;

        if (this.value != null) {
            veObj = this.value.getValueExpression(ctx, Object.class);
        } else {

            veObj = ctx.getExpressionFactory().createValueExpression(ctx.getFacesContext().getELContext(), bodyValue.toString(), Object.class);
        }

        // Otherwise, if var is set, ignore the other attributes
        if (this.var != null) {
            String scopeStr, varStr = this.var.getValue(ctx);

            // If scope is set, check for validity
            if (null != this.scope) {
                if (0 == this.scope.getValue().length()) {
                    throw new TagException(tag, "zero length scope attribute set");
                }

                if (this.scope.isLiteral()) {
                    scopeStr = this.scope.getValue();
                } else {
                    scopeStr = this.scope.getValue(ctx);
                }
                if (scopeStr.equals("page")) {
                    throw new TagException(tag, "page scope does not exist in JSF, consider using view scope instead.");
                }
                if (scopeStr.equals("request") || scopeStr.equals("session") || scopeStr.equals("application") || scopeStr.equals("view")) {
                    scopeStr = scopeStr + "Scope";
                }
                // otherwise, assume it's a valid scope. With custom scopes,
                // it may be.
                // Conjure up an expression
                expr = "#{" + scopeStr + "." + varStr + "}";
                lhs = ctx.getExpressionFactory().createValueExpression(ctx, expr, Object.class);
                lhs.setValue(ctx, veObj.getValue(ctx));
            } else {
                ctx.getVariableMapper().setVariable(varStr, veObj);
            }
        } else {

            // Otherwise, target, property and value must be set
            if ((null == this.target || null == this.target.getValue() || this.target.getValue().length() <= 0)
                    || (null == this.property || null == this.property.getValue() || this.property.getValue().length() <= 0) || !valSet) {

                throw new TagException(tag, "when using this tag either one of var and value, or (target, property, value) must be set.");
            }
            // Ensure that target is an expression
            if (this.target.isLiteral()) {
                throw new TagException(tag, "value of target attribute must be an expression");
            }
            // Get the value of property
            String propertyStr = null;
            if (this.property.isLiteral()) {
                propertyStr = this.property.getValue();
            } else {
                propertyStr = this.property.getValue(ctx);
            }
            ValueExpression targetVe = this.target.getValueExpression(ctx, Object.class);
            Object targetValue = targetVe.getValue(ctx);
            ctx.getFacesContext().getELContext().getELResolver().setValue(ctx, targetValue, propertyStr, veObj.getValue(ctx));

        }
    }

    // Swallow children - if they're text, we've already handled them.
    protected void applyNextHandler(FaceletContext ctx, UIComponent c) {
    }
}
