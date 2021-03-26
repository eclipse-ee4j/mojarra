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
 * <p class="changed_added_2_0">
 * <strong>UniqueIdVendor</strong> is an interface implemented by <code>UIComponents</code> that also implement
 * {@link NamingContainer} so that they can provide unique ids based on their own clientId. This will reduce the amount
 * of id generation variance between different renderings of the same view and is helpful for improved state saving.
 * </p>
 *
 * @since 2.0
 */
public interface UniqueIdVendor {

    /**
     * <p>
     * Generate an identifier for a component. The identifier will be prefixed with UNIQUE_ID_PREFIX, and will be unique
     * within this component-container. Optionally, a unique seed value can be supplied by component creators which should
     * be included in the generated unique id.
     * </p>
     *
     * @param context FacesContext
     * @param seed an optional seed value - e.g. based on the position of the component in the VDL-template
     * @return a unique-id in this component-container
     */
    String createUniqueId(FacesContext context, String seed);

}
