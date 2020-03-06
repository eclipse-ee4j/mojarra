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

package com.sun.faces.facelets.tag;

import com.sun.faces.facelets.el.LegacyMethodBinding;

import jakarta.faces.el.MethodBinding;
import jakarta.faces.view.facelets.*;

import jakarta.el.MethodExpression;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Optional Rule for binding Method[Binding|Expression] properties
 * 
 * @author Mike Kienenberger
 * @author Jacob Hookom
 */
public final class MethodRule extends MetaRule {

    private final String methodName;

    private final Class returnTypeClass;

    private final Class[] params;

    public MethodRule(String methodName, Class returnTypeClass, Class[] params) {
        this.methodName = methodName;
        this.returnTypeClass = returnTypeClass;
        this.params = params;
    }

    @Override
    public Metadata applyRule(String name, TagAttribute attribute,
            MetadataTarget meta) {
        if (!name.equals(this.methodName))
            return null;

        if (MethodBinding.class.equals(meta.getPropertyType(name))) {
            Method method = meta.getWriteMethod(name);
            if (method != null) {
                return new MethodBindingMetadata(method, attribute,
                        this.returnTypeClass, this.params);
            }
        } else if (MethodExpression.class.equals(meta.getPropertyType(name))) {
            Method method = meta.getWriteMethod(name);
            if (method != null) {
                return new MethodExpressionMetadata(method, attribute,
                        this.returnTypeClass, this.params);
            }
        }

        return null;
    }

    private static class MethodBindingMetadata extends Metadata {
        private final Method _method;

        private final TagAttribute _attribute;

        private Class[] _paramList;

        private Class _returnType;

        public MethodBindingMetadata(Method method, TagAttribute attribute,
                Class returnType, Class[] paramList) {
            _method = method;
            _attribute = attribute;
            _paramList = paramList;
            _returnType = returnType;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            MethodExpression expr = _attribute.getMethodExpression(ctx,
                    _returnType, _paramList);

            try {
                _method.invoke(instance, new LegacyMethodBinding(expr) );
            } catch (InvocationTargetException e) {
                throw new TagAttributeException(_attribute, e.getCause());
            } catch (Exception e) {
                throw new TagAttributeException(_attribute, e);
            }
        }
    }

    private static class MethodExpressionMetadata extends Metadata {
        private final Method _method;

        private final TagAttribute _attribute;

        private Class[] _paramList;

        private Class _returnType;

        public MethodExpressionMetadata(Method method, TagAttribute attribute,
                Class returnType, Class[] paramList) {
            _method = method;
            _attribute = attribute;
            _paramList = paramList;
            _returnType = returnType;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            MethodExpression expr = _attribute.getMethodExpression(ctx,
                    _returnType, _paramList);

            try {
                _method.invoke(instance, expr );
            } catch (InvocationTargetException e) {
                throw new TagAttributeException(_attribute, e.getCause());
            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new TagAttributeException(_attribute, e);
            }
        }
    }
}
