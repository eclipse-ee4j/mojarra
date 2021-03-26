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

package jakarta.faces.component;

import jakarta.faces.context.FacesContext;

/**
 *
 * <p>
 * A simple callback interace that enables taking action on a specific UIComponent (either facet or child) in the view
 * while preserving any contextual state for that component instance in the view.
 * </p>
 *
 */
public interface ContextCallback {

    /**
     * <p>
     * This method will be called by an implementation of {@link UIComponent#invokeOnComponent} and must be passed the
     * component with the <code>clientId</code> given as an argument to <code>invokeOnComponent</code>. At the point in time
     * when this method is called, the argument <code>target</code> is guaranteed to be in the proper state with respect to
     * its ancestors in the View.
     * </p>
     *
     * @param context the <code>FacesContext</code> for this request.
     *
     * @param target the {@link UIComponent} that was located by <code>clientId</code> by a call to
     * {@link UIComponent#invokeOnComponent}.
     */
    void invokeContextCallback(FacesContext context, UIComponent target);

}
