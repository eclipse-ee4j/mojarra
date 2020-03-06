/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.faces.facelets.tag.TagLibrary;

import java.util.ArrayList;
import java.util.List;

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
            this.namespace = ns;
        }
    }

    private final List namespaces;

    /**
     *
     */
    public NamespaceManager() {
        this.namespaces = new ArrayList();
    }

    public void reset() {
        this.namespaces.clear();
    }

    public void pushNamespace(String prefix, String namespace) {
        NS ns = new NS(prefix, namespace);
        this.namespaces.add(0, ns);
    }

    public String getNamespace(String prefix) {
        NS ns = null;
        for (int i = 0; i < this.namespaces.size(); i++) {
            ns = (NS) this.namespaces.get(i);
            if (ns.prefix.equals(prefix)) {
                return ns.namespace;
            }
        }
        return null;
    }

    public void popNamespace(String prefix) {
        NS ns = null;
        for (int i = 0; i < this.namespaces.size(); i++) {
            ns = (NS) this.namespaces.get(i);
            if (ns.prefix.equals(prefix)) {
                this.namespaces.remove(i);
                return;
            }
        }
    }

    public NamespaceUnit toNamespaceUnit(TagLibrary library) {
        NamespaceUnit unit = new NamespaceUnit(library);
        if (this.namespaces.size() > 0) {
            NS ns = null;
            for (int i = this.namespaces.size() - 1; i >= 0; i--) {
                ns = (NS) this.namespaces.get(i);
                unit.setNamespace(ns.prefix, ns.namespace);
            }
        }
        return unit;
    }

}
