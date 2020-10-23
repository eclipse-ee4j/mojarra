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

package com.sun.faces.context;

import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.context.PartialViewContextFactory;

/**
 * <p>
 * Factory to vend the default <code>PartialViewContext</code> instance.
 * </p>
 */
public class PartialViewContextFactoryImpl extends PartialViewContextFactory {

    public PartialViewContextFactoryImpl() {
        super(null);
    }

    // ---------------------------------- Methods from PartialViewContextFactory

    /**
     * @see PartialViewContextFactory#getPartialViewContext(jakarta.faces.context.FacesContext)
     */
    @Override
    public PartialViewContext getPartialViewContext(FacesContext context) {

        return new PartialViewContextImpl(context);

    }

}
