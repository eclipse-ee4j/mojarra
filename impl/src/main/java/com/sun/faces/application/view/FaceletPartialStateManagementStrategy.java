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

import static com.sun.faces.RIConstants.DYNAMIC_ACTIONS;
import static com.sun.faces.RIConstants.DYNAMIC_COMPONENT;
import static com.sun.faces.util.ComponentStruct.ADD;
import static com.sun.faces.util.ComponentStruct.REMOVE;
import static com.sun.faces.util.Util.isEmpty;
import static jakarta.faces.component.visit.VisitHint.SKIP_ITERATION;
import static jakarta.faces.component.visit.VisitResult.ACCEPT;
import static jakarta.faces.component.visit.VisitResult.COMPLETE;
import static jakarta.faces.component.visit.VisitResult.REJECT;
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
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.ComponentStruct;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
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
     * Find the given component in the component tree.
     *
     * @param context the Faces context.
     * @param clientId the client id of the component to find.
     */
    private UIComponent locateComponentByClientId(final FacesContext context, final UIComponent subTree, final String clientId) {
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.log(FINEST, "FaceletPartialStateManagementStrategy.locateComponentByClientId", clientId);
        }

        final List<UIComponent> found = new ArrayList<>();
        UIComponent result = null;

        VisitContext visitContext = VisitContext.createVisitContext(context, null, SKIP_ITERATION_HINT);
        subTree.visitTree(visitContext, (visitContext1, component) -> {
            VisitResult result1 = ACCEPT;
            if (component.getClientId(visitContext1.getFacesContext()).equals(clientId)) {
                /*
                 * If the client id matches up we have found our match.
                 */
                found.add(component);
                result1 = COMPLETE;
            } else if (component instanceof UIForm) {
                /*
                 * If the component is a UIForm and it is prepending its id then we can short circuit out of here if the the client id
                 * of the component we are trying to find does not begin with the id of the UIForm.
                 */
                UIForm form = (UIForm) component;
                if (form.isPrependId() && !clientId.startsWith(form.getClientId(visitContext1.getFacesContext()))) {
                    result1 = REJECT;
                }
            } else if (component instanceof NamingContainer && !clientId.startsWith(component.getClientId(visitContext1.getFacesContext()))) {
                /*
                 * If the component is a naming container then assume it is prepending its id so if our client id we are looking for
                 * does not start with the naming container id we can skip visiting this tree.
                 */
                result1 = REJECT;
            }

            return result1;
        });

        if (!found.isEmpty()) {
            result = found.get(0);
        }

        return result;
    }

    /**
     * Methods that takes care of pruning and re-adding an action to the dynamic action list.
     *
     * <p>
     * If you remove a component, re-add it to the same parent and then remove it again, you only have to capture the FIRST
     * remove. Similarly if you add a component, remove it, and then re-add it to the same parent you only need to capture
     * the LAST add.
     * </p>
     *
     * @param dynamicActionList the dynamic action list.
     * @param struct the component struct to add.
     */
    private void pruneAndReAddToDynamicActions(List<ComponentStruct> dynamicActionList, ComponentStruct struct) {
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("FaceletPartialStateManagementStrategy.pruneAndReAddToDynamicActions");
        }

        int firstIndex = dynamicActionList.indexOf(struct);
        if (firstIndex == -1) {
            dynamicActionList.add(struct);
        } else {
            int lastIndex = dynamicActionList.lastIndexOf(struct);
            if (lastIndex == -1 || lastIndex == firstIndex) {
                dynamicActionList.add(struct);
            } else {
                if (ADD.equals(struct.getAction())) {
                    dynamicActionList.remove(lastIndex);
                    dynamicActionList.remove(firstIndex);
                    dynamicActionList.add(struct);
                }
                if (REMOVE.equals(struct.getAction())) {
                    dynamicActionList.remove(lastIndex);
                }
            }
        }
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
            for (Object savedAction : savedActions) {
                ComponentStruct action = new ComponentStruct();
                action.restoreState(context, savedAction);
                if (ADD.equals(action.getAction())) {
                    restoreDynamicAdd(context, stateMap, action);
                }
                if (REMOVE.equals(action.getAction())) {
                    restoreDynamicRemove(context, action);
                }
                pruneAndReAddToDynamicActions(actions, action);
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
    private void restoreDynamicAdd(FacesContext context, Map<String, Object> state, ComponentStruct struct) {
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("FaceletPartialStateManagementStrategy.restoreDynamicAdd");
        }

        UIComponent parent = locateComponentByClientId(context, context.getViewRoot(), struct.getParentClientId());

        if (parent != null) {
            UIComponent child = locateComponentByClientId(context, parent, struct.getClientId());

            /*
             * If Facelets engine restored the child before us we are going to use it, but we need to remove it before we can add it
             * in the correct place.
             */
            if (child != null) {
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
                if (struct.getFacetName() != null) {
                    parent.getFacets().put(struct.getFacetName(), child);
                } else {
                    int childIndex = -1;
                    if (child.getAttributes().containsKey(DYNAMIC_COMPONENT)) {
                        childIndex = (Integer) child.getAttributes().get(DYNAMIC_COMPONENT);
                    }
                    child.setId(struct.getId());
                    if (childIndex >= parent.getChildCount() || childIndex == -1) {
                        parent.getChildren().add(child);
                    } else {
                        parent.getChildren().add(childIndex, child);
                    }
                    child.getClientId();
                }
                child.getAttributes().put(DYNAMIC_COMPONENT, child.getParent().getChildren().indexOf(child));
                stateContext.getDynamicComponents().put(struct.getClientId(), child);
            }
        }
    }

    /**
     * Method that takes care of restoring a dynamic remove.
     *
     * @param context the Faces context.
     * @param struct the component struct.
     */
    private void restoreDynamicRemove(FacesContext context, ComponentStruct struct) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("FaceletPartialStateManagementStrategy.restoreDynamicRemove");
        }

        UIComponent child = locateComponentByClientId(context, context.getViewRoot(), struct.getClientId());
        if (child != null) {
            StateContext stateContext = StateContext.getStateContext(context);
            stateContext.getDynamicComponents().put(struct.getClientId(), child);
            UIComponent parent = child.getParent();
            if (struct.getFacetName() != null) {
            	parent.getFacets().remove(struct.getFacetName());
            } else {
            	parent.getChildren().remove(child);
            }
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
            } finally {
                stateContext.setTrackViewModifications(true);
            }
        } else {
            viewRoot = null;
        }
        context.setProcessingEvents(processingEvents);
        return viewRoot;
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
            List<Object> savedActions = new ArrayList<>(actions.size());
            for (ComponentStruct action : actions) {
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

        Util.checkIdUniqueness(context, viewRoot, new HashSet<>(viewRoot.getChildCount() << 1));

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
