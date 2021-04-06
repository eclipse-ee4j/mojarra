/*
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

package com.sun.faces.context;

import static com.sun.faces.RIConstants.DYNAMIC_CHILD_COUNT;
import static com.sun.faces.RIConstants.DYNAMIC_COMPONENT;
import static com.sun.faces.util.ComponentStruct.ADD;
import static com.sun.faces.util.ComponentStruct.REMOVE;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import com.sun.faces.RIConstants;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.ApplicationStateInfo;
import com.sun.faces.facelets.tag.faces.ComponentSupport;
import com.sun.faces.util.ComponentStruct;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MostlySingletonSet;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.event.PreRemoveFromViewEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;

/**
 * Context for dealing with partial state saving mechanics.
 */
public class StateContext {

    private static final String KEY = StateContext.class.getName() + "_KEY";

    private boolean partial;
    private boolean partialLocked;
    private boolean trackMods = true;
    private AddRemoveListener modListener;
    private ApplicationStateInfo stateInfo;
    private WeakReference<UIViewRoot> viewRootRef = new WeakReference<>(null);

    private static final Logger LOGGER = FacesLogger.CONTEXT.getLogger();

    // ------------------------------------------------------------ Constructors

    private StateContext(ApplicationStateInfo stateInfo) {

        this.stateInfo = stateInfo;

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
            ApplicationAssociate associate = ApplicationAssociate.getCurrentInstance();
            ApplicationStateInfo info = associate.getApplicationStateInfo();
            stateCtx = new StateContext(info);
            ctx.getAttributes().put(KEY, stateCtx);
        }
        return stateCtx;

    }

    /**
     * @param ctx FacesContext.
     * @param viewId the view ID to check or null if viewId is unknown.
     * @return <code>true</code> if partial state saving should be used for the specified view ID, otherwise
     * <code>false</code>
     */
    public boolean isPartialStateSaving(FacesContext ctx, String viewId) {
        // track UIViewRoot changes
        UIViewRoot root = ctx.getViewRoot();
        UIViewRoot refRoot = viewRootRef.get();
        if (root != refRoot) {
            // set weak reference to current viewRoot
            viewRootRef = new WeakReference<>(root);

            // On first call in restore phase, viewRoot is null, so we treat the first
            // change to not null not as a changing viewRoot.
            if (refRoot != null) {
                // view root changed in request processing - force usage of a
                // new AddRemoveListener instance for the new viewId ...
                modListener = null;
                // ... and also force check for partial state saving for the new viewId
                partialLocked = false;
            }
        }

        if (!partialLocked) {
            if (viewId == null) {
                if (root != null) {
                    viewId = root.getViewId();
                } else {
                    // View root has not yet been initialized. Check to see whether
                    // the target view id has been stashed away for us.
                    viewId = (String) ctx.getAttributes().get(RIConstants.VIEWID_KEY_NAME);
                }
            }

            partial = stateInfo.usePartialStateSaving(viewId);
            partialLocked = true;
        }
        return partial;

    }

    /**
     * @return <code>true</code> if view modifications outside of the initial construction of the view are being tracked.
     */
    public boolean trackViewModifications() {

        return trackMods;

    }

    /**
     * Installs a <code>SystemEventListener</code> on the <code>UIViewRoot</code> to track components added to or removed
     * from the view.
     */
    public void startTrackViewModifications(FacesContext ctx, UIViewRoot root) {
        if (modListener == null) {
            if (root != null) {
                modListener = createAddRemoveListener(ctx, root);
                root.subscribeToViewEvent(PostAddToViewEvent.class, modListener);
                root.subscribeToViewEvent(PreRemoveFromViewEvent.class, modListener);
            } else {
                LOGGER.warning("Unable to attach AddRemoveListener to UIViewRoot because it is null");
            }
        }
        setTrackViewModifications(true);
    }

    /**
     * Toggles the current modification tracking status.
     *
     * @param trackMods if <code>true</code> and the listener installed by
     * {@link #startTrackViewModifications(jakarta.faces.context.FacesContext, jakarta.faces.component.UIViewRoot) is
     * present, then view modifications will be tracked. If <code>false</code>, then modification events will be ignored.
     */
    public void setTrackViewModifications(boolean trackMods) {

        this.trackMods = trackMods;

    }

    /**
     * @param c the UIComponent to check
     * @return <code>true</code> if the component was added after the initial view construction
     */
    public boolean componentAddedDynamically(UIComponent c) {
        return c.getAttributes().containsKey(DYNAMIC_COMPONENT);
    }

    public int getIndexOfDynamicallyAddedChildInParent(UIComponent c) {
        int result = -1;
        Map<String, Object> attrs = c.getAttributes();
        if (attrs.containsKey(DYNAMIC_COMPONENT)) {
            result = (Integer) attrs.get(DYNAMIC_COMPONENT);
        }
        return result;
    }

    public boolean hasOneOrMoreDynamicChild(UIComponent parent) {
        return parent.getAttributes().containsKey(DYNAMIC_CHILD_COUNT);
    }

    private int incrementDynamicChildCount(FacesContext context, UIComponent parent) {
        int result;
        Map<String, Object> attrs = parent.getAttributes();
        Integer cur = (Integer) attrs.get(DYNAMIC_CHILD_COUNT);
        if (null != cur) {
            result = cur++;
        } else {
            result = 1;
        }
        attrs.put(DYNAMIC_CHILD_COUNT, result);
        context.getViewRoot().getAttributes().put(RIConstants.TREE_HAS_DYNAMIC_COMPONENTS, Boolean.TRUE);

        return result;
    }

    private int decrementDynamicChildCount(FacesContext context, UIComponent parent) {
        int result = 0;
        Map<String, Object> attrs = parent.getAttributes();
        Integer cur = (Integer) attrs.get(DYNAMIC_CHILD_COUNT);
        if (null != cur) {
            result = 0 < cur ? cur-- : 0;

        }
        if (0 == result && null != cur) {
            attrs.remove(DYNAMIC_CHILD_COUNT);
        }
        context.getViewRoot().getAttributes().put(RIConstants.TREE_HAS_DYNAMIC_COMPONENTS, Boolean.TRUE);

        return result;
    }

    /**
     * Get the dynamic list (of adds and removes).
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

    // ---------------------------------------------------------- Nested Classes

    private AddRemoveListener createAddRemoveListener(FacesContext context, UIViewRoot root) {
        return isPartialStateSaving(context, root.getViewId()) ? new DynamicAddRemoveListener(context) : new StatelessAddRemoveListener(context);
    }

    abstract private class AddRemoveListener implements SystemEventListener {

        /**
         * Stores the state context we work for,
         */
        private StateContext stateCtx;

        /**
         * Constructor.
         *
         * @param context the Faces context.
         */
        protected AddRemoveListener(FacesContext context) {
            stateCtx = StateContext.getStateContext(context);
        }

        /**
         * Get the list of adds/removes.
         *
         * @return the list of adds/removes.
         */
        abstract public List<ComponentStruct> getDynamicActions();

        /**
         * Get the hash map of dynamic components.
         *
         * @return the hash map of dynamic components.
         */
        abstract public HashMap<String, UIComponent> getDynamicComponents();

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
                    ctx.getViewRoot().getAttributes().put(RIConstants.TREE_HAS_DYNAMIC_COMPONENTS, Boolean.TRUE);
                }
            } else {
                if (stateCtx.trackViewModifications()) {
                    handleAdd(ctx, ((PostAddToViewEvent) event).getComponent());
                    ctx.getViewRoot().getAttributes().put(RIConstants.TREE_HAS_DYNAMIC_COMPONENTS, Boolean.TRUE);
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
         * Handle the remove.
         *
         * @param context the Faces context.
         * @param component the UI component to add to the list as a REMOVE.
         */
        abstract protected void handleRemove(FacesContext context, UIComponent component);

        /**
         * Handle the add.
         *
         * @param context the Faces context.
         * @param component the UI component to add to the list as an ADD.
         */
        abstract protected void handleAdd(FacesContext context, UIComponent component);
    }

    public class NoopAddRemoveListener extends AddRemoveListener {

        // This is silly. We should be able to use Colletions.emptyMap(),
        // but cannot as StateContext.getDynamicComponents() API returns a
        // HashMap instead of a Map.
        private HashMap emptyComponentsMap = new HashMap();

        public NoopAddRemoveListener(FacesContext context) {
            super(context);
        }

        @Override
        public List<ComponentStruct> getDynamicActions() {
            return Collections.emptyList();
        }

        @Override
        public HashMap<String, UIComponent> getDynamicComponents() {
            return emptyComponentsMap;
        }

        @Override
        protected void handleRemove(FacesContext context, UIComponent component) {
        }

        @Override
        protected void handleAdd(FacesContext context, UIComponent component) {
        }
    }

    /**
     * An AddRemoveListener that implements the new dynamic component strategy where no state is managed by the listener
     * itself. Instead, we use expando attributes on the dynamic components (and their parents) to track/preserve the
     * dynamic nature of these components.
     */
    public class StatelessAddRemoveListener extends NoopAddRemoveListener {

        public StatelessAddRemoveListener(FacesContext context) {
            super(context);
        }

        private boolean thisEventCorrespondsToSubtreeRootRemove(FacesContext context, UIComponent c) {
            boolean result = false;
            if (null != c) {
                c = c.getParent();
                if (null != c) {
                    result = c.isInView();
                }
            }

            return result;
        }

        private boolean thisEventCorrespondsToSubtreeRootAdd(FacesContext context, UIComponent c) {
            boolean result = false;
            Map<Object, Object> contextMap = context.getAttributes();
            UIViewRoot root = context.getViewRoot();
            UIComponent originalComponent = c;
            if (null != c) {
                Collection<UIComponent> dynamics = getDynamicComponentCollection(contextMap);
                if (dynamics.contains(c)) {
                    result = true;
                } else {
                    c = c.getParent();
                    while (null != c && !dynamics.contains(c)) {
                        c = c.getParent();
                    }
                    if (null == c || root.equals(c)) {
                        dynamics.add(originalComponent);
                        result = true;
                    }
                }
            }

            return result;
        }

        private static final String DYNAMIC_COMPONENT_ADD_COLLECTION = RIConstants.FACES_PREFIX + "DynamicComponentSubtreeRoots";

        private Collection<UIComponent> getDynamicComponentCollection(Map<Object, Object> contextMap) {
            Collection<UIComponent> result = (Collection<UIComponent>) contextMap.get(DYNAMIC_COMPONENT_ADD_COLLECTION);
            if (null == result) {
                result = new HashSet<>();
                contextMap.put(DYNAMIC_COMPONENT_ADD_COLLECTION, result);
            }
            return result;
        }

        @Override
        protected void handleRemove(FacesContext context, UIComponent component) {
            if (!thisEventCorrespondsToSubtreeRootRemove(context, component)) {
                return;
            }

            Map<String, Object> attrs = component.getAttributes();

            // If the component is a tag-created child, we remove its
            // MARK_CREATED expando so that it will now be treated as
            // a dynamic/non-tag created component.
            String tagId = (String) attrs.remove(ComponentSupport.MARK_CREATED);
            if (tagId != null) {
                // Actually, we don't just remove the MARK_CREATED - we need
                // to stash it away so that we can restore it later if the
                // component happens to be re-added to its original parent.
                attrs.put(ComponentSupport.MARK_CREATED_REMOVED, tagId);
                childRemovedFromParent(component.getParent(), tagId);
            }
        }

        private void childRemovedFromParent(UIComponent parent, String childTagId) {
            if (parent != null) {
                Collection<String> removedChildrenIds = getPreviouslyRemovedChildren(parent);
                removedChildrenIds.add(childTagId);

                markChildrenModified(parent);
            }
        }

        private Collection<String> getPreviouslyRemovedChildren(UIComponent parent) {
            Map<String, Object> attrs = parent.getAttributes();
            Collection<String> removedChildrenIds = (Collection<String>) attrs.get(ComponentSupport.REMOVED_CHILDREN);

            if (removedChildrenIds == null) {
                removedChildrenIds = new MostlySingletonSet<>();
                attrs.put(ComponentSupport.REMOVED_CHILDREN, removedChildrenIds);
            }

            return removedChildrenIds;
        }

        private void markChildrenModified(UIComponent parent) {
            parent.getAttributes().put(ComponentSupport.MARK_CHILDREN_MODIFIED, true);
        }

        @Override
        protected void handleAdd(FacesContext context, UIComponent component) {
            if (!thisEventCorrespondsToSubtreeRootAdd(context, component)) {
                return;
            }

            Map<String, Object> attrs = component.getAttributes();
            String tagId = (String) attrs.get(ComponentSupport.MARK_CREATED_REMOVED);

            if (childAddedToSameParentAsBefore(component.getParent(), tagId)) {

                // Restore MARK_CREATED if the added component was originally
                // created as a tag-based child of this parent.
                attrs.remove(ComponentSupport.MARK_CREATED_REMOVED);
                attrs.put(ComponentSupport.MARK_CREATED, tagId);
            }

            markChildrenModified(component.getParent());
        }

        // Handles the addition of a new child to the parent. Returns true
        // if the child was previously removed from this parent.
        private boolean childAddedToSameParentAsBefore(UIComponent parent, String childTagId) {
            if (parent != null) {
                Map<String, Object> attrs = parent.getAttributes();
                Collection<String> removedChildrenIds = (Collection<String>) attrs.get(ComponentSupport.REMOVED_CHILDREN);
                if (removedChildrenIds != null && removedChildrenIds.remove(childTagId)) {
                    if (removedChildrenIds.isEmpty()) {
                        attrs.remove(ComponentSupport.REMOVED_CHILDREN);
                    }
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * A system event listener which is used to listen for changes on the component tree after restore view and before
     * rendering out the view.
     */
    public class DynamicAddRemoveListener extends AddRemoveListener {

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
            super(context);
        }

        /**
         * Get the list of adds/removes.
         *
         * @return the list of adds/removes.
         */
        @Override
        public List<ComponentStruct> getDynamicActions() {
            synchronized (this) {
                if (dynamicActions == null) {
                    dynamicActions = new ArrayList<>();
                }
            }
            return dynamicActions;
        }

        /**
         * Get the hash map of dynamic components.
         *
         * @return the hash map of dynamic components.
         */
        @Override
        public HashMap<String, UIComponent> getDynamicComponents() {
            synchronized (this) {
                if (dynamicComponents == null) {
                    dynamicComponents = new HashMap<>();
                }
            }
            return dynamicComponents;
        }

        /**
         * Handle the remove.
         *
         * @param context the Faces context.
         * @param component the UI component to add to the list as a REMOVE.
         */
        @Override
        protected void handleRemove(FacesContext context, UIComponent component) {
            if (component.isInView()) {
                decrementDynamicChildCount(context, component.getParent());
                handleAddRemoveWithAutoPrune(
                    component, 
                    new ComponentStruct(REMOVE, findFacetNameForComponent(component), component.getClientId(context), component.getId())
                );
           }
        }

        /**
         * Handle the add.
         *
         * @param context the Faces context.
         * @param component the UI component to add to the list as an ADD.
         */
        @Override
        protected void handleAdd(FacesContext context, UIComponent component) {
            if (component.getParent() != null && component.getParent().isInView()) {
                String id = component.getId();

                /*
                 * Since adding a component, can mean you are really reparenting it, we need to make sure the OLD clientId is not
                 * cached, we do that by setting the id.
                 */
                if (id != null) {
                    component.setId(id);
                }

                String facetName = findFacetNameForComponent(component);
                if (facetName != null) {
                    incrementDynamicChildCount(context, component.getParent());
                    component.clearInitialState();
                    component.getAttributes().put(DYNAMIC_COMPONENT, component.getParent().getChildren().indexOf(component));

                    ComponentStruct struct = new ComponentStruct(ADD, facetName, component.getParent().getClientId(context), component.getClientId(context),
                            component.getId());

                    handleAddRemoveWithAutoPrune(component, struct);
                } else {
                    incrementDynamicChildCount(context, component.getParent());
                    component.clearInitialState();
                    component.getAttributes().put(DYNAMIC_COMPONENT, component.getParent().getChildren().indexOf(component));

                    ComponentStruct struct = new ComponentStruct(ADD, null, component.getParent().getClientId(context), component.getClientId(context),
                            component.getId());

                    handleAddRemoveWithAutoPrune(component, struct);
                }
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
         * Methods that takes care of pruning and adding an action to the dynamic action list.
         *
         * <pre>
         *  If you add a component and the dynamic action list does not contain
         *  the component yet then add it to the dynamic action list, regardless
         *  whether or not if was an ADD or REMOVE.
         * </pre>
         *
         * <pre>
         *  Else if you add a component and it is already in the dynamic action
         *  list and it is the only action for that client id in the dynamic
         *  action list then:
         *   1) If the previous action was an ADD then
         *      a) If the current action is a REMOVE then remove the component
         *         out of the dynamic action list.
         *      b) If the current action is an ADD then throw a FacesException.
         *   2) If the previous action was a REMOVE then
         *      a) If the current action is an ADD then add it to the dynamic
         *         action list.
         *      b) If the current action is a REMOVE then throw a FacesException.
         * </pre>
         *
         * <pre>
         *  Else if a REMOVE and ADD where captured before then:
         *   1) If the current action is REMOVE then remove the last dynamic
         *      action out of the dynamic action list.
         *   2) If the current action is ADD then throw a FacesException.
         * </pre>
         *
         * @param component the UI component.
         * @param struct the dynamic action.
         */
        private void handleAddRemoveWithAutoPrune(UIComponent component, ComponentStruct struct) {

            List<ComponentStruct> actionList = getDynamicActions();
            HashMap<String, UIComponent> componentMap = getDynamicComponents();

            int firstIndex = actionList.indexOf(struct);
            if (firstIndex == -1) {
                actionList.add(struct);
                componentMap.put(struct.getClientId(), component);
            } else {
                int lastIndex = actionList.lastIndexOf(struct);
                if (lastIndex == firstIndex) {
                    ComponentStruct previousStruct = actionList.get(firstIndex);
                    if (ADD.equals(previousStruct.getAction())) {
                        if (ADD.equals(struct.getAction())) {
                            throw new FacesException("Cannot add the same component twice: " + struct.getClientId());
                        }
                        if (REMOVE.equals(struct.getAction())) {
                            actionList.remove(firstIndex);
                            componentMap.remove(struct.getClientId());
                        }
                    }
                    if (REMOVE.equals(previousStruct.getAction())) {
                        if (ADD.equals(struct.getAction())) {
                            actionList.add(struct);
                            componentMap.put(struct.getClientId(), component);
                        }
                        if (REMOVE.equals(struct.getAction())) {
                            throw new FacesException("Cannot remove the same component twice: " + struct.getClientId());
                        }
                    }
                } else {
                    if (ADD.equals(struct.getAction())) {
                        throw new FacesException("Cannot add the same component twice: " + struct.getClientId());
                    }
                    if (REMOVE.equals(struct.getAction())) {
                        actionList.remove(lastIndex);
                    }
                }
            }
        }

    } // END AddRemoveListener

}
