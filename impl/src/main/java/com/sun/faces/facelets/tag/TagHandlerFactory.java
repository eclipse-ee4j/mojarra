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

package com.sun.faces.facelets.tag;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

/**
 * Delegate class for TagLibraries
 *
 * @see TagLibrary
 * @author Jacob Hookom
 * @version $Id$
 */
interface TagHandlerFactory {
    /**
     * A new TagHandler instantiated with the passed TagConfig
     *
     * @param cfg TagConfiguration information
     * @return a new TagHandler
     * @throws FacesException
     * @throws ELException
     */
    TagHandler createHandler(TagConfig cfg) throws FacesException, ELException;
}
