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

package com.sun.faces.facelets.tag.faces;

import static com.sun.faces.util.Util.unmodifiableSet;

import java.util.Set;

import com.sun.faces.facelets.tag.AbstractTagLibrary;

public final class PassThroughAttributeLibrary extends AbstractTagLibrary {

    private final static String JcpNamespace = "http://xmlns.jcp.org/jsf/passthrough";
    private final static String JakartaNamespace = "jakarta.faces.passthrough";

    public final static Set<String> NAMESPACES = unmodifiableSet(JakartaNamespace, JcpNamespace);
    public final static String DEFAULT_NAMESPACE = JakartaNamespace;

    public PassThroughAttributeLibrary(String namespace) {
        super(namespace);
    }
}
