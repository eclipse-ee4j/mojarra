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

package com.sun.faces.systest;

import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * <p>
 * Test <code>UIComponent</code> for sys tests.
 * </p>
 */

public class FacetComponent extends UIOutput {

    public FacetComponent() {
        super();
    }

    public FacetComponent(String id) {
        super();
        setId(id);
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        UIOutput facet = (UIOutput) getFacet("header");
        if (facet != null) {
            writer.write("[" + facet.getValue() + "] ");
        } else {
            writer.write("[] ");
        }
        writer.write("[");
        writer.write((String) getValue());
        writer.write("] ");
        facet = (UIOutput) getFacet("footer");
        if (facet != null) {
            writer.write("[" + facet.getValue() + "] ");
        } else {
            writer.write("[] ");
        }
    }

}
