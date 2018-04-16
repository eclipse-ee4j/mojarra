/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package javax.faces.component;

/**
 * <p>
 * <strong class="changed_modified_2_0">NamingContainer</strong> is an interface that must be
 * implemented by any {@link UIComponent} that wants to be a naming container. Naming containers
 * affect the behavior of the {@link UIComponent#findComponent} and {@link UIComponent#getClientId}
 * methods; see those methods for further information.
 * </p>
 */

public interface NamingContainer {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p class="changed_modified_2_0">
     * The separator character used in component identifiers to demarcate navigation to a child
     * naming container.
     * </p>
     * 
     * @deprecated use {@link UINamingContainer#getSeparatorChar(javax.faces.context.FacesContext)}
     */
    public static final char SEPARATOR_CHAR = ':';

}
