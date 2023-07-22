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

package com.sun.faces.application.view;

import static com.sun.faces.RIConstants.VIEWID_KEY_NAME;
import static com.sun.faces.util.Util.isEmpty;
import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.faces.RIConstants;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.context.FacesFileNotFoundException;
import com.sun.faces.facelets.impl.DefaultFaceletFactory;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIImportConstants;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewMetadata;

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

            // Stash away view id before invoking handlers so that
            // StateContext.partialStateSaving() can determine the current
            // view.
            context.getAttributes().put(RIConstants.VIEWID_KEY_NAME, viewId);

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

            faceletFactory.getMetadataFacelet(context, metadataView.getViewId())
                          .applyMetadata(context, metadataView);

            importConstantsIfNecessary(context, metadataView);

        } catch (FacesFileNotFoundException ffnfe) {
            try {
                context.getExternalContext().responseSendError(404, ffnfe.getMessage());
            } catch (IOException ioe) {
            }
            context.responseComplete();
        } catch (IOException ioe) {
            throw new FacesException(ioe);
        } finally {
            context.getAttributes().remove(VIEWID_KEY_NAME);
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
        for (UIImportConstants importConstants : getImportConstants(root)) {
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
    }

    /**
     * Collect constants of the given type. That are, all public static final fields of the given type.
     *
     * @param type The fully qualified name of the type to collect constants for.
     * @return Constants of the given type.
     */
    private static Map<String, Object> collectConstants(String type) {
        Map<String, Object> constants = new LinkedHashMap<>();

        for (Field field : toClass(type).getFields()) {
            int modifiers = field.getModifiers();

            if (isPublic(modifiers) && isStatic(modifiers) && isFinal(modifiers)) {
                try {
                    constants.put(field.getName(), field.get(null));
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                            String.format("UIImportConstants cannot access constant field '%s' of type '%s'.", type, field.getName()), e);
                }
            }
        }

        return unmodifiableMap(new ConstantsMap(constants, type));
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
        } catch (ClassNotFoundException e) {
            // Perhaps it's an inner enum which is incorrectly specified as com.example.SomeClass.SomeEnum.
            // Let's be lenient on that although the proper type notation should be com.example.SomeClass$SomeEnum.
            int i = type.lastIndexOf('.');

            if (i > 0) {
                try {
                    return toClass(new StringBuilder(type).replace(i, i + 1, "$").toString());
                } catch (Exception ignore) {
                    ignore = null; // Just continue to IllegalArgumentException on original ClassNotFoundException.
                }
            }

            throw new IllegalArgumentException(String.format("UIImportConstants cannot find type '%s' in classpath.", type), e);
        }
    }

    /**
     * Specific map implementation which wraps the given map in {@link Collections#unmodifiableMap(Map)} and throws an
     * {@link IllegalArgumentException} in {@link ConstantsMap#get(Object)} method when the key doesn't exist at all.
     *
     * @author Bauke Scholtz
     * @since 2.3
     */
    private static class ConstantsMap extends HashMap<String, Object> {

        private static final long serialVersionUID = 7036447585721834948L;
        private String type;

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
