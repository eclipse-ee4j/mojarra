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

import java.io.Serializable;

import jakarta.faces.flow.ViewNode;

public class ViewNodeImpl extends ViewNode implements Serializable {

    private static final long serialVersionUID = -7577859001307479164L;

    private final String id;
    private final String vdlDocumentId;

    public ViewNodeImpl(String id, String vdlDocumentIdIn) {
        this.id = id;

        int i = vdlDocumentIdIn.indexOf("META-INF/flows");

        if (-1 != i) {
            vdlDocumentIdIn = vdlDocumentIdIn.substring(i + 14);
        } else if (vdlDocumentIdIn.startsWith("/WEB-INF")) {
            vdlDocumentIdIn = vdlDocumentIdIn.substring(8);
        } else if (vdlDocumentIdIn.startsWith("WEB-INF")) {
            vdlDocumentIdIn = vdlDocumentIdIn.substring(7);
        }
        vdlDocumentId = vdlDocumentIdIn;

    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getVdlDocumentId() {
        return vdlDocumentId;
    }

}
