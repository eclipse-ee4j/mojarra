/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.facelets.tag.faces.html;

import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;

/**
 * <code>ComponentResourceDelegate</code> for stylesheet references.
 */
public class StylesheetResourceDelegate extends ComponentResourceDelegate {

    // ------------------------------------------------------------ Constructors

    public StylesheetResourceDelegate(ComponentHandler owner) {
        super(owner);
    }

    // ----------------------------------- Methods from ComponentResourceDelegate

    /**
     * @param ctx the <code>FacesContext</code> for the current request
     * @return <code>head</code> as external stylesheet references are only valid in the head of a document, this method
     * returns <code>head</code>
     */
    @Override
    protected String getLocationTarget(FaceletContext ctx) {

        return "head";

    }

}
