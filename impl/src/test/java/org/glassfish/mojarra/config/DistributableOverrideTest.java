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

package org.glassfish.mojarra.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.glassfish.mojarra.context.AlwaysPuttingSessionMap;
import org.glassfish.mojarra.context.ExternalContextImpl;
import org.glassfish.mojarra.mock.MockApplication;
import org.glassfish.mojarra.mock.MockFacesContext;
import org.glassfish.mojarra.mock.MockHttpServletRequest;
import org.glassfish.mojarra.mock.MockHttpServletResponse;
import org.glassfish.mojarra.mock.MockHttpSession;
import org.glassfish.mojarra.mock.MockServletContext;
import org.junit.jupiter.api.Test;

/**
 * A <code>&lt;distributable/&gt;</code> in <code>web.xml</code> turns on <code>org.glassfish.mojarra.enableDistributable</code> without the parameter itself
 * being declared, which {@link ConfigureListener} applies by overriding what the parameter resolved to. A reader which goes to the servlet context for the
 * declared value instead of asking this configuration therefore misses it, and the session map is silently not the replicating one.
 */
class DistributableOverrideTest {

    @Test
    void anOverriddenParameterIsSeenByItsReaders() {
        MockServletContext servletContext = new MockServletContext();
        WebConfiguration.getInstance(servletContext).overrideValue(MojarraContextParam.ENABLE_DISTRIBUTABLE, true);

        assertTrue(sessionMapOf(servletContext) instanceof AlwaysPuttingSessionMap, "session map replicates");
    }

    /**
     * And without one, nothing pretends the application is distributable.
     */
    @Test
    void anUndeclaredParameterLeavesItOff() {
        assertFalse(sessionMapOf(new MockServletContext()) instanceof AlwaysPuttingSessionMap, "session map replicates");
    }

    private static Object sessionMapOf(MockServletContext servletContext) {
        ExternalContextImpl externalContext = new ExternalContextImpl(
            servletContext, new MockHttpServletRequest(new MockHttpSession()),
            new MockHttpServletResponse()
        );
        MockFacesContext facesContext = new MockFacesContext(externalContext);
        facesContext.setApplication(new MockApplication());

        return externalContext.getSessionMap();
    }

}
