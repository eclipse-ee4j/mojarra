/*
 * Copyright (c) Contributors to Eclipse Foundation.
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

import static java.util.logging.Level.FINE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.BehaviorHandler;
import jakarta.faces.view.facelets.ConverterHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletHandler;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.ValidatorHandler;

import com.sun.faces.RIConstants;
import com.sun.faces.util.FacesLogger;

/**
 * What the view build in progress decided: everything a build-time-dynamic handler evaluated to produce what it
 * produced, in the order the build evaluated it.
 *
 * <p>
 * A view holding build-time-dynamic content has its facelet re-applied on every (re)build, since what the build
 * produces depends on more than the facelet. The re-apply is redundant when every recorded decision still yields the
 * value it yielded: the same handlers then take the same branches, create the same components and apply the same
 * values, so the re-apply reproduces the identical view. Proving that costs one evaluation per decision instead of a
 * walk over the whole tree, which is what {@code FaceletViewHandlingStrategy.buildView} spends the skip on.
 *
 * <p>
 * A decision is either an expression that must still evaluate to its recorded value, or a supplier that must still
 * yield its recorded value. The latter covers what an expression cannot answer: whether the value a handler applied is
 * still the one in effect, which the state restored over the tree after the build can replace, and whether a
 * comparison over more than one input still holds, such as a whole item list.
 *
 * <p>
 * A handler that produces something no decision can express marks the build unreproducible instead, which denies the
 * skip for the whole build. So does a decision that yields another value or fails to be evaluated at all, which is
 * what an expression that only holds under the branch that guarded it does.
 *
 * @see TagHandlerImpl#recordBuildTimeDecision(jakarta.faces.view.facelets.FaceletContext, ValueExpression, Object)
 */
public final class BuildTimeDecisions {

    private static final Logger LOGGER = FacesLogger.FACELETS_FACELET.getLogger();

    private static final String KEY = RIConstants.FACES_PREFIX + "buildTimeDecisions";

    /**
     * The package of {@code DelegatingMetaTagHandler} and its subclasses, which leave what they build to a delegate.
     */
    private static final String FACELETS_API_PACKAGE = "jakarta.faces.view.facelets.";

    /**
     * The package of the tag handlers of this implementation, every one of which is audited to either contribute the
     * same thing to every build of a view or record what it decided.
     */
    private static final String FACELETS_IMPL_PACKAGE = "com.sun.faces.facelets.";

    private static final class Decision {

        private final Supplier<?> value;
        private final Object recordedValue;
        private final UIComponent component;
        private final UIComponent compositeComponent;

        private Decision(Supplier<?> value, Object recordedValue, UIComponent component, UIComponent compositeComponent) {
            this.value = value;
            this.recordedValue = recordedValue;
            this.component = component;
            this.compositeComponent = compositeComponent;
        }

        /**
         * Re-evaluates this decision under the components it was recorded under. An expression reaching {@code cc} or
         * {@code component} resolves them from the component stack rather than from a variable, and that stack is empty
         * outside a build: {@code cc} would resolve to null, and an operator over null yields a value of its own rather
         * than failing, which a recorded value can match.
         */
        private Object currentValue(FacesContext context) {
            if (compositeComponent != null) {
                compositeComponent.pushComponentToEL(context, compositeComponent);
            }
            if (component != null) {
                component.pushComponentToEL(context, component);
            }

            try {
                return value.get();
            } finally {
                if (component != null) {
                    component.popComponentFromEL(context);
                }
                if (compositeComponent != null) {
                    compositeComponent.popComponentFromEL(context);
                }
            }
        }
    }

    private final List<Decision> decisions = new ArrayList<>();
    private int unreproducibleMarks;

    private BuildTimeDecisions() {
    }

    /**
     * Whether the given tag handler leaves the build reproducible, which holds in four cases. It attaches a converter,
     * validator or behavior to its parent, which a re-apply does not act on at all. It does not take over
     * {@code apply} at all, leaving what it builds to the delegate of {@code ComponentHandler} and its siblings. Or the
     * class that takes it over is one of this implementation's own, each of which is audited to either contribute the
     * same thing to every build or record what it decided. Or its tag carries no expression at all, leaving it nothing
     * to decide on that the lifecycle could have changed since the last build of this view: both builds are performed
     * within the same request, so everything such a handler reads from that request - its parameters, whether it is a
     * postback, the project stage - is the same for both, and only what an expression reaches can have changed in
     * between.
     *
     * @param handler the tag handler to classify
     * @param tag the tag it handles
     * @return whether the given tag handler leaves the build reproducible
     */
    public static boolean keepsBuildReproducible(FaceletHandler handler, Tag tag) {
        return attachesObject(handler) || !takesOverApply(handler) || !hasExpression(tag);
    }

    /**
     * Whether the given tag handler attaches an object to its parent rather than building anything: a converter, a
     * validator or a behavior. Such a handler acts only while its parent is new to the tree, so a re-apply is a no-op
     * for it however it takes over {@code apply}, which is what a handler deferring the evaluation of its attributes to
     * the render pass does.
     */
    private static boolean attachesObject(FaceletHandler handler) {
        return handler instanceof ConverterHandler || handler instanceof ValidatorHandler || handler instanceof BehaviorHandler;
    }

    private static boolean takesOverApply(FaceletHandler handler) {
        Class<?> handlerClass = handler.getClass();

        try {
            String declaringClassName = handlerClass.getMethod("apply", FaceletContext.class, UIComponent.class).getDeclaringClass().getName();
            return !declaringClassName.startsWith(FACELETS_API_PACKAGE) && !declaringClassName.startsWith(FACELETS_IMPL_PACKAGE);
        } catch (NoSuchMethodException e) {
            LOGGER.log(FINE, e, () -> handlerClass + " has no apply(FaceletContext, UIComponent), treating it as unreproducible");
            return true;
        }
    }

    private static boolean hasExpression(Tag tag) {
        for (TagAttribute attribute : tag.getAttributes().getAll()) {
            if (!attribute.isLiteral()) {
                return true;
            }
        }

        return false;
    }

    private static BuildTimeDecisions of(FacesContext context) {
        return (BuildTimeDecisions) context.getAttributes().computeIfAbsent(KEY, key -> new BuildTimeDecisions());
    }

    public static void record(FacesContext context, ValueExpression expression, Object value) {
        record(context, () -> expression.getValue(context.getELContext()), value);
    }

    public static void record(FacesContext context, Supplier<?> value, Object recordedValue) {
        of(context).decisions.add(new Decision(value, recordedValue, UIComponent.getCurrentComponent(context),
                UIComponent.getCurrentCompositeComponent(context)));
    }

    public static void markUnreproducible(FacesContext context) {
        of(context).unreproducibleMarks++;
    }

    /**
     * Discards the decisions of the previous build, to be called by whoever is about to apply a facelet.
     *
     * @param context the {@link FacesContext} for the current request
     */
    public static void reset(FacesContext context) {
        context.getAttributes().remove(KEY);
    }

    /**
     * The number of decisions the build has recorded so far, for a handler comparing it against a later count to see
     * whether the part of the build in between recorded any of its own.
     *
     * @param context the {@link FacesContext} for the current request
     * @return the number of decisions the build has recorded so far
     */
    public static int size(FacesContext context) {
        return of(context).decisions.size();
    }

    /**
     * The number of times the build has been marked unreproducible so far, for a handler comparing it against a later
     * count to see whether the part of the build in between marked it.
     *
     * @param context the {@link FacesContext} for the current request
     * @return the number of times the build has been marked unreproducible so far
     */
    public static int unreproducibleMarks(FacesContext context) {
        return of(context).unreproducibleMarks;
    }

    /**
     * Whether re-applying the facelet would reproduce what the last build of this view produced, which is the case
     * when nothing marked the build unreproducible and every decision it recorded still yields the value it had.
     *
     * @param context the {@link FacesContext} for the current request
     * @return whether re-applying the facelet would reproduce what the last build of this view produced
     */
    public static boolean reproducesBuild(FacesContext context) {
        BuildTimeDecisions instance = (BuildTimeDecisions) context.getAttributes().get(KEY);

        if (instance == null) {
            return true;
        }

        if (instance.unreproducibleMarks > 0) {
            return false;
        }

        for (Decision decision : instance.decisions) {
            try {
                if (!Objects.equals(decision.recordedValue, decision.currentValue(context))) {
                    return false;
                }
            } catch (RuntimeException e) {
                LOGGER.log(FINE, e, () -> "A build time decision could not be re-evaluated, rebuilding the view");
                return false;
            }
        }

        return true;
    }

}
