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

package com.sun.faces.component;

import java.util.Stack;

import jakarta.faces.application.Resource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.Location;

/**
 * <p>
 * <code>CompositeComponentStackManager</code> is responsible for managing the two different composite component stacks
 * currently used by Mojarra.
 * </p>
 *
 * <p>
 * The stacks are identified by the {@link StackType} enum which has two elements,
 * <code>TreeCreation</code> and <code>Evaluation</code>.
 * </p>
 *
 * <p>
 * The <code>TreeCreation</code> stack represents the composite components that have been pushed by the TagHandlers
 * responsible for building the tree.
 * </p>
 *
 * <p>
 * The <code>Evaluation</code> stack is used by the EL in order to properly resolve nested composite component
 * expressions.
 * </p>
 */
public class CompositeComponentStackManager {

    private static final String MANAGER_KEY = CompositeComponentStackManager.class.getName();

    public enum StackType {
        TreeCreation, Evaluation
    }

    private final StackHandler treeCreation = new TreeCreationStackHandler();
    private final StackHandler runtime = new RuntimeStackHandler();

    // ------------------------------------------------------------ Constructors

    private CompositeComponentStackManager() {
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * @param ctx the <code>FacesContext</code> for the current request
     * @return the <code>CompositeComponentStackManager</code> for the current request
     */
    public static CompositeComponentStackManager getManager(FacesContext ctx) {

        CompositeComponentStackManager manager = (CompositeComponentStackManager) ctx.getAttributes().get(MANAGER_KEY);
        if (manager == null) {
            manager = new CompositeComponentStackManager();
            ctx.getAttributes().put(MANAGER_KEY, manager);
        }

        return manager;

    }

    /**
     * <p>
     * Pushes the specified composite component to the <code>Evaluation</code> stack.
     * </p>
     *
     * @param compositeComponent the component to push
     * @return <code>true</code> if the component was pushed, otherwise returns <code>false</code>
     */
    public boolean push(UIComponent compositeComponent) {
        return getStackHandler(StackType.Evaluation).push(compositeComponent);
    }

    /**
     * <p>
     * Pushes the specified composite component to the desired <code>StackType</code> stack.
     * </p>
     *
     * @param compositeComponent the component to push
     * @param stackType the stack to push to the component to
     * @return <code>true</code> if the component was pushed, otherwise returns <code>false</code>
     */
    public boolean push(UIComponent compositeComponent, StackType stackType) {
        return getStackHandler(stackType).push(compositeComponent);
    }

    /**
     * <p>
     * Pushes a component derived by the push logic to the <code>Evaluation</code> stack.
     * </p>
     *
     * @return <code>true</code> if the component was pushed, otherwise returns <code>false</code>
     */
    public boolean push() {
        return getStackHandler(StackType.Evaluation).push();
    }

    /**
     * <p>
     * Pushes a component derived by the push logic to the specified stack.
     * </p>
     *
     * @param stackType the stack to push to the component to
     *
     * @return <code>true</code> if the component was pushed, otherwise returns <code>false</code>
     */
    public boolean push(StackType stackType) {
        return getStackHandler(stackType).push();
    }

    /**
     * <p>
     * Pops the top-level component from the stack.
     * </p>
     *
     * @param stackType the stack to pop the top level component from
     */
    public void pop(StackType stackType) {
        getStackHandler(stackType).pop();
    }

    /**
     * <p>
     * Pops the top-level component from the <code>Evaluation</code> stack.
     * </p>
     */
    public void pop() {
        getStackHandler(StackType.Evaluation).pop();
    }

    /**
     * @return the top-level component from the <code>Evaluation</code> stack without removing the element
     */
    public UIComponent peek() {
        return getStackHandler(StackType.Evaluation).peek();
    }

    /**
     * @param stackType the stack to push to the component to
     *
     * @return the top-level component from the specified stack without removing the element
     */
    public UIComponent peek(StackType stackType) {
        return getStackHandler(stackType).peek();
    }

    public UIComponent getParentCompositeComponent(StackType stackType, FacesContext ctx, UIComponent forComponent) {
        return getStackHandler(stackType).getParentCompositeComponent(ctx, forComponent);
    }

    public UIComponent findCompositeComponentUsingLocation(FacesContext ctx, Location location) {

        StackHandler sh = getStackHandler(StackType.TreeCreation);
        Stack<UIComponent> s = sh.getStack(false);
        if (s != null) {
            String path = location.getPath();
            for (int i = s.size(); i > 0; i--) {
                UIComponent cc = s.get(i - 1);
                Resource r = (Resource) cc.getAttributes().get(Resource.COMPONENT_RESOURCE_KEY);
                if (path.endsWith('/' + r.getResourceName()) && path.contains(r.getLibraryName())) {
                    return cc;
                }
            }
        } else {
            // runtime eval
            String path = location.getPath();
            UIComponent cc = UIComponent.getCurrentCompositeComponent(ctx);
            while (cc != null) {
                Resource r = (Resource) cc.getAttributes().get(Resource.COMPONENT_RESOURCE_KEY);
                if (path.endsWith('/' + r.getResourceName()) && path.contains(r.getLibraryName())) {
                    return cc;
                }
                cc = UIComponent.getCompositeComponentParent(cc);
            }
        }

        // we could not find the composite component because the location was not found,
        // this will happen if the #{cc} refers to a composite component one level up,
        // so we are going after the current composite component.
        //
        return UIComponent.getCurrentCompositeComponent(ctx);
    }

    // --------------------------------------------------------- Private Methods

    private StackHandler getStackHandler(StackType type) {

        StackHandler handler = null;
        switch (type) {
        case TreeCreation:
            handler = treeCreation;
            break;
        case Evaluation:
            handler = runtime;
            break;
        }
        return handler;

    }

    // ------------------------------------------------------ Private Interfaces

    private interface StackHandler {

        boolean push(UIComponent compositeComponent);

        boolean push();

        void pop();

        UIComponent peek();

        UIComponent getParentCompositeComponent(FacesContext ctx, UIComponent forComponent);

        void delete();

        Stack<UIComponent> getStack(boolean create);

    }

    // ---------------------------------------------------------- Nested Classes

    private abstract class BaseStackHandler implements StackHandler {

        protected Stack<UIComponent> stack;

        // ------------------------------------------- Methods from StackHandler

        @Override
        public void delete() {

            stack = null;

        }

        @Override
        public Stack<UIComponent> getStack(boolean create) {

            if (stack == null && create) {
                stack = new Stack<>();
            }
            return stack;

        }

        @Override
        public UIComponent peek() {

            if (stack != null && !stack.isEmpty()) {
                return stack.peek();
            }
            return null;

        }

    } // END BaseStackHandler

    private final class RuntimeStackHandler extends BaseStackHandler {

        // ------------------------------------------- Methods from StackHandler

        @Override
        public void delete() {

            Stack<UIComponent> s = getStack(false);
            if (s != null) {
                s.clear();
            }

        }

        @Override
        public void pop() {

            Stack<UIComponent> s = getStack(false);
            if (s != null && !s.isEmpty()) {
                s.pop();
            }

        }

        @Override
        public boolean push() {

            return push(null);

        }

        @Override
        public boolean push(UIComponent compositeComponent) {

            Stack<UIComponent> tstack = treeCreation.getStack(false);
            Stack<UIComponent> stack = getStack(false);
            UIComponent ccp;
            if (tstack != null) {
                // We have access to the stack of composite components
                // the tree creation process has made available.
                // Since we can' reliably access the parent composite component
                // of the current composite component, use the index of the
                // current composite component within the stack to locate the
                // parent.
                ccp = compositeComponent;
            } else {
                // no tree creation stack available, so use the runtime stack.
                // If the current stack isn't empty, then use the component
                // on the stack as the current composite component.
                stack = getStack(false);

                if (compositeComponent == null) {
                    if (stack != null && !stack.isEmpty()) {
                        ccp = getCompositeParent(stack.peek());
                    } else {
                        ccp = getCompositeParent(UIComponent.getCurrentCompositeComponent(FacesContext.getCurrentInstance()));
                    }
                } else {
                    ccp = compositeComponent;
                }
            }

            if (ccp != null) {
                if (stack == null) {
                    stack = getStack(true);
                }
                stack.push(ccp);
                return true;
            }
            return false;

        }

        @Override
        public UIComponent getParentCompositeComponent(FacesContext ctx, UIComponent forComponent) {

            return getCompositeParent(forComponent);

        }

        // ----------------------------------------------------- Private Methods

        private UIComponent getCompositeParent(UIComponent comp) {

            return UIComponent.getCompositeComponentParent(comp);

        }

    } // END RuntimeStackHandler

    private final class TreeCreationStackHandler extends BaseStackHandler {

        // ------------------------------------------- Methods from StackHandler

        @Override
        public void pop() {

            Stack<UIComponent> s = getStack(false);
            if (s != null && !stack.isEmpty()) {
                stack.pop();
                if (stack.isEmpty()) {
                    delete();
                }
            }

        }

        @Override
        public boolean push() {

            return false;

        }

        @Override
        public boolean push(UIComponent compositeComponent) {

            if (compositeComponent != null) {
                assert UIComponent.isCompositeComponent(compositeComponent);
                Stack<UIComponent> s = getStack(true);
                s.push(compositeComponent);
                return true;
            }
            return false;

        }

        @Override
        public UIComponent getParentCompositeComponent(FacesContext ctx, UIComponent forComponent) {

            Stack<UIComponent> s = getStack(false);
            if (s == null) {
                return null;
            } else {
                int idx = s.indexOf(forComponent);
                if (idx == 0) { // no parent
                    return null;
                }
                return s.get(idx - 1);
            }
        }

    } // END TreeCreationStackHandler

}
