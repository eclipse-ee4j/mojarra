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

package jakarta.faces.event;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 *
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_2">When</span> an instance of this event is passed to
 * {@link SystemEventListener#processEvent} or {@link ComponentSystemEventListener#processEvent}, the listener
 * implementation may assume that the <code>source</code> of this event instance is a {@link UIComponent} instance and
 * that either that instance or an ancestor of that instance was just added to the view. Therefore, the implementation
 * may assume it is safe to call {@link UIComponent#getParent}, {@link UIComponent#getClientId}, and other methods that
 * depend upon the component instance being added into the view.
 * </p>
 *
 * <div class="changed_added_2_0 changed_deleted_2_2">
 *
 * <p>
 * The implementation must guarantee that {@link jakarta.faces.application.Application#publishEvent} is called,
 * immediately after any <code>UIComponent</code> instance is added to the view hierarchy <strong>except</strong> in the
 * case where {@link jakarta.faces.render.ResponseStateManager#isPostback} returns <code>true</code> <strong>at the same
 * time as</strong> {@link jakarta.faces.context.FacesContext#getCurrentPhaseId} returns
 * {@link jakarta.faces.event.PhaseId#RESTORE_VIEW}. When both of those conditions are met,
 * {@link jakarta.faces.application.Application#publishEvent} must not be called.
 * </p>
 *
 * </div>
 *
 * <div class="changed_added_2_2">
 *
 * <p>
 * The implementation must guarantee that {@link jakarta.faces.application.Application#publishEvent} is called in the
 * following cases.
 * </p>
 *
 * <ul>
 *
 * <li>
 * <p>
 * Upon the initial construction of the view, when each instance is added to the view.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * On a non-initial rendering of the view, if a component is added to the view by the View Declaration Language
 * implementation as a result of changes in evaluation result of Jakarta Expression Language expressions referenced by
 * VDL tags such as <code>c:if</code>, <code>ui:include</code>, and other tags that dynamically influence the assembly
 * of the view.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * If a component is programmatically added to the view using the Java API directly. For example, user code manually
 * adds children using <code>comp.getChildren().add()</code>, where <code>comp</code> is a <code>UIComponent</code>.
 * </p>
 * </li>
 *
 * </ul>
 *
 *
 * </div>
 *
 * @since 2.0
 */
public class PostAddToViewEvent extends ComponentSystemEvent {

    private static final long serialVersionUID = -1113592223476173895L;

    // ------------------------------------------------------------ Constructors

    /**
     * <p class="changed_added_2_0">
     * Instantiate a new <code>PostAddToViewEvent</code> that indicates the argument <code>component</code> was just added
     * to the view.
     * </p>
     *
     * @param component the <code>UIComponent</code> that has just been added to the view.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    public PostAddToViewEvent(UIComponent component) {

        super(component);

    }

    /**
     * <p class="changed_added_2_3">
     * Instantiate a new <code>PostAddToViewEvent</code> that indicates the argument <code>component</code> was just added
     * to the view.
     * </p>
     *
     * @param facesContext the Faces context.
     * @param component the <code>UIComponent</code> that has just been added to the view.
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    public PostAddToViewEvent(FacesContext facesContext, UIComponent component) {
        super(facesContext, component);
    }

    // --------------------------------------- Methods from ComponentSystemEvent

    /**
     * <p class="changed_added_2_0">
     * Returns <code>true</code> if and only if the argument <code>listener</code> is an instance of
     * {@link SystemEventListener}.
     * </p>
     *
     * @param listener the faces listener.
     * @return true if it is an appropriate listener, false otherwise.
     */
    @Override
    public boolean isAppropriateListener(FacesListener listener) {

        return listener instanceof SystemEventListener;

    }

}
