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

package org.glassfish.mojarra.facelets.tag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import jakarta.el.ValueExpression;
import jakarta.faces.view.facelets.CompositeFaceletHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletHandler;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

/**
 *
 * @author edburns
 */
public abstract class TagHandlerImpl extends TagHandler {

    public TagHandlerImpl(TagConfig config) {
        super(config);
    }

    /**
     * Mark the current view build unreproducible, so its facelet is re-applied on every (re)build. A handler calls
     * this when what it contributes to the view follows from something no decision can express; the skip of the
     * redundant render-time re-apply is then denied for the whole build (see {@code refreshTransientBuildOnPSS}).
     *
     * @param ctx the {@link FaceletContext} for the current build
     */
    protected static void markUnreproducibleBuild(FaceletContext ctx) {
        BuildTimeDecisions.markUnreproducible(ctx.getFacesContext());
    }

    /**
     * Record the decision this handler took: the expression it evaluated and the value it got. Re-applying the facelet
     * is then known to reproduce what this build produced as long as that expression still evaluates to that value.
     *
     * @param ctx the {@link FaceletContext} for the current build
     * @param decision the expression this handler decided on
     * @param value the value that expression had during this build
     */
    protected static void recordBuildTimeDecision(FaceletContext ctx, ValueExpression decision, Object value) {
        BuildTimeDecisions.record(ctx.getFacesContext(), decision, value);
    }

    /**
     * Record the decision this handler took on the given tag attribute: the value it evaluated to during this build.
     * A literal attribute cannot evaluate to anything else and is therefore no decision at all, as is an absent one.
     *
     * @param ctx the {@link FaceletContext} for the current build
     * @param attribute the attribute this handler decided on, may be {@code null}
     */
    protected static void recordBuildTimeDecision(FaceletContext ctx, TagAttribute attribute) {
        if (isDynamic(attribute)) {
            ValueExpression expression = attribute.getValueExpression(ctx, Object.class);
            recordBuildTimeDecision(ctx, expression, expression.getValue(ctx));
        }
    }

    /**
     * Record the decision this handler took on the given tag attribute, whose value this handler has already
     * evaluated. Recording that value rather than evaluating the attribute again keeps the decision the one the build
     * acted on, and spares an evaluation of an expression that may not be free.
     *
     * @param ctx the {@link FaceletContext} for the current build
     * @param attribute the attribute this handler decided on, may be {@code null}
     * @param type the type the attribute was evaluated as
     * @param value the value it evaluated to during this build
     */
    protected static void recordBuildTimeDecision(FaceletContext ctx, TagAttribute attribute, Class<?> type, Object value) {
        if (isDynamic(attribute)) {
            recordBuildTimeDecision(ctx, attribute.getValueExpression(ctx, type), value);
        }
    }

    /**
     * Record the decision this handler took as a supplier of the value it must still yield, for what an expression
     * cannot answer: whether the value this build applied is still the one in effect, which the state restored over
     * the tree can replace, and whether a comparison over more than one input still holds.
     *
     * @param ctx the {@link FaceletContext} for the current build
     * @param decision the supplier of the value this handler decided on
     * @param value the value that supplier yielded during this build
     */
    protected static void recordBuildTimeDecision(FaceletContext ctx, Supplier<?> decision, Object value) {
        BuildTimeDecisions.record(ctx.getFacesContext(), decision, value);
    }

    /**
     * Whether the given attribute can evaluate to something other than what it evaluated to before, which an absent or
     * literal attribute cannot.
     *
     * @param attribute the attribute to test, may be {@code null}
     * @return whether the given attribute can evaluate to something other than what it evaluated to before
     */
    protected static boolean isDynamic(TagAttribute attribute) {
        return attribute != null && !attribute.isLiteral();
    }

    /**
     * Searches child handlers, starting at the 'nextHandler' for all instances of the passed type. This process will stop
     * searching a branch if an instance is found.
     *
     * @param type Class type to search for
     * @return iterator over instances of FaceletHandlers of the matching type
     */
    protected final <T> Iterator<T> findNextByType(Class<T> type) {
        return findNextByType(nextHandler, type);
    }

    public static final <T> Iterator<T> findNextByType(FaceletHandler nextHandler, Class<T> type) {
        List<T> found = new ArrayList<>();
        if (type.isAssignableFrom(nextHandler.getClass())) {
            found.add(type.cast(nextHandler));
        } else if (nextHandler instanceof CompositeFaceletHandler) {
            FaceletHandler[] h = ((CompositeFaceletHandler) nextHandler).getHandlers();
            for (FaceletHandler handler : h) {
                if (type.isAssignableFrom(handler.getClass())) {
                    found.add(type.cast(handler));
                }
            }
        }
        return found.iterator();
    }

}
