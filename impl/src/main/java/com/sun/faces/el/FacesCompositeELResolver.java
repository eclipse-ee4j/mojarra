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

package com.sun.faces.el;

import jakarta.el.CompositeELResolver;
import jakarta.el.ELResolver;

/**
 * Maintains an ordered composite list of child <code>ELResolver for Faces</code>.
 *
 */
public abstract class FacesCompositeELResolver extends CompositeELResolver {

    /**
     * <p>
     * <b>JSP</b> indicates this CompositeELResolver instance is the Jakarta Server Pages chain, specified in section 5.6.1 of the spec.
     * </p>
     *
     * <p>
     * <b>Faces</b> indicates this CompositeELResolver instance is the Faces chain, specified in section 5.6.2 of the spec.
     * </p>
     */
    public enum ELResolverChainType {
        JSP, Faces
    }

    public abstract ELResolverChainType getChainType();

    public abstract void addRootELResolver(ELResolver elResolver);

    public abstract void addPropertyELResolver(ELResolver elResolver);
}
