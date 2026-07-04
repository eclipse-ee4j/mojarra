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

package com.sun.faces.facelets.tag.jstl.core;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.sun.faces.RIConstants;
import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.facelets.tag.faces.ComponentSupport;
import com.sun.faces.facelets.tag.faces.IterationIdManager;

import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;
import jakarta.faces.view.facelets.TagConfig;

/**
 * @author Jacob Hookom
 * @author Andrew Robinson
 */
public final class ForEachHandler extends TagHandlerImpl {

    private static class ArrayIterator implements Iterator {

        protected final Object array;

        protected int i;

        protected final int len;

        public ArrayIterator(Object src) {
            i = 0;
            array = src;
            len = Array.getLength(src);
        }

        @Override
        public boolean hasNext() {
            return i < len;
        }

        @Override
        public Object next() {
            try {
                return Array.get(array, i++);
            } catch (ArrayIndexOutOfBoundsException ioob) {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final TagAttribute begin;

    private final TagAttribute end;

    private final TagAttribute items;

    private final TagAttribute step;

    private final TagAttribute tranzient;

    private final TagAttribute var;

    private final TagAttribute varStatus;

    // Request-attribute key prefix under which an indexable c:forEach records, at its restore-time (re)build, the
    // iteration it produced (range plus, for items, the element snapshot) and the child components it created. When
    // the same view is built a second time in the same request (render under partial state saving) with an unchanged
    // iteration, that subtree is still correct - the index-based var expressions render content changes live - so the
    // redundant re-apply of the body is skipped and the retained children are un-marked from the parent refresh's
    // pending deletion. Keyed additionally by parent identity + tag location.
    private static final String ITERATION_STATE = "com.sun.faces.facelets.forEachIterationState:";

    /**
     * @param config
     */
    public ForEachHandler(TagConfig config) {
        super(config);
        items = getAttribute("items");
        var = getAttribute("var");
        begin = getAttribute("begin");
        end = getAttribute("end");
        step = getAttribute("step");
        varStatus = getAttribute("varStatus");
        tranzient = getAttribute("transient");

        if (items == null && begin != null && end == null) {
            throw new TagAttributeException(tag, begin,
                    "If the 'items' attribute is not specified, but the 'begin' attribute is, then the 'end' attribute is required");
        }
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {

        markDynamicTransientBuild(ctx);
        int s = getBegin(ctx);
        int e = getEnd(ctx);
        int m = getStep(ctx);
        Integer sO = begin != null ? s : null;
        Integer eO = end != null ? e : null;
        Integer mO = step != null ? m : null;

        boolean t = getTransient(ctx);
        Object src = null;
        ValueExpression srcVE = null;
        if (items != null) {
            srcVE = items.getValueExpression(ctx, Object.class);
            src = srcVE.getValue(ctx);
        } else {
            byte[] b = new byte[e + 1];
            for (int i = 0; i < b.length; i++) {
                b[i] = (byte) i;
            }
            src = b;
        }

        // See ITERATION_STATE. Retaining the built subtree across the redundant render-time re-apply is only safe when
        // the iteration is positionally indexable, so each var reference reads its element by position: items over a
        // List or array (IndexedValueExpression, a live index read) or a begin/end integer range (build-time-baked
        // value). A Map is not indexable - it iterates as a MappedValueExpression over a snapshotted Map.Entry, with no
        // positional index to compare against or read live - so it, and any other non-indexable Collection, is left to
        // the normal re-apply path.
        boolean indexable = src != null && (srcVE == null || src instanceof List || src.getClass().isArray());
        Map<Object, Object> contextAttributes = ctx.getFacesContext().getAttributes();
        String stateKey = null;
        int[] range = null;
        List<UIComponent> childrenBeforeBuild = null;
        if (indexable) {
            stateKey = ITERATION_STATE + System.identityHashCode(parent) + ':' + tag.getLocation();
            range = new int[] { s, e, m };
            if (ctx.getFacesContext().getCurrentPhaseId() == PhaseId.RESTORE_VIEW) {
                // Restore-time (re)build under PSS rebuilds this transient subtree from scratch; snapshot the existing
                // children so the ones created below can be recorded for the render pass to retain. Clear the
                // build-time-dynamic marker first: if applying the body re-sets it, the body holds nested dynamic
                // content (a nested c:forEach/c:if/...) that could change without this item list changing, so it must
                // not be skip-retained - only a fully static body is safe.
                childrenBeforeBuild = new ArrayList<>(parent.getChildren());
                contextAttributes.remove(RIConstants.DYNAMIC_TRANSIENT_BUILD);
            } else {
                Object[] state = (Object[]) contextAttributes.get(stateKey);
                if (state != null && sameIteration(state, range, srcVE == null ? null : src)) {
                    // Unchanged since the restore-time build: retain the existing subtree (undo the pending-deletion
                    // marks the parent's refresh set on it) and skip re-applying the body.
                    @SuppressWarnings("unchecked")
                    List<UIComponent> retained = (List<UIComponent>) state[2];
                    for (UIComponent child : retained) {
                        child.getAttributes().remove(ComponentSupport.MARK_DELETED);
                    }
                    return;
                }
            }
        }

        if (src != null) {
            Iterator itr = toIterator(src);
            if (itr != null) {
                int i = 0;

                // move to start
                while (i < s && itr.hasNext()) {
                    itr.next();
                    i++;
                }

                String v = getVarName(ctx);
                String vs = getVarStatusName(ctx);
                VariableMapper vars = ctx.getVariableMapper();
                ValueExpression ve = null;
                ValueExpression vO = capture(v, vars);
                ValueExpression vsO = capture(vs, vars);
                int mi = 0;
                Object value = null;
                int count = 0;

                IterationIdManager.startIteration(ctx);

                try {
                    boolean first = true;
                    while (i <= e && itr.hasNext()) {
                        count++;
                        value = itr.next();

                        // set the var
                        if (v != null) {
                            if (t || srcVE == null) {
                                ctx.setAttribute(v, value);
                            } else {
                                ve = getVarExpr(srcVE, src, value, i, s);
                                vars.setVariable(v, ve);
                            }
                        }

                        // set the varStatus
                        if (vs != null) {
                            JstlIterationStatus itrS = new JstlIterationStatus(first, !itr.hasNext(), i, sO, eO, mO, value, count);
                            if (t || srcVE == null) {
                                ctx.setAttribute(vs, itrS);
                            } else {
                                ve = new IterationStatusExpression(itrS);
                                vars.setVariable(vs, ve);
                            }
                        }

                        // execute body
                        nextHandler.apply(ctx, parent);

                        // increment steps
                        mi = 1;
                        while (mi < m && itr.hasNext()) {
                            itr.next();
                            mi++;
                            i++;
                        }
                        i++;

                        first = false;
                    }
                } finally {
                    if (v != null) {
                        vars.setVariable(v, vO);
                    }
                    if (vs != null) {
                        vars.setVariable(vs, vsO);
                    }
                    IterationIdManager.stopIteration(ctx);
                }
            }
        }

        if (childrenBeforeBuild != null) {
            // The body just applied re-set the dynamic marker iff it holds nested build-time-dynamic content; capture
            // that, then restore the marker this handler itself set (this c:forEach is build-time-dynamic).
            boolean nestedDynamic = contextAttributes.containsKey(RIConstants.DYNAMIC_TRANSIENT_BUILD);
            markDynamicTransientBuild(ctx);

            List<UIComponent> created = new ArrayList<>();
            for (UIComponent child : parent.getChildren()) {
                if (!childrenBeforeBuild.contains(child)) {
                    created.add(child);
                }
            }
            // Record (enabling the render-pass skip) only for a static body that actually created the subtree here: a
            // nested-dynamic body could change with this item list unchanged, and an empty delta means this build did
            // not create the subtree (e.g. full state saving already restored it) so there is nothing to retain.
            if (!nestedDynamic && !created.isEmpty()) {
                contextAttributes.put(stateKey, new Object[] { range, srcVE == null ? null : snapshotElements(src), created });
            }
        }
    }

    /**
     * Whether the recorded iteration matches the current one: same range and, for items, the same element sequence
     * (element-wise {@link Object#equals equality}; items without a value-based {@code equals} fall back to identity,
     * as a per-element refresh diff would). {@code currentItems} is {@code null} for a begin/end integer range.
     */
    private static boolean sameIteration(Object[] state, int[] range, Object currentItems) {
        if (!Arrays.equals((int[]) state[0], range)) {
            return false;
        }
        List<?> recordedItems = (List<?>) state[1];
        if (currentItems == null) {
            return recordedItems == null;
        }
        return recordedItems != null && sameElements(recordedItems, currentItems);
    }

    private static boolean sameElements(List<?> recorded, Object currentItems) {
        boolean list = currentItems instanceof List;
        int size = list ? ((List<?>) currentItems).size() : Array.getLength(currentItems);
        if (recorded.size() != size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            Object a = recorded.get(i);
            Object b = list ? ((List<?>) currentItems).get(i) : Array.get(currentItems, i);
            if (a == null ? b != null : !a.equals(b)) {
                return false;
            }
        }
        return true;
    }

    private static List<Object> snapshotElements(Object src) {
        if (src instanceof List) {
            return new ArrayList<>((List<?>) src);
        }
        int length = Array.getLength(src);
        List<Object> copy = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            copy.add(Array.get(src, i));
        }
        return copy;
    }

    private ValueExpression capture(String name, VariableMapper vars) {
        if (name != null) {
            return vars.setVariable(name, null);
        }
        return null;
    }

    private int getBegin(FaceletContext ctx) {
        if (begin != null) {
            return begin.getInt(ctx);
        }
        return 0;
    }

    private int getEnd(FaceletContext ctx) {
        if (end != null) {
            return end.getInt(ctx);
        }
        return Integer.MAX_VALUE - 1; // hotspot bug in the JVM
    }

    private int getStep(FaceletContext ctx) {
        if (step != null) {
            return step.getInt(ctx);
        }
        return 1;
    }

    private boolean getTransient(FaceletContext ctx) {
        if (tranzient != null) {
            return tranzient.getBoolean(ctx);
        }
        return false;
    }

    private ValueExpression getVarExpr(ValueExpression ve, Object src, Object value, int i, int start) {
        if (src instanceof List || src.getClass().isArray()) {
            return new IndexedValueExpression(ve, i);
        } else if (src instanceof Map && value instanceof Map.Entry) {
            return new MappedValueExpression(ve, (Map.Entry) value);
        } else if (src instanceof Collection) {
            return new IteratedValueExpression(ve, start, i);
        }
        throw new IllegalStateException("Cannot create VE for: " + src);
    }

    private String getVarName(FaceletContext ctx) {
        if (var != null) {
            return var.getValue(ctx);
        }
        return null;
    }

    private String getVarStatusName(FaceletContext ctx) {
        if (varStatus != null) {
            return varStatus.getValue(ctx);
        }
        return null;
    }

    private Iterator toIterator(Object src) {
        if (src == null) {
            return null;
        } else if (src instanceof Collection) {
            return ((Collection) src).iterator();
        } else if (src instanceof Map) {
            return ((Map) src).entrySet().iterator();
        } else if (src.getClass().isArray()) {
            return new ArrayIterator(src);
        } else {
            throw new TagAttributeException(tag, items, "Must evaluate to a Collection, Map, Array, or null.");
        }
    }

}
