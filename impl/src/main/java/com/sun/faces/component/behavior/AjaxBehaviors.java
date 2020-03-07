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

package com.sun.faces.component.behavior;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.faces.application.Application;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHint;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_0">
 * An instance of the class is used to manage {@link AjaxBehavior} instances.
 * </p>
 *
 * @since 2.0
 */
public class AjaxBehaviors implements Serializable {

    private static final long serialVersionUID = 1617682489423771119L;

    private static final String AJAX_BEHAVIORS = "jakarta.faces.component.AjaxBehaviors";

    private ArrayDeque<BehaviorInfo> behaviorStack = null;

    public AjaxBehaviors() {
        behaviorStack = new ArrayDeque<>();
    }

    // Returns the AjaxBehaviors instance, creating it if necessary.
    public static AjaxBehaviors getAjaxBehaviors(FacesContext context, boolean createIfNull) {

        Map<Object, Object> attrs = context.getAttributes();
        AjaxBehaviors ajaxBehaviors = (AjaxBehaviors) attrs.get(AJAX_BEHAVIORS);

        if (ajaxBehaviors == null && createIfNull) {
            ajaxBehaviors = new AjaxBehaviors();
            attrs.put(AJAX_BEHAVIORS, ajaxBehaviors);
        }

        return ajaxBehaviors;
    }

    // Adds AjaxBehaviors to the specified ClientBehaviorHolder
    public void addBehaviors(FacesContext context, ClientBehaviorHolder behaviorHolder) {

        if (behaviorStack == null || behaviorStack.isEmpty()) {
            return;
        }

        // Loop over pushed Behaviors and add to the ClientBehaviorHolder.
        // Note that we add most recently pushed behaviors first. That
        // way the nearest behaviors take precedence. Behaviors that were
        // pushed earlier won't be added since we'll already have a
        // submitting behavior attached.
        Iterator<BehaviorInfo> descendingIter = behaviorStack.descendingIterator();
        while (descendingIter.hasNext()) {
            descendingIter.next().addBehavior(context, behaviorHolder);
        }
    }

    /**
     * <p>
     * Push the {@link AjaxBehavior} instance onto the <code>List</code>.
     * </p>
     *
     * @param ajaxBehavior the {@link AjaxBehavior} instance
     * @param eventName the name of the event that the behavior is associated with.
     *
     * @since 2.0
     */
    public void pushBehavior(FacesContext context, AjaxBehavior ajaxBehavior, String eventName) {
        behaviorStack.add(new BehaviorInfo(context, ajaxBehavior, eventName));
    }

    /**
     * <p>
     * Pop the last {@link AjaxBehavior} instance from the <code>List</code>.
     * </p>
     *
     * @since 2.0
     */
    public void popBehavior() {
        if (behaviorStack.size() > 0) {
            behaviorStack.removeLast();
        }
    }

    // Helper class for storing and creating/applying inherited
    // AjaxBehaviors
    public static class BehaviorInfo implements Serializable {
        private String eventName;
        private Object behaviorState;
        private static final long serialVersionUID = -7679229822647712959L;

        public BehaviorInfo(FacesContext context, AjaxBehavior ajaxBehavior, String eventName) {
            this.eventName = eventName;

            // We don't actually need the AjaxBehavior - just
            // its state.
            behaviorState = ajaxBehavior.saveState(context);
        }

        public void addBehavior(FacesContext context, ClientBehaviorHolder behaviorHolder) {

            String myEventName = eventName;
            if (myEventName == null) {
                myEventName = behaviorHolder.getDefaultEventName();

                // No event name, default or otherwise - we're done
                if (myEventName == null) {
                    return;
                }
            }

            // We only add the
            if (shouldAddBehavior(behaviorHolder, myEventName)) {
                ClientBehavior behavior = createBehavior(context);
                behaviorHolder.addClientBehavior(myEventName, behavior);
            }

        }

        // Tests whether we should add an AjaxBehavior to the specified
        // ClientBehaviorHolder/event name.
        private boolean shouldAddBehavior(ClientBehaviorHolder behaviorHolder, String eventName) {

            // First need to make sure that this ClientBehaviorHolder
            // supports the specified event type.
            if (!behaviorHolder.getEventNames().contains(eventName)) {
                return false;
            }

            // Check for a submitting behavior already attached.
            // If we've already got one, we don't add another.
            Map<String, List<ClientBehavior>> allBehaviors = behaviorHolder.getClientBehaviors();
            List<ClientBehavior> eventBehaviors = allBehaviors.get(eventName);

            if (eventBehaviors == null || eventBehaviors.isEmpty()) {
                return true;
            }

            for (ClientBehavior behavior : eventBehaviors) {
                Set<ClientBehaviorHint> hints = behavior.getHints();

                if (hints.contains(ClientBehaviorHint.SUBMITTING)) {
                    return false;
                }
            }

            return true;
        }

        // Creates the AjaxBehavior
        private ClientBehavior createBehavior(FacesContext context) {
            Application application = context.getApplication();

            // Re-create the instance via the Application
            AjaxBehavior behavior = (AjaxBehavior) application.createBehavior(AjaxBehavior.BEHAVIOR_ID);

            // And re-initialize its state
            behavior.restoreState(context, behaviorState);

            return behavior;
        }

        private BehaviorInfo() {
        }
    }

}
