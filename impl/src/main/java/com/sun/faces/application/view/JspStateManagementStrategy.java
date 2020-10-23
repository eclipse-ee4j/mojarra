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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.ResponseStateManager;
import jakarta.faces.view.StateManagementStrategy;

/**
 * A state management strategy for JSP.
 *
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
public class JspStateManagementStrategy extends StateManagementStrategy {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = FacesLogger.APPLICATION_VIEW.getLogger();
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
    public JspStateManagementStrategy() {
        this(FacesContext.getCurrentInstance());
    }

    /**
     * Constructor.
     *
     * @param context the Faces context.
     */
    public JspStateManagementStrategy(FacesContext context) {
        isDevelopmentMode = context.isProjectStage(ProjectStage.Development);
        classMap = new ConcurrentHashMap<>(32);
    }

    /**
     * Capture the child.
     *
     * @param tree the tree.
     * @param parent the parent.
     * @param c the component.
     */
    private void captureChild(List<TreeNode> tree, int parent, UIComponent c) {

        if (!c.isTransient()) {
            TreeNode n = new TreeNode(parent, c);
            int pos = tree.size();
            tree.add(n);
            captureRest(tree, pos, c);
        }
    }

    /**
     * Capture the facet.
     *
     * @param tree the tree.
     * @param parent the parent.
     * @param name the facet name.
     * @param c the component.
     */
    private void captureFacet(List<TreeNode> tree, int parent, String name, UIComponent c) {

        if (!c.isTransient()) {
            FacetNode n = new FacetNode(parent, name, c);
            int pos = tree.size();
            tree.add(n);
            captureRest(tree, pos, c);
        }
    }

    /**
     * Capture the rest.
     *
     * @param tree the tree.
     * @param pos the position.
     * @param c the component.
     */
    private void captureRest(List<TreeNode> tree, int pos, UIComponent c) {

        int sz = c.getChildCount();
        if (sz > 0) {
            List<UIComponent> child = c.getChildren();
            for (int i = 0; i < sz; i++) {
                captureChild(tree, pos, child.get(i));
            }
        }

        sz = c.getFacetCount();
        if (sz > 0) {
            for (Map.Entry<String, UIComponent> entry : c.getFacets().entrySet()) {
                captureFacet(tree, pos, entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Create a new component instance.
     *
     * @param n the tree node.
     * @return the UI component.
     * @throws FacesException when a serious error occurs.
     */
    private UIComponent newInstance(TreeNode n) throws FacesException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "newInstance", n.componentType);
        }

        try {
            Class<?> t = classMap != null ? classMap.get(n.componentType) : null;
            if (t == null) {
                t = Util.loadClass(n.componentType, n);
                if (t != null && classMap != null) {
                    classMap.put(n.componentType, t);
                } else {
                    if (!isDevelopmentMode) {
                        throw new NullPointerException();
                    }
                }
            }

            assert t != null;
            UIComponent c = (UIComponent) t.newInstance();
            c.setId(n.id);

            return c;
        } catch (ClassNotFoundException | NullPointerException | InstantiationException | IllegalAccessException e) {
            throw new FacesException(e);
        }

    }

    /**
     * Restore the component tree.
     *
     * @param renderKitId the render kit id.
     * @param tree the saved tree.
     * @return the view root.
     * @throws FacesException when a serious error occurs.
     */
    private UIViewRoot restoreTree(FacesContext context, String renderKitId, Object[] tree) throws FacesException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "restoreTree", renderKitId);
        }

        UIComponent c;
        FacetNode fn;
        TreeNode tn;
        for (int i = 0; i < tree.length; i++) {
            if (tree[i] instanceof FacetNode) {
                fn = (FacetNode) tree[i];
                c = newInstance(fn);
                tree[i] = c;
                if (i != fn.getParent()) {
                    ((UIComponent) tree[fn.getParent()]).getFacets().put(fn.facetName, c);
                }

            } else {
                tn = (TreeNode) tree[i];
                c = newInstance(tn);
                tree[i] = c;
                if (i != tn.parent) {
                    ((UIComponent) tree[tn.parent]).getChildren().add(c);
                } else {
                    assert c instanceof UIViewRoot;
                    UIViewRoot viewRoot = (UIViewRoot) c;
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
            LOGGER.log(Level.FINEST, "restoreView", new Object[] { viewId, renderKitId });
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
            /*
             * Restore the component state.
             */
            if (result != null && state[1] != null) {
                result.processRestoreState(context, state[1]);
            }
        }

        return result;
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
            LOGGER.finest("saveView");
        }

        Object[] result;
        UIViewRoot viewRoot = context.getViewRoot();

        /*
         * Check uniqueness.
         */
        Util.checkIdUniqueness(context, viewRoot, new HashSet<>(viewRoot.getChildCount() << 1));

        /*
         * Save the component state.
         */
        Object state = viewRoot.processSaveState(context);

        /*
         * Save the tree structure.
         */
        List<TreeNode> treeList = new ArrayList<>(32);
        captureChild(treeList, 0, viewRoot);
        Object[] tree = treeList.toArray();

        result = new Object[] { tree, state };
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
}
