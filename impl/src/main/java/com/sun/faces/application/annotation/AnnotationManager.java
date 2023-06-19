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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.Behavior;
import jakarta.faces.component.behavior.ClientBehaviorBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.render.ClientBehaviorRenderer;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.Renderer;
import jakarta.faces.validator.Validator;

/**
 * This class represents the central point for annotation handling within a web application.
 */
public class AnnotationManager {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();
    private static final Scanner RESOURCE_DEPENDENCY_SCANNER = new ResourceDependencyScanner();
    private static final Scanner LISTENER_FOR_SCANNER = new ListenerForScanner();

    /**
     * {@link Scanner} instances to be used against {@link Behavior} classes.
     */
    private static final Scanner[] BEHAVIOR_SCANNERS = { RESOURCE_DEPENDENCY_SCANNER };

    /**
     * {@link Scanner} instances to be used against {@link ClientBehaviorRenderer} classes.
     */
    private static final Scanner[] CLIENT_BEHAVIOR_RENDERER_SCANNERS = { RESOURCE_DEPENDENCY_SCANNER };

    /**
     * {@link Scanner} instances to be used against {@link UIComponent} classes.
     */
    private static final Scanner[] UICOMPONENT_SCANNERS = { RESOURCE_DEPENDENCY_SCANNER, LISTENER_FOR_SCANNER };

    /**
     * {@link Scanner} instances to be used against {@link Validator} classes.
     */
    private static final Scanner[] VALIDATOR_SCANNERS = { RESOURCE_DEPENDENCY_SCANNER };

    /**
     * {@link Scanner} instances to be used against {@link Converter} classes.
     */
    private static final Scanner[] CONVERTER_SCANNERS = { RESOURCE_DEPENDENCY_SCANNER

    };

    /**
     * {@link Scanner} instances to be used against {@link Renderer} classes.
     */
    private static final Scanner[] RENDERER_SCANNERS = { RESOURCE_DEPENDENCY_SCANNER, LISTENER_FOR_SCANNER };

    private static final Scanner[] EVENTS_SCANNERS = { RESOURCE_DEPENDENCY_SCANNER };

    /**
     * Enum of the different processing targets and their associated {@link Scanner}s
     */
    private enum ProcessingTarget {

        Behavior(BEHAVIOR_SCANNERS),
        ClientBehaviorRenderer(CLIENT_BEHAVIOR_RENDERER_SCANNERS),
        UIComponent(UICOMPONENT_SCANNERS),
        Validator(VALIDATOR_SCANNERS),
        Converter(CONVERTER_SCANNERS),
        Renderer(RENDERER_SCANNERS),
        SystemEvent(EVENTS_SCANNERS);

        private final Scanner[] scanners;

        ProcessingTarget(Scanner[] scanners) {
            this.scanners = scanners;
        }

    }

    /**
     * The backing cache for all annotation metadata.
     */
    private final ConcurrentMap<Class<?>, Future<Map<Class<? extends Annotation>, RuntimeAnnotationHandler>>> cache;

    // ------------------------------------------------------------ Constructors

    /**
     * Construct a new AnnotationManager instance.
     */
    public AnnotationManager() {
        cache = new ConcurrentHashMap<>(40, .75f, 32);
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * <p>
     * Apply the configuration metadata contained with in the <code>Collection</code> of annotated classes.
     * </p>
     *
     * @param ctx FacesContext available during application initialization
     * @param annotationType the involved annotation type
     * @param annotatedClasses <code>Collection</code> of class names known to contain one or more Faces configuration
     * annotations
     */
    public void applyConfigAnnotations(FacesContext ctx, Class<? extends Annotation> annotationType, Set<? extends Class> annotatedClasses) {
        if (annotatedClasses != null && !annotatedClasses.isEmpty()) {
            ConfigAnnotationHandler handler = getConfigAnnotationHandlers().get(annotationType);
            if (handler == null) {
                throw new IllegalStateException("Internal Error: No ConfigAnnotationHandler for type: " + annotationType);
            }

            for (Class<?> clazz : annotatedClasses) {
                handler.collect(clazz, clazz.getAnnotation(annotationType));
            }

            // metadata collected, now push the configuration to the system
            handler.push(ctx);
        }
    }

    /**
     * Apply annotations relevant to {@link jakarta.faces.component.behavior.Behavior} instances.
     *
     * @param ctx the {@link jakarta.faces.context.FacesContext} for the current request
     * @param b the target <code>Behavior</code> to process
     */
    public void applyBehaviorAnnotations(FacesContext ctx, Behavior b) {
        applyAnnotations(ctx, b.getClass(), ProcessingTarget.Behavior, b);
        if (b instanceof ClientBehaviorBase) {
            ClientBehaviorBase clientBehavior = (ClientBehaviorBase) b;
            String rendererType = clientBehavior.getRendererType();
            RenderKit renderKit = ctx.getRenderKit();
            if (null != rendererType && null != renderKit) {
                ClientBehaviorRenderer behaviorRenderer = renderKit.getClientBehaviorRenderer(rendererType);
                if (null != behaviorRenderer) {
                    applyClientBehaviorRendererAnnotations(ctx, behaviorRenderer);
                }
            }
        }

    }

    /**
     * Apply annotations relevant to {@link jakarta.faces.render.ClientBehaviorRenderer} instances.
     *
     * @param ctx the {@link jakarta.faces.context.FacesContext} for the current request
     * @param b the target <code>ClientBehaviorRenderer</code> to process
     */
    public void applyClientBehaviorRendererAnnotations(FacesContext ctx, ClientBehaviorRenderer b) {
        applyAnnotations(ctx, b.getClass(), ProcessingTarget.ClientBehaviorRenderer, b);
    }

    /**
     * Apply annotations relevant to {@link jakarta.faces.component.UIComponent} instances.
     *
     * @param ctx the {@link jakarta.faces.context.FacesContext} for the current request
     * @param c the target <code>UIComponent</code> to process
     */
    public void applyComponentAnnotations(FacesContext ctx, UIComponent c) {
        applyAnnotations(ctx, c.getClass(), ProcessingTarget.UIComponent, c);
    }

    /**
     * Apply annotations relevant to {@link jakarta.faces.validator.Validator} instances.
     *
     * @param ctx the {@link jakarta.faces.context.FacesContext} for the current request
     * @param v the target <code>Validator</code> to process
     */
    public void applyValidatorAnnotations(FacesContext ctx, Validator v) {
        applyAnnotations(ctx, v.getClass(), ProcessingTarget.Validator, v);
    }

    /**
     * Apply annotations relevant to {@link jakarta.faces.convert.Converter} instances.
     *
     * @param ctx the {@link jakarta.faces.context.FacesContext} for the current request
     * @param c the target <code>Converter</code> to process
     */
    public void applyConverterAnnotations(FacesContext ctx, Converter c) {
        applyAnnotations(ctx, c.getClass(), ProcessingTarget.Converter, c);
    }

    /**
     * Apply annotations relevent to {@link jakarta.faces.render.Renderer} instances.
     *
     * @param ctx the {@link jakarta.faces.context.FacesContext} for the current request
     * @param r the <code>Renderer</code> to process
     * @param c the <code>UIComponent</code> instances that is associated with this <code>Renderer</code>
     */
    public void applyRendererAnnotations(FacesContext ctx, Renderer r, UIComponent c) {
        applyAnnotations(ctx, r.getClass(), ProcessingTarget.Renderer, r, c);
    }

    public void applySystemEventAnnotations(FacesContext ctx, SystemEvent e) {
        applyAnnotations(ctx, e.getClass(), ProcessingTarget.SystemEvent, e);
    }

    // --------------------------------------------------------- Private Methods

    /**
     * @return a new <code>Map</code> which maps the types of annotations to a specific
     * <code>ConfigAnnotationHandler</code>. Note that each invocation of this method constructs a new <code>Map</code> with
     * new <code>ConfigAnnotationhandler</code> instances as they are not thread safe.
     */
    private Map<Class<? extends Annotation>, ConfigAnnotationHandler> getConfigAnnotationHandlers() {
        ConfigAnnotationHandler[] handlers = {
                new ComponentConfigHandler(),
                new ConverterConfigHandler(),
                new ValidatorConfigHandler(),
                new BehaviorConfigHandler(),
                new RenderKitConfigHandler(),
                new NamedEventConfigHandler() };

        Map<Class<? extends Annotation>, ConfigAnnotationHandler> handlerMap = new HashMap<>();
        for (ConfigAnnotationHandler handler : handlers) {
            Collection<Class<? extends Annotation>> handledClasses = handler.getHandledAnnotations();
            for (Class<? extends Annotation> handled : handledClasses) {
                handlerMap.put(handled, handler);
            }
        }

        return handlerMap;
    }

    /**
     * Apply all annotations associated with <code>targetClass</code>
     *
     * @param ctx the {@link jakarta.faces.context.FacesContext} for the current request
     * @param targetClass class of the <code>processingTarget</code>
     * @param processingTarget the type of component that is being processed
     * @param params one or more parameters to be passed to each {@link RuntimeAnnotationHandler}
     */
    private void applyAnnotations(FacesContext ctx, Class<?> targetClass, ProcessingTarget processingTarget, Object... params) {
        Map<Class<? extends Annotation>, RuntimeAnnotationHandler> map = getHandlerMap(targetClass, processingTarget);
        if (map != null && !map.isEmpty()) {
            for (RuntimeAnnotationHandler handler : map.values()) {
                handler.apply(ctx, params);
            }
        }
    }

    /**
     * Helper method to look up cached annotation metadata.
     *
     * @param targetClass class of the <code>processingTarget</code>
     * @param processingTarget the type of component being processed
     * @return a Map keyed by Annotation class with an AnnotationHandler as the value
     */
    private Map<Class<? extends Annotation>, RuntimeAnnotationHandler> getHandlerMap(Class<?> targetClass, ProcessingTarget processingTarget) {

        while (true) {
            Future<Map<Class<? extends Annotation>, RuntimeAnnotationHandler>> f = cache.get(targetClass);
            if (f == null) {
                ProcessAnnotationsTask t = new ProcessAnnotationsTask(targetClass, processingTarget.scanners);
                FutureTask<Map<Class<? extends Annotation>, RuntimeAnnotationHandler>> ft = new FutureTask<>(t);
                f = cache.putIfAbsent(targetClass, ft);
                if (f == null) {
                    f = ft;
                    ft.run();
                }
            }
            try {
                return f.get();
            } catch (CancellationException | InterruptedException ce) {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.log(Level.FINEST, ce.toString(), ce);
                }
                cache.remove(targetClass);
            } catch (ExecutionException ee) {
                throw new FacesException(ee);
            }
        }

    }

    // ----------------------------------------------------------- Inner Classes

    /**
     * This <code>Callable</code> will leverage the provided <code>Scanner</code>s to build a mapping between a particular
     * annotation type and an <code>AnnotationHandler</code> for that type.
     */
    private static final class ProcessAnnotationsTask implements Callable<Map<Class<? extends Annotation>, RuntimeAnnotationHandler>> {

        @SuppressWarnings({ "unchecked" })
        private static final Map<Class<? extends Annotation>, RuntimeAnnotationHandler> EMPTY = Collections.EMPTY_MAP;
        private final Class<?> clazz;
        private final Scanner[] scanners;

        // -------------------------------------------------------- Constructors

        public ProcessAnnotationsTask(Class<?> clazz, Scanner[] scanners) {

            this.clazz = clazz;
            this.scanners = scanners;

        }

        // ------------------------------------------------------ Public Methods

        @Override
        public Map<Class<? extends Annotation>, RuntimeAnnotationHandler> call() throws Exception {

            Map<Class<? extends Annotation>, RuntimeAnnotationHandler> map = null;
            for (Scanner scanner : scanners) {
                RuntimeAnnotationHandler handler = scanner.scan(clazz);
                if (handler != null) {
                    if (map == null) {
                        map = new HashMap<>(2, 1.0f);
                    }
                    map.put(scanner.getAnnotation(), handler);
                }
            }

            return map != null ? map : EMPTY;

        }

    } // END ProcessAnnotationsTask
}
