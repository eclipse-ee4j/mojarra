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

import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import com.sun.faces.facelets.tag.BuildTimeDecisions;
import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.facelets.tag.faces.ComponentSupport;
import com.sun.faces.facelets.tag.faces.IterationIdManager;
import com.sun.faces.facelets.tag.jstl.core.RestoredItemValueExpression.Access;
import com.sun.faces.util.FacesLogger;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import jakarta.faces.view.Location;
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

    private static final Logger LOGGER = FacesLogger.FACELETS_FACELET.getLogger();

    /**
     * Reported for the rows a build reproduced over items which no longer hold their elements.
     */
    private static final String VANISHED_ITEMS_REPORT = "The build which restored this postback reproduced {0,number,#} of the {1,number,#} rows {2} rendered"
            + " over items which no longer hold their elements, so a value submitted for such a row is decoded into a stand-in for the element"
            + " that is gone rather than into the model. The items a c:forEach iterated must still hold the elements its rows were rendered"
            + " for while the postback which submits them is restored. Hold them in a @ViewScoped bean, or recompute them from the relevant"
            + " request parameters in the @PostConstruct of the request scoped one.";

    /**
     * Reported for the rows a build could not reproduce because the items are no longer the map they were rendered
     * over.
     */
    private static final String OTHER_ITEMS_REPORT = "The items {0} iterates no longer hand out their elements the way the {1,number,#} rows of"
            + " this view were rendered over them - a map hands them out by key and every other items by position - so those rows are not"
            + " reproduced and a value submitted for one of them is decoded by nothing. The items a c:forEach iterated must still be what its"
            + " rows were rendered over while the postback which submits them is restored.";

    /**
     * Reported for an iteration whose rows cannot be reproduced because the keys they were rendered for cannot be
     * saved with the state.
     */
    private static final String UNSAVABLE_KEYS_REPORT = "The entries {0} iterates are keyed by {1}, which is not serializable, so the rows of"
            + " this iteration are not reproduced when the postback which submits them is restored and a value submitted for one of them is"
            + " decoded by nothing. Everything the state holds has to be serializable, since a session is serialized as well when it is"
            + " passivated or fails over - a key which is serializable but holds something which is not fails later instead, while the state"
            + " is written.";

    private final TagAttribute begin;

    private final TagAttribute end;

    private final TagAttribute items;

    private final TagAttribute step;

    private final TagAttribute tranzient;

    private final TagAttribute var;

    private final TagAttribute varStatus;

    /**
     * Whether the keys of this iteration have been reported as unsavable. Their type is what makes them so, which does
     * not change, so reporting it once says everything every further build of this tag would say again.
     */
    private volatile boolean unsavableKeysReported;

    // Request-attribute key prefix under which an indexable c:forEach records, at its restore-time (re)build, the
    // iteration it produced (range plus, for items, the element snapshot) and the child components it created. When
    // the same view is built a second time in the same request (render under partial state saving) with an unchanged
    // iteration, that subtree is still correct - the index-based var expressions render content changes live - so the
    // redundant re-apply of the body is skipped and the retained children are un-marked from the parent refresh's
    // pending deletion. Keyed additionally by parent identity + tag location.
    private static final String ITERATION_STATE = "com.sun.faces.facelets.forEachIterationState:";

    // Indices into the range saved for an iteration: what it iterated over and how many iterations it produced.
    // Replaying those four is what makes the build restoring a view produce the rows that were rendered, each var
    // reference still reading its own element live from the items the model holds now - by position, or by the key
    // saved beside the range where the iteration was over a map.
    private static final int BEGIN = 0;
    private static final int END = 1;
    private static final int STEP = 2;
    private static final int COUNT = 3;
    private static final int TAG_LINE = 4;
    private static final int TAG_COLUMN = 5;
    private static final int RANGE_LENGTH = 6;

    // Indices into the decision of an iteration which is reproduced by key: the range beside the keys it iterated.
    private static final int RANGE = 0;
    private static final int KEYS = 1;
    private static final int PARTS = 2;

    /**
     * The iteration count of a build which reproduces no earlier one, so iterates as far as the items reach.
     */
    private static final int NO_REPLAY = -1;

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

        boolean t = getTransient(ctx);
        Object src = null;
        ValueExpression srcVE = null;
        if (items != null) {
            srcVE = items.getValueExpression(ctx, Object.class);
            src = srcVE.getValue(ctx);
        }

        // Reproduce the iteration the build which rendered the view produced, so that a value submitted for a row it
        // held is decoded by that row rather than by nothing. Only the range, the row count and, over a map, the keys
        // are saved, never the elements: each row reads its own element live, by position or by key, so that it edits
        // what the model holds now rather than a copy of what it held. A row whose element the model no longer holds
        // reads a stand-in for it instead - see RestoredItemValueExpression - and is reported below.
        String decisionKey = isReplayable() ? buildTimeDecisionKey(ctx) : null;
        Object rendered = replayBuildTimeDecision(ctx, decisionKey);
        int[] renderedRange = renderedRange(rendered);
        Object[] renderedKeys = renderedKeys(rendered);
        int notReproduced = 0;

        if (renderedRange != null && renderedKeys != null && renderedKeys.length != renderedRange[COUNT]) {
            // A key per row is what the rows are reproduced from, so a decision holding another number of them than
            // the rows it counts is not one this tag saved, whatever else it says.
            renderedRange = null;
            renderedKeys = null;
        }

        if (renderedRange != null && !isTagOf(renderedRange)) {
            // The keys of the decisions of a facelet edited since the response was rendered name other decisions than
            // they did, and what one tag then reads of another is a range and a row count with nothing to do with the
            // items it iterates - an iteration over items bounds its range by them, so it saves the largest range
            // there is where no end was given. Reproducing an iteration of another tag is reproducing nothing.
            renderedRange = null;
            renderedKeys = null;
        }

        if (renderedRange != null && src != null && (renderedKeys != null) != (src instanceof Map)) {
            // A map hands out its elements by key and every other items by position, so items which have become the
            // one where they were the other hold nothing the rows were rendered over: reproducing those rows anyway
            // would bind every one of them to an element which is not there, and the component a row is reproduced as
            // outlives the re-apply which follows the model. Items which are gone altogether are another matter - the
            // rows are reproduced over them and each reads the stand-in for the element it no longer reaches.
            notReproduced = renderedRange[COUNT];
            renderedRange = null;
            renderedKeys = null;
        }

        int s;
        int e;
        int m;
        int replayCount;

        if (renderedRange != null) {
            // The range this build iterates is the one which was iterated, never the one the attributes evaluate to
            // now: an expression which only held under what guarded it while the response was rendered is an
            // expression which fails to evaluate at all while the postback is restored.
            s = renderedRange[BEGIN];
            e = renderedRange[END];
            m = renderedRange[STEP];
            replayCount = renderedRange[COUNT];
        } else {
            s = getBegin(ctx);
            e = getEnd(ctx);
            m = getStep(ctx);
            replayCount = NO_REPLAY;
        }

        boolean replaying = replayCount != NO_REPLAY;

        Integer sO = begin != null ? s : null;
        Integer eO = end != null ? e : null;
        Integer mO = step != null ? m : null;

        if (items == null) {
            byte[] b = new byte[e + 1];
            for (int i = 0; i < b.length; i++) {
                b[i] = (byte) i;
            }
            src = b;
        }

        // The keys of the entries this build iterates, which is what reproduces its rows over a map: an entry is
        // reached by key rather than by position, so neither the count nor the range names the one a row was rendered
        // for. Collected only while the decisions are saved at all, since nothing else reads them.
        boolean keyed = decisionKey != null && srcVE != null && (replaying ? renderedKeys != null : src instanceof Map);
        List<Object> keys = keyed ? new ArrayList<>() : null;

        // See ITERATION_STATE. Retaining the built subtree across the redundant render-time re-apply is only safe when
        // the iteration is positionally indexable, so each var reference reads its element by position: items over a
        // List or array (IndexedValueExpression, a live index read) or a begin/end integer range (build-time-baked
        // value). A Map is not indexable - it iterates as a MappedValueExpression over an entry reached by key, with
        // no positional index to compare against - so it, and any other non-indexable Collection, is left to the
        // normal re-apply path.
        boolean indexable = src != null && (srcVE == null || isIndexable(src));
        FacesContext facesContext = ctx.getFacesContext();
        Map<Object, Object> contextAttributes = facesContext.getAttributes();
        String stateKey = null;
        int[] range = null;
        List<UIComponent> childrenBeforeBuild = null;
        int decisionsBeforeBody = 0;
        int unreproducibleMarksBeforeBody = 0;
        if (indexable) {
            recordDecisions(ctx, srcVE, src, s, e, m, t);
            if (replaying) {
                recordIterationCount(ctx, srcVE, s, e, m, replayCount);
            }
            stateKey = ITERATION_STATE + System.identityHashCode(parent) + ':' + tag.getLocation();
            range = new int[] { s, e, m };
            if (facesContext.getCurrentPhaseId() == PhaseId.RESTORE_VIEW) {
                // Restore-time (re)build under PSS rebuilds this transient subtree from scratch; snapshot the existing
                // children so the ones created below can be recorded for the render pass to retain, and the decisions
                // recorded so far so the ones the body records are recognizable: a body holding nested dynamic content
                // (a nested c:forEach/c:if/...) could change without this item list changing, so it must not be
                // skip-retained - only a fully static body is safe.
                childrenBeforeBuild = new ArrayList<>(parent.getChildren());
                decisionsBeforeBody = BuildTimeDecisions.size(facesContext);
                unreproducibleMarksBeforeBody = BuildTimeDecisions.unreproducibleMarks(facesContext);
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
        } else {
            markUnreproducibleBuild(ctx);
        }

        int count = 0;
        int vanished = 0;
        Class<?> unsavableKey = null;

        if (src != null || replaying) {
            // A keyed replay reaches every element by its key, so walking the entries would be for nothing.
            Iterator itr = replaying && renderedKeys != null ? null : toIterator(src);

            if (itr != null || replaying) {
                int i = 0;

                // move to start
                while (i < s && itr != null && itr.hasNext()) {
                    itr.next();
                    i++;
                }

                // The walk itself can neither start before the first element nor stand still: it skips to the start
                // by moving forward only, and it advances at least once per round whatever the step says.
                int firstIndex = Math.max(s, 0);
                int indexStep = Math.max(m, 1);

                if (replaying) {
                    // Items falling short of the rows that were rendered leave the iterator behind, while the rows
                    // themselves are reproduced whole, each still reading its own element where it read it.
                    i = firstIndex;
                }

                String v = getVarName(ctx);
                String vs = getVarStatusName(ctx);
                VariableMapper vars = ctx.getVariableMapper();
                ValueExpression ve = null;
                ValueExpression vO = capture(v, vars);
                ValueExpression vsO = capture(vs, vars);
                int mi = 0;
                Object value = null;

                IterationIdManager.startIteration(ctx);

                try {
                    boolean first = true;
                    while (replaying ? count < replayCount : i <= e && itr.hasNext()) {
                        count++;
                        value = itr != null && itr.hasNext() ? itr.next() : null;
                        Object rowKey = null;

                        if (srcVE != null && replaying) {
                            boolean keyedRow = renderedKeys != null;
                            Object position = keyedRow ? renderedKeys[count - 1] : i;
                            RestoredItemValueExpression element = restoredVarExpr(srcVE, src, position, s, keyedRow);

                            rowKey = position;

                            if (v != null && !t) {
                                ve = element;
                            }

                            if (!element.supplies(ctx)) {
                                vanished++;
                                value = RestoredItemValueExpression.vanishedItem(position, keyedRow);
                            } else if (keyedRow && (t || vs != null)) {
                                // A positional row is the one the iterator stands on, so only a keyed row has to read
                                // its element, and only where a transient var or a varStatus reads the element itself.
                                value = element.getValue(ctx);
                            }
                        } else if (srcVE != null) {
                            if (keys != null) {
                                rowKey = ((Map.Entry<?, ?>) value).getKey();
                            }
                            if (v != null && !t) {
                                ve = getVarExpr(srcVE, src, value, i, s);
                            }
                        }

                        if (keys != null) {
                            if (rowKey == null || rowKey instanceof Serializable) {
                                keys.add(rowKey);
                            } else {
                                // Without its keys the iteration cannot be reproduced at all: replaying its rows by
                                // position would reach every entry of a map by an index no entry of it answers to.
                                unsavableKey = rowKey.getClass();
                                keys = null;
                            }
                        }

                        // set the var
                        if (v != null) {
                            if (t || srcVE == null) {
                                ctx.setAttribute(v, value);
                            } else {
                                vars.setVariable(v, ve);
                            }
                        }

                        // set the varStatus
                        if (vs != null) {
                            boolean last = replaying ? count == replayCount : !itr.hasNext();
                            JstlIterationStatus itrS = new JstlIterationStatus(first, last, i, sO, eO, mO, value, count);
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
                        while (mi < m && itr != null && itr.hasNext()) {
                            itr.next();
                            mi++;
                            i++;
                        }
                        i++;

                        if (replaying) {
                            i = firstIndex + count * indexStep;
                        }

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

        if (vanished > 0 && LOGGER.isLoggable(WARNING)) {
            LOGGER.log(WARNING, VANISHED_ITEMS_REPORT, new Object[] { vanished, count, tag });
        }

        if (notReproduced > 0 && LOGGER.isLoggable(WARNING)) {
            LOGGER.log(WARNING, OTHER_ITEMS_REPORT, new Object[] { tag, notReproduced });
        }

        if (unsavableKey != null && !unsavableKeysReported && LOGGER.isLoggable(WARNING)) {
            unsavableKeysReported = true;
            LOGGER.log(WARNING, UNSAVABLE_KEYS_REPORT, new Object[] { tag, unsavableKey.getName() });
        }

        if (decisionKey != null && unsavableKey == null) {
            Location location = tag.getLocation();
            saveBuildTimeDecision(ctx, decisionKey, decision(new int[] { s, e, m, count, location.getLine(), location.getColumn() }, keys));
        }

        if (childrenBeforeBuild != null && count == iterationCount(src, s, e, m)) {
            // The body just applied recorded a decision of its own, or marked the build unreproducible, iff it holds
            // nested build-time-dynamic content.
            boolean nestedDynamic = BuildTimeDecisions.size(facesContext) > decisionsBeforeBody
                    || BuildTimeDecisions.unreproducibleMarks(facesContext) > unreproducibleMarksBeforeBody;

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
     * Whether one build of a view can iterate this tag differently than another, which a range of literals cannot: it
     * produces the same rows every time and there is nothing to replay.
     */
    private boolean isReplayable() {
        return items != null || isDynamic(begin) || isDynamic(end) || isDynamic(step);
    }

    /**
     * Whether the given range was saved by this tag rather than by another one of the same facelet, which is what it
     * is placed at that tells apart: an edit which moves a tag is an edit which moves what its decision describes.
     */
    private boolean isTagOf(int[] range) {
        Location location = tag.getLocation();
        return range[TAG_LINE] == location.getLine() && range[TAG_COLUMN] == location.getColumn();
    }

    /**
     * What this build iterated, to be saved with the state: the range it iterated over, how many iterations it
     * produced and, over a map, the key each of those iterations was produced for.
     */
    private static Object decision(int[] range, List<Object> keys) {
        return keys == null ? range : new Object[] { range, keys.toArray() };
    }

    /**
     * The range the build which rendered the view iterated over, plus how many iterations it produced.
     */
    private static int[] renderedRange(Object decision) {
        Object range = decision instanceof Object[] ? part((Object[]) decision, RANGE) : decision;
        return range instanceof int[] && ((int[]) range).length == RANGE_LENGTH ? (int[]) range : null;
    }

    /**
     * The key each iteration of the build which rendered the view was produced for, or {@code null} where its rows are
     * reproduced by position rather than by key.
     */
    private static Object[] renderedKeys(Object decision) {
        Object keys = decision instanceof Object[] ? part((Object[]) decision, KEYS) : null;
        return keys instanceof Object[] ? (Object[]) keys : null;
    }

    /**
     * The given part of a decision, or {@code null} where what stands under the key is not one this handler saved: the
     * keys of the decisions of a facelet edited since the response was rendered name other decisions than they did.
     */
    private static Object part(Object[] decision, int index) {
        return decision.length == PARTS ? decision[index] : null;
    }

    /**
     * The var expression of a row the build which restores the view reproduces: it reads its element from the items
     * the model holds now, at the position or under the key the row was rendered for, and reads a stand-in for it
     * where the model no longer holds it. Whether the row is reached by key follows from what was saved for the
     * iteration, never from the type of the position: a map may be keyed by the very type an index has.
     */
    private RestoredItemValueExpression restoredVarExpr(ValueExpression srcVE, Object src, Object position, int start, boolean keyed) {
        ValueExpression element;
        Access access;

        if (keyed) {
            element = new MappedValueExpression(srcVE, position);
            access = Access.KEYED;
        } else if (src instanceof Collection && !(src instanceof List)) {
            element = new IteratedValueExpression(srcVE, start, (Integer) position);
            access = Access.ITERATED;
        } else {
            element = new IndexedValueExpression(srcVE, (Integer) position);
            access = Access.INDEXED;
        }

        return new RestoredItemValueExpression(srcVE, element, position, access, start);
    }

    /**
     * Records what this build iterated over, so the redundant render-time re-apply is skipped as long as it would
     * iterate the same way and is performed when it would not (see {@code refreshTransientBuildOnPSS}). The item
     * sequence takes a decision of its own, since it is a comparison over every element rather than a single value;
     * the body's own decisions stand beside it, each captured with this iteration's index-based var expression and
     * therefore reading its own element live.
     */
    private void recordDecisions(FaceletContext ctx, ValueExpression srcVE, Object src, int begin, int end, int step, boolean tranzient) {
        recordBuildTimeDecision(ctx, this.begin, Integer.class, begin);
        recordBuildTimeDecision(ctx, this.end, Integer.class, end);
        recordBuildTimeDecision(ctx, this.step, Integer.class, step);
        recordBuildTimeDecision(ctx, this.tranzient, Boolean.class, tranzient);
        recordBuildTimeDecision(ctx, var);
        recordBuildTimeDecision(ctx, varStatus);

        if (srcVE != null) {
            ELContext elContext = ctx.getFacesContext().getELContext();
            List<Object> recordedItems = snapshotElements(src);
            recordBuildTimeDecision(ctx, () -> sameItems(recordedItems, srcVE.getValue(elContext)), Boolean.TRUE);
        }
    }

    /**
     * Records that this build produced the iterations the build which rendered the view produced rather than the ones
     * the items hold now, so that the render time re-apply which would produce another count is performed rather than
     * skipped. A begin/end range needs none of this: its count follows from the range, which is recorded already.
     */
    private void recordIterationCount(FaceletContext ctx, ValueExpression srcVE, int begin, int end, int step, int count) {
        if (srcVE != null) {
            ELContext elContext = ctx.getFacesContext().getELContext();
            recordBuildTimeDecision(ctx, () -> iterationCount(srcVE.getValue(elContext), begin, end, step), count);
        }
    }

    /**
     * Whether each element of the given items is read by position, which is what lets a row of the subtree built over
     * them read its own element live rather than from a value saved with the state.
     */
    private static boolean isIndexable(Object src) {
        return src instanceof List || src != null && src.getClass().isArray();
    }

    /**
     * How many iterations the given items produce over the given range, which is none where they are not the indexable
     * sequence they were iterated as: this is re-evaluated after the build, when they may hold anything at all. A
     * range which reaches before the first element or does not advance is counted the way the iteration walks it,
     * which is from the first element and one element at a time.
     */
    private static int iterationCount(Object src, int begin, int end, int step) {
        if (!isIndexable(src)) {
            return 0;
        }

        int first = Math.max(begin, 0);
        int by = Math.max(step, 1);
        int last = Math.min(end, elementCount(src) - 1);
        return last < first ? 0 : (last - first) / by + 1;
    }

    private static int elementCount(Object src) {
        return src instanceof List ? ((List<?>) src).size() : Array.getLength(src);
    }

    /**
     * Whether the current items are still the indexable sequence of elements that was recorded, which is what makes
     * the subtree built over them reproducible.
     */
    private static boolean sameItems(List<Object> recordedItems, Object currentItems) {
        return currentItems != null && (currentItems instanceof List || currentItems.getClass().isArray())
                && sameElements(recordedItems, currentItems);
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
