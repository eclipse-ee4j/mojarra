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

package com.sun.faces.facelets.tag.faces;

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.PartialStateSaving;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;

import com.sun.faces.RIConstants;
import com.sun.faces.context.StateContext;
import com.sun.faces.facelets.tag.faces.core.FacetHandler;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;

/**
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public final class ComponentSupport {

    private final static String MARK_DELETED = "com.sun.faces.facelets.MARK_DELETED";
    public final static String MARK_CREATED = "com.sun.faces.facelets.MARK_ID";
    private final static String MARK_ID_CACHE = "com.sun.faces.facelets.MARK_ID_CACHE";

    // Expando boolean attribute used to identify parent components that have had
    // a dynamic child addition or removal.
    public final static String MARK_CHILDREN_MODIFIED = "com.sun.faces.facelets.MARK_CHILDREN_MODIFIED";

    // Expando Collection<String> attribute used to identify tagIds of child components that
    // have been removed from a parent component.
    public final static String REMOVED_CHILDREN = "com.sun.faces.facelets.REMOVED_CHILDREN";

    // Expando attribute used to mark dynamic UIComponents that have had their
    // ComponentSupport.MARK_CREATED expando removed.
    public static final String MARK_CREATED_REMOVED = StateContext.class.getName() + "_MARK_CREATED_REMOVED";

    private final static String IMPLICIT_PANEL = "com.sun.faces.facelets.IMPLICIT_PANEL";

    /**
     * Key to a FacesContext scoped Map where the keys are UIComponent instances and the values are Tag instances.
     */
    public static final String COMPONENT_TO_TAG_MAP_NAME = "com.sun.faces.facelets.COMPONENT_TO_LOCATION_MAP";

    public static boolean handlerIsResourceRelated(ComponentHandler handler) {
        ComponentConfig config = handler.getComponentConfig();
        if (!"jakarta.faces.Output".equals(config.getComponentType())) {
            return false;
        }

        String rendererType = config.getRendererType();
        return "jakarta.faces.resource.Script".equals(rendererType) || "jakarta.faces.resource.Stylesheet".equals(rendererType);
    }

    public static boolean isBuildingNewComponentTree(FacesContext context) {
        return !context.isPostback() || context.getCurrentPhaseId().equals(PhaseId.RESTORE_VIEW);
    }

    public static boolean isImplicitPanel(UIComponent component) {
        return component.getAttributes().containsKey(IMPLICIT_PANEL);
    }

    /**
     * Used in conjunction with markForDeletion where any UIComponent marked will be removed.
     *
     * @param c UIComponent to finalize
     */
    public static void finalizeForDeletion(UIComponent c) {
        // remove any existing marks of deletion
        c.getAttributes().remove(MARK_DELETED);

        // finally remove any children marked as deleted
        int sz = c.getChildCount();
        if (sz > 0) {
            UIComponent cc = null;
            List cl = c.getChildren();
            while (--sz >= 0) {
                cc = (UIComponent) cl.get(sz);
                if (cc.getAttributes().containsKey(MARK_DELETED)) {
                    cl.remove(sz);
                }
            }
        }

        Map<String, UIComponent> facets = c.getFacets();
        // remove any facets marked as deleted
        if (facets.size() > 0) {
            Set<Entry<String, UIComponent>> col = facets.entrySet();
            UIComponent fc;
            Entry<String, UIComponent> curEntry;
            for (Iterator<Entry<String, UIComponent>> itr = col.iterator(); itr.hasNext();) {
                curEntry = itr.next();
                fc = curEntry.getValue();
                Map<String, Object> attrs = fc.getAttributes();
                if (attrs.containsKey(MARK_DELETED)) {
                    itr.remove();
                } else if (UIComponent.COMPOSITE_FACET_NAME.equals(curEntry.getKey())
                        || attrs.containsKey(IMPLICIT_PANEL) && !curEntry.getKey().equals(UIViewRoot.METADATA_FACET_NAME)) {
                    List<UIComponent> implicitPanelChildren = fc.getChildren();
                    UIComponent innerChild;
                    for (Iterator<UIComponent> innerItr = implicitPanelChildren.iterator(); innerItr.hasNext();) {
                        innerChild = innerItr.next();
                        if (innerChild.getAttributes().containsKey(MARK_DELETED)) {
                            innerItr.remove();
                        }

                    }
                }
            }
        }
    }

    public static Tag setTagForComponent(FacesContext context, UIComponent c, Tag t) {
        Map<Object, Object> contextMap = context.getAttributes();
        Map<Integer, Tag> componentToTagMap;
        componentToTagMap = (Map<Integer, Tag>) contextMap.get(COMPONENT_TO_TAG_MAP_NAME);
        if (null == componentToTagMap) {
            componentToTagMap = new HashMap<>();
            contextMap.put(COMPONENT_TO_TAG_MAP_NAME, componentToTagMap);
        }
        return componentToTagMap.put(System.identityHashCode(c), t);
    }

    public static Tag getTagForComponent(FacesContext context, UIComponent c) {
        Tag result = null;
        Map<Object, Object> contextMap = context.getAttributes();
        Map<Integer, Tag> componentToTagMap;
        componentToTagMap = (Map<Integer, Tag>) contextMap.get(COMPONENT_TO_TAG_MAP_NAME);
        if (null != componentToTagMap) {
            result = componentToTagMap.get(System.identityHashCode(c));
        }

        return result;
    }

    /**
     * A lighter-weight version of UIComponent's findChild.
     *
     * @param parent parent to start searching from
     * @param id to match to
     * @return UIComponent found or null
     */
    public static UIComponent findChild(UIComponent parent, String id) {
        int sz = parent.getChildCount();
        if (sz > 0) {
            UIComponent c = null;
            List cl = parent.getChildren();
            while (--sz >= 0) {
                c = (UIComponent) cl.get(sz);
                if (id.equals(c.getId())) {
                    return c;
                }
            }
        }
        return null;
    }

    // Obvious performance optimization. First, assume this method
    // is only called from UIInstructionHandler.apply(). With that assumption
    // in place a few optimizations can be had on the cheap.

    // If this method is called on an initial page
    // render it will always return null, so we can just return
    // null in that case without any iteration.

    // If this method is called during RestoreView, it will always return null
    // so we can just return null in that case without any iteration.

    // If PartialStateSaving is false, the UIInstruction components will
    // never be in the tree at this point, so we can return null and skip iterating.

    public static UIComponent findUIInstructionChildByTagId(FacesContext context, UIComponent parent, String id) {
        UIComponent result = null;
        if (isBuildingNewComponentTree(context)) {
            return null;
        }
        if (isPartialStateSaving(context)) {
            result = getDescendantMarkIdCache(parent).get(id);
        }

        return result;
    }

    private static boolean isPartialStateSaving(FacesContext context) {
        return context.getAttributes().get(PartialStateSaving) == Boolean.TRUE;
    }

    /**
     * By TagId, find Child
     *
     * @param parent the parent UI component
     * @param id the id
     * @return the UI component
     */
    public static UIComponent findChildByTagId(FacesContext context, UIComponent parent, String id) {
        if (isPartialStateSaving(context)) {
            // fast path - get the child from the descendant mark id cache
            return getDescendantMarkIdCache(parent).get(id);
        }
        else {
            // original impl - traverse the tree
            return findChildByTagIdFullStateSaving(context, parent, id);
        }
    }

    private static UIComponent findChildByTagIdFullStateSaving(FacesContext context, UIComponent parent, String id) {
        UIComponent c = null;
        UIViewRoot root = context.getViewRoot();
        boolean hasDynamicComponents = (null != root && root.getAttributes().containsKey(RIConstants.TREE_HAS_DYNAMIC_COMPONENTS));
        String cid = null;
        List<UIComponent> components;
        String facetName = getFacetName(parent);
        if (null != facetName) {
            c = parent.getFacet(facetName);
            // We will have a facet name, but no corresponding facet in the
            // case of facets with composite components.  In this case,
            // we must do the brute force search.
            if (null != c) {
                cid = (String) c.getAttributes().get(MARK_CREATED);
                if (id.equals(cid)) {
                    return c;
                }
            }
        }
        if (0 < parent.getFacetCount()) {
            components = new ArrayList<>();
            components.addAll(parent.getFacets().values());
            components.addAll(parent.getChildren());
        } else {
            components = parent.getChildren();
        }

        int len = components.size();
        for (int i = 0; i < len; i++) {
            c = components.get(i);
            cid = (String) c.getAttributes().get(MARK_CREATED);
            if (id.equals(cid)) {
                return c;
            }
            if (c instanceof UIPanel && c.getAttributes().containsKey(IMPLICIT_PANEL)) {
                for (UIComponent c2 : c.getChildren()) {
                    cid = (String) c2.getAttributes().get(MARK_CREATED);
                    if (id.equals(cid)) {
                        return c2;
                    }
                }
            }
            if (hasDynamicComponents) {
                /*
                 * Make sure we look for the child recursively it might have moved
                 * into a different parent in the parent hierarchy. Note currently
                 * we are only looking down the tree. Maybe it would be better
                 * to use the VisitTree API instead.
                 */
                UIComponent foundChild = findChildByTagId(context, c, id);
                if (foundChild != null) {
                    return foundChild;
                }
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, UIComponent> getDescendantMarkIdCache(UIComponent component) {
        Map<String, UIComponent> descendantMarkIdCache = (Map<String, UIComponent>) component.getTransientStateHelper().getTransient(MARK_ID_CACHE);

        if (descendantMarkIdCache == null) {
            descendantMarkIdCache = new HashMap<String, UIComponent>();
            component.getTransientStateHelper().putTransient(MARK_ID_CACHE, descendantMarkIdCache);
        }

        return descendantMarkIdCache;
    }

    /**
     * Adds the mark id of the specified {@link UIComponent} <code>otherComponent</code> to the mark id cache of this component,
     * including all its descendant mark ids. Changes are propagated up the component tree.
     */
    public static void addToDescendantMarkIdCache(UIComponent component, UIComponent otherComponent) {
        String markId = (String) otherComponent.getAttributes().get(MARK_CREATED);
        if (markId != null) {
            addSingleDescendantMarkId(component, markId, otherComponent);
        }
        Map<String, UIComponent> otherMarkIds = getDescendantMarkIdCache(otherComponent);
        if (!otherMarkIds.isEmpty()) {
            addAllDescendantMarkIds(component, otherMarkIds);
        }
    }

    /**
     * Adds the specified <code>markId</code> and its corresponding {@link UIComponent} <code>otherComponent</code>
     * to the mark id cache of this component. Changes are propagated up the component tree.
     */
    private static void addSingleDescendantMarkId(UIComponent component, String markId, UIComponent otherComponent) {
        getDescendantMarkIdCache(component).put(markId, otherComponent);
        UIComponent parent = component.getParent();
        if (parent != null) {
            addSingleDescendantMarkId(parent, markId, otherComponent);
        }
    }

    /**
     * Adds all specified <code>otherMarkIds</code> to the mark id cache of this component.
     * Changes are propagated up the component tree.
     */
    private static void addAllDescendantMarkIds(UIComponent component, Map<String, UIComponent> otherMarkIds) {
        getDescendantMarkIdCache(component).putAll(otherMarkIds);
        UIComponent parent = component.getParent();
        if (parent != null) {
            addAllDescendantMarkIds(parent, otherMarkIds);
        }
    }

    /**
     * Removes the mark id of the specified {@link UIComponent} <code>otherComponent</code> from the mark id cache of this component,
     * including all its descendant mark ids. Changes are propagated up the component tree.
     */
    public static void removeFromDescendantMarkIdCache(UIComponent component, UIComponent otherComponent) {
        String markId = (String) otherComponent.getAttributes().get(MARK_CREATED);
        if (markId != null) {
            removeSingleDescendantMarkId(component, markId);
        }
        Map<String, UIComponent> otherMarkIds = getDescendantMarkIdCache(otherComponent);
        if (!otherMarkIds.isEmpty()) {
            removeAllDescendantMarkIds(component, otherMarkIds);
        }
    }

    /**
     * Removes the specified <code>markId</code> from the mark id cache of this component.
     * Changes are propagated up the component tree.
     */
    private static void removeSingleDescendantMarkId(UIComponent component, String markId) {
        getDescendantMarkIdCache(component).remove(markId);
        UIComponent parent = component.getParent();
        if (parent != null) {
            removeSingleDescendantMarkId(parent, markId);
        }
    }

    /**
     * Removes all specified <code>otherMarkIds</code> from the mark id cache of this component.
     * Changes are propagated up the component tree.
     */
    private static void removeAllDescendantMarkIds(UIComponent component, Map<String, UIComponent> otherMarkIds) {
        Map<String, UIComponent> descendantMarkIdCache = getDescendantMarkIdCache(component);
        Iterator<String> iterator = otherMarkIds.keySet().iterator();
        while (iterator.hasNext()) {
            descendantMarkIdCache.remove(iterator.next());
        }
        UIComponent parent = component.getParent();
        if (parent != null) {
            removeAllDescendantMarkIds(parent, otherMarkIds);
        }
    }

    /**
     * Returns <code>true</code> if the given faces context is <strong>not</strong> {@link FacesContext#isReleased()},
     * and its current phase ID is <strong>not</strong> {@link PhaseId#RENDER_RESPONSE}.
     */
    public static boolean isNotRenderingResponse(FacesContext context) {
        return !context.isReleased() && context.getCurrentPhaseId() != PhaseId.RENDER_RESPONSE;
    }

    /**
     * According to Faces 1.2 tag specs, this helper method will use the TagAttribute passed in determining the Locale
     * intended.
     *
     * @param ctx FaceletContext to evaluate from
     * @param attr TagAttribute representing a Locale
     * @return Locale found
     * @throws TagAttributeException if the Locale cannot be determined
     */
    public static Locale getLocale(FaceletContext ctx, TagAttribute attr) throws TagAttributeException {
        Object obj = attr.getObject(ctx);
        if (obj instanceof Locale) {
            return (Locale) obj;
        }
        if (obj instanceof String) {
            String s = (String) obj;
            try {
                return Util.getLocaleFromString(s);
            } catch (IllegalArgumentException iae) {
                throw new TagAttributeException(attr, "Invalid Locale Specified: " + s);
            }
        } else {
            throw new TagAttributeException(attr, "Attribute did not evaluate to a String or Locale: " + obj);
        }
    }

    /**
     * Tries to walk up the parent to find the UIViewRoot, if not found, then go to FaceletContext's FacesContext for the
     * view root.
     *
     * @param ctx FaceletContext
     * @param parent UIComponent to search from
     * @return UIViewRoot instance for this evaluation
     */
    public static UIViewRoot getViewRoot(FaceletContext ctx, UIComponent parent) {
        UIComponent c = parent;
        do {
            if (c instanceof UIViewRoot) {
                return (UIViewRoot) c;
            } else {
                c = c.getParent();
            }
        } while (c != null);
        return ctx.getFacesContext().getViewRoot();
    }

    /**
     * Marks all direct children and Facets with an attribute for deletion.
     *
     * @see #finalizeForDeletion(UIComponent)
     * @param c UIComponent to mark
     */
    public static void markForDeletion(UIComponent c) {
        // flag this component as deleted
        c.getAttributes().put(MARK_DELETED, Boolean.TRUE);

        // mark all children to be deleted
        int sz = c.getChildCount();
        if (sz > 0) {
            UIComponent cc = null;
            List cl = c.getChildren();
            while (--sz >= 0) {
                cc = (UIComponent) cl.get(sz);
                if (cc.getAttributes().containsKey(MARK_CREATED)) {
                    cc.getAttributes().put(MARK_DELETED, Boolean.TRUE);
                }
            }
        }

        // mark all facets to be deleted
        if (c.getFacets().size() > 0) {
            Set col = c.getFacets().entrySet();
            UIComponent fc;
            for (Iterator itr = col.iterator(); itr.hasNext();) {
                Map.Entry entry = (Map.Entry) itr.next();
                String facet = (String) entry.getKey();
                fc = (UIComponent) entry.getValue();
                Map<String, Object> attrs = fc.getAttributes();
                if (attrs.containsKey(MARK_CREATED)) {
                    attrs.put(MARK_DELETED, Boolean.TRUE);
                } else if (UIComponent.COMPOSITE_FACET_NAME.equals(facet)) {
                    // mark the inner pannel components to be deleted
                    sz = fc.getChildCount();
                    if (sz > 0) {
                        UIComponent cc = null;
                        List cl = fc.getChildren();
                        while (--sz >= 0) {
                            cc = (UIComponent) cl.get(sz);
                            cc.getAttributes().put(MARK_DELETED, Boolean.TRUE);
                        }
                    }
                } else if (attrs.containsKey(IMPLICIT_PANEL)) {
                    List<UIComponent> implicitPanelChildren = fc.getChildren();
                    Map<String, Object> innerAttrs = null;
                    for (UIComponent cur : implicitPanelChildren) {
                        innerAttrs = cur.getAttributes();
                        if (innerAttrs.containsKey(MARK_CREATED)) {
                            innerAttrs.put(MARK_DELETED, Boolean.TRUE);
                        }
                    }
                }
            }
        }
    }

    public static void encodeRecursive(FacesContext context, UIComponent viewToRender) throws IOException, FacesException {
        if (viewToRender.isRendered()) {
            viewToRender.encodeBegin(context);
            if (viewToRender.getRendersChildren()) {
                viewToRender.encodeChildren(context);
            } else if (viewToRender.getChildCount() > 0) {
                Iterator kids = viewToRender.getChildren().iterator();
                while (kids.hasNext()) {
                    UIComponent kid = (UIComponent) kids.next();
                    encodeRecursive(context, kid);
                }
            }
            viewToRender.encodeEnd(context);
        }
    }

    public static void removeTransient(UIComponent c) {
        UIComponent d, e;
        if (c.getChildCount() > 0) {
            for (Iterator itr = c.getChildren().iterator(); itr.hasNext();) {
                d = (UIComponent) itr.next();
                if (d.getFacets().size() > 0) {
                    for (Iterator jtr = d.getFacets().values().iterator(); jtr.hasNext();) {
                        e = (UIComponent) jtr.next();
                        if (e.isTransient()) {
                            jtr.remove();
                        } else {
                            removeTransient(e);
                        }
                    }
                }
                if (d.isTransient()) {
                    itr.remove();
                } else {
                    removeTransient(d);
                }
            }
        }
        if (c.getFacets().size() > 0) {
            for (Iterator itr = c.getFacets().values().iterator(); itr.hasNext();) {
                d = (UIComponent) itr.next();
                if (d.isTransient()) {
                    itr.remove();
                } else {
                    removeTransient(d);
                }
            }
        }
    }

    /**
     * <p class="changed_added_2_0">
     * Add the child component to the parent. If the parent is a facet, check to see whether the facet is already defined.
     * If it is, wrap the existing component in a panel group, if it's not already, then add the child to the panel group.
     * If the facet does not yet exist, make the child the facet.
     * </p>
     */
    public static void addComponent(FaceletContext ctx, UIComponent parent, UIComponent child) {

        String facetName = getFacetName(parent);
        if (facetName == null) {
            if (child.getAttributes().containsKey(RIConstants.DYNAMIC_COMPONENT)) {
                int childIndex = (Integer) child.getAttributes().get(RIConstants.DYNAMIC_COMPONENT);
                if (childIndex >= parent.getChildCount() || childIndex == -1) {
                    parent.getChildren().add(child);
                } else {
                    parent.getChildren().add(childIndex, child);
                }
            } else {
                parent.getChildren().add(child);
            }
        } else {
            UIComponent existing = parent.getFacets().get(facetName);
            if (existing != null && existing != child) {
                if (existing.getAttributes().get(ComponentSupport.IMPLICIT_PANEL) == null) {
                    // move existing component under a panel group
                    UIComponent panelGroup = ctx.getFacesContext().getApplication().createComponent(UIPanel.COMPONENT_TYPE);
                    parent.getFacets().put(facetName, panelGroup);
                    Map<String, Object> attrs = panelGroup.getAttributes();
                    attrs.put(ComponentSupport.IMPLICIT_PANEL, true);
                    panelGroup.getChildren().add(existing);
                    existing = panelGroup;
                }
                if (existing.getAttributes().get(ComponentSupport.IMPLICIT_PANEL) != null) {
                    // we have a panel group, so add the new component to it
                    existing.getChildren().add(child);
                } else {
                    parent.getFacets().put(facetName, child);
                }
            } else {
                parent.getFacets().put(facetName, child);
            }
        }
    }

    public static String getFacetName(UIComponent parent) {
        return (String) parent.getAttributes().get(FacetHandler.KEY);
    }

    public static boolean suppressViewModificationEvents(FacesContext ctx) {

        // NO UIViewRoot means this was called during restore view -
        // no need to suppress events at that time
        UIViewRoot root = ctx.getViewRoot();
        if (root != null) {
            String viewId = root.getViewId();
            if (viewId != null) {
                StateContext stateCtx = StateContext.getStateContext(ctx);
                return stateCtx.isPartialStateSaving(ctx, viewId);
            }
        }
        return false;

    }

    public static void copyPassthroughAttributes(FaceletContext ctx, UIComponent c, Tag t) {

        if (null == c || null == t) {
            return;
        }

        for (String namespace : PassThroughAttributeLibrary.NAMESPACES) {
            TagAttribute[] passthroughAttrs = t.getAttributes().getAll(namespace);
            if (null != passthroughAttrs && 0 < passthroughAttrs.length) {
                Map<String, Object> componentPassthroughAttrs = c.getPassThroughAttributes(true);
                Object attrValue = null;
                for (TagAttribute cur : passthroughAttrs) {
                    attrValue = cur.isLiteral() ? cur.getValue(ctx) : cur.getValueExpression(ctx, Object.class);
                    componentPassthroughAttrs.put(cur.getLocalName(), attrValue);
                }
            }
        }
    }

    public static Collection<Object[]> saveDescendantInitialComponentStates(FacesContext facesContext, Iterator<UIComponent> childIterator, boolean saveChildFacets) {
        Collection<Object[]> childStates = null;
        while (childIterator.hasNext()) {
            if (childStates == null) {
                childStates = new ArrayList<>();
            }

            UIComponent child = childIterator.next();
            if (!child.isTransient()) {
                // Add an entry to the collection, being an array of two
                // elements. The first element is the state of the children
                // of this component; the second is the state of the current
                // child itself.

                Iterator<UIComponent> childsIterator;
                if (saveChildFacets) {
                    childsIterator = child.getFacetsAndChildren();
                } else {
                    childsIterator = child.getChildren().iterator();
                }
                Object descendantState = saveDescendantInitialComponentStates(facesContext, childsIterator, true);
                Object state = child.saveState(facesContext);
                childStates.add(new Object[] { state, descendantState });
            }
        }
        return childStates;
    }

    public static Map<String, Object> saveDescendantComponentStates(FacesContext facesContext, Map<String, Object> stateMap, Iterator<UIComponent> childIterator, BiFunction<UIComponent, FacesContext, Object> stateSaver, boolean saveChildFacets) {
        while (childIterator.hasNext()) {
            UIComponent child = childIterator.next();
            if (!child.isTransient()) {
                Iterator<UIComponent> childsIterator;
                if (saveChildFacets) {
                    childsIterator = child.getFacetsAndChildren();
                } else {
                    childsIterator = child.getChildren().iterator();
                }
                stateMap = saveDescendantComponentStates(facesContext, stateMap, childsIterator, stateSaver, true);
                Object state = stateSaver.apply(child, facesContext);
                if (state != null) {
                    if (stateMap == null) {
                        stateMap = new HashMap<>();
                    }
                    stateMap.put(child.getClientId(facesContext), state);
                }
            }
        }
        return stateMap;
    }

    public static void restoreFullDescendantComponentStates(FacesContext facesContext, Iterator<UIComponent> childIterator, Object state, boolean restoreChildFacets) {
        Iterator<? extends Object[]> descendantStateIterator = null;
        while (childIterator.hasNext()) {
            if (descendantStateIterator == null && state != null) {
                descendantStateIterator = ((Collection<? extends Object[]>) state).iterator();
            }
            UIComponent component = childIterator.next();

            // reset the client id (see spec 3.1.6)
            component.setId(component.getId());
            if (!component.isTransient()) {
                Object childState = null;
                Object descendantState = null;
                if (descendantStateIterator != null && descendantStateIterator.hasNext()) {
                    Object[] object = descendantStateIterator.next();
                    childState = object[0];
                    descendantState = object[1];
                }

                component.clearInitialState();
                component.restoreState(facesContext, childState);
                component.markInitialState();

                Iterator<UIComponent> childsIterator;
                if (restoreChildFacets) {
                    childsIterator = component.getFacetsAndChildren();
                } else {
                    childsIterator = component.getChildren().iterator();
                }
                restoreFullDescendantComponentStates(facesContext, childsIterator, descendantState, true);
            }
        }
    }

    public static void restoreFullDescendantComponentDeltaStates(FacesContext facesContext, Iterator<UIComponent> childIterator, Object state, Object initialState, boolean restoreChildFacets) {
        Map<String, Object> descendantStateIterator = null;
        Iterator<? extends Object[]> descendantFullStateIterator = null;
        while (childIterator.hasNext()) {
            if (descendantStateIterator == null && state != null) {
                descendantStateIterator = (Map<String, Object>) state;
            }
            if (descendantFullStateIterator == null && initialState != null) {
                descendantFullStateIterator = ((Collection<? extends Object[]>) initialState).iterator();
            }
            UIComponent component = childIterator.next();

            // reset the client id (see spec 3.1.6)
            component.setId(component.getId());
            if (!component.isTransient()) {
                Object childInitialState = null;
                Object descendantInitialState = null;
                Object childState = null;
                if (descendantStateIterator != null && descendantStateIterator.containsKey(component.getClientId(facesContext))) {
                    // Object[] object = (Object[]) descendantStateIterator.get(component.getClientId(facesContext));
                    // childState = object[0];
                    childState = descendantStateIterator.get(component.getClientId(facesContext));
                }
                if (descendantFullStateIterator != null && descendantFullStateIterator.hasNext()) {
                    Object[] object = descendantFullStateIterator.next();
                    childInitialState = object[0];
                    descendantInitialState = object[1];
                }

                component.clearInitialState();
                if (childInitialState != null) {
                    component.restoreState(facesContext, childInitialState);
                    component.markInitialState();
                    component.restoreState(facesContext, childState);
                } else {
                    component.restoreState(facesContext, childState);
                    component.markInitialState();
                }

                Iterator<UIComponent> childsIterator;
                if (restoreChildFacets) {
                    childsIterator = component.getFacetsAndChildren();
                } else {
                    childsIterator = component.getChildren().iterator();
                }
                restoreFullDescendantComponentDeltaStates(facesContext, childsIterator, state, descendantInitialState, true);
            }
        }
    }

    public static void restoreTransientDescendantComponentStates(FacesContext facesContext, Iterator<UIComponent> childIterator, Map<String, Object> state, boolean restoreChildFacets) {
        while (childIterator.hasNext()) {
            UIComponent component = childIterator.next();

            // reset the client id (see spec 3.1.6)
            component.setId(component.getId());
            if (!component.isTransient()) {
                component.restoreTransientState(facesContext, state == null ? null : state.get(component.getClientId(facesContext)));

                Iterator<UIComponent> childsIterator;
                if (restoreChildFacets) {
                    childsIterator = component.getFacetsAndChildren();
                } else {
                    childsIterator = component.getChildren().iterator();
                }
                restoreTransientDescendantComponentStates(facesContext, childsIterator, state, true);
            }
        }

    }


    // --------------------------------------------------------- private classes

//    private static UIViewRoot getViewRoot(FacesContext ctx, UIComponent parent) {
//
//        if (parent instanceof UIViewRoot) {
//            return (UIViewRoot) parent;
//        }
//        UIViewRoot root = ctx.getViewRoot();
//        if (root != null) {
//            return root;
//        }
//        UIComponent c = parent.getParent();
//        while (c != null) {
//            if (c instanceof UIViewRoot) {
//                root = (UIViewRoot) c;
//                break;
//            } else {
//                c = c.getParent();
//            }
//        }
//
//        return root;
//
//    }
}
