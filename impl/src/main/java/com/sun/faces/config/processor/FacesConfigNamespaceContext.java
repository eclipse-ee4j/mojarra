/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.config.processor;

import java.util.Collections;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

class FacesConfigNamespaceContext implements NamespaceContext {

    private final String defaultNamespace;

    FacesConfigNamespaceContext() {
        this.defaultNamespace = "https://jakarta.ee/xml/ns/jakartaee";
    }

    FacesConfigNamespaceContext(String namespace) {
        this.defaultNamespace = namespace;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return this.defaultNamespace;
    }

    @Override
    public String getPrefix(String namespaceURI) {
        return "ns1";
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        return Collections.emptyIterator();
    }

}
