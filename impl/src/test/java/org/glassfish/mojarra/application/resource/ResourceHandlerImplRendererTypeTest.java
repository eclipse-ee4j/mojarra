/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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
package org.glassfish.mojarra.application.resource;

import static org.glassfish.mojarra.application.resource.ResourceHandlerImpl.getRendererType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Tests that the renderer type is derived from the resource name itself, as specified by
 * {@link jakarta.faces.application.ResourceHandler#getRendererTypeForResourceName(String)}, and hence does not depend on
 * the content type the container happens to map the file extension to.
 */
class ResourceHandlerImplRendererTypeTest {

    private static final String SCRIPT = "jakarta.faces.resource.Script";
    private static final String STYLESHEET = "jakarta.faces.resource.Stylesheet";

    @Test
    void rendersScriptForJavaScriptFileExtension() {
        assertEquals(SCRIPT, getRendererType("mycomponent.js"));
        assertEquals(SCRIPT, getRendererType("js/mycomponent.js"));
        assertEquals(SCRIPT, getRendererType("mycomponent.JS"));
    }

    @Test
    void rendersStylesheetForCascadingStyleSheetFileExtension() {
        assertEquals(STYLESHEET, getRendererType("mystyle.css"));
        assertEquals(STYLESHEET, getRendererType("css/mystyle.css"));
        assertEquals(STYLESHEET, getRendererType("mystyle.CSS"));
    }

    /**
     * The query string is not part of the resource name identifying the file, so it does not hide the file extension.
     */
    @Test
    void rendersSameTypeForResourceNameCarryingQueryString() {
        assertEquals(SCRIPT, getRendererType("mycomponent.js?v=1"));
        assertEquals(STYLESHEET, getRendererType("mystyle.css?v=1"));
    }

    @Test
    void rendersNothingForAnyOtherResourceName() {
        assertNull(getRendererType(null));
        assertNull(getRendererType(""));
        assertNull(getRendererType("duke.gif"));
        assertNull(getRendererType("mycomponent.js.gz"));
        assertNull(getRendererType("js"));
    }

}
