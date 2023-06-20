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

import java.lang.annotation.Annotation;
import java.util.*;

import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.ClientBehaviorRenderer;
import jakarta.faces.render.FacesBehaviorRenderer;
import jakarta.faces.render.FacesRenderer;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.render.Renderer;

/**
 * <p>
 * <code>ConfigAnnotationHandler</code> {@link FacesRenderer} annotated classes.
 * </p>
 */
public class RenderKitConfigHandler implements ConfigAnnotationHandler {

    private static final Collection<Class<? extends Annotation>> HANDLES = List.of(
            FacesRenderer.class,FacesBehaviorRenderer.class);

    Map<Class<?>, Annotation> annotatedRenderers;

    // ------------------------------------- Methods from ComponentConfigHandler

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

        if (annotatedRenderers == null) {
            annotatedRenderers = new HashMap<>();
        }
        annotatedRenderers.put(target, annotation);

    }

    /**
     * @see com.sun.faces.application.annotation.ConfigAnnotationHandler#push(jakarta.faces.context.FacesContext)
     */
    @Override
    public void push(FacesContext ctx) {

        if (annotatedRenderers != null) {
            RenderKitFactory rkf = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
            for (Map.Entry<Class<?>, Annotation> entry : annotatedRenderers.entrySet()) {
                Class<?> rClass = entry.getKey();
                if (entry.getValue() instanceof FacesRenderer) {
                    FacesRenderer ra = (FacesRenderer) entry.getValue();
                    try {
                        RenderKit rk = rkf.getRenderKit(ctx, ra.renderKitId());
                        if (rk == null) {
                            throw new IllegalStateException("Error processing annotated Renderer " + ra.toString() + " on class " + rClass.getName()
                                    + ".  Unable to find specified RenderKit.");
                        }
                        rk.addRenderer(ra.componentFamily(), ra.rendererType(),
                                (Renderer) rClass.getDeclaredConstructor().newInstance());
                    } catch (IllegalStateException | ReflectiveOperationException | SecurityException e) {
                        throw new FacesException(e);
                    }
                } else if (entry.getValue() instanceof FacesBehaviorRenderer) {
                    FacesBehaviorRenderer bra = (FacesBehaviorRenderer) entry.getValue();
                    try {
                        RenderKit rk = rkf.getRenderKit(ctx, bra.renderKitId());
                        if (rk == null) {
                            throw new IllegalStateException("Error processing annotated ClientBehaviorRenderer " + bra.toString() + " on class "
                                    + rClass.getName() + ".  Unable to find specified RenderKit.");
                        }
                        rk.addClientBehaviorRenderer(bra.rendererType(),
                                (ClientBehaviorRenderer) rClass.getDeclaredConstructor().newInstance());
                    } catch (IllegalStateException | ReflectiveOperationException | SecurityException e) {
                        throw new FacesException(e);
                    }
                }
            }
        }

    }

}
