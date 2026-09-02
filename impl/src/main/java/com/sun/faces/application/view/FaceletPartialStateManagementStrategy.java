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

package com.sun.faces.application.view;

import static com.sun.faces.RIConstants.BUILD_TIME_DECISIONS;
import static com.sun.faces.RIConstants.DYNAMIC_ACTIONS;
import static com.sun.faces.RIConstants.DYNAMIC_COMPONENT;
import static com.sun.faces.RIConstants.RENDERED_TAGS;
import static com.sun.faces.RIConstants.VIEW_REBUILT_AT_RENDER;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.RestoreBuildTimeDecisions;
import static com.sun.faces.util.ComponentStruct.ADD;
import static com.sun.faces.util.ComponentStruct.REMOVE;
import static com.sun.faces.util.Util.isEmpty;
import static jakarta.faces.component.visit.VisitHint.SKIP_ITERATION;
import static jakarta.faces.component.visit.VisitResult.ACCEPT;
import static java.util.logging.Level.FINEST;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.context.StateContext;
import com.sun.faces.facelets.tag.SavedBuildTimeDecisions;
import com.sun.faces.facelets.tag.faces.ComponentSupport;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.ComponentStruct;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.ResponseStateManager;
import jakarta.faces.view.StateManagementStrategy;

/**
 * The state management strategy for PSS.
 *
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
public class FaceletPartialStateManagementStrategy extends StateManagementStrategy {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = FacesLogger.APPLICATION_VIEW.getLogger();

    /**
     * Stores the skip hint.
     */
    private static final Set<VisitHint> SKIP_ITERATION_HINT = EnumSet.of(SKIP_ITERATION);

    /**
     * Stores the skip and lifecycle hints.
     */
    private static final Set<VisitHint> SKIP_ITERATION_AND_EXECUTE_LIFECYCLE_HINTS = EnumSet.of(VisitHint.SKIP_ITERATION, VisitHint.EXECUTE_LIFECYCLE);

    /**
     * How many client ids at most are named when reporting a mismatch between the rendered and the rebuilt view.
     */
    private static final int MAX_REPORTED_CLIENT_IDS = 10;

    /**
     * What resolves a mismatch between the rendered and the rebuilt view, appended to the report of either.
     */
    private static final String REMEDY = " A build time condition such as c:if, c:choose, a c:forEach range or a variable ui:include path must"
            + " evaluate to the same value while the postback is restored as it did while the response was rendered, and the items a c:forEach"
            + " iterated must still hold the rows it produced. Hold what they depend on in a @ViewScoped bean, or recompute it from the relevant"
            + " request parameters in the @PostConstruct of the request scoped one, or set the " + RestoreBuildTimeDecisions.getQualifiedName()
            + " context parameter to true.";

    /**
     * Reported for the components the rendered view held which the rebuilt one does not.
     */
    static final String NOT_REBUILT_REPORT = "The view rebuilt for this postback does not hold {0} of the components the response was rendered"
            + " from, so their state is restored into nothing: {1}. Of these, {2} carry a submitted value which is therefore decoded by"
            + " nothing: {3}." + REMEDY;

    /**
     * Reported for the client ids another tag occupies in the rebuilt view than the one that rendered them.
     */
    static final String REBUILT_FROM_ANOTHER_TAG_REPORT = "The view rebuilt for this postback holds {0} client ids built from another tag than"
            + " the one the response was rendered from, so the state saved by one tag is restored into the component of another: {1}. The"
            + " facelet may also have been edited since the response was rendered, which moves every tag." + REMEDY;

    /**
     * Constructor.
     */
    public FaceletPartialStateManagementStrategy() {
        this(FacesContext.getCurrentInstance());
    }

    /**
     * Constructor.
     *
     * @param context the Faces context.
     */
    public FaceletPartialStateManagementStrategy(FacesContext context) {
    }

    /**
     * Builds a {@code clientId -> component} index over the given subtree in a single {@code visitTree} pass.
     *
     * <p>
     * Dynamic-action replay resolves each action's parent and child by client id against this index in O(1), so
     * replay is O(n) in the number of dynamically added components; a per-action {@code visitTree} would be O(n&sup2;).
     * </p>
     *
     * @param context the Faces context.
     * @param root the subtree to index.
     * @return a mutable {@code clientId -> component} map; callers keep it in sync as components are added/removed.
     */
    private Map<String, UIComponent> buildClientIdIndex(FacesContext context, UIComponent root) {
        Map<String, UIComponent> index = new HashMap<>();
        indexSubtree(context, root, index);
        return index;
    }

    /**
     * Adds {@code root} and every descendant to the {@code clientId -> component} index. Used both to build the
     * initial index and to register a subtree that replay adds, so a later action targeting a descendant (a
     * dynamically added component nested under another) resolves against the live tree rather than restoring a
     * duplicate.
     *
     * @param context the Faces context.
     * @param root the subtree to index.
     * @param index the index to populate.
     */
    private void indexSubtree(FacesContext context, UIComponent root, Map<String, UIComponent> index) {
        VisitContext visitContext = VisitContext.createVisitContext(context, null, SKIP_ITERATION_HINT);
        root.visitTree(visitContext, (visitContext1, component) -> {
            index.put(component.getClientId(visitContext1.getFacesContext()), component);
            return ACCEPT;
        });
    }

    /**
     * Restore the list of dynamic actions and replay them.
     *
     * @param context the Faces context.
     * @param stateContext the state context.
     * @param stateMap the state.
     */
    private void restoreDynamicActions(FacesContext context, StateContext stateContext, Map<String, Object> stateMap) {
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("FaceletPartialStateManagementStrategy.restoreDynamicActions");
        }

        @SuppressWarnings("unchecked")
        List<Object> savedActions = (List<Object>) stateMap.get(DYNAMIC_ACTIONS);
        List<ComponentStruct> actions = stateContext.getDynamicActions();

        if (!isEmpty(savedActions)) {
            // Index the tree once (O(n)) so each action resolves its parent/child in O(1), keeping replay O(n);
            // kept in sync as components are added/removed below.
            Map<String, UIComponent> componentIndex = buildClientIdIndex(context, context.getViewRoot());
            for (Object savedAction : savedActions) {
                ComponentStruct action = new ComponentStruct();
                action.restoreState(context, savedAction);
                if (ADD.equals(action.getAction())) {
                    restoreDynamicAdd(context, stateMap, action, componentIndex);
                }
                if (REMOVE.equals(action.getAction())) {
                    restoreDynamicRemove(context, action, componentIndex);
                }
                // The saved list is already pruned (see saveDynamicActions), so just rebuild the live list in order.
                actions.add(action);
            }
        }
    }

    /**
     * Returns the index the given action's child must be restored at. The action carries it, as the component
     * does not necessarily survive to hold it: a facelet-created child which was dynamically moved to another
     * parent is deleted and recreated by the facelet refresh, losing its marker. The marker on the component
     * is only consulted for state saved before the action carried the index.
     *
     * @param struct the component struct.
     * @param child the child being restored.
     * @return the index within the parent's children, or {@link ComponentStruct#APPEND} to append.
     */
    private static int indexOf(ComponentStruct struct, UIComponent child) {
        if (struct.getIndex() != ComponentStruct.APPEND) {
            return struct.getIndex();
        }
        if (child.getAttributes().get(DYNAMIC_COMPONENT) instanceof Integer index) {
            return index;
        }
        return ComponentStruct.APPEND;
    }

    /**
     * Method that takes care of restoring a dynamic add.
     *
     * @param context the Faces context.
     * @param state the state.
     * @param struct the component struct.
     */
    private void restoreDynamicAdd(FacesContext context, Map<String, Object> state, ComponentStruct struct, Map<String, UIComponent> componentIndex) {
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("FaceletPartialStateManagementStrategy.restoreDynamicAdd");
        }

        UIComponent parent = componentIndex.get(struct.getParentClientId());

        if (parent != null) {
            UIComponent child = componentIndex.get(struct.getClientId());

            /*
             * The buildView reapply that runs earlier in restoreView preserves programmatically-added
             * children across the facelet refresh in their original order, so the child often already sits
             * correctly under its parent. In that case skip the remove + re-add: getChildren().remove and
             * getChildren().add each scan for the child (O(n) indexOf), i.e. O(n^2) over a large dynamic
             * subtree, only to reproduce the position it already holds.
             */
            boolean alreadyInPlace = child != null && struct.getFacetName() == null && child.getParent() == parent;

            /*
             * If Facelets engine restored the child before us we are going to use it, but we need to remove it before we can add it
             * in the correct place.
             */
            if (child != null && !alreadyInPlace) {
                if (struct.getFacetName() == null) {
                    parent.getChildren().remove(child);
                } else {
                    parent.getFacets().remove(struct.getFacetName());
                }
            }

            /*
             * The child was not build previously, so we are going to check if the component was saved in the state.
             */
            if (child == null) {
                StateHolderSaver saver = (StateHolderSaver) state.get(struct.getClientId());
                if (saver != null) {
                    child = (UIComponent) saver.restore(context);
                }
            }

            /*
             * Are we adding =BACK= in a component that was not in the state, because it was added by the initial buildView and
             * removed by another dynamic action?
             */
            StateContext stateContext = StateContext.getStateContext(context);
            if (child == null) {
                child = stateContext.getDynamicComponents().get(struct.getClientId());
            }

            /*
             * Now if we have the child we are going to add it back in.
             */
            if (child != null) {
                if (alreadyInPlace) {
                    // Child survived the facelet refresh under this parent; leave it where it is. The
                    // bookkeeping below still runs.
                } else if (struct.getFacetName() != null) {
                    parent.getFacets().put(struct.getFacetName(), child);
                    child.getAttributes().put(DYNAMIC_COMPONENT, child.getParent().getChildren().indexOf(child));
                } else {
                    int childIndex = indexOf(struct, child);
                    child.setId(struct.getId());
                    int storedIndex;
                    if (childIndex >= parent.getChildCount() || childIndex == -1) {
                        parent.getChildren().add(child);
                        storedIndex = parent.getChildCount() - 1;
                    } else {
                        parent.getChildren().add(childIndex, child);
                        storedIndex = childIndex;
                    }
                    child.getClientId();
                    // Position the child was added at; avoids an O(n) getChildren().indexOf(child) per dynamic add.
                    child.getAttributes().put(DYNAMIC_COMPONENT, storedIndex);
                }
                stateContext.getDynamicComponents().put(struct.getClientId(), child);
                if (child.getChildCount() == 0 && child.getFacetCount() == 0) {
                    componentIndex.put(struct.getClientId(), child);
                } else {
                    // A subtree was (re)added: index its descendants too, so a later action targeting one of them
                    // resolves to the live component instead of restoring a duplicate.
                    indexSubtree(context, child, componentIndex);
                }
            }
        }
    }

    /**
     * Method that takes care of restoring a dynamic remove.
     *
     * @param context the Faces context.
     * @param struct the component struct.
     */
    private void restoreDynamicRemove(FacesContext context, ComponentStruct struct, Map<String, UIComponent> componentIndex) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("FaceletPartialStateManagementStrategy.restoreDynamicRemove");
        }

        UIComponent child = componentIndex.get(struct.getClientId());
        if (child != null) {
            StateContext stateContext = StateContext.getStateContext(context);
            stateContext.getDynamicComponents().put(struct.getClientId(), child);
            UIComponent parent = child.getParent();
            if (struct.getFacetName() != null) {
            	parent.getFacets().remove(struct.getFacetName());
            } else {
            	parent.getChildren().remove(child);
            }
            componentIndex.remove(struct.getClientId());
        }
    }

    /**
     * Restore the view.
     *
     * @param context the Faces context.
     * @param viewId the view id.
     * @param renderKitId the render kit id.
     * @return the view root.
     */
    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId, String renderKitId) {

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "FaceletPartialStateManagementStrategy.restoreView", new Object[] { viewId, renderKitId });
        }

        ResponseStateManager rsm = RenderKitUtils.getResponseStateManager(context, renderKitId);
        boolean processingEvents = context.isProcessingEvents();
        UIViewRoot viewRoot = context.getViewRoot();

        Object[] rawState = (Object[]) rsm.getState(context, viewId);
        if (rawState == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        final Map<String, Object> state = (Map<String, Object>) rawState[1];
        final StateContext stateContext = StateContext.getStateContext(context);

        if (state != null) {
            try {
                stateContext.setTrackViewModifications(false);
                // No saved dynamic actions => no component carries DYNAMIC_COMPONENT, so the per-component
                // marker lookup in componentAddedDynamically can be skipped across the whole restore traversal.
                @SuppressWarnings("unchecked")
                List<Object> savedActions = (List<Object>) state.get(DYNAMIC_ACTIONS);
                boolean hasDynamicActions = !isEmpty(savedActions);
                stateContext.setHasDynamicComponents(hasDynamicActions);

                if (!hasDynamicActions && isViewRootOnlyState(viewRoot.getClientId(context), state)) {
                    // No dynamic actions and no per-component delta (or only the view root's): restore the
                    // view root in isolation and skip the full O(N) restore traversal. Any component whose
                    // state actually changed carries a non-null delta in the map, which forces the walk via
                    // the else-branch, so this short-circuit is behavior-neutral for static / low-delta views.
                    restoreViewRootOnly(context, viewRoot, state);
                } else {
                    VisitContext visitContext = VisitContext.createVisitContext(context, null, SKIP_ITERATION_AND_EXECUTE_LIFECYCLE_HINTS);
                    viewRoot.visitTree(visitContext, (context1, target) -> {
                        VisitResult result = VisitResult.ACCEPT;
                        String cid = target.getClientId(context1.getFacesContext());
                        Object stateObj = state.get(cid);
                        if (stateObj != null && !stateContext.componentAddedDynamically(target)) {
                            boolean restoreStateNow = true;
                            if (stateObj instanceof StateHolderSaver) {
                                restoreStateNow = !((StateHolderSaver) stateObj).componentAddedDynamically();
                            }
                            if (restoreStateNow) {
                                try {
                                    target.restoreState(context1.getFacesContext(), stateObj);
                                } catch (Exception e) {
                                    String msg = MessageUtils.getExceptionMessageString(MessageUtils.PARTIAL_STATE_ERROR_RESTORING_ID, cid, e.toString());
                                    throw new FacesException(msg, e);
                                }
                            }
                        }

                        return result;
                    });
                    restoreDynamicActions(context, stateContext, state);
                }

                if (context.isProjectStage(ProjectStage.Development)) {
                    logRebuildMismatches(context, viewRoot, state);
                }
            } finally {
                stateContext.setHasDynamicComponents(true);
                stateContext.setTrackViewModifications(true);
            }
        } else {
            viewRoot = null;
        }
        context.setProcessingEvents(processingEvents);
        return viewRoot;
    }

    /**
     * Determine whether the saved state holds no per-component delta other than (optionally) the view
     * root's own state. The {@code DYNAMIC_ACTIONS}, {@code RENDERED_TAGS} and {@code BUILD_TIME_DECISIONS}
     * entries are bookkeeping rather than a component delta, so they are excluded from the count.
     *
     * @param viewRootClientId the client id of the view root.
     * @param state the saved state map.
     * @return true if only the view root needs restoring, allowing the full restore traversal to be skipped.
     */
    static boolean isViewRootOnlyState(String viewRootClientId, Map<String, Object> state) {
        int componentStateCount = state.size();
        if (state.containsKey(DYNAMIC_ACTIONS)) {
            componentStateCount--;
        }
        if (state.containsKey(RENDERED_TAGS)) {
            componentStateCount--;
        }
        if (state.containsKey(BUILD_TIME_DECISIONS)) {
            componentStateCount--;
        }
        return componentStateCount == 0
                || componentStateCount == 1 && state.containsKey(viewRootClientId);
    }

    /**
     * The tag a component was built from, which tells two components apart that share a client id because only one of
     * them is ever built.
     *
     * <p>
     * Only a component a facelet built carries one, and only such a component is worth comparing across the two
     * builds: a build time condition governs which tags are applied, so it can add or drop only what a tag built. A
     * component the facelet did not build - the view root, a component resource, anything added programmatically -
     * holds no tag, and one of those left without an id of its own does not even hold a stable client id:
     * {@link UIViewRoot#createUniqueId(FacesContext, String)} numbers it from a counter which the view root's state
     * restores past the number the rebuild reached, so it is numbered anew on every request.
     * </p>
     *
     * @param component the component.
     * @return the tag the given component was built from, or {@code null} when no facelet built it.
     */
    static String tagOf(UIComponent component) {
        return (String) component.getAttributes().get(ComponentSupport.MARK_CREATED);
    }

    /**
     * The client ids the rendered view held which the rebuilt one does not, so that the state saved for them is
     * restored into nothing and a value submitted for them is decoded by nothing.
     *
     * @param renderedTags the tag per client id of the view that rendered the response.
     * @param rebuiltTags the tag per client id of the view rebuilt for this postback.
     * @return the client ids that were not rebuilt.
     */
    static List<String> clientIdsNotRebuilt(Map<String, String> renderedTags, Map<String, String> rebuiltTags) {
        List<String> clientIds = new ArrayList<>();
        for (Map.Entry<String, String> renderedTag : renderedTags.entrySet()) {
            if (!rebuiltTags.containsKey(renderedTag.getKey())) {
                clientIds.add(renderedTag.getKey());
            }
        }
        return clientIds;
    }

    /**
     * The client ids another tag occupies in the rebuilt view than the one that rendered them, so that the state
     * saved by one tag is restored into the component of another. Two tags sharing a client id is legitimate as long
     * as only one of them is ever built, which a build time conditional deciding differently breaks.
     *
     * @param renderedTags the tag per client id of the view that rendered the response.
     * @param rebuiltTags the tag per client id of the view rebuilt for this postback.
     * @return the client ids that were rebuilt from another tag.
     */
    static List<String> clientIdsRebuiltFromAnotherTag(Map<String, String> renderedTags, Map<String, String> rebuiltTags) {
        List<String> clientIds = new ArrayList<>();
        for (Map.Entry<String, String> renderedTag : renderedTags.entrySet()) {
            String rebuiltTag = rebuiltTags.get(renderedTag.getKey());
            if (rebuiltTag != null && !rebuiltTag.equals(renderedTag.getValue())) {
                clientIds.add(renderedTag.getKey());
            }
        }
        return clientIds;
    }

    /**
     * Report where the view rebuilt for this postback holds another component than the one the response was rendered
     * from, which is what a build time conditional evaluating to another value than it did produces. The state of such
     * a component is restored into nothing or into the wrong component, and a value submitted for it is decoded by
     * neither, none of which fails loudly on its own.
     *
     * @param context the Faces context.
     * @param viewRoot the view root.
     * @param state the saved state map.
     */
    private void logRebuildMismatches(FacesContext context, UIViewRoot viewRoot, Map<String, Object> state) {
        @SuppressWarnings("unchecked")
        Map<String, String> renderedTags = (Map<String, String>) state.get(RENDERED_TAGS);
        if (renderedTags == null || !LOGGER.isLoggable(Level.WARNING)) {
            return;
        }

        Map<String, String> rebuiltTags = new HashMap<>(renderedTags.size());
        VisitContext visitContext = VisitContext.createVisitContext(context, null, SKIP_ITERATION_HINT);
        viewRoot.visitTree(visitContext, (context1, target) -> {
            if (target.isTransient()) {
                return VisitResult.REJECT;
            }
            String tag = tagOf(target);
            if (tag != null) {
                rebuiltTags.put(target.getClientId(context1.getFacesContext()), tag);
            }
            return ACCEPT;
        });

        List<String> notRebuilt = clientIdsNotRebuilt(renderedTags, rebuiltTags);
        if (!notRebuilt.isEmpty()) {
            Set<String> requestParameterNames = context.getExternalContext().getRequestParameterMap().keySet();
            List<String> submittedNotRebuilt = new ArrayList<>(notRebuilt);
            submittedNotRebuilt.retainAll(requestParameterNames);
            LOGGER.log(Level.WARNING, NOT_REBUILT_REPORT,
                    new Object[] { notRebuilt.size(), truncated(notRebuilt), submittedNotRebuilt.size(), truncated(submittedNotRebuilt) });
        }

        List<String> rebuiltFromAnotherTag = clientIdsRebuiltFromAnotherTag(renderedTags, rebuiltTags);
        if (!rebuiltFromAnotherTag.isEmpty()) {
            LOGGER.log(Level.WARNING, REBUILT_FROM_ANOTHER_TAG_REPORT,
                    new Object[] { rebuiltFromAnotherTag.size(), truncated(rebuiltFromAnotherTag) });
        }
    }

    /**
     * The given client ids, cut down to what is worth reading: a conditional over a collection can drop as many
     * components as the collection had elements, of which the first few say the same as all of them.
     *
     * @param clientIds the client ids.
     * @return the client ids to log.
     */
    static List<String> truncated(List<String> clientIds) {
        return clientIds.size() <= MAX_REPORTED_CLIENT_IDS ? clientIds : clientIds.subList(0, MAX_REPORTED_CLIENT_IDS);
    }

    /**
     * Restore only the view root's own state, skipping the per-component restore traversal.
     *
     * @param context the Faces context.
     * @param viewRoot the view root.
     * @param state the saved state map.
     */
    private void restoreViewRootOnly(FacesContext context, UIViewRoot viewRoot, Map<String, Object> state) {
        Object viewRootState = state.get(viewRoot.getClientId(context));
        if (viewRootState != null) {
            viewRoot.pushComponentToEL(context, viewRoot);
            try {
                viewRoot.restoreState(context, viewRootState);
            } finally {
                viewRoot.popComponentFromEL(context);
            }
        }
    }

    /**
     * Save the dynamic actions.
     *
     * @param context the Faces context.
     * @param stateContext the state context.
     * @param stateMap the state.
     */
    private void saveDynamicActions(FacesContext context, StateContext stateContext, Map<String, Object> stateMap) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("FaceletPartialStateManagementStrategy.saveDynamicActions");
        }

        List<ComponentStruct> actions = stateContext.getDynamicActions();
        Map<String, UIComponent> componentMap = stateContext.getDynamicComponents();

        if (actions != null) {
            // Actions are recorded raw (append-only) per event; collapse redundant add/remove pairs once here.
            List<ComponentStruct> prunedActions = StateContext.pruneDynamicActions(actions);
            List<Object> savedActions = new ArrayList<>(prunedActions.size());
            for (ComponentStruct action : prunedActions) {
                UIComponent component = componentMap.get(action.getClientId());
                if (component == null && context.isProjectStage(ProjectStage.Development)) {
                    LOGGER.log(Level.WARNING, "Unable to save dynamic action with clientId ''{0}'' because the UIComponent cannot be found",
                            action.getClientId());
                }
                if (component != null) {
                    // The tree walk above has just stamped each dynamically added component with the index it
                    // actually ended up at, which supersedes the one recorded when the action was fired.
                    if (ADD.equals(action.getAction()) && component.getAttributes().get(DYNAMIC_COMPONENT) instanceof Integer index) {
                        action.setIndex(index);
                    }
                    savedActions.add(action.saveState(context));
                }
            }
            stateMap.put(DYNAMIC_ACTIONS, savedActions);
        }
    }

    /**
     * Save the view.
     *
     * @param context the Faces context.
     * @return the saved view.
     */
    @Override
    public Object saveView(FacesContext context) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("FaceletPartialStateManagementStrategy.saveView");
        }

        if (context == null) {
            return null;
        }

        UIViewRoot viewRoot = context.getViewRoot();
        if (viewRoot.isTransient()) {
            return null;
        }

        // Skip the whole-tree duplicate-id walk when the render-time build skipped the facelet re-apply: the tree is
        // then identical to the one already validated when it was first built (see VIEW_REBUILT_AT_RENDER). A rebuilt
        // or freshly-built (GET / navigation / JSTL) tree, or an unset flag, still runs the check.
        if (!Boolean.FALSE.equals(context.getAttributes().get(VIEW_REBUILT_AT_RENDER))) {
            Util.checkIdUniqueness(context, viewRoot, new HashSet<>(64));
        }

        final Map<String, Object> stateMap = new HashMap<>();
        final StateContext stateContext = StateContext.getStateContext(context);
        final Map<String, String> renderedTags = context.isProjectStage(ProjectStage.Development) ? new HashMap<>() : null;

        VisitContext visitContext = VisitContext.createVisitContext(context, null, SKIP_ITERATION_HINT);
        final FacesContext finalContext = context;

        viewRoot.visitTree(visitContext, (context1, target) -> {
            VisitResult result = VisitResult.ACCEPT;
            Object stateObj;
            if (!target.isTransient()) {
                if (stateContext.componentAddedDynamically(target)) {
                    target.getAttributes().put(DYNAMIC_COMPONENT, target.getParent().getChildren().indexOf(target));
                    stateObj = new StateHolderSaver(finalContext, target);
                } else {
                    stateObj = target.saveState(context1.getFacesContext());
                }
                String tag = renderedTags != null ? tagOf(target) : null;
                if (tag != null || stateObj != null) {
                    String clientId = target.getClientId(context1.getFacesContext());
                    if (tag != null) {
                        renderedTags.put(clientId, tag);
                    }
                    if (stateObj != null) {
                        stateMap.put(clientId, stateObj);
                    }
                }
            } else {
                return VisitResult.REJECT;
            }
            return result;
        });

        if (renderedTags != null) {
            stateMap.put(RENDERED_TAGS, renderedTags);
        }

        SavedBuildTimeDecisions.save(context, stateMap);
        saveDynamicActions(context, stateContext, stateMap);
        StateContext.release(context);
        return new Object[] { null, stateMap };
    }
}
