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

package com.sun.faces.facelets.tag.jstl.core;

import java.util.IdentityHashMap;
import java.util.Map;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;

/**
 * Per-phase memoization of a {@code c:forEach} <em>items</em> expression.
 *
 * <p>Every unrolled iteration var expression ({@link IndexedValueExpression}, {@link IteratedValueExpression},
 * {@link MappedValueExpression}) resolves the same shared items expression ({@code orig}) to reach its element, and
 * resolving it is the expensive part - it walks the whole EL/CDI chain (e.g. {@code #{dataBean.groups}} through the
 * CDI/bean resolvers). Because the body is unrolled, that walk is paid once per cell per phase even though every cell
 * of one {@code c:forEach} shares the same {@code orig} instance and the same result.
 *
 * <p>Within a single lifecycle phase the items collection is stable - it is only structurally replaced <em>between</em>
 * phases (by an action), not mid-phase - so this caches {@code orig}'s value keyed by {@code orig} identity and scoped
 * to the current {@link PhaseId}. The chain is then walked once per items expression per phase instead of once per
 * cell, while staying live across phases: a content change made by an action is picked up by the render phase's fresh
 * cache, and a reused component still reads current content because its var expression holds the same {@code orig}.
 */
final class IterationBaseCache {

    private static final String KEY = "com.sun.faces.facelets.forEachBaseCache";

    /** Distinguishes a cached {@code null} result from an absent entry, so a null-valued source is cached too. */
    private static final Object NULL = new Object();

    private final PhaseId phase;
    private final Map<ValueExpression, Object> bases = new IdentityHashMap<>();

    private IterationBaseCache(PhaseId phase) {
        this.phase = phase;
    }

    /**
     * Resolve {@code orig} against the current phase's cache, populating it on first use. Falls back to a direct,
     * uncached resolution when there is no active lifecycle phase (nothing to key the cache by).
     */
    static Object getValue(ELContext elContext, ValueExpression orig) {
        FacesContext context = FacesContext.getCurrentInstance();
        PhaseId phase = context == null ? null : context.getCurrentPhaseId();
        if (phase == null) {
            return orig.getValue(elContext);
        }

        Map<Object, Object> attributes = context.getAttributes();
        IterationBaseCache cache = (IterationBaseCache) attributes.get(KEY);
        if (cache == null || cache.phase != phase) {
            cache = new IterationBaseCache(phase);
            attributes.put(KEY, cache);
        }

        Object base = cache.bases.get(orig);
        if (base == null) {
            base = orig.getValue(elContext);
            cache.bases.put(orig, base == null ? NULL : base);
            return base;
        }
        return base == NULL ? null : base;
    }
}
