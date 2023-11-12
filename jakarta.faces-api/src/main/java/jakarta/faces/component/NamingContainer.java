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

/**
 * <p>
 * <strong class="changed_modified_2_0">NamingContainer</strong> is an interface that must be implemented by any
 * {@link UIComponent} that wants to be a naming container. Naming containers affect the behavior of the
 * {@link UIComponent#findComponent} and {@link UIComponent#getClientId} methods; see those methods for further
 * information.
 * </p>
 */

public interface NamingContainer {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p class="changed_modified_2_0">
     * The <span class="changed_modified_4_0">default</span> separator character used in component identifiers to demarcate navigation to a child naming container,
     * <span class="changed_added_4_0">in case {@link UINamingContainer#getSeparatorChar(jakarta.faces.context.FacesContext)} does not resolve to a valid value.
     * It is not recommended to use this value directly, the {@link UINamingContainer#getSeparatorChar(jakarta.faces.context.FacesContext)} must be used instead</span>.
     * </p>
     */
    char SEPARATOR_CHAR = ':';

}
