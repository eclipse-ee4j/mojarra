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

package com.sun.faces.mgbean;

import com.sun.faces.RIConstants;
import com.sun.faces.util.MessageUtils;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import jakarta.el.ExpressionFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This builder creates standard managed beans, i.e. beans that aren't List or Map instances.
 * </p>
 */
public class ManagedBeanBuilder extends BeanBuilder {

    private List<BakedProperty> properties;

    private enum PropertyType {
        MAP, LIST, BEAN
    }

    // ------------------------------------------------------------ Constructors

    public ManagedBeanBuilder(ManagedBeanInfo beanInfo) {

        super(beanInfo);

    }

    // ------------------------------------------------ Methods from BeanBuilder

    @Override
    void bake() {
        if (!isBaked()) {
            super.bake();
            if (beanInfo.hasManagedProperties()) {
                properties = new ArrayList<>(beanInfo.getManagedProperties().size());
                String propertyName = null;
                try {
                    for (ManagedBeanInfo.ManagedProperty property : beanInfo.getManagedProperties()) {
                        propertyName = property.getPropertyName();
                        switch (getPropertyType(property)) {
                        case MAP:
                            bakeMapProperty(property);
                            break;
                        case LIST:
                            bakeListProperty(property);
                            break;
                        default:
                            bakeBeanProperty(property);
                        }
                    }
                } catch (Exception e) {
                    if (e instanceof ManagedBeanPreProcessingException) {
                        throw (ManagedBeanPreProcessingException) e;
                    } else {
                        String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_PROPERTY_UNKNOWN_PROCESSING_ERROR_ID, propertyName);
                        throw new ManagedBeanPreProcessingException(message, e, ManagedBeanPreProcessingException.Type.UNCHECKED);
                    }
                }
            }
            baked();
            Introspector.flushFromCaches(getBeanClass());
        }
    }

    @Override
    protected void buildBean(Object bean, FacesContext context) {

        if (properties != null) {
            for (BakedProperty property : properties) {
                property.set(bean, context);
            }
        }

    }

    // --------------------------------------------------------- Private Methods

    private PropertyType getPropertyType(ManagedBeanInfo.ManagedProperty property) {

        if (property.hasListEntry()) {
            if (property.hasMapEntry() || property.hasPropertyValue()) {
                String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_LIST_PROPERTY_CONFIG_ERROR_ID, property.getPropertyName(),
                        beanInfo.getName());
                throw new ManagedBeanPreProcessingException(message);
            }

            return PropertyType.LIST;
        }
        if (property.hasMapEntry()) {

            if (property.hasPropertyValue()) {
                String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_MAP_PROPERTY_CONFIG_ERROR_ID, property.getPropertyName(),
                        beanInfo.getName());
                throw new ManagedBeanPreProcessingException(message);
            }
            return PropertyType.MAP;
        }

        return PropertyType.BEAN;

    }

    private void bakeMapProperty(ManagedBeanInfo.ManagedProperty property) {

        ManagedBeanInfo.MapEntry rawEntry = property.getMapEntry();
        Map<Expression, Expression> mapEntries = getBakedMap(rawEntry.getKeyClass(), rawEntry.getValueClass(), rawEntry.getEntries());

        // Find property setter and validate.
        PropertyDescriptor pd = getPropertyDescriptor(property.getPropertyName());
        if (pd == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_PROPERTY_DOES_NOT_EXIST_ERROR_ID, property.getPropertyName(),
                    beanInfo.getName());
            queueMessage(message);
        } else {
            if (pd.getWriteMethod() != null) {
                Class<?>[] params = pd.getWriteMethod().getParameterTypes();
                if (!Map.class.isAssignableFrom(params[0])) {
                    String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_MAP_PROPERTY_INCORRECT_SETTER_ERROR_ID,
                            property.getPropertyName(), beanInfo.getName());
                    queueMessage(message);
                }
            } else {
                // no write method, let's hope there is a read method that returns
                // a non-null map
                if (pd.getReadMethod() == null) {
                    String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_PROPERTY_DOES_NOT_EXIST_ERROR_ID,
                            property.getPropertyName(), beanInfo.getName());
                    queueMessage(message);
                } else {
                    Class<?> returnType = pd.getReadMethod().getReturnType();
                    if (!Map.class.isAssignableFrom(returnType)) {
                        String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_MAP_PROPERTY_INCORRECT_GETTER_ERROR_ID,
                                property.getPropertyName(), beanInfo.getName());
                        queueMessage(message);
                    }
                }
            }
        }

        if (!this.hasMessages()) {
            // all clear - create the BakedMapProperty and add it to the properties
            // map
            BakedMapProperty baked = new BakedMapProperty(mapEntries, pd);
            properties.add(baked);
        }

    }

    private void bakeListProperty(ManagedBeanInfo.ManagedProperty property) {

        ManagedBeanInfo.ListEntry rawEntry = property.getListEntry();
        List<Expression> listEntry = getBakedList(rawEntry.getValueClass(), rawEntry.getValues());

        PropertyDescriptor pd = getPropertyDescriptor(property.getPropertyName());
        if (pd == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_PROPERTY_DOES_NOT_EXIST_ERROR_ID, property.getPropertyName(),
                    beanInfo.getName());
            queueMessage(message);
        } else {
            if (pd.getReadMethod() == null) {
                // a null read method means we create a new List or
                // array and pass it to the bean. Validate that the
                // setter takes either an array or List.
                if (pd.getWriteMethod() == null) {
                    String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_PROPERTY_DOES_NOT_EXIST_ERROR_ID,
                            property.getPropertyName(), beanInfo.getName());
                    queueMessage(message);
                } else {
                    Class<?>[] params = pd.getWriteMethod().getParameterTypes();
                    if (params.length != 1) {
                        String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_PROPERTY_INCORRECT_ARGS_ERROR_ID,
                                property.getPropertyName(), beanInfo.getName());
                        queueMessage(message);
                    } else {
                        if (!params[0].isArray() && !List.class.isAssignableFrom(params[0])) {
                            String message = MessageUtils.getExceptionMessageString(
                                    MessageUtils.MANAGED_BEAN_LIST_SETTER_DOES_NOT_ACCEPT_LIST_OR_ARRAY_ERROR_ID, property.getPropertyName(),
                                    beanInfo.getName());
                            queueMessage(message);
                        }
                    }
                }
            } else {
                // a getter exists. ensure it returns a List or array.
                // if it returns an array, ensure a setter exists
                Class<?> retType = pd.getReadMethod().getReturnType();
                if (retType.isArray()) {
                    if (pd.getWriteMethod() == null) {
                        String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_LIST_GETTER_ARRAY_NO_SETTER_ERROR_ID,
                                property.getPropertyName(), beanInfo.getName());
                        queueMessage(message);
                    }
                    // validate setter
                } else {
                    if (!List.class.isAssignableFrom(retType)) {
                        String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_LIST_GETTER_DOES_NOT_RETURN_LIST_OR_ARRAY_ERROR_ID,
                                property.getPropertyName(), beanInfo.getName());
                        queueMessage(message);
                    }
                }
            }

        }

        if (!this.hasMessages()) {
            BakedListProperty baked = new BakedListProperty(listEntry, pd);
            properties.add(baked);
        }

    }

    private void bakeBeanProperty(ManagedBeanInfo.ManagedProperty property) {

        String className = property.getPropertyClass();
        PropertyDescriptor pd = getPropertyDescriptor(property.getPropertyName());

        if (pd == null || pd.getWriteMethod() == null) {
            if (!UIComponent.class.isAssignableFrom(getBeanClass())) {
                String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_PROPERTY_DOES_NOT_EXIST_ERROR_ID, property.getPropertyName(),
                        beanInfo.getName());
                queueMessage(message);
            }
        } else {
            Method method = pd.getWriteMethod();
            Class<?>[] param = method.getParameterTypes();
            if (param.length != 1) {
                String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_PROPERTY_INCORRECT_ARGS_ERROR_ID, property.getPropertyName(),
                        beanInfo.getName());
                queueMessage(message);
            }
        }

        Expression value = null;
        if (pd != null) {
            Class<?> propertyClass;
            if (className != null) {
                propertyClass = loadClass(className);
            } else {
                propertyClass = pd.getPropertyType();
            }

            if (className != null) {
                if (!pd.getWriteMethod().getParameterTypes()[0].isAssignableFrom(propertyClass)) {
                    String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_DEFINED_PROPERTY_CLASS_NOT_COMPATIBLE_ERROR_ID,
                            property.getPropertyName(), beanInfo.getName(), property.getPropertyClass());
                    queueMessage(message);
                }
            }

            String propertyValue = property.getPropertyValue();
            if (!ManagedBeanInfo.NULL_VALUE.equals(propertyValue)) {
                value = new Expression(propertyValue, propertyClass);
            }
        } else {
            String propertyValue = property.getPropertyValue();
            if (!ManagedBeanInfo.NULL_VALUE.equals(propertyValue)) {
                value = new Expression(propertyValue, String.class);
            }
        }

        if (!this.hasMessages()) {
            BakedBeanProperty baked = new BakedBeanProperty(property.getPropertyName(), pd, value);
            properties.add(baked);
        }

    }

    private PropertyDescriptor getPropertyDescriptor(String propertyName) {

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(getBeanClass());
            PropertyDescriptor pds[] = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if (propertyName.equals(pd.getName())) {
                    return pd;
                }
            }
        } catch (IntrospectionException ie) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_INTROSPECTION_ERROR_ID, beanInfo.getName());
            throw new ManagedBeanPreProcessingException(message);
        }

        return null;

    }

    // ----------------------------------------------------------- Inner Classes

    private static interface BakedProperty {

        abstract void set(Object bean, FacesContext context);

    } // END BakedProperty

    private class BakedMapProperty implements BakedProperty {

        Map<Expression, Expression> mapEntries;
        PropertyDescriptor pd;

        BakedMapProperty(Map<Expression, Expression> mapEntries, PropertyDescriptor pd) {

            this.mapEntries = mapEntries;
            this.pd = pd;

        }

        @Override
        public void set(Object bean, FacesContext context) {

            Method readMethod = pd.getReadMethod();
            Map target = null;
            boolean mapReturned = false;
            if (readMethod != null) {
                // see if a Map already exists, if so, we'll
                // add the config entries to the existing
                try {
                    target = (Map) readMethod.invoke(bean, RIConstants.EMPTY_METH_ARGS);
                    mapReturned = (target != null);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ignored) {
                    // ignored
                }
            }
            if (target == null) {
                // noinspection CollectionWithoutInitialCapacity
                target = new HashMap();
            }
            initMap(mapEntries, target, context);

            if (!mapReturned) {
                Method writeMethod = pd.getWriteMethod();
                try {
                    writeMethod.invoke(bean, target);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_UNABLE_TO_SET_PROPERTY_ERROR_ID, pd.getName(),
                            beanInfo.getName());
                    throw new ManagedBeanPreProcessingException(message, e);
                }
            }
        }

    } // END BakedMapProperty

    private class BakedListProperty implements BakedProperty {

        private List<Expression> listEntries;
        private PropertyDescriptor pd;

        BakedListProperty(List<Expression> listEntries, PropertyDescriptor pd) {

            this.listEntries = listEntries;
            this.pd = pd;

        }

        @Override
        public void set(Object bean, FacesContext context) {

            // check to see if there is a value returned by
            // the getter, if any.
            Method readMethod = pd.getReadMethod();
            Object temp = null;
            if (readMethod != null) {
                try {
                    temp = readMethod.invoke(bean, RIConstants.EMPTY_METH_ARGS);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ignored) {
                    // ignored
                }
            }

            List target = null;
            if (temp != null) {
                if (temp.getClass().isArray()) {
                    for (int i = 0, len = Array.getLength(temp); i < len; i++) {
                        if (target == null) {
                            target = new ArrayList(len);
                        }
                        // noinspection unchecked
                        target.add(Array.get(temp, i));
                    }
                } else {
                    target = (List) temp;
                }
            }
            if (target == null) {
                // noinspection CollectionWithoutInitialCapacity
                target = new ArrayList();
            }
            ExpressionFactory expFactory = context.getApplication().getExpressionFactory();
            initList(listEntries, target, context);

            // handle the case where the getter returned a non-null value
            if (temp != null && !temp.getClass().isArray()) {
                // the returned object was a List - no action
                // necessary
                return;
            } else if (temp != null) {

                // getter returned an array. Converter the List
                // 'target' to an array and call the setter
                Class<?> arrayType = temp.getClass().getComponentType();
                Object result = Array.newInstance(arrayType, target.size());
                for (int i = 0, len = target.size(); i < len; i++) {
                    Array.set(result, i, expFactory.coerceToType(target.get(i), arrayType));
                }
                try {
                    pd.getWriteMethod().invoke(bean, result);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_UNABLE_TO_SET_PROPERTY_ERROR_ID, pd.getName(),
                            beanInfo.getName());
                    throw new ManagedBeanCreationException(message, e);
                }
            } else {
                // no value returned from the getter.
                Method writeMethod = pd.getWriteMethod();
                Class<?>[] param = writeMethod.getParameterTypes();

                if (param[0].isArray()) {
                    Class<?> arrayType = param[0].getComponentType();
                    Object result = Array.newInstance(arrayType, target.size());
                    for (int i = 0, len = target.size(); i < len; i++) {
                        Array.set(result, i, expFactory.coerceToType(target.get(i), arrayType));
                    }
                    try {
                        writeMethod.invoke(bean, result);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_UNABLE_TO_SET_PROPERTY_ERROR_ID, pd.getName(),
                                beanInfo.getName());
                        throw new ManagedBeanCreationException(message, e);
                    }
                } else {
                    try {
                        writeMethod.invoke(bean, target);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_UNABLE_TO_SET_PROPERTY_ERROR_ID, pd.getName(),
                                beanInfo.getName());
                        throw new ManagedBeanCreationException(message, e);
                    }
                }
            }
        }

    } // END BakedListProperty

    private class BakedBeanProperty implements BakedProperty {

        private String propertyName;
        private PropertyDescriptor pd;
        private Expression value;

        BakedBeanProperty(String propertyName, PropertyDescriptor pd, Expression value) {

            this.propertyName = propertyName;
            this.pd = pd;
            this.value = value;

        }

        @Override
        public void set(Object bean, FacesContext context) {

            if (pd != null) {
                Method writeMethod = pd.getWriteMethod();
                try {
                    writeMethod.invoke(bean, ((value != null) ? value.evaluate(context.getELContext()) : null));
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    String message = MessageUtils.getExceptionMessageString(MessageUtils.MANAGED_BEAN_UNABLE_TO_SET_PROPERTY_ERROR_ID, pd.getName(),
                            beanInfo.getName());
                    throw new ManagedBeanCreationException(message, e);
                }
            } else {
                // no PropertyDescriptor means this bean is a UIComponent
                ((UIComponent) bean).getAttributes().put(propertyName, ((value != null) ? value.evaluate(context.getELContext()) : ""));
            }

        }

    } // END BakedBeanProperty

}
