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

import java.util.Map;

/**
 * <p class="changed_added_2_0">
 * A structure that contains the name of the scope and the scope itself exposed as a <code>Map&lt;String,
 * Object&gt;</code>.
 * </p>
 *
 * @since 2.0
 */
public class ScopeContext {

    private final String scopeName;
    private final Map<String, Object> scope;

    // ------------------------------------------------------------ Constructors

    /**
     * <p class="changed_added_2_0">
     * Construct this structure with the supplied arguments.
     * </p>
     *
     * @param scopeName the name of the scope
     * @param scope the scope itself
     */
    public ScopeContext(String scopeName, Map<String, Object> scope) {

        this.scopeName = scopeName;
        this.scope = scope;

    }

    // ---------------------------------------------------------- Public Methods

    /**
     * <p class="changed_added_2_0">
     * Return the name of this custom scope.
     * </p>
     *
     * @return Return the name of this custom scope.
     */
    public String getScopeName() {

        return scopeName;

    }

    /**
     * <p class="changed_modified_2_0">
     * Return the scope itself, exposed as a <code>Map</code>.
     * </p>
     *
     * @return Return the scope itself, exposed as a <code>Map</code>
     */
    public Map<String, Object> getScope() {

        return scope;

    }

}
