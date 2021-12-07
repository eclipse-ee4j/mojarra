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

package jakarta.faces.component.behavior;

import java.util.ArrayList;
import java.util.List;

import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.BehaviorEvent;
import jakarta.faces.event.BehaviorListener;

/**
 * <p class="changed_added_2_0">
 * <strong>BehaviorBase</strong> is a convenience base class that provides a default implementation of the
 * {@link Behavior} contract. It also provides behavior listener registration and state saving support.
 * </p>
 *
 * @since 2.0
 */
public class BehaviorBase implements Behavior, PartialStateHolder {

    /**
     * <p>
     * Our {@link jakarta.faces.event.BehaviorListener}s. This data structure is lazily instantiated as necessary.
     * </p>
     */
    private List<BehaviorListener> listeners;

    // Flag indicating a desire to not participate in state saving.
    private boolean transientFlag = false;

    // Flag indicating that initial state has been marked.
    private boolean initialState = false;

    /**
     * <p class="changed_added_2_0">
     * Default implementation of {@link Behavior#broadcast}. Delivers the specified {@link BehaviorEvent} to all registered
     * {@link BehaviorListener} event listeners who have expressed an interest in events of this type. Listeners are called
     * in the order in which they were registered (added).
     * </p>
     *
     * @param event The {@link BehaviorEvent} to be broadcast
     *
     * @throws AbortProcessingException Signal the Jakarta Faces implementation that no further processing on the
     * current event should be performed
     * @throws IllegalArgumentException if the implementation class of this {@link BehaviorEvent} is not supported by this
     * component
     * @throws NullPointerException if <code>event</code> is <code>null</code>
     *
     * @since 2.0
     */
    @Override
    public void broadcast(BehaviorEvent event) throws AbortProcessingException {

        if (null == event) {
            throw new NullPointerException();
        }

        if (null != listeners) {
            for (BehaviorListener listener : listeners) {
                if (event.isAppropriateListener(listener)) {
                    event.processListener(listener);
                }
            }
        }
    }

    /**
     * <p class="changed_added_2_0">
     * Implementation of {@link jakarta.faces.component.StateHolder#isTransient}.
     */
    @Override
    public boolean isTransient() {
        return transientFlag;
    }

    /**
     * <p class="changed_added_2_0">
     * Implementation of {@link jakarta.faces.component.StateHolder#setTransient}.
     */
    @Override
    public void setTransient(boolean transientFlag) {
        this.transientFlag = transientFlag;
    }

    /**
     * <p class="changed_added_2_0">
     * Implementation of {@link jakarta.faces.component.StateHolder#saveState}.
     */
    @Override
    public Object saveState(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        // If initial state has been marked, we don't need to
        // save any state.
        if (initialStateMarked()) {
            return null;
        }

        // At the moment, the only state we need to save is our listeners
        return UIComponentBase.saveAttachedState(context, listeners);
    }

    /**
     * <p class="changed_added_2_0">
     * Implementation of {@link jakarta.faces.component.StateHolder#restoreState}.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void restoreState(FacesContext context, Object state) {

        if (context == null) {
            throw new NullPointerException();
        }

        if (state != null) {

            // Unchecked cast from Object to List<BehaviorListener>
            listeners = (List<BehaviorListener>) UIComponentBase.restoreAttachedState(context, state);

            // If we saved state last time, save state again next time.
            clearInitialState();
        }
    }

    /**
     * <p class="changed_added_2_0">
     * Implementation of {@link jakarta.faces.component.PartialStateHolder#markInitialState}.
     */
    @Override
    public void markInitialState() {
        initialState = true;
    }

    /**
     * <p class="changed_added_2_0">
     * Implementation of {@link jakarta.faces.component.PartialStateHolder#initialStateMarked}.
     */
    @Override
    public boolean initialStateMarked() {
        return initialState;
    }

    /**
     * <p class="changed_added_2_0">
     * Clears the initial state flag, causing the behavior to revert from partial to full state saving.
     * </p>
     */
    @Override
    public void clearInitialState() {
        initialState = false;
    }

    /**
     * <p class="changed_added_2_0">
     * Add the specified {@link BehaviorListener} to the set of listeners registered to receive event notifications from
     * this {@link Behavior}. It is expected that {@link Behavior} classes acting as event sources will have corresponding
     * typesafe APIs for registering listeners of the required type, and the implementation of those registration methods
     * will delegate to this method. For example:
     * </p>
     *
     * <pre>
     * public class AjaxBehaviorEvent extends BehaviorEvent { ... }
     *
     * public interface AjaxBehaviorListener extends BehaviorListener {
     *   public void processAjaxBehavior(FooEvent event);
     * }
     *
     * public class AjaxBehavior extends ClientBehaviorBase {
     *   ...
     *   public void addAjaxBehaviorListener(AjaxBehaviorListener listener) {
     *     addBehaviorListener(listener);
     *   }
     *   public void removeAjaxBehaviorListener(AjaxBehaviorListener listener) {
     *     removeBehaviorListener(listener);
     *   }
     *   ...
     * }
     * </pre>
     *
     * @param listener The {@link BehaviorListener} to be registered
     *
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     *
     * @since 2.0
     */
    protected void addBehaviorListener(BehaviorListener listener) {

        if (listener == null) {
            throw new NullPointerException();
        }
        if (listeners == null) {
            // noinspection CollectionWithoutInitialCapacity
            listeners = new ArrayList<>();
        }
        listeners.add(listener);

        clearInitialState();
    }

    /**
     * <p class="changed_added_2_0">
     * Remove the specified {@link BehaviorListener} from the set of listeners registered to receive event notifications
     * from this {@link Behavior}.
     *
     * @param listener The {@link BehaviorListener} to be deregistered
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     *
     * @since 2.0
     */
    protected void removeBehaviorListener(BehaviorListener listener) {

        if (listener == null) {
            throw new NullPointerException();
        }
        if (listeners == null) {
            return;
        }
        listeners.remove(listener);

        clearInitialState();
    }
}
