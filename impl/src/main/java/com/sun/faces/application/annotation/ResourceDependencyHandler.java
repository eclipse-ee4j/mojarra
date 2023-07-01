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

package com.sun.faces.application.annotation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sun.faces.el.ELUtils;
import com.sun.faces.util.RequestStateManager;

import jakarta.el.ValueExpression;
import jakarta.faces.application.Application;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * {@link RuntimeAnnotationHandler} responsible for processing {@link ResourceDependency} annotations.
 */
class ResourceDependencyHandler implements RuntimeAnnotationHandler {

    private final ResourceDependency[] dependencies;
    private final Map<ResourceDependency, Expressions> expressionsMap;

    // ------------------------------------------------------------ Constructors

    public ResourceDependencyHandler(ResourceDependency[] dependencies) {

        this.dependencies = dependencies;
        Map<Object, Object> attrs = FacesContext.getCurrentInstance().getAttributes();
        expressionsMap = new HashMap<>(dependencies.length, 1.0f);
        for (ResourceDependency dep : dependencies) {
            Expressions exprs = new Expressions();
            exprs.name = dep.name();
            String lib = dep.library();
            if (lib.length() > 0) {
                // Take special action to resolve the "this" library name
                if ("this".equals(lib)) {
                    String thisLibrary = (String) attrs.get(com.sun.faces.application.ApplicationImpl.THIS_LIBRARY);
                    assert null != thisLibrary;
                    lib = thisLibrary;
                }

                exprs.library = lib;
            }
            String tgt = dep.target();
            if (tgt.length() > 0) {
                exprs.target = tgt;
            }
            expressionsMap.put(dep, exprs);
        }

    }

    // ----------------------------------- Methods from RuntimeAnnotationHandler

    @SuppressWarnings({ "UnusedDeclaration" })
    @Override
    public void apply(FacesContext ctx, Object... params) {

        for (ResourceDependency dep : dependencies) {
            if (!hasBeenProcessed(ctx, dep)) {
                pushResourceToRoot(ctx, createComponentResource(ctx, dep));
                markProcssed(ctx, dep);
            }
        }

    }

    // --------------------------------------------------------- Private Methods

    /**
     * Adds the specified {@link UIComponent} as a component resource to the {@link jakarta.faces.component.UIViewRoot}
     *
     * @param ctx the {@link FacesContext} for the current request
     * @param c the component resource
     */
    private void pushResourceToRoot(FacesContext ctx, UIComponent c) {

        ctx.getViewRoot().addComponentResource(ctx, c, (String) c.getAttributes().get("target"));

    }

    /**
     * Determines of the specified {@link ResourceDependency} has already been previously processed.
     *
     * @param ctx the {@link FacesContext} for the current request
     * @param dep the {@link ResourceDependency} in question
     * @return <code>true</code> if the {@link ResourceDependency} has been processed, otherwise <code>false</code>
     */
    @SuppressWarnings({ "unchecked" })
    private boolean hasBeenProcessed(FacesContext ctx, ResourceDependency dep) {

        Set<ResourceDependency> dependencies = (Set<ResourceDependency>) RequestStateManager.get(ctx, RequestStateManager.PROCESSED_RESOURCE_DEPENDENCIES);
        return dependencies != null && dependencies.contains(dep);

    }

    /**
     * Construct a new component resource based off the provided {@link ValueExpression}s.
     *
     * @param ctx the {@link FacesContext} for the current request
     * @param dep the ResourceDependency that the component resource will be constructed from
     * @return a new component resource based of the provided annotation
     */
    private UIComponent createComponentResource(FacesContext ctx, ResourceDependency dep) {

        Expressions exprs = expressionsMap.get(dep);
        Application app = ctx.getApplication();
        String resname = exprs.getName(ctx);
        UIComponent c = ctx.getApplication().createComponent("jakarta.faces.Output");
        c.setRendererType(app.getResourceHandler().getRendererTypeForResourceName(resname));
        Map<String, Object> attrs = c.getAttributes();
        attrs.put("name", resname);
        if (exprs.library != null) {
            attrs.put("library", exprs.getLibrary(ctx));
        }
        if (exprs.target != null) {
            attrs.put("target", exprs.getTarget(ctx));
        }
        return c;

    }

    /**
     * Indicates that the specified ResourceDependency has been processed.
     *
     * @param ctx the {@link FacesContext} for the current request
     * @param dep the {@link ResourceDependency}
     */
    @SuppressWarnings({ "unchecked" })
    private void markProcssed(FacesContext ctx, ResourceDependency dep) {

        Set<ResourceDependency> dependencies = (Set<ResourceDependency>) RequestStateManager.get(ctx, RequestStateManager.PROCESSED_RESOURCE_DEPENDENCIES);
        if (dependencies == null) {
            dependencies = new HashSet<>(6);
            RequestStateManager.set(ctx, RequestStateManager.PROCESSED_RESOURCE_DEPENDENCIES, dependencies);
        }
        dependencies.add(dep);

    }

    // ----------------------------------------------------------- Inner Classes

    /**
     * This helper class hides expression evaluation complexity.
     */
    private static final class Expressions {

        ValueExpression nameExpression;
        ValueExpression libraryExpression;
        ValueExpression targetExpression;
        String name;
        String library;
        String target;

        String getName(FacesContext ctx) {
            if (nameExpression == null) {
                nameExpression = ELUtils.createValueExpression(name, String.class);
            }
            return (String) nameExpression.getValue(ctx.getELContext());
        }

        String getLibrary(FacesContext ctx) {
            if (library != null) {
                if (libraryExpression == null) {
                    libraryExpression = ELUtils.createValueExpression(library, String.class);
                }
                return (String) libraryExpression.getValue(ctx.getELContext());
            }
            return null;
        }

        String getTarget(FacesContext ctx) {
            if (target != null) {
                if (targetExpression == null) {
                    targetExpression = ELUtils.createValueExpression(target, String.class);
                }
                return (String) targetExpression.getValue(ctx.getELContext());
            }
            return null;
        }

    }

}
