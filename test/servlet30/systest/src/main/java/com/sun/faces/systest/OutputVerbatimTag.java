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
import javax.faces.webapp.UIComponentBodyTag;
import javax.servlet.jsp.JspException;


/**
 * <p><code>UIComponentBodyTag</code> for <code>Verbatim</code> rendering.
 * In other words, the body content of this tag is captured and then
 * rendered as the value of this component.</p>
 */

public class OutputVerbatimTag extends UIComponentBodyTag {


    // -------------------------------------------------------------- Attributes


    // ---------------------------------------------------------- Public Methods


    public String getComponentType() {
        return ("Output");
    }


    public String getRendererType() {
        return ("Text");
    }


    // Assign the trimmed body content of this tag as the value of the
    // current component.
    public int doAfterBody() throws JspException {

        // Save the trimmed body content of this tag (if any)
        if (getBodyContent() != null) {
            String value = getBodyContent().getString().trim();
            if (value != null) {
                ((UIOutput) getComponentInstance()).setValue(value);
            }
        }

        // Perform normal superclass processing
        return (super.doAfterBody());

    }


    // ------------------------------------------------------- Protected Methods


}
