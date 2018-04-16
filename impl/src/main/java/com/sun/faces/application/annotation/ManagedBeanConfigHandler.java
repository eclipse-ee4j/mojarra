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

package com.sun.faces.application.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import javax.faces.bean.*;
import javax.faces.context.FacesContext;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.mgbean.BeanManager;
import com.sun.faces.mgbean.ManagedBeanInfo;

/**
 * <p>
 * <code>ConfigAnnotationHandler</code> for {@link ManagedBean} annotated
 * classes.
 * </p>
 */
public class ManagedBeanConfigHandler implements ConfigAnnotationHandler {

    private static final Class<?>[] SCOPES = {
          RequestScoped.class,
          ViewScoped.class,
          SessionScoped.class,
          ApplicationScoped.class,
          NoneScoped.class,
          CustomScoped.class
    };

    private static final Collection<Class<? extends Annotation>> HANDLES;
    static {
        Collection<Class<? extends Annotation>> handles =
              new ArrayList<>(2);
        handles.add(ManagedBean.class);
        HANDLES = Collections.unmodifiableCollection(handles);
    }

    private Map<Class<?>,Annotation> managedBeans;


    // ------------------------------------ Methods from ConfigAnnotationHandler


    /**
     * @see com.sun.faces.application.annotation.ConfigAnnotationHandler#getHandledAnnotations()
     */
    @Override
    public Collection<Class<? extends Annotation>> getHandledAnnotations() {

        return HANDLES;

    }


    /**
     * @see com.sun.faces.application.annotation.ConfigAnnotationHandler#collect(Class, java.lang.annotation.Annotation)
     */
    @Override
    public void collect(Class<?> target, Annotation annotation) {

        if (managedBeans == null) {
            managedBeans = new HashMap<>();
        }
        managedBeans.put(target, annotation);

    }


    /**
     * @see com.sun.faces.application.annotation.ConfigAnnotationHandler#push(javax.faces.context.FacesContext)
     */
    @Override
    public void push(FacesContext ctx) {

        if (managedBeans != null) {
            ApplicationAssociate associate =
                  ApplicationAssociate.getInstance(ctx.getExternalContext());
            if (associate != null) {
                BeanManager manager = associate.getBeanManager();
                for (Map.Entry<Class<?>,Annotation> entry : managedBeans.entrySet()) {
                    process(manager, entry.getKey(), entry.getValue());
                }
            }
        }
    }


    // --------------------------------------------------------- Private Methods


    private void process(BeanManager manager,
                         Class<?> annotatedClass,
                         Annotation annotation) {

            manager.register(getBeanInfo(annotatedClass,
                                         (ManagedBean) annotation));
    }


    private ManagedBeanInfo getBeanInfo(Class<?> annotatedClass,
                                        ManagedBean metadata) {

        String name = getName(metadata, annotatedClass);
        String scope = getScope(annotatedClass);
        boolean eager = metadata.eager();
        
        Map<String,Field> annotatedFields = new LinkedHashMap<>();
        //Map<String, Method> annotatedMethods = new LinkedHashMap<String,Method>();
        collectAnnotatedFields(annotatedClass, annotatedFields);
        //collectAnnotatedMethods(annotatedClass,
        //                        annotatedMethods,
        //                        annotatedFields.keySet());

        List<ManagedBeanInfo.ManagedProperty> properties = null;

        if (!annotatedFields.isEmpty()) {
            properties = new ArrayList<>(annotatedFields.size());
            for (Map.Entry<String,Field> entry : annotatedFields.entrySet()) {
                Field f = entry.getValue();
                ManagedProperty property = f.getAnnotation(ManagedProperty.class);
                ManagedBeanInfo.ManagedProperty propertyInfo =
                          new ManagedBeanInfo.ManagedProperty(entry.getKey(),
                                                              f.getType().getName(),
                                                              property.value(),
                                                              null,
                                                              null);
                properties.add(propertyInfo);
            }
        }
        /*
        if (!annotatedMethods.isEmpty()) {
            if (properties == null) {
                properties = new ArrayList<ManagedBeanInfo.ManagedProperty>(annotatedMethods.size());
                for (Map.Entry<String,Method> entry : annotatedMethods.entrySet()) {
                    Method m = entry.getValue();
                    ManagedProperty property = m.getAnnotation(ManagedProperty.class);
                    String alias = property.name();
                    if (alias != null && alias.length() == 0) {
                        alias = null;
                    }
                    ManagedBeanInfo.ManagedProperty propertyInfo =
                          new ManagedBeanInfo.ManagedProperty(alias,
                                                              entry.getKey(),
                                                              m.getParameterTypes()[0].getName(),
                                                              property.value(),
                                                              null,
                                                              null);
                    properties.add(propertyInfo);
                }
            }
        }
        */

        return new ManagedBeanInfo(name,
                                   annotatedClass.getName(),
                                   scope,
                                   eager,
                                   null,
                                   null,
                                   properties,
                                   null);

    }


//    private void collectAnnotatedMethods(Class<?> baseClass,
//                                         Map<String,Method> annotatedMethods,
//                                         Set<String> annotatedFields) {
//
//        Method[] methods = baseClass.getDeclaredMethods();
//        for (Method method : methods) {
//            ManagedProperty property = method.getAnnotation(ManagedProperty.class);
//            if (property != null) {
//
//                if (!method.getName().startsWith("set")
//                    || method.getParameterTypes().length != 1) {
//                    continue;
//                }
//                StringBuilder sb =
//                      new StringBuilder(method.getName().substring(3));
//                char c = sb.charAt(0);
//                sb.deleteCharAt(0);
//                sb.insert(0, Character.toLowerCase(c));
//                String propName = sb.toString();
//
//                if (!annotatedFields.contains(propName) && !annotatedMethods.containsKey(propName)) {
//                    annotatedMethods.put(propName, method);
//                }
//            }
//        }
//        Class<?> superClass = baseClass.getSuperclass();
//        if (!Object.class.equals(superClass)) {
//            collectAnnotatedMethods(superClass, annotatedMethods, annotatedFields);
//        }
//    }


    private void collectAnnotatedFields(Class<?> baseClass, Map<String,Field> annotatedFields) {

        Field[] fields = baseClass.getDeclaredFields();
        for (Field field : fields) {
            ManagedProperty property = field.getAnnotation(ManagedProperty.class);
            if (property != null) {
                String propName = property.name();
                if (propName == null || propName.length() == 0) {
                    propName = field.getName();
                }
                // if the field has already been collected, don't replace the existing
                // value as that value represents an override.
                if (!annotatedFields.containsKey(propName)) {
                    annotatedFields.put(propName, field);
                }
            }
        }
        Class<?> superClass = baseClass.getSuperclass();
        if (!Object.class.equals(superClass)) {
            collectAnnotatedFields(superClass, annotatedFields);
        }

    }


    private String getScope(Class<?> annotatedClass) {

        for (Class<?> scope : SCOPES) {
            //noinspection unchecked
            Annotation a = annotatedClass.getAnnotation((Class<? extends Annotation>) scope);
            if (a != null) {
                if (a instanceof RequestScoped) {
                    return "request";
                } else if (a instanceof ViewScoped) {
                    return "view";
                } if (a instanceof SessionScoped) {
                    return "session";
                } else if (a instanceof ApplicationScoped) {
                    return "application";
                } else if (a instanceof NoneScoped) {
                    return "none";
                } else if (a instanceof CustomScoped) {
                    return ((CustomScoped) a).value();
                }
            }
        }

        return "request";

    }


    private String getName(ManagedBean managedBean, Class<?> annotatedClass) {

        String name = managedBean.name();

        if (name.length() == 0) {
            String t = annotatedClass.getName();
            name = t.substring(t.lastIndexOf('.') + 1);
            char[] nameChars = name.toCharArray();
            nameChars[0] = Character.toLowerCase(nameChars[0]);
            name = new String(nameChars);
        }

        return name;

    }


}
