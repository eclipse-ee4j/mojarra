/*
 * Copyright (c) 2023 Contributors to Eclipse Foundation.
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

package com.sun.faces.el;

import static com.sun.faces.component.CompositeComponentStackManager.StackType.Evaluation;
import static com.sun.faces.component.CompositeComponentStackManager.StackType.TreeCreation;
import static com.sun.faces.util.Util.notNull;

import com.sun.faces.component.CompositeComponentStackManager;
import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.CompositeComponentExpressionHolder;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * This {@link ELResolver} will handle the resolution of <code>attrs</code> when processing a composite component
 * instance.
 * </p>
 */
public class CompositeComponentAttributesELResolver extends ELResolver {

    /**
     * Implicit object related only to the cc implicitObject.
     */
    private static final String COMPOSITE_COMPONENT_ATTRIBUTES_NAME = "attrs";

    /**
     * Implicit object related only to the cc implicit object and refers to the composite component parent (if any).
     */
    private static final String COMPOSITE_COMPONENT_PARENT_NAME = "parent";

    /**
     * Key to which we store the mappings between composite component instances and their ExpressionEvalMap.
     */
    private static final String EVAL_MAP_KEY = CompositeComponentAttributesELResolver.class.getName() + "_EVAL_MAP";

    // ------------------------------------------------- Methods from ELResolver

    /**
     * <p>
     * If <code>base</code> is a composite component and <code>property</code> is <code>attrs</code>, return a new
     * <code>ExpressionEvalMap</code> which wraps the composite component's attributes map.
     * </p>
     *
     * <p>
     * The <code>ExpressionEvalMap</code> simple evaluates any {@link ValueExpression} instances stored in the composite
     * component's attribute map and returns the result.
     * </p>
     *
     * <p>
     * If <code>base</code> is a composite component and <code>property</code> is <code>parent</code> attempt to resolve the
     * composite componet parent of the current composite component by calling
     * {@link UIComponent#getCompositeComponentParent(jakarta.faces.component.UIComponent)}) and returning that value.
     * </p>
     *
     * @see jakarta.el.ELResolver#getValue(jakarta.el.ELContext, Object, Object)
     * @see com.sun.faces.el.CompositeComponentAttributesELResolver.ExpressionEvalMap
     */
    @Override
    public Object getValue(ELContext context, Object base, Object property) {

        notNull("context", context);

        if (base instanceof UIComponent && UIComponent.isCompositeComponent((UIComponent) base) && property != null) {

            String propertyName = property.toString();
            if (COMPOSITE_COMPONENT_ATTRIBUTES_NAME.equals(propertyName)) {
                UIComponent c = (UIComponent) base;
                context.setPropertyResolved(true);
                FacesContext ctx = (FacesContext) context.getContext(FacesContext.class);
                return getEvalMapFor(c, ctx);
            }

            if (COMPOSITE_COMPONENT_PARENT_NAME.equals(propertyName)) {
                UIComponent c = (UIComponent) base;
                context.setPropertyResolved(true);
                FacesContext ctx = (FacesContext) context.getContext(FacesContext.class);
                CompositeComponentStackManager m = CompositeComponentStackManager.getManager(ctx);
                UIComponent ccp = m.getParentCompositeComponent(TreeCreation, ctx, c);
                if (ccp == null) {
                    ccp = m.getParentCompositeComponent(Evaluation, ctx, c);
                }
                return ccp;
            }
        }

        return null;

    }

    /**
     * <p>
     * Readonly, so return <code>null</code>.
     * </p>
     *
     * @see ELResolver#getType(jakarta.el.ELContext, Object, Object)
     */
    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        notNull("context", context);
        if (!(base instanceof ExpressionEvalMap && property instanceof String)) {
            return null;
        }
        Class<?> exprType = null;
        Class<?> metaType = null;

        ExpressionEvalMap evalMap = (ExpressionEvalMap) base;
        ValueExpression ve = evalMap.getExpression((String) property);
        if (ve != null) {
            exprType = ve.getType(context);
        }

        if (!"".equals(property)) {
            FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
            UIComponent cc = UIComponent.getCurrentCompositeComponent(facesContext);
            BeanInfo metadata = (BeanInfo) cc.getAttributes().get(UIComponent.BEANINFO_KEY);
            assert null != metadata;
            PropertyDescriptor[] attributes = metadata.getPropertyDescriptors();
            if (null != attributes) {
                for (PropertyDescriptor cur : attributes) {
                    if (property.equals(cur.getName())) {
                        Object type = cur.getValue("type");
                        if (null != type) {
                            assert type instanceof Class;
                            metaType = (Class<?>) type;
                            break;
                        }
                    }
                }
            }
        }
        if (metaType != null) {
            // override exprType only if metaType is narrower:
            if (exprType == null || exprType.isAssignableFrom(metaType)) {
                context.setPropertyResolved(true);
                return metaType;
            }
        }
        return exprType;
    }

    /**
     * <p>
     * This is a no-op.
     * </p>
     *
     * @see ELResolver#setValue(jakarta.el.ELContext, Object, Object, Object)
     */
    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {

        notNull("context", context);

    }

    /**
     * <p>
     * Readonly, so return <code>true</code>
     * </p>
     *
     * @see jakarta.el.ELResolver#isReadOnly(jakarta.el.ELContext, Object, Object)
     */
    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {

        notNull("context", context);
        return true;

    }

    /**
     * <p>
     * <code>attrs</code> is considered a <code>String</code> property.
     * </p>
     *
     * @see jakarta.el.ELResolver#getCommonPropertyType(jakarta.el.ELContext, Object)
     */
    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        notNull("context", context);
        return String.class;
    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>
     * Creates (if necessary) and caches an <code>ExpressionEvalMap</code> instance associated with the owning
     * {@link UIComponent}
     * </p>
     *
     * @param c the owning {@link UIComponent}
     * @param ctx the {@link FacesContext} for the current request
     * @return an <code>ExpressionEvalMap</code> for the specified component
     */
    public Map<String, Object> getEvalMapFor(UIComponent c, FacesContext ctx) {

        Map<Object, Object> ctxAttributes = ctx.getAttributes();
        // noinspection unchecked
        Map<UIComponent, Map<String, Object>> topMap = (Map<UIComponent, Map<String, Object>>) ctxAttributes.get(EVAL_MAP_KEY);
        Map<String, Object> evalMap = null;
        if (topMap == null) {
            topMap = new HashMap<>();
            ctxAttributes.put(EVAL_MAP_KEY, topMap);
            evalMap = new ExpressionEvalMap(ctx, c);
            topMap.put(c, evalMap);
        } else {
            evalMap = topMap.get(c);
            if (evalMap == null) {
                evalMap = new ExpressionEvalMap(ctx, c);
                topMap.put(c, evalMap);
            } else {
                // JAVASERVERFACES-2508 - running as Portlet2 FacesContext must be updated for rendering, or
                // ExpressionEvalMap would have to be reconstructed for the second Portlet phase
                ((ExpressionEvalMap) evalMap).updateFacesContext(ctx);
            }
        }

        return evalMap;

    }

    // ---------------------------------------------------------- Nested Classes

    /**
     * Simple Map implementation to evaluate any <code>ValueExpression</code> stored directly within the provided attributes
     * map.
     */
    private static final class ExpressionEvalMap implements Map<String, Object>, CompositeComponentExpressionHolder {

        private final Map<String, Object> attributesMap;
        private PropertyDescriptor[] declaredAttributes;
        private Map<Object, Object> declaredDefaultValues;
        private FacesContext ctx;
        private final UIComponent cc;

        // -------------------------------------------------------- Constructors

        ExpressionEvalMap(FacesContext ctx, UIComponent cc) {

            this.cc = cc;
            attributesMap = cc.getAttributes();
            BeanInfo metadata = (BeanInfo) attributesMap.get(UIComponent.BEANINFO_KEY);
            if (null != metadata) {
                declaredAttributes = metadata.getPropertyDescriptors();
                declaredDefaultValues = new HashMap<>(5);
            }
            this.ctx = ctx;

        }

        // --------------------- Methods from CompositeComponentExpressionHolder

        @Override
        public ValueExpression getExpression(String name) {
            Object ve = cc.getValueExpression(name);
            return ve instanceof ValueExpression ? (ValueExpression) ve : null;
        }

        // ---------------------------------------------------- Methods from Map

        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(Object key) {
            boolean result = attributesMap.containsKey(key);
            if (!result) {
                result = null != getDeclaredDefaultValue(key);
            }
            return result;
        }

        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object get(Object key) {
            Object v = attributesMap.get(key);
            if (v == null) {
                v = getDeclaredDefaultValue(key);
                if (v != null) {
                    return ((ValueExpression) v).getValue(ctx.getELContext());
                }
            }
            if (v instanceof MethodExpression) {
                return v;
            }
            return v;
        }

        @Override
        public Object put(String key, Object value) {
            // Unlike AttributesMap.get() which will obtain a value from
            // a ValueExpression, AttributesMap.put(), when passed a value,
            // will never call ValueExpression.setValue(), so we have to take
            // matters into our own hands...
            ValueExpression ve = cc.getValueExpression(key);
            if (ve != null) {
                ve.setValue(ctx.getELContext(), value);
            } else {
                attributesMap.put(key, value);
            }
            return null;
        }

        @Override
        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends String, ?> t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> keySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<Object> values() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Map.Entry<String, Object>> entrySet() {
            throw new UnsupportedOperationException();
        }

        private Object getDeclaredDefaultValue(Object key) {
            Object result = null;

            // If it's not in the cache...
            if (!declaredDefaultValues.containsKey(key)) {
                // iterate through the property descriptors...
                boolean found = false;
                for (PropertyDescriptor cur : declaredAttributes) {
                    // and if you find a match...
                    if (cur.getName().equals(key)) {
                        found = true;
                        // put it in the cache, returning the value.
                        declaredDefaultValues.put(key, result = cur.getValue("default"));
                        break;
                    }
                }
                // Otherwise, if no attribute was declared
                if (!found) {
                    // put null into the cache for future lookups.
                    declaredDefaultValues.put(key, null);
                }
            } else {
                // It's in the cache, just return the value.
                result = declaredDefaultValues.get(key);
            }

            return result;
        }

        public void updateFacesContext(FacesContext ctx) {
            if (this.ctx != ctx) {
                this.ctx = ctx;
            }
        }
    }
}
