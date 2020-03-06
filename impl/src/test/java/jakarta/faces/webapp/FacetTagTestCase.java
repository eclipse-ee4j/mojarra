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

package jakarta.faces.webapp;

import junit.framework.Test;
import junit.framework.TestSuite;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.webapp.FacetTag;
import jakarta.faces.webapp.UIComponentTag;

/**
 * <p>
 * Unit tests for <code>FacetTag</code>.</p>
 */
public class FacetTagTestCase extends TagTestCaseBase {

    // ------------------------------------------------------ Instance Variables
    protected UIComponentTag ctag = null; // Component tag
    protected FacetTag ftag = null;       // Facet tag
    protected UIComponentTag rtag = null; // Root tag

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public FacetTagTestCase(String name) {

        super(name);

    }

    // ---------------------------------------------------- Overall Test Methods
    /**
     * Set up our root and component tags.
     * @throws java.lang.Exception
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        rtag = new TagTestImpl("ROOT", "root") {
            @Override
            protected void setProperties(UIComponent component) {
            }
        };
        rtag.setPageContext(this.pageContext);

        ftag = new FacetTag();
        ftag.setPageContext(this.pageContext);
        ftag.setParent(this.rtag);

        ctag = new OutputTagTestImpl();
        ctag.setPageContext(this.pageContext);
        ctag.setParent(this.ftag);
    }

    /**
     * Return the tests included in this test suite.
     * @return 
     */
    public static Test suite() {
        return (new TestSuite(FacetTagTestCase.class));
    }

    /**
     * Clear our root and component tags.
     * @throws java.lang.Exception
     */
    @Override
    public void tearDown() throws Exception {
        ctag = null;
        ftag = null;
        rtag = null;
        super.tearDown();
    }

    // ------------------------------------------------- Individual Test Methods
    // Test literal facet name
    public void testLiteral() throws Exception {
        rtag.doStartTag();
        ftag.setName("foo");
        ftag.doStartTag();
        ctag.doStartTag();

        UIComponent component = rtag.getComponentInstance();
        assertNotNull(component);
        UIComponent facet = component.getFacet("foo");
        assertNotNull(facet);
        assertTrue(facet instanceof UIOutput);

        ctag.doEndTag();
        ftag.doEndTag();
        rtag.doEndTag();
    }
}
