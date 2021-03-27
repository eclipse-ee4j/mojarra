/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.action;

import jakarta.faces.context.FacesContext;

/**
 * The ActionMappingMatcher API.
 */
public interface ActionMappingMatcher {

    /**
     * Match request to an action mapping.
     * 
     * @param facesContext the Faces context.
     * @return the action mapping match, or null if not found.
     */
    public ActionMappingMatch match(FacesContext facesContext);
}
