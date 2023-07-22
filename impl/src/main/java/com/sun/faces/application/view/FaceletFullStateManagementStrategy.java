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
import static com.sun.faces.util.Util.isAnyNull;
import static com.sun.faces.util.Util.isEmpty;
import static com.sun.faces.util.Util.loadClass;
import static jakarta.faces.application.ProjectStage.Development;
import static jakarta.faces.component.visit.VisitHint.SKIP_ITERATION;
import static jakarta.faces.component.visit.VisitResult.ACCEPT;
import static jakarta.faces.component.visit.VisitResult.COMPLETE;
import static jakarta.faces.component.visit.VisitResult.REJECT;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.WARNING;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.context.StateContext;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.ComponentStruct;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.application.ViewHandler;
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
import jakarta.faces.view.ViewDeclarationLanguage;

/**
 * A state management strategy for FSS.
 *
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
public class FaceletFullStateManagementStrategy extends StateManagementStrategy {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = FacesLogger.APPLICATION_VIEW.getLogger();

    /**
     * Stores the skip hint.
     */
    private static final String SKIP_ITERATION_HINT = "jakarta.faces.visit.SKIP_ITERATION";

    /**
     * Stores the class map.
     */
    private Map<String, Class<?>> classMap;

    /**
     * Are we in development mode.
     */
    private boolean isDevelopmentMode;

    /**
     * Constructor.
     */
    public FaceletFullStateManagementStrategy() {
        this(FacesContext.getCurrentInstance());
    }

    /**
     * Constructor.
     *
     * @param context the Faces context.
     */
    public FaceletFullStateManagementStrategy(FacesContext context) {
        isDevelopmentMode = context.isProjectStage(Development);
        classMap = new ConcurrentHashMap<>(32);
    }

    /**
     * Capture the child.
     *
     * @param tree the tree.
     * @param parent the parent.
     * @param component the component.
     */
    private void captureChild(List<TreeNode> tree, int parent, UIComponent component) {

        if (!component.isTransient() && !component.getAttributes().containsKey(DYNAMIC_COMPONENT)) {
            TreeNode treeNode = new TreeNode(parent, component);
            int pos = tree.size();
            tree.add(treeNode);
            captureRest(tree, pos, component);
        }
    }

    /**
     * Capture the facet.
     *
     * @param tree the tree.
     * @param parent the parent.
     * @param name the facet name.
     * @param component the component.
     */
    private void captureFacet(List<TreeNode> tree, int parent, String name, UIComponent component) {

        if (!component.isTransient() && !component.getAttributes().containsKey(DYNAMIC_COMPONENT)) {
            FacetNode facetNode = new FacetNode(parent, name, component);
            int pos = tree.size();
            tree.add(facetNode);
            captureRest(tree, pos, component);
        }
    }

    /**
     * Capture the rest.
     *
     * @param tree the tree.
     * @param pos the position.
     * @param component the component.
     */
    private void captureRest(List<TreeNode> tree, int pos, UIComponent component) {

        int sz = component.getChildCount();
        if (sz > 0) {
            List<UIComponent> child = component.getChildren();
            for (int i = 0; i < sz; i++) {
                captureChild(tree, pos, child.get(i));
            }
        }

        sz = component.getFacetCount();
        if (sz > 0) {
            for (Map.Entry<String, UIComponent> entry : component.getFacets().entrySet()) {
                captureFacet(tree, pos, entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Find the given component in the component tree.
     *
     * @param context the Faces context.
     * @param clientId the client id of the component to find.
     */
    private UIComponent locateComponentByClientId(final FacesContext context, final UIComponent subTree, final String clientId) {
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.log(FINEST, "FaceletFullStateManagementStrategy.locateComponentByClientId", clientId);
        }

        final List<UIComponent> found = new ArrayList<>();
        UIComponent result = null;

        try {
            context.getAttributes().put(SKIP_ITERATION_HINT, true);
            Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);

            VisitContext visitContext = VisitContext.createVisitContext(context, null, hints);
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
        } finally {
            context.getAttributes().remove(SKIP_ITERATION_HINT);
        }

        if (!found.isEmpty()) {
            result = found.get(0);
        }

        return result;
    }

    /**
     * Create a new component instance.
     *
     * @param treeNode the tree node.
     * @return the UI component.
     * @throws FacesException when a serious error occurs.
     */
    private UIComponent newInstance(TreeNode treeNode) throws FacesException {
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.log(FINEST, "FaceletFullStateManagementStrategy.newInstance", treeNode.componentType);
        }

        try {
            Class<?> clazz = classMap != null ? classMap.get(treeNode.componentType) : null;
            if (clazz == null) {
                clazz = loadClass(treeNode.componentType, treeNode);
                if (!isAnyNull(clazz, classMap)) {
                    classMap.put(treeNode.componentType, clazz);
                } else {
                    if (!isDevelopmentMode) {
                        throw new NullPointerException();
                    }
                }
            }

            UIComponent component = (UIComponent) clazz.getDeclaredConstructor().newInstance();
            component.setId(treeNode.id);

            return component;
        } catch (NullPointerException | IllegalArgumentException | ReflectiveOperationException | SecurityException e) {
            throw new FacesException(e);
        }
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
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("FaceletFullStateManagementStrategy.pruneAndReAddToDynamicActions");
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
     * Restore the component state.
     *
     * @param context the Faces context.
     * @param state the component state.
     */
    private void restoreComponentState(final FacesContext context, final HashMap<String, Object> state) {

        final StateContext stateContext = StateContext.getStateContext(context);
        final UIViewRoot viewRoot = context.getViewRoot();

        try {
            context.getAttributes().put(SKIP_ITERATION_HINT, true);
            Set<VisitHint> hints = EnumSet.of(SKIP_ITERATION);
            VisitContext visitContext = VisitContext.createVisitContext(context, null, hints);

            viewRoot.visitTree(visitContext, (visitContext1, component) -> {
                VisitResult result = ACCEPT;

                String clientId = component.getClientId(context);
                Object stateObj = state.get(clientId);

                if (stateObj != null && !stateContext.componentAddedDynamically(component)) {
                    boolean restoreStateNow = true;
                    if (stateObj instanceof StateHolderSaver) {
                        restoreStateNow = !((StateHolderSaver) stateObj).componentAddedDynamically();
                    }
                    if (restoreStateNow) {
                        try {
                            component.restoreState(context, stateObj);
                        } catch (Exception e) {
                            throw new FacesException(e);
                        }
                    }
                }

                return result;
            });
        } finally {
            context.getAttributes().remove(SKIP_ITERATION_HINT);
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
    private void restoreDynamicActions(FacesContext context, StateContext stateContext, HashMap<String, Object> state) {
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("FaceletFullStateManagementStrategy.restoreDynamicActions");
        }

        @SuppressWarnings("unchecked")
        List<Object> savedActions = (List<Object>) context.getViewRoot().getAttributes().get(DYNAMIC_ACTIONS);
        List<ComponentStruct> actions = stateContext.getDynamicActions();

        if (!isEmpty(savedActions)) {
            for (Object savedAction : savedActions) {
                ComponentStruct action = new ComponentStruct();
                action.restoreState(context, savedAction);
                if (ADD.equals(action.getAction())) {
                    restoreDynamicAdd(context, state, action);
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
            LOGGER.finest("FaceletFullStateManagementStrategy.restoreDynamicAdd");
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
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("FaceletFullStateManagementStrategy.restoreDynamicRemove");
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
     * Restore the component tree.
     *
     * @param context the Faces context.
     * @param renderKitId the render kit id.
     * @param tree the saved tree.
     * @return the view root.
     * @throws FacesException when a serious error occurs.
     */
    private UIViewRoot restoreTree(FacesContext context, String renderKitId, Object[] tree) throws FacesException {

        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.log(FINEST, "FaceletFullStateManagementStrategy.restoreTree", renderKitId);
        }

        UIComponent component;
        FacetNode facetNode;
        TreeNode treeNode;
        for (int i = 0; i < tree.length; i++) {
            if (tree[i] instanceof FacetNode) {
                facetNode = (FacetNode) tree[i];
                component = newInstance(facetNode);
                tree[i] = component;
                if (i != facetNode.getParent()) {
                    ((UIComponent) tree[facetNode.getParent()]).getFacets().put(facetNode.facetName, component);
                }

            } else {
                treeNode = (TreeNode) tree[i];
                component = newInstance(treeNode);
                tree[i] = component;
                if (i != treeNode.parent) {
                    ((UIComponent) tree[treeNode.parent]).getChildren().add(component);
                } else {
                    UIViewRoot viewRoot = (UIViewRoot) component;
                    context.setViewRoot(viewRoot);
                    viewRoot.setRenderKitId(renderKitId);
                }
            }
        }

        return (UIViewRoot) tree[0];
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
            LOGGER.log(Level.FINEST, "FaceletFullStateManagementStrategy.restoreView", new Object[] { viewId, renderKitId });
        }

        UIViewRoot result = null;

        ResponseStateManager rsm = RenderKitUtils.getResponseStateManager(context, renderKitId);
        Object[] state = (Object[]) rsm.getState(context, viewId);

        if (state != null && state.length >= 2) {
            /*
             * Restore the component tree.
             */
            if (state[0] != null) {
                result = restoreTree(context, renderKitId, ((Object[]) state[0]).clone());
                context.setViewRoot(result);
            }

            if (result != null) {
                StateContext stateContext = StateContext.getStateContext(context);
                stateContext.startTrackViewModifications(context, result);
                stateContext.setTrackViewModifications(false);

                try {
                    @SuppressWarnings("unchecked")
                    HashMap<String, Object> stateMap = (HashMap<String, Object>) state[1];
                    if (stateMap != null) {
                        /*
                         * Restore the component state.
                         */
                        restoreComponentState(context, stateMap);

                        /*
                         * Restore the dynamic actions.
                         */
                        restoreDynamicActions(context, stateContext, stateMap);
                    }
                } finally {
                    stateContext.setTrackViewModifications(true);
                }
            }
        }

        /*
         * Make sure the library contracts get setup as well.
         */
        ViewHandler viewHandler = context.getApplication().getViewHandler();
        ViewDeclarationLanguage vdl = viewHandler.getViewDeclarationLanguage(context, viewId);
        context.setResourceLibraryContracts(vdl.calculateResourceLibraryContracts(context, viewId));

        return result;
    }

    /**
     * Save the component state.
     *
     * @param context the Faces context.
     * @return the saved state.
     */
    private Object saveComponentState(FacesContext context) {

        final HashMap<String, Object> stateMap = new HashMap<>();
        final StateContext stateContext = StateContext.getStateContext(context);
        final UIViewRoot viewRoot = context.getViewRoot();
        final FacesContext finalContext = context;

        context.getAttributes().put(SKIP_ITERATION_HINT, true);
        Set<VisitHint> hints = EnumSet.of(SKIP_ITERATION);
        VisitContext visitContext = VisitContext.createVisitContext(context, null, hints);

        try {
            viewRoot.visitTree(visitContext, (context1, component) -> {
                VisitResult result = ACCEPT;
                Object stateObj;
                if (!component.isTransient()) {
                    if (stateContext.componentAddedDynamically(component)) {
                        component.getAttributes().put(DYNAMIC_COMPONENT, new Integer(getProperChildIndex(component)));
                        stateObj = new StateHolderSaver(finalContext, component);
                    } else {
                        stateObj = component.saveState(finalContext);
                    }
                    if (stateObj != null) {
                        stateMap.put(component.getClientId(finalContext), stateObj);
                    }
                } else {
                    result = REJECT;
                }

                return result;
            });
        } finally {
            context.getAttributes().remove(SKIP_ITERATION_HINT);
        }

        return stateMap;
    }

    /**
     * Save the dynamic actions.
     *
     * @param context the Faces context.
     * @param stateContext the state context.
     * @param stateMap the state.
     */
    private void saveDynamicActions(FacesContext context, StateContext stateContext, UIViewRoot viewRoot) {
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("FaceletFullStateManagementStrategy.saveDynamicActions");
        }

        List<ComponentStruct> actions = stateContext.getDynamicActions();
        HashMap<String, UIComponent> componentMap = stateContext.getDynamicComponents();

        if (actions != null) {
            List<Object> savedActions = new ArrayList<>(actions.size());
            for (ComponentStruct action : actions) {
                UIComponent component = componentMap.get(action.getClientId());
                if (component == null && context.isProjectStage(Development)) {
                    LOGGER.log(WARNING, "Unable to save dynamic action with clientId ''{0}'' because the UIComponent cannot be found", action.getClientId());
                }
                if (component != null) {
                    savedActions.add(action.saveState(context));
                }
            }
            viewRoot.getAttributes().put(DYNAMIC_ACTIONS, savedActions);
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
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.finest("FaceletFullStateManagementStrategy.saveView");
        }

        Object[] result;
        UIViewRoot viewRoot = context.getViewRoot();

        /*
         * Check uniqueness.
         */
        Util.checkIdUniqueness(context, viewRoot, new HashSet<>(viewRoot.getChildCount() << 1));

        /**
         * Save the dynamic actions.
         */
        StateContext stateContext = StateContext.getStateContext(context);
        saveDynamicActions(context, stateContext, viewRoot);

        /*
         * Save the component state.
         */
        Object state = saveComponentState(context);

        /*
         * Save the tree structure.
         */
        List<TreeNode> treeList = new ArrayList<>(32);
        captureChild(treeList, 0, viewRoot);
        Object[] tree = treeList.toArray();

        result = new Object[] { tree, state };
        StateContext.release(context);

        return result;
    }

    /**
     * Inner class used to store a facet in the saved component tree.
     */
    private static final class FacetNode extends TreeNode {

        /**
         * Stores the serial version UID.
         */
        private static final long serialVersionUID = -3777170310958005106L;

        /**
         * Stores the facet name.
         */
        private String facetName;

        /**
         * Constructor.
         */
        public FacetNode() {
        }

        /**
         * Constructor.
         *
         * @param parent the parent.
         * @param name the facet name.
         * @param c the component.
         */
        public FacetNode(int parent, String name, UIComponent c) {

            super(parent, c);
            facetName = name;
        }

        /**
         * Read the facet node in.
         *
         * @param in the object input.
         * @throws IOException when an I/O error occurs.
         * @throws ClassNotFoundException when the class could not be found.
         */
        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

            super.readExternal(in);
            facetName = in.readUTF();

        }

        /**
         * Write the facet node out.
         *
         * @param out the object output.
         * @throws IOException when an I/O error occurs.
         */
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {

            super.writeExternal(out);
            out.writeUTF(facetName);

        }
    }

    /**
     * Inner class used to store a node in the saved component tree.
     */
    private static class TreeNode implements Externalizable {

        /**
         * Stores the serial version UID.
         */
        private static final long serialVersionUID = -835775352718473281L;

        /**
         * Stores the NULL_ID constant.
         */
        private static final String NULL_ID = "";

        /**
         * Stores the component type.
         */
        private String componentType;

        /**
         * Stores the id.
         */
        private String id;

        /**
         * Stores the parent.
         */
        private int parent;

        /**
         * Constructor.
         */
        public TreeNode() {
        }

        /**
         * Constructor.
         *
         * @param parent the parent.
         * @param c the component.
         */
        public TreeNode(int parent, UIComponent c) {
            this.parent = parent;
            id = c.getId();
            componentType = c.getClass().getName();
        }

        /**
         * Read the tree node in.
         *
         * @param in the object input.
         * @throws IOException when an I/O error occurs.
         * @throws ClassNotFoundException when the class could not be found.
         */
        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

            parent = in.readInt();
            componentType = in.readUTF();
            id = in.readUTF();
            if (id.length() == 0) {
                id = null;
            }
        }

        /**
         * Write the tree node out.
         *
         * @param out the object output.
         * @throws IOException when an I/O error occurs.
         */
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {

            out.writeInt(parent);
            out.writeUTF(componentType);
            if (id != null) {
                out.writeUTF(id);
            } else {
                out.writeUTF(NULL_ID);
            }
        }

        public int getParent() {
            return parent;
        }
    }

    /**
     * Helper method that determines what the index of the given child component will be taking transient siblings into
     * account.
     *
     * @param component the UI component.
     * @return the calculated index.
     */
    private int getProperChildIndex(UIComponent component) {
        int result = -1;

        if (component.getParent().getChildren().contains(component)) {
            UIComponent parent = component.getParent();
            int index = 0;
            Iterator<UIComponent> iterator = parent.getChildren().iterator();
            while (iterator.hasNext()) {
                UIComponent child = iterator.next();
                if (child == component) {
                    break;
                } else {
                    if (!child.isTransient()) {
                        index++;
                    }
                }
            }

            if (index == 0 && !parent.getChildren().isEmpty() && parent.getChildren().get(0).isTransient()) {
                index = -1;
            }

            result = index;
        }

        return result;
    }
}
