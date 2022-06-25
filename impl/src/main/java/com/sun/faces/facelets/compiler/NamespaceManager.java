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

package com.sun.faces.facelets.compiler;

import java.util.ArrayList;
import java.util.List;

import com.sun.faces.facelets.tag.TagLibrary;

/**
 * @author Jacob Hookom
 * @version $Id$
 */
final class NamespaceManager {

    private final static class NS {
        public final String prefix;

        public final String namespace;

        public NS(String prefix, String ns) {
            this.prefix = prefix;
            namespace = ns;
        }
    }

    private final List<NS> namespaces;

    /**
     *
     */
    public NamespaceManager() {
        namespaces = new ArrayList<>();
    }

    public void reset() {
        namespaces.clear();
    }

    public void pushNamespace(String prefix, String namespace) {
        NS ns = new NS(prefix, namespace);
        namespaces.add(0, ns);
    }

    public String getNamespace(String prefix) {
        NS ns = null;
        for (int i = 0; i < namespaces.size(); i++) {
            ns = namespaces.get(i);
            if (ns.prefix.equals(prefix)) {
                return ns.namespace;
            }
        }
        return null;
    }

    public void popNamespace(String prefix) {
        NS ns = null;
        for (int i = 0; i < namespaces.size(); i++) {
            ns = namespaces.get(i);
            if (ns.prefix.equals(prefix)) {
                namespaces.remove(i);
                return;
            }
        }
    }

    public NamespaceUnit toNamespaceUnit(TagLibrary library) {
        NamespaceUnit unit = new NamespaceUnit(library);
        if (namespaces.size() > 0) {
            NS ns = null;
            for (int i = namespaces.size() - 1; i >= 0; i--) {
                ns = namespaces.get(i);
                unit.setNamespace(ns.prefix, ns.namespace);
            }
        }
        return unit;
    }

}
