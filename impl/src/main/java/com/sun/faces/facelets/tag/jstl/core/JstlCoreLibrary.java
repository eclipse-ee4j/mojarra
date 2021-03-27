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

package com.sun.faces.facelets.tag.jstl.core;

import static com.sun.faces.util.Util.unmodifiableSet;

import java.util.Set;

import com.sun.faces.facelets.tag.AbstractTagLibrary;

/**
 * The JSTL c library.
 *
 * @author Jacob Hookom
 */
public final class JstlCoreLibrary extends AbstractTagLibrary {

    private final static String SunNamespace = "http://java.sun.com/jsp/jstl/core";
    private final static String FaceletsNamespace = "http://java.sun.com/jstl/core";
    private final static String JcpNamespace = "http://xmlns.jcp.org/jsp/jstl/core";
    private final static String JakartaNamespace = "jakarta.tags.core";

    public final static Set<String> NAMESPACES = unmodifiableSet(JakartaNamespace, JcpNamespace, FaceletsNamespace, SunNamespace);
    public final static String DEFAULT_NAMESPACE = JakartaNamespace;

    /**
     * Constructor.
     *
     * <p>
     * This constructor is used to allow the namespace 'http://java.sun.com/jstl/core' to be used as another way to resolve
     * to the JSTL c library. This is used for backwards compatibility.
     * </p>
     *
     * @param namespace the namespace.
     */
    public JstlCoreLibrary(String namespace) {
        super(namespace);
        addTagHandler("if", IfHandler.class);
        addTagHandler("forEach", ForEachHandler.class);
        addTagHandler("catch", CatchHandler.class);
        addTagHandler("choose", ChooseHandler.class);
        addTagHandler("when", ChooseWhenHandler.class);
        addTagHandler("otherwise", ChooseOtherwiseHandler.class);
        addTagHandler("set", SetHandler.class);
    }
}
