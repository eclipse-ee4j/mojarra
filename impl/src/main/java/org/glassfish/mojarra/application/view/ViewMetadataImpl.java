/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.mojarra.application.view;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static org.glassfish.mojarra.util.Util.isEmpty;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIImportConstants;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewMetadata;

import org.glassfish.mojarra.application.ApplicationAssociate;
import org.glassfish.mojarra.context.FacesFileNotFoundException;
import org.glassfish.mojarra.facelets.impl.DefaultFaceletFactory;
import org.glassfish.mojarra.facelets.tag.SavedBuildTimeDecisions;

/**
 * @see jakarta.faces.view.ViewMetadata
 */
public class ViewMetadataImpl extends ViewMetadata {

    private final String viewId;
    private DefaultFaceletFactory faceletFactory;

    // ---------------------------------------------------------------------------------------------------- Constructors

    public ViewMetadataImpl(String viewId) {
        this.viewId = viewId;
    }

    // --------------------------------------------------------------------------------------- Methods from ViewMetadata

    /**
     * @see jakarta.faces.view.ViewMetadata#getViewId()
     */
    @Override
    public String getViewId() {
        return viewId;
    }

    /**
     * @see jakarta.faces.view.ViewMetadata#createMetadataView(jakarta.faces.context.FacesContext)
     */
    @Override
    public UIViewRoot createMetadataView(FacesContext context) {
        UIViewRoot metadataView = null;
        UIViewRoot currentViewRoot = context.getViewRoot();
        Map<String, Object> currentViewMapShallowCopy = emptyMap();

        try {
            context.setProcessingEvents(false);
            if (faceletFactory == null) {
                faceletFactory = ApplicationAssociate.getInstance(context.getExternalContext())
                    .getFaceletFactory();
            }

            metadataView = context.getApplication()
                .getViewHandler()
                .createView(context, viewId);

            // If the currentViewRoot has a viewMap, make sure the entries are
            // copied to the temporary UIViewRoot before invoking handlers.
            if (currentViewRoot != null) {
                Map<String, Object> currentViewMap = currentViewRoot.getViewMap(false);

                if (!isEmpty(currentViewMap)) {
                    currentViewMapShallowCopy = new HashMap<>(currentViewMap);
                    metadataView.getViewMap(true)
                        .putAll(currentViewMapShallowCopy);
                }
            }

            // Only replace the current context's UIViewRoot if there is one to replace.
            if (currentViewRoot != null) {
                // This clears the ViewMap of the current UIViewRoot before
                // setting the argument as the new UIViewRoot.
                context.setViewRoot(metadataView);
            }

            SavedBuildTimeDecisions.suspend(context);

            try {
                faceletFactory.getMetadataFacelet(context, metadataView.getViewId())
                    .applyMetadata(context, metadataView);
            }
            finally {
                SavedBuildTimeDecisions.resume(context);
            }

            importConstantsIfNecessary(context, metadataView);

        }
        catch (FacesFileNotFoundException ffnfe) {
            try {
                context.getExternalContext().responseSendError(404, ffnfe.getMessage());
            }
            catch (IOException ioe) {
            }
            context.responseComplete();
        }
        catch (IOException ioe) {
            throw new FacesException(ioe);
        }
        finally {
            if (currentViewRoot != null) {
                context.setViewRoot(currentViewRoot);
                if (!currentViewMapShallowCopy.isEmpty()) {
                    currentViewRoot.getViewMap(true).putAll(currentViewMapShallowCopy);
                    currentViewMapShallowCopy.clear();
                }
            }
            context.setProcessingEvents(true);
        }

        return metadataView;
    }

    // ----------------------------------------------------------------------------------------------- UIImportConstants

    private static void importConstantsIfNecessary(FacesContext context, UIViewRoot root) {
        getImportConstants(root).forEach(importConstants -> importConstants(context, importConstants));
    }

    /**
     * Import constants declared by given {@link UIImportConstants} instance into the application map of the given {@link FacesContext}.
     */
    public static void importConstants(FacesContext context, UIImportConstants importConstants) {
        String type = importConstants.getType();

        if (type == null) {
            throw new IllegalArgumentException("UIImportConstants type attribute is required.");
        }

        String var = importConstants.getVar();

        if (var == null) {
            int innerClass = type.lastIndexOf('$');
            int outerClass = type.lastIndexOf('.');
            var = type.substring(Math.max(innerClass, outerClass) + 1);
        }

        Map<String, Object> applicationMap = context.getExternalContext().getApplicationMap();

        if (!applicationMap.containsKey(type)) {
            applicationMap.putIfAbsent(var, collectConstants(type));
        }
    }

    /**
     * Collect constants of the given type. That are, all public static final fields of the given type and of all its super classes and interfaces. They are
     * collected in declaration order, starting with those of the given type itself. When the same constant field name is declared more than once in the
     * hierarchy, then the one declared by the most specific type wins.
     *
     * @param type The fully qualified name of the type to collect constants for.
     * @return Constants of the given type, in declaration order.
     */
    static Map<String, Object> collectConstants(String type) {
        Map<String, Object> constants = new LinkedHashMap<>();
        Class<?> clazz = toClass(type);

        if (clazz.isEnum()) {
            // The order of getEnumConstants() is defined, the order of getDeclaredFields() is not.
            for (Object enumConstant : clazz.getEnumConstants()) {
                constants.put(((Enum<?>) enumConstant).name(), enumConstant);
            }
        }

        for (Class<?> declaredType : getDeclaredTypes(clazz)) {
            for (Field field : declaredType.getDeclaredFields()) {
                int modifiers = field.getModifiers();

                if (isPublic(modifiers) && isStatic(modifiers) && isFinal(modifiers)) {
                    try {
                        constants.putIfAbsent(field.getName(), field.get(null));
                    }
                    catch (Exception e) {
                        throw new IllegalArgumentException(
                            String.format("UIImportConstants cannot access constant field '%s' of type '%s'.", field.getName(), type), e
                        );
                    }
                }
            }
        }

        return unmodifiableMap(new ConstantsMap(constants, type));
    }

    /**
     * Collect the given type and all its super classes and interfaces, except {@link Object}, in the order in which their constant fields must be collected.
     * That is, the given type first, then its super classes from the most specific one on, and then their interfaces.
     *
     * @param type The type to collect the declared types for.
     * @return The given type and all its super classes and interfaces, except {@link Object}.
     */
    private static Set<Class<?>> getDeclaredTypes(Class<?> type) {
        Set<Class<?>> declaredTypes = new LinkedHashSet<>();
        declaredTypes.add(type);
        fillAllSuperClasses(type, declaredTypes);

        for (Class<?> declaredType : List.copyOf(declaredTypes)) {
            fillAllInterfaces(declaredType, declaredTypes);
        }

        return declaredTypes;
    }

    private static void fillAllSuperClasses(Class<?> type, Set<Class<?>> declaredTypes) {
        for (Class<?> superClass = type.getSuperclass(); superClass != null && superClass != Object.class; superClass = superClass.getSuperclass()) {
            declaredTypes.add(superClass);
        }
    }

    private static void fillAllInterfaces(Class<?> type, Set<Class<?>> declaredTypes) {
        for (Class<?> interfaceType : type.getInterfaces()) {
            if (declaredTypes.add(interfaceType)) {
                fillAllInterfaces(interfaceType, declaredTypes);
            }
        }
    }

    /**
     * Convert the given type, which should represent a fully qualified name, to a concrete {@link Class} instance.
     *
     * @param type The fully qualified name of the class.
     * @return The concrete {@link Class} instance.
     * @throws IllegalArgumentException When it is missing in the classpath.
     */
    private static Class<?> toClass(String type) {
        try {
            return Class.forName(type, true, Thread.currentThread().getContextClassLoader());
        }
        catch (ClassNotFoundException e) {
            // Perhaps it's an inner enum which is incorrectly specified as com.example.SomeClass.SomeEnum.
            // Let's be lenient on that although the proper type notation should be com.example.SomeClass$SomeEnum.
            int i = type.lastIndexOf('.');

            if (i > 0) {
                try {
                    return toClass(new StringBuilder(type).replace(i, i + 1, "$").toString());
                }
                catch (Exception ignore) {
                    ignore = null; // Just continue to IllegalArgumentException on original ClassNotFoundException.
                }
            }

            throw new IllegalArgumentException(String.format("UIImportConstants cannot find type '%s' in classpath.", type), e);
        }
    }

    /**
     * Specific map implementation which wraps the given map in {@link Collections#unmodifiableMap(Map)} and throws an {@link IllegalArgumentException} in
     * {@link ConstantsMap#get(Object)} method when the key doesn't exist at all.
     *
     * @author Bauke Scholtz
     * @since 2.3
     */
    private static class ConstantsMap extends LinkedHashMap<String, Object> {

        private static final long serialVersionUID = 7036447585721834948L;

        private final String type;

        public ConstantsMap(Map<String, Object> map, String type) {
            this.type = type;
            putAll(map);
        }

        @Override
        public Object get(Object key) {
            if (!containsKey(key)) {
                throw new IllegalArgumentException(String.format("UIImportConstants type '%s' does not have the constant '%s'.", type, key));
            }

            return super.get(key);
        }

        @Override
        public boolean equals(Object object) {
            return super.equals(object) && type.equals(((ConstantsMap) object).type);
        }

        @Override
        public int hashCode() {
            return super.hashCode() + type.hashCode();
        }

    }

}
