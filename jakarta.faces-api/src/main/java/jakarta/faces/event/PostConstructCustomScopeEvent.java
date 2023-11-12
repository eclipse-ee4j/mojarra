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

import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_0">
 * This class is provided to allow custom scopes to publish a "post construct" event in the same way that other scopes
 * do to let the application become aware of the beginning of the scope. The runtime must listen for this event and
 * invoke any <code>PostConstruct</code> annotated methods on any of the beans in this scope as appropriate. The
 * following code can be used as a template for publishing such an event.
 * </p>
 *
 * <pre>
 * <code>

Map&lt;String, Object&gt; myScope = getMyScope();
ScopeContext scopeContext = new ScopeContext("myScope", myScope);
applicationPublishEvent(PostConstructCustomScopeEvent.class, scopeContext);
</code>
 * </pre>
 *
 * @since 2.0
 * @deprecated because {@code CustomScope} has been removed from Faces in favor of CDI.
 */
@Deprecated(since = "4.1", forRemoval = true)
public class PostConstructCustomScopeEvent extends SystemEvent {

    // ------------------------------------------------------------ Constructors

    private static final long serialVersionUID = 4749939775937702379L;

    /**
     * <p class="changed_added_2_0">
     * An instance of this event indicates that the custom scope enclosed within the argument <code>scopeContext</code> was
     * just created.
     * </p>
     *
     * @param scopeContext A structure that contains the name of the scope and the scope itself exposed as a
     * <code>Map&lt;String,
     * Object&gt;</code>.
     *
     */
    public PostConstructCustomScopeEvent(ScopeContext scopeContext) {

        super(scopeContext);

    }

    /**
     * <p class="changed_added_2_3">
     * An instance of this event indicates that the custom scope enclosed within the argument <code>scopeContext</code> was
     * just created.
     * </p>
     *
     * @param facesContext the Faces context.
     * @param scopeContext A structure that contains the name of the scope and the scope itself exposed as a
     * <code>Map&lt;String,
     * Object&gt;</code>.
     */
    public PostConstructCustomScopeEvent(FacesContext facesContext, ScopeContext scopeContext) {
        super(facesContext, scopeContext);
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * <p class="changed_added_2_0">
     * Return the <code>ScopeContext</code> for this event.
     * </p>
     *
     * @return the scope context.
     */
    public ScopeContext getContext() {

        return (ScopeContext) getSource();

    }

}
