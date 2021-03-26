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


import javax.faces.webapp.UIComponentBodyTag;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;


/**
 * <p><code>UIComponentBodyTag</code> for <code>ChildrenComponent</code>.</p>
 */

public class ChildrenComponentBodyTag extends UIComponentBodyTag {

    private boolean firstPass = true;

    // -------------------------------------------------------------- Attributes


    // ---------------------------------------------------------- Public Methods


    public String getComponentType() {
        return ("ChildrenComponent");
    }


    public String getRendererType() {
        return (null);
    }


    /**
     * <p>Handle the ending of the nested body content for this tag.  The
     * default implementation simply calls <code>getDoAfterBodyValue()</code> to
     * retrieve the flag value to be returned.</p>
     *
     * @throws javax.servlet.jsp.JspException if an error is encountered
     */
    public int doAfterBody() throws JspException {
        if (firstPass) {
            System.out.println("Evaluating body again...");
            BodyContent cont = getBodyContent();
            cont.clearBody();
            firstPass = false;
            return EVAL_BODY_AGAIN;
        }
        else {
            return super.doAfterBody();
        }
    }


    // ------------------------------------------------------- Protected Methods


}
