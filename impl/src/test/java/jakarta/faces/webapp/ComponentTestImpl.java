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

package jakarta.faces.webapp;

import java.io.IOException;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

// Test UIComponent Class
public class ComponentTestImpl extends UIComponentBase {

    public ComponentTestImpl() {
    }

    public ComponentTestImpl(String id) {
        setId(id);
    }

    @Override
    public String getFamily() {
        return ("Test");
    }

    private String label = null;

    public String getLabel() {
        if (this.label != null) {
            return (this.label);
        }
        ValueExpression vb = getValueExpression("label");
        if (vb != null) {
            return ((String) vb.getValue(getFacesContext().getELContext()));
        } else {
            return (null);
        }
    }

    public void setLabel(String label) {
        this.label = label;
    }

    private boolean rendersChildren = false;

    @Override
    public boolean getRendersChildren() {
        return (this.rendersChildren);
    }

    public void setRendersChildren(boolean rendersChildren) {
        this.rendersChildren = rendersChildren;
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();
        writer.write("/b");
        String id = getId();
        if (id != null) {
            writer.write(id);
        }
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        if (isRendered()) {
            super.encodeChildren(context);
        }
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        ResponseWriter writer = context.getResponseWriter();
        writer.write("/e");
        String id = getId();
        if (id != null) {
            writer.write(id);
        }
    }
}
