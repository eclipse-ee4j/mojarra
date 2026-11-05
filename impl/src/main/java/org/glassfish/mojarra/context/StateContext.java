/*
 * Copyright (c) 2021, 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.mojarra.context;

import static org.glassfish.mojarra.RIConstants.DYNAMIC_CHILD_COUNT;
import static org.glassfish.mojarra.facelets.tag.faces.ComponentSupport.DYNAMIC_COMPONENT;
import static org.glassfish.mojarra.util.ComponentStruct.ADD;
import static org.glassfish.mojarra.util.ComponentStruct.REMOVE;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.event.PreRemoveFromViewEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;

import org.glassfish.mojarra.util.ComponentStruct;
import org.glassfish.mojarra.util.FacesLogger;

/**
 * Context for dealing with state saving mechanics.
 */
public class StateContext {

    private static final String KEY = StateContext.class.getName() + "_KEY";

    private boolean trackMods = true;
    // A view whose saved state carries no dynamic add/remove actions has no component bearing the
    // DYNAMIC_COMPONENT marker, so componentAddedDynamically can skip the per-component attribute
    // lookup. The state restore sets this false for such views; true (always check) otherwise.
    private boolean hasDynamicComponents = true;
    private DynamicAddRemoveListener modListener;
    private WeakReference<UIViewRoot> viewRootRef = new WeakReference<>(null);

    private static final Logger LOGGER = FacesLogger.CONTEXT.getLogger();

    // ------------------------------------------------------------ Constructors

    private StateContext() {
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * Release the state context.
     *
     * @param facesContext the Faces context.
     */
    public static void release(FacesContext facesContext) {
        StateContext stateContext = (StateContext) facesContext.getAttributes().get(KEY);
        UIViewRoot viewRoot = facesContext.getViewRoot();
        if (viewRoot != null && stateContext.modListener != null) {
            viewRoot.unsubscribeFromViewEvent(PostAddToViewEvent.class, stateContext.modListener);
            viewRoot.unsubscribeFromViewEvent(PreRemoveFromViewEvent.class, stateContext.modListener);
        }
        facesContext.getAttributes().remove(KEY);
    }

    /**
     * @param ctx the <code>FacesContext</code> for the current request
     * @return <code>StateContext</code> for this request
     */
    public static StateContext getStateContext(FacesContext ctx) {
        StateContext stateCtx = (StateContext) ctx.getAttributes().get(KEY);
        if (stateCtx == null) {
            stateCtx = new StateContext();
            ctx.getAttributes().put(KEY, stateCtx);
        }

        return stateCtx;
    }

    /**
     * @return <code>true</code> if view modifications outside of the initial construction of the view are being tracked.
     */
    public boolean trackViewModifications() {
        return trackMods;
    }

    /**
     * Installs a <code>SystemEventListener</code> on the <code>UIViewRoot</code> to track components added to or removed from the view.
     *
     * @param ctx the involved faces context
     * @param root the involved view root
     */
    public void startTrackViewModifications(FacesContext ctx, UIViewRoot root) {
        UIViewRoot refRoot = viewRootRef.get();
        if (root != refRoot) {
            viewRootRef = new WeakReference<>(root);

            // On the first call in the restore phase the view root is null, so the first change from null is not
            // a changing view root. Any later change is, and the new view needs its own DynamicAddRemoveListener.
            if (refRoot != null) {
                modListener = null;
            }
        }

        if (modListener == null) {
            if (root != null) {
                modListener = new DynamicAddRemoveListener(ctx);
                root.subscribeToViewEvent(PostAddToViewEvent.class, modListener);
                root.subscribeToViewEvent(PreRemoveFromViewEvent.class, modListener);
            }
            else {
                LOGGER.warning("Unable to attach DynamicAddRemoveListener to UIViewRoot because it is null");
            }
        }
        setTrackViewModifications(true);
    }

    /**
     * Toggles the current modification tracking status.
     *
     * @param trackMods if <code>true</code> and the listener installed by <code>startTrackViewModifications</code> is* present, then view modifications will be
     * tracked. If <code>false</code>, then modification events will be ignored.
     */
    public void setTrackViewModifications(boolean trackMods) {
        this.trackMods = trackMods;
    }

    /**
     * @param c the UIComponent to check
     * @return <code>true</code> if the component was added after the initial view construction
     */
    public boolean componentAddedDynamically(UIComponent c) {
        return hasDynamicComponents && c.getAttributes().containsKey(DYNAMIC_COMPONENT);
    }

    /**
     * Hint from the state-management strategy: when a restored view's saved state carries no dynamic add/remove actions, no component can bear the
     * {@code DYNAMIC_COMPONENT} marker, so {@link #componentAddedDynamically} skips the per-component attribute lookup. Defaults to {@code true} (always check)
     * for every other call path.
     *
     * @param hasDynamicComponents whether the current view may contain dynamically added/removed components
     */
    public void setHasDynamicComponents(boolean hasDynamicComponents) {
        this.hasDynamicComponents = hasDynamicComponents;
    }

    /**
     * Put the dynamically added children at the index they hold, in every parent the given ADD actions add a child to. The index of a dynamic child does not
     * count the children which the REMOVE actions remove. So this must run after all given actions are replayed.
     *
     * @param actions the replayed actions.
     * @param componentIndex the components of the view by client id.
     */
    public static void placeDynamicChildren(List<ComponentStruct> actions, Map<String, UIComponent> componentIndex) {
        Set<UIComponent> parents = null;
        for (ComponentStruct action : actions) {
            if (ADD.equals(action.getAction()) && action.getFacetName() == null) {
                UIComponent parent = componentIndex.get(action.getParentClientId());
                if (parent != null) {
                    if (parents == null) {
                        parents = Collections.newSetFromMap(new IdentityHashMap<>());
                    }
                    parents.add(parent);
                }
            }
        }
        if (parents != null) {
            for (UIComponent parent : parents) {
                placeDynamicChildren(parent);
            }
        }
    }

    /**
     * Put each dynamically added child of the given parent at the index it holds, between the other children in their current order. The children are put back
     * in the order of their index, so that each one lands at its own index whatever order an earlier step left them in.
     *
     * @param parent the parent whose dynamically added children to place.
     */
    public static void placeDynamicChildren(UIComponent parent) {
        if (!hasDynamicChild(parent)) {
            return;
        }

        List<UIComponent> children = parent.getChildren();
        // In the steady state every dynamic child is in place, and the O(dynamic * children) re-add below is skipped.
        if (allDynamicChildrenInPlace(children)) {
            return;
        }

        List<UIComponent> dynamicChildren = new ArrayList<>(children.size());
        for (UIComponent child : children) {
            if (isDynamicChild(child)) {
                dynamicChildren.add(child);
            }
        }
        for (UIComponent child : dynamicChildren) {
            children.remove(child);
        }
        dynamicChildren.sort(Comparator.comparingInt(StateContext::indexOfDynamicChild));

        for (UIComponent child : dynamicChildren) {
            int index = indexOfDynamicChild(child);
            if (index < children.size()) {
                children.add(index, child);
            }
            else {
                children.add(child);
            }
        }
    }

    /**
     * Mark {@code child} as dynamically added at the given index of its parent, and mark its parent as having a dynamically added child. Neither marker is
     * saved with the state, so each restore of a dynamic add sets them again from its {@link ComponentStruct}.
     *
     * @param child the dynamically added child, already attached to its parent.
     * @param index the index of the child within its parent's children, or {@link ComponentStruct#APPEND} for a facet.
     */
    public static void markDynamicallyAdded(UIComponent child, int index) {
        child.getAttributes().put(DYNAMIC_COMPONENT, index);
        markHasDynamicChild(child.getParent());
    }

    /**
     * Add the given action's child to the given parent at the index {@link #indexToRestore} returns, or append it when that index is beyond the children, and
     * mark it as dynamically added at that index. A child can be appended because its siblings before it are not there yet. It then still holds its own index,
     * and {@link #placeDynamicChildren(UIComponent)} puts it there.
     *
     * @param parent the parent to add the child to.
     * @param child the dynamically added child.
     * @param struct the component struct of the ADD action.
     */
    public static void addDynamicChild(UIComponent parent, UIComponent child, ComponentStruct struct) {
        int childIndex = indexToRestore(struct, child);
        child.setId(struct.getId());
        if (childIndex >= parent.getChildCount() || childIndex == ComponentStruct.APPEND) {
            parent.getChildren().add(child);
        }
        else {
            parent.getChildren().add(childIndex, child);
        }
        child.getClientId();
        markDynamicallyAdded(child, childIndex == ComponentStruct.APPEND ? parent.getChildCount() - 1 : childIndex);
    }

    /**
     * Mark the given action's child as dynamically added when it already sits where it belongs, so that the O(n) remove and re-add can be skipped. That is at
     * the index the child holds as a dynamically added child, as the Facelets refresh has put it back there, or else at the index {@link #indexToRestore}
     * returns. Or last when that index is beyond the other children.
     *
     * @param parent the parent of the action.
     * @param child the child of the action.
     * @param struct the component struct of the ADD action.
     * @return true when the child is already in place and has been marked.
     */
    public static boolean markIfAlreadyInPlace(UIComponent parent, UIComponent child, ComponentStruct struct) {
        if (child.getParent() != parent || parent.getChildCount() == 0) {
            return false;
        }
        List<UIComponent> children = parent.getChildren();
        int last = children.size() - 1;
        int index = isDynamicChild(child) ? indexOfDynamicChild(child) : indexToRestore(struct, child);
        int position = index == ComponentStruct.APPEND || index >= last ? last : index;
        if (children.get(position) != child) {
            return false;
        }
        markDynamicallyAdded(child, position);
        return true;
    }

    /**
     * Returns the index the given action's child must be restored or reapplied at. The action carries it, as the component does not necessarily survive to hold
     * it: a facelet-created child which was dynamically moved to another parent is deleted and recreated by the facelet refresh, losing its marker. The marker
     * on the component is only consulted when the action carries no index: state saved by an older version, or a child to append.
     *
     * @param struct the component struct.
     * @param child the child being restored or reapplied.
     * @return the index within the parent's children, or {@link ComponentStruct#APPEND} to append.
     */
    public static int indexToRestore(ComponentStruct struct, UIComponent child) {
        if (struct.getIndex() != ComponentStruct.APPEND) {
            return struct.getIndex();
        }
        if (child.getAttributes().get(DYNAMIC_COMPONENT) instanceof Integer index) {
            return index;
        }
        return ComponentStruct.APPEND;
    }

    /**
     * Mark {@code parent} as having a dynamically added child. {@link #placeDynamicChildren(UIComponent)} only places the dynamic children of a parent so
     * marked, and tests only for the key's presence, so this is a set-once marker, not a count.
     */
    private static void markHasDynamicChild(UIComponent parent) {
        Map<String, Object> attrs = parent.getAttributes();
        if (!attrs.containsKey(DYNAMIC_CHILD_COUNT)) {
            attrs.put(DYNAMIC_CHILD_COUNT, 1);
        }
    }

    /**
     * Get the dynamic list (of adds and removes).
     *
     * @return the dynamic list
     */
    public List<ComponentStruct> getDynamicActions() {
        return modListener != null ? modListener.getDynamicActions() : null;
    }

    /**
     * Get the hash map of dynamic components.
     *
     * @return the hash map of dynamic components.
     */
    public HashMap<String, UIComponent> getDynamicComponents() {
        return modListener != null ? modListener.getDynamicComponents() : null;
    }

    /**
     * Set the index each dynamically added child holds to the index it has now among the children of its parent. The Facelets refresh at render puts a
     * dynamically added child back at the index it holds, so this must run while the tree is still in the order the request left it.
     */
    public void syncIndexesOfDynamicChildren() {
        List<ComponentStruct> actions = getDynamicActions();
        if (actions != null && !actions.isEmpty()) {
            syncIndexesOfDynamicChildren(getDynamicComponents().values());
        }
    }

    /**
     * Set the index of each given dynamically added child, and of its dynamically added siblings, to the index it has now among the children of its parent. A
     * facet or a component which is not in the view anymore is skipped.
     *
     * @param components the dynamically added components.
     */
    static void syncIndexesOfDynamicChildren(Collection<UIComponent> components) {
        Set<UIComponent> parents = Collections.newSetFromMap(new IdentityHashMap<>());
        for (UIComponent component : components) {
            UIComponent parent = component.getParent();
            if (parent != null && isDynamicChild(component)) {
                parents.add(parent);
            }
        }

        for (UIComponent parent : parents) {
            List<UIComponent> children = parent.getChildren();
            for (int i = 0, size = children.size(); i < size; i++) {
                Map<String, Object> attributes = children.get(i).getAttributes();
                Object index = attributes.get(DYNAMIC_COMPONENT);
                if (isDynamicIndex(index) && (Integer) index != i) {
                    attributes.put(DYNAMIC_COMPONENT, i);
                }
            }
        }
    }

    private static boolean hasDynamicChild(UIComponent parent) {
        return parent.getAttributes().containsKey(DYNAMIC_CHILD_COUNT);
    }

    private static boolean allDynamicChildrenInPlace(List<UIComponent> children) {
        for (int index = 0, size = children.size(); index < size; index++) {
            UIComponent child = children.get(index);
            if (isDynamicChild(child) && indexOfDynamicChild(child) != index) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param component the component to check.
     * @return whether the given component was dynamically added as a child, as opposed to a facet, so that it holds the index
     * {@link #indexOfDynamicChild(UIComponent)} returns.
     */
    public static boolean isDynamicChild(UIComponent component) {
        return isDynamicIndex(component.getAttributes().get(DYNAMIC_COMPONENT));
    }

    /**
     * @param component a component for which {@link #isDynamicChild(UIComponent)} holds.
     * @return the index the given dynamically added child holds among the children of its parent.
     */
    public static int indexOfDynamicChild(UIComponent component) {
        return (Integer) component.getAttributes().get(DYNAMIC_COMPONENT);
    }

    private static boolean isDynamicIndex(Object index) {
        return index instanceof Integer && (Integer) index != ComponentStruct.APPEND;
    }

    /**
     * Collapses a raw, append-ordered dynamic action list into the minimal set to replay or save.
     *
     * <p>
     * Actions are recorded append-only per event (see {@code recordDynamicAction}); redundant add/remove pairs for the same client id are collapsed here in a
     * single O(n) pass rather than per event (a per-event prune is O(n&sup2;)). Per client id the net effect is one of: an ADD (added and still present),
     * nothing (added then removed again), a REMOVE (a pre-existing component removed), or a REMOVE followed by an ADD (pre-existing removed then re-added).
     * Each surviving action keeps its recorded position, so a component re-added under a new parent is replayed after that parent, and a REMOVE is replayed
     * before every ADD recorded after it.
     * </p>
     *
     * @param rawActions the dynamic actions in the order they were recorded, or {@code null}.
     * @return the pruned actions in replay order, or {@code null} if {@code rawActions} was {@code null}.
     */
    public static List<ComponentStruct> pruneDynamicActions(List<ComponentStruct> rawActions) {
        if (rawActions == null) {
            return null;
        }
        // Collapsing an ADD that a later REMOVE cancels (and coalescing a REMOVE with a re-ADD) for the same
        // client id can only happen when the list holds both an ADD and a REMOVE. A homogeneous list -- all
        // ADDs or all REMOVEs, i.e. the common bulk add or bulk clear in an action -- has nothing to collapse:
        // each action is already a distinct net add/remove. Skip the per-id map for it.
        boolean hasAdd = false, hasRemove = false;
        for (ComponentStruct action : rawActions) {
            if (ADD.equals(action.getAction())) {
                hasAdd = true;
            }
            else if (REMOVE.equals(action.getAction())) {
                hasRemove = true;
            }
            if (hasAdd && hasRemove) {
                break;
            }
        }
        if (!hasAdd || !hasRemove) {
            return rawActions;
        }
        // value[0] = effective REMOVE for the client id (or null); value[1] = effective ADD (or null)
        Map<String, ComponentStruct[]> netByClientId = new HashMap<>();
        for (ComponentStruct action : rawActions) {
            ComponentStruct[] net = netByClientId.computeIfAbsent(action.getClientId(), key -> new ComponentStruct[2]);
            if (ADD.equals(action.getAction())) {
                net[1] = action;
            }
            else if (net[1] != null) {
                net[1] = null; // an earlier ADD is cancelled by this REMOVE
            }
            else if (net[0] == null) {
                net[0] = action; // first REMOVE of a pre-existing component
            }
        }
        List<ComponentStruct> pruned = new ArrayList<>(rawActions.size());
        for (ComponentStruct action : rawActions) {
            ComponentStruct[] net = netByClientId.get(action.getClientId());
            if (action == net[0] || action == net[1]) {
                pruned.add(action);
            }
        }
        return pruned;
    }

    /**
     * Drops the iteration index of every iterating ancestor from the given client id, e.g. {@code form:table:1:group} becomes {@code form:table:group}.
     *
     * <p>
     * An action is recorded whenever the tree is modified, which can be while an iterating component has a row index set -- an add performed by a command
     * button inside a row, for instance, as {@link UIData#broadcast} sets the row index before the action runs. The client id then carries that row, but the
     * component does not: a component inside an iterating one is a single instance shared by every row, so the modification belongs to all of them and the row
     * says only when it happened. Recording it would key the action to a position which does not exist, which nothing can resolve it against afterwards.
     * </p>
     *
     * <p>
     * A numeric segment is unambiguous: a component id cannot be one, as {@link UIComponent#setId} requires the first character to be a letter or an
     * underscore. Only an iterating component contributes one.
     * </p>
     *
     * @param separatorChar the naming-container separator character.
     * @param clientId the client id to strip.
     * @return the given client id without the iteration index of any iterating ancestor.
     */
    static String stripIterationIndex(char separatorChar, String clientId) {
        StringBuilder builder = new StringBuilder(clientId.length());
        boolean stripped = false;
        int segmentStart = 0;

        for (int i = 0; i <= clientId.length(); i++) {
            if (i < clientId.length() && clientId.charAt(i) != separatorChar) {
                continue;
            }

            if (isIterationIndex(clientId, segmentStart, i)) {
                stripped = true;
            }
            else {
                if (builder.length() > 0) {
                    builder.append(separatorChar);
                }
                builder.append(clientId, segmentStart, i);
            }

            segmentStart = i + 1;
        }

        return stripped ? builder.toString() : clientId;
    }

    private static boolean isIterationIndex(String clientId, int start, int end) {
        if (start >= end) {
            return false;
        }

        for (int i = start; i < end; i++) {
            if (!Character.isDigit(clientId.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    // ---------------------------------------------------------- Nested Classes

    /**
     * A system event listener which is used to listen for changes on the component tree after restore view and before rendering out the view.
     */
    public class DynamicAddRemoveListener implements SystemEventListener {

        /**
         * Stores the state context we work for,
         */
        private StateContext stateCtx;

        /**
         * Stores the list of adds/removes.
         */
        private List<ComponentStruct> dynamicActions;
        /**
         * Stores the hash map of dynamic components.
         */
        private transient HashMap<String, UIComponent> dynamicComponents;

        /**
         * Constructor.
         *
         * @param context the Faces context.
         */
        public DynamicAddRemoveListener(FacesContext context) {
            stateCtx = StateContext.getStateContext(context);
        }

        /**
         * Process the add/remove event.
         *
         * @param event the add/remove event.
         * @throws AbortProcessingException when processing should be aborted.
         */
        @Override
        public void processEvent(SystemEvent event) throws AbortProcessingException {
            FacesContext ctx = FacesContext.getCurrentInstance();
            if (event instanceof PreRemoveFromViewEvent) {
                if (stateCtx.trackViewModifications()) {
                    handleRemove(ctx, ((PreRemoveFromViewEvent) event).getComponent());
                }
            }
            else {
                if (stateCtx.trackViewModifications()) {
                    handleAdd(ctx, ((PostAddToViewEvent) event).getComponent());
                }
            }
        }

        /**
         * Are we listening for these particular changes.
         *
         * <p>
         * Note we are only interested in UIComponent adds/removes that are not the UIViewRoot itself.
         * </p>
         *
         * @param source the source object we might be listening for.
         * @return true if the source is OK, false otherwise.
         */
        @Override
        public boolean isListenerForSource(Object source) {
            return source instanceof UIComponent && !(source instanceof UIViewRoot);
        }

        /**
         * Get the list of adds/removes.
         *
         * @return the list of adds/removes.
         */
        public List<ComponentStruct> getDynamicActions() {
            if (dynamicActions == null) {
                dynamicActions = new ArrayList<>();
            }

            return dynamicActions;
        }

        /**
         * Get the hash map of dynamic components.
         *
         * @return the hash map of dynamic components.
         */
        public HashMap<String, UIComponent> getDynamicComponents() {
            if (dynamicComponents == null) {
                dynamicComponents = new HashMap<>();
            }

            return dynamicComponents;
        }

        /**
         * Handle the remove.
         *
         * @param context the Faces context.
         * @param component the UI component to add to the list as a REMOVE.
         */
        protected void handleRemove(FacesContext context, UIComponent component) {
            if (component.isInView()) {
                recordDynamicAction(
                    component,
                    new ComponentStruct(
                        REMOVE, findFacetNameForComponent(component),
                        stripIterationIndex(UINamingContainer.getSeparatorChar(context), component.getClientId(context)), component.getId()
                    )
                );
            }
        }

        /**
         * Handle the add.
         *
         * @param context the Faces context.
         * @param component the UI component to add to the list as an ADD.
         */
        protected void handleAdd(FacesContext context, UIComponent component) {
            if (component.getParent() != null && component.getParent().isInView()) {
                // The stale clientId that a reparent could leave behind is already invalidated by
                // UIComponentBase.setParent, which runs before this event, so no setId is needed here.
                String facetName = findFacetNameForComponent(component);
                int index = indexInParent(component);
                component.clearInitialState();
                markDynamicallyAdded(component, index);

                char separatorChar = UINamingContainer.getSeparatorChar(context);
                ComponentStruct struct = new ComponentStruct(
                    ADD, facetName,
                    stripIterationIndex(separatorChar, component.getParent().getClientId(context)),
                    stripIterationIndex(separatorChar, component.getClientId(context)),
                    component.getId()
                );
                struct.setIndex(index);

                recordDynamicAction(component, struct);
            }
        }

        /**
         * Return the facet name for the given component or null if the component is not the value of a facets map entry.
         *
         * @param component the component to look for in the facets map entry value.
         * @return the facet name or null if the component is not the value of a facets map entry.
         */
        private String findFacetNameForComponent(UIComponent component) {
            Set<Entry<String, UIComponent>> entrySet = component.getParent().getFacets().entrySet();
            Iterator<Entry<String, UIComponent>> entries = entrySet.iterator();
            while (entries.hasNext()) {
                Entry<String, UIComponent> candidate = entries.next();
                if (component == candidate.getValue()) {
                    return candidate.getKey();
                }
            }
            return null;
        }

        /**
         * Records a dynamic add/remove action by appending it to the dynamic action list (O(1)).
         *
         * <p>
         * Redundant add/remove pairs for the same client id (e.g. a component added then removed within a request) are collapsed in a single pass at save time
         * (see {@code FaceletStateManagementStrategy#saveDynamicActions}) rather than per event. A per-event prune has to {@code indexOf}/{@code remove} on the
         * action list for every add or remove, which is O(n&sup2;) over n dynamically added components; appending and pruning once at save keeps recording O(1)
         * per event.
         * </p>
         *
         * @param component the UI component.
         * @param struct the dynamic action.
         */
        private void recordDynamicAction(UIComponent component, ComponentStruct struct) {
            getDynamicActions().add(struct);
            getDynamicComponents().put(struct.getClientId(), component);
        }

        /**
         * Index of the component within its parent's children list, computed in O(1) for the common case where the component was just appended (a dynamic add
         * typically appends), with a scan fallback otherwise. Returns -1 when the component is not in the children list (e.g. it is a facet).
         *
         * @param component the component whose position to report.
         * @return the child index, or -1 if not a child of its parent.
         */
        private int indexInParent(UIComponent component) {
            List<UIComponent> children = component.getParent().getChildren();
            int last = children.size() - 1;
            if (last >= 0 && children.get(last) == component) {
                return last;
            }
            return children.indexOf(component);
        }

    }

}
