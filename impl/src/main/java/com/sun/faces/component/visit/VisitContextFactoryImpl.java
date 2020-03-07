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

package com.sun.faces.component.visit;

import java.util.Collection;
import java.util.Set;

import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitContextFactory;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * Default implementation of {@link VisitContextFactory}.
 * </p>
 */
public class VisitContextFactoryImpl extends VisitContextFactory {

    public VisitContextFactoryImpl() {
        super(null);
    }

    @Override
    public VisitContext getVisitContext(FacesContext context, Collection<String> ids, Set<VisitHint> hints) {
        VisitContext result = null;

        // If ids null (not empty), we create a FullVisitContext.
        // Otherwise, we create a PartialVisitContext. Note that
        // an empty collection still means partial - the client
        // can add ids to visit after they create the VisitContext.
        if (null == ids) {
            result = new FullVisitContext(context, hints);
        } else {
            result = new PartialVisitContext(context, ids, hints);
        }

        return result;
    }

}
