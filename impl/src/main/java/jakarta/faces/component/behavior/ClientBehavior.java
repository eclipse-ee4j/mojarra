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

import java.util.Set;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_0">
 * <strong>ClientBehavior</strong> is the base contract for {@link Behavior}s that attach script content to client-side
 * events exposed by {@link ClientBehaviorHolder} components. Instances of <code>ClientBehavior</code> may be attached
 * to components that implement the {@link ClientBehaviorHolder} contract by calling
 * {@link ClientBehaviorHolder#addClientBehavior}. Once a <code>ClientBehavior</code> has been attached to a
 * {@link ClientBehaviorHolder} component, the component calls {@link #getScript} to obtain the behavior's script and
 * the component wires this up to the appropriate client-side event handler. Note that the script content returned by
 * this method is always in-line script content. If the implementing class wants to invoke functions defined in other
 * script resources, the implementing class must use the {@link jakarta.faces.application.ResourceDependency} or
 * {@link jakarta.faces.application.ResourceDependencies} annotation.
 * </p>
 *
 * @since 2.0
 */
public interface ClientBehavior extends Behavior {

    /**
     * <p class="changed_added_2_0">
     * Return the script that implements this ClientBehavior's client-side logic.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * ClientBehavior.getScript() implementations are allowed to return null to indicate that no script is required for this
     * particular getScript() call. For example, a ClientBehavior implementation may return null if the Behavior is
     * disabled.
     * </p>
     *
     * </div>
     *
     * @param behaviorContext the {@link ClientBehaviorContext} that provides properties that might influence this
     * getScript() call. Note that ClientBehaviorContext instances are short-lived objects that are only valid for the
     * duration of the call to getScript(). ClientBehavior implementations must not hold onto references to
     * ClientBehaviorContexts.
     *
     * @return script that provides the client-side behavior, or null if no script is required.
     * @throws NullPointerException if <code>behaviorContext</code> is <code>null</code>
     *
     * @since 2.0
     */
    String getScript(ClientBehaviorContext behaviorContext);

    /**
     * <p class="changed_added_2_0">
     * Decode any new state of this {@link ClientBehavior} from the request contained in the specified {@link FacesContext}.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * During decoding, events may be enqueued for later processing (by event listeners who have registered an interest), by
     * calling <code>queueEvent()</code>. Default implementation delegates decoding to
     * {@link jakarta.faces.render.ClientBehaviorRenderer#decode(FacesContext, UIComponent, ClientBehavior)}
     * </p>
     *
     * </div>
     *
     * @param context {@link FacesContext} for the request we are processing
     * @param component {@link UIComponent} the component associated with this {@link Behavior}
     *
     * @throws NullPointerException if <code>context</code> or <code>component</code> is <code>null</code>.
     *
     * @since 2.0
     */
    void decode(FacesContext context, UIComponent component);

    /**
     * <p class="changed_added_2_0">
     * Returns hints that describe the behavior of the ClientBehavior implementation. The hints may impact how Renderers
     * behave in the presence of Behaviors. For example, when a Behavior that specifies
     * <code>ClientBehaviorHint.SUBMITTING</code> is present, the Renderer may choose to alternate the scripts that it
     * generates itself.
     * </p>
     *
     * @return a non-null, unmodifiable collection of ClientBehaviorHints.
     *
     * @since 2.0
     */
    Set<ClientBehaviorHint> getHints();
}
