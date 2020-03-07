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

package jakarta.faces.flow.builder;

/**
 * <p class="changed_added_2_2">
 * Base interface for building all kinds of flow nodes.
 * </p>
 *
 * @since 2.2
 */

public interface NodeBuilder {

    /**
     * <p class="changed_added_2_2">
     * Mark this node as the start node in the flow. Any other node that had been marked as the start node will no longer be
     * the start node.
     * </p>
     *
     * @since 2.2
     *
     * @return the builder instance
     */

    NodeBuilder markAsStartNode();

}
