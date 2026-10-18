/*
 * Copyright (c) Contributors to Eclipse Foundation.
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

package com.sun.faces.util;

import static com.sun.faces.RIConstants.FACELETS_ENCODING_DYNAMIC_KEY;
import static com.sun.faces.RIConstants.FACELETS_ENCODING_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.junit.JUnitFacesTestCaseBase;

import jakarta.faces.component.UIViewRoot;

class UtilResponseEncodingTest extends JUnitFacesTestCaseBase {

    private UIViewRoot viewRoot;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        viewRoot = new UIViewRoot();
        viewRoot.setViewId("/test.xhtml");
        facesContext.setViewRoot(viewRoot);
        viewRoot.markInitialState();
    }

    /**
     * The response encoding is computed again on every render, so remembering it for the remainder of the request must
     * not add anything to the state which partial state saving saves for a view whose components did not change.
     */
    @Test
    void responseEncodingIsNotSavedInViewState() {
        Util.setResponseEncoding(facesContext, "ISO-8859-1");

        assertNull(viewRoot.saveState(facesContext));
        assertEquals("ISO-8859-1", Util.getResponseEncoding(facesContext));
    }

    /**
     * An encoding from an expression in {@code <f:view encoding>} may evaluate differently on the next request, so the
     * view state must carry the encoding the view was rendered with.
     */
    @Test
    void responseEncodingFromExpressionIsSavedInViewState() {
        facesContext.getAttributes().put(FACELETS_ENCODING_DYNAMIC_KEY, Boolean.TRUE);

        Util.setResponseEncoding(facesContext, "ISO-8859-1");

        assertNotNull(viewRoot.saveState(facesContext));
        assertEquals("ISO-8859-1", Util.getResponseEncoding(facesContext));
    }

    /**
     * Within the request, the remembered response encoding takes precedence over every other source of the encoding.
     */
    @Test
    void responseEncodingIsReturnedForRemainderOfRequest() {
        facesContext.getAttributes().put(FACELETS_ENCODING_KEY, "UTF-8");

        Util.setResponseEncoding(facesContext, "ISO-8859-1");

        assertEquals("ISO-8859-1", Util.getResponseEncoding(facesContext));
    }

    /**
     * Before the response is rendered, the encoding which {@code <f:view encoding>} put in the attributes of the view
     * root, or which was restored from the view state, is the response encoding.
     */
    @Test
    void responseEncodingFromViewRootAttributeIsReturned() {
        viewRoot.getAttributes().put(FACELETS_ENCODING_KEY, "ISO-8859-1");

        assertEquals("ISO-8859-1", Util.getResponseEncoding(facesContext));
    }

    /**
     * The encoding which was actually set on the response takes precedence over the one in the attributes of the view
     * root.
     */
    @Test
    void responseEncodingSetOnResponseTakesPrecedenceOverViewRootAttribute() {
        viewRoot.getAttributes().put(FACELETS_ENCODING_KEY, "UTF-8");

        Util.setResponseEncoding(facesContext, "ISO-8859-1");

        assertEquals("ISO-8859-1", Util.getResponseEncoding(facesContext));
    }
}
