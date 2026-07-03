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

package org.glassfish.mojarra.application.view;

import static jakarta.faces.component.visit.VisitHint.SKIP_ITERATION;
import static jakarta.faces.component.visit.VisitResult.ACCEPT;
import static java.util.logging.Level.FINEST;
import static org.glassfish.mojarra.RIConstants.DYNAMIC_ACTIONS;
import static org.glassfish.mojarra.RIConstants.VIEW_REBUILT_AT_RENDER;
import static org.glassfish.mojarra.facelets.tag.faces.ComponentSupport.DYNAMIC_COMPONENT;
import static org.glassfish.mojarra.util.ComponentStruct.ADD;
import static org.glassfish.mojarra.util.ComponentStruct.REMOVE;
import static org.glassfish.mojarra.util.Util.isEmpty;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import org.glassfish.mojarra.context.StateContext;
import org.glassfish.mojarra.renderkit.RenderKitUtils;
import org.glassfish.mojarra.util.ComponentStruct;
import org.glassfish.mojarra.util.FacesLogger;
import org.glassfish.mojarra.util.MessageUtils;
import org.glassfish.mojarra.util.Util;

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
     * @param viewRoot the view root.
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
                    int childIndex = -1;
                    if (child.getAttributes().containsKey(DYNAMIC_COMPONENT)) {
                        childIndex = (Integer) child.getAttributes().get(DYNAMIC_COMPONENT);
                    }
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

                if (!hasDynamicActions && isViewRootOnlyState(context, viewRoot, state)) {
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
     * root's own state. The {@code DYNAMIC_ACTIONS} entry is bookkeeping rather than a component delta,
     * so it is excluded from the count.
     *
     * @param context the Faces context.
     * @param viewRoot the view root.
     * @param state the saved state map.
     * @return true if only the view root needs restoring, allowing the full restore traversal to be skipped.
     */
    private boolean isViewRootOnlyState(FacesContext context, UIViewRoot viewRoot, Map<String, Object> state) {
        int componentStateCount = state.containsKey(DYNAMIC_ACTIONS) ? state.size() - 1 : state.size();
        return componentStateCount == 0
                || componentStateCount == 1 && state.containsKey(viewRoot.getClientId(context));
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
        HashMap<String, UIComponent> componentMap = stateContext.getDynamicComponents();

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
                if (stateObj != null) {
                    stateMap.put(target.getClientId(context1.getFacesContext()), stateObj);
                }
            } else {
                return VisitResult.REJECT;
            }
            return result;
        });

        saveDynamicActions(context, stateContext, stateMap);
        StateContext.release(context);
        return new Object[] { null, stateMap };
    }
}
