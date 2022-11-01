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

package com.sun.faces.flow;

public class FlowDiscoveryInfo {

    private Class<?> definingClass;
    private String id;
    private String definingDocument;

    public String getDefiningDocument() {
        return definingDocument;
    }

    public void setDefiningDocument(String definingDocument) {
        this.definingDocument = definingDocument;
    }

    public FlowDiscoveryInfo(Class definingClass, String id, String definingDocument) {
        this.definingClass = definingClass;
        this.id = id;
        this.definingDocument = definingDocument;
    }

    public Class getDefiningClass() {
        return definingClass;
    }

    public void setDefiningClass(Class definingClass) {
        this.definingClass = definingClass;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
