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

import jakarta.faces.view.ViewDeclarationLanguage;

/**
 * <p class="changed_added_2_3">
 * Defines the view traversal options.
 * </p>
 *
 * @since 2.3
 *
 * @see ViewHandler#getViews(jakarta.faces.context.FacesContext, String, int, ViewVisitOption...)
 * @see ViewDeclarationLanguage#getViews(jakarta.faces.context.FacesContext, String, int, ViewVisitOption...)
 *
 */
public enum ViewVisitOption {

    /**
     * Return the logical views in the most minimal form form such that they can still be used for an implicit match by the
     * navigation handler as described in the Jakarta Faces Specification Document section 7.4.2 "Default NavigationHandler Algorithm".
     * <p>
     * For example, for the Facelets VDL a view such as <code>/foo/bar.xhtml</code> would be returned as
     * <code>/foo/bar</code>.
     *
     */
    RETURN_AS_MINIMAL_IMPLICIT_OUTCOME

}
