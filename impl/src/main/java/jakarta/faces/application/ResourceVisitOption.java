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

package jakarta.faces.application;

/**
 * <p class="changed_added_2_3">
 * Defines the resource traversal options.
 * </p>
 *
 * @since 2.3
 *
 * @see ResourceHandler#getViewResources(jakarta.faces.context.FacesContext, String, int, ResourceVisitOption...)
 *
 */
public enum ResourceVisitOption {

    /**
     * Only visit resources that are top level views, i.e. views that can be used to serve a request as opposed to those
     * that can only be used for includes.
     */
    TOP_LEVEL_VIEWS_ONLY

}
