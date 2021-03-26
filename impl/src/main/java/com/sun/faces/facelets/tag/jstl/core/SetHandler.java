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

import java.io.IOException;
import java.util.Iterator;

import com.sun.faces.facelets.tag.TagHandlerImpl;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;
import jakarta.faces.view.facelets.TextHandler;

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
        value = getAttribute("value");
        var = getAttribute("var");
        target = getAttribute("target");
        property = getAttribute("property");
        scope = getAttribute("scope");

    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {

        StringBuilder bodyValue = new StringBuilder();

        Iterator iter = TagHandlerImpl.findNextByType(nextHandler, TextHandler.class);
        while (iter.hasNext()) {
            TextHandler text = (TextHandler) iter.next();
            bodyValue.append(text.getText(ctx));
        }

        // true if either a value in body or value attr
        boolean valSet = bodyValue.length() > 0 || value != null && value.getValue().length() > 0;

        // Apply precedence algorithm for attributes. The JstlCoreTLV doesn't
        // seem to enforce much in the way of this, so I edburns needs to check
        // with an authority on the matter, probabyl Kin-Man Chung

        ValueExpression veObj;
        ValueExpression lhs;
        String expr;

        if (value != null) {
            veObj = value.getValueExpression(ctx, Object.class);
        } else {

            veObj = ctx.getExpressionFactory().createValueExpression(ctx.getFacesContext().getELContext(), bodyValue.toString(), Object.class);
        }

        // Otherwise, if var is set, ignore the other attributes
        if (var != null) {
            String scopeStr, varStr = var.getValue(ctx);

            // If scope is set, check for validity
            if (null != scope) {
                if (0 == scope.getValue().length()) {
                    throw new TagException(tag, "zero length scope attribute set");
                }

                if (scope.isLiteral()) {
                    scopeStr = scope.getValue();
                } else {
                    scopeStr = scope.getValue(ctx);
                }
                if (scopeStr.equals("page")) {
                    throw new TagException(tag, "page scope does not exist in Faces, consider using view scope instead.");
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
            if (null == target || null == target.getValue() || target.getValue().length() <= 0
                    || null == property || null == property.getValue() || property.getValue().length() <= 0 || !valSet) {

                throw new TagException(tag, "when using this tag either one of var and value, or (target, property, value) must be set.");
            }
            // Ensure that target is an expression
            if (target.isLiteral()) {
                throw new TagException(tag, "value of target attribute must be an expression");
            }
            // Get the value of property
            String propertyStr = null;
            if (property.isLiteral()) {
                propertyStr = property.getValue();
            } else {
                propertyStr = property.getValue(ctx);
            }
            ValueExpression targetVe = target.getValueExpression(ctx, Object.class);
            Object targetValue = targetVe.getValue(ctx);
            ctx.getFacesContext().getELContext().getELResolver().setValue(ctx, targetValue, propertyStr, veObj.getValue(ctx));

        }
    }

    // Swallow children - if they're text, we've already handled them.
    protected void applyNextHandler(FaceletContext ctx, UIComponent c) {
    }
}
