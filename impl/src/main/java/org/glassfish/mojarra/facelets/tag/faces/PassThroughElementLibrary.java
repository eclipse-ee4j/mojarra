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

package org.glassfish.mojarra.facelets.tag.faces;


import java.util.Set;

import org.glassfish.mojarra.facelets.tag.AbstractTagLibrary;

public final class PassThroughElementLibrary extends AbstractTagLibrary {

    private final static String JcpNamespace = "http://xmlns.jcp.org/jsf";
    private final static String JakartaNamespace = "jakarta.faces";

    public final static Set<String> NAMESPACES = Set.of(JakartaNamespace, JcpNamespace);
    public final static String DEFAULT_NAMESPACE = JakartaNamespace;

    public PassThroughElementLibrary(String namespace) {
        super(namespace);

        this.addComponent("element", "jakarta.faces.Panel", "jakarta.faces.passthrough.Element", PassThroughElementComponentHandler.class);
    }
}
