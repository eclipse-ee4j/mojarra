///
/// Copyright (c) 2026 Contributors to the Eclipse Foundation.
///
/// This program and the accompanying materials are made available under the
/// terms of the Eclipse Public License v. 2.0, which is available at
/// http://www.eclipse.org/legal/epl-2.0.
///
/// This Source Code may also be made available under the following Secondary
/// Licenses when the conditions for such availability set forth in the
/// Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
/// version 2 with the GNU Classpath Exception, which is available at
/// https://www.gnu.org/software/classpath/license.html.
///
/// SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
///

/**
 * Bundle wiring for the public `faces` namespace.
 *
 * Each member is implemented in its own module; this file binds the
 * imports onto `window.faces` under the legacy spec-version guard so that
 * an already-loaded faces.js of equal-or-newer version is not overwritten.
 */

import { ajax as facesAjax } from "./faces/ajax";
import { getProjectStage, getViewState, getClientWindow } from "./faces/api";
import { push as facesPush } from "./faces/push";
import { chain as utilChain } from "./faces/util";

// Detect if this is already loaded, and if loaded, if it's a higher version
if (!((window.faces && faces.specversion && faces.specversion >= parseInt('#{applicationScope["org.glassfish.mojarra.mojarraVersion"].specversion}', 10)) &&
      (faces.implversion && faces.implversion >= parseInt('#{applicationScope["org.glassfish.mojarra.mojarraVersion"].implversion}', 10)))) {

    /**
     * <span class="changed_modified_2_2">The top level global namespace
     * for Jakarta Faces functionality.</span>
     * @name faces
     * @namespace
     */
    window.faces = {};

    /**
     * The namespace for Ajax functionality.
     * @see api/.../faces.d.ts faces.ajax
     */
    faces.ajax = facesAjax;

    /**
     * Return the value of Application.getProjectStage() for the currently running application instance.
     * @see api/.../faces.d.ts faces.getProjectStage
     */
    faces.getProjectStage = getProjectStage;

    /**
     * Collect and encode state for input controls associated with the specified form element.
     * @see api/.../faces.d.ts faces.getViewState
     */
    faces.getViewState = getViewState;

    /**
     * Return the windowId of the window in which the argument form is rendered.
     * @see api/.../faces.d.ts faces.getClientWindow
     */
    faces.getClientWindow = getClientWindow;

    /**
     * <p class="changed_added_2_3">The Push functionality.</p>
     * @name faces.push
     * @namespace
     */
    faces.push = facesPush;


    /**
     * The namespace for Jakarta Faces JavaScript utilities.
     * @name faces.util
     * @namespace
     */
    faces.util = { chain: utilChain };

    /**
     * <p class="changed_added_2_2">The result of calling
     * <code>UINamingContainer.getNamingContainerSeparatorChar().</code></p>
     */
    faces.separatorchar = "#{facesContext.namingContainerSeparatorChar}";

    /**
     * <p class="changed_added_2_3">
     * The result of calling <code>ExternalContext.getRequestContextPath()</code>.
     */
    faces.contextpath = "#{facesContext.externalContext.requestContextPath}";

    /**
     * <p>An integer specifying the specification version that this file implements.
     * Its format is: rightmost two digits, bug release number, next two digits,
     * minor release number, leftmost digits, major release number.
     * This number may only be incremented by a new release of the specification.</p>
     */
    faces.specversion = parseInt('#{applicationScope["org.glassfish.mojarra.mojarraVersion"].specversion}', 10);

    /**
     * <p>An integer specifying the implementation version that this file implements.
     * It's a monotonically increasing number, reset with every increment of
     * <code>faces.specversion</code>
     * This number is implementation dependent.</p>
     */
    faces.implversion = parseInt('#{applicationScope["org.glassfish.mojarra.mojarraVersion"].implversion}', 10);


} //end if version detection block
