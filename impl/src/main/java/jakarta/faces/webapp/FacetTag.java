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

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

/**
 * <p>
 * <strong>FacetTag</strong> is the Jakarta Server Pages mechanism for denoting a
 * {@link jakarta.faces.component.UIComponent} is to be added as a <code>facet</code> to the component associated with
 * its parent.
 * </p>
 *
 * <p>
 * A <strong>FacetTag</strong> must have one and only one child. This child must be a {@link UIComponentTag} instance
 * representing a single {@link jakarta.faces.component.UIComponent} instance.
 * </p>
 */

public class FacetTag extends TagSupport {

    // -------------------------------------------------------------- Properties

    private static final long serialVersionUID = 5019035861261307895L;

    /**
     * <p>
     * The name of this facet. This will be used as the facet name for our <code>UIComponentTag</code> child in our
     * <code>UIComponentTag</code> parent's facet list.
     * </p>
     */
    private String name = null;

    /**
     * <p>
     * Return the name to be assigned to this facet.
     * </p>
     *
     * @return the name
     */
    public String getName() {

        return name;

    }

    /**
     * <p>
     * Set the name to be assigned to this facet.
     * </p>
     *
     * @param name The new facet name
     */
    public void setName(String name) {

        this.name = name;

    }

    // ------------------------------------------------------------- Tag Methods

    /**
     * <p>
     * Release any resources allocated by this tag instance.
     */
    @Override
    public void release() {

        super.release();
        name = null;

    }

    /**
     * <p>
     * Return <code>EVAL_BODY_INCLUDE</code> to cause nested body content to be evaluated.
     * </p>
     */
    @Override
    public int doStartTag() throws JspException {

        return EVAL_BODY_INCLUDE;

    }

}
