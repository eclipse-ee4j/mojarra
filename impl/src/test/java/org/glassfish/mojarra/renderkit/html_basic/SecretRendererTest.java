/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.mojarra.renderkit.html_basic;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;

import jakarta.faces.component.html.HtmlInputSecret;
import jakarta.faces.context.FacesContext;

import org.glassfish.mojarra.context.ExternalContextImpl;
import org.glassfish.mojarra.mock.MockFacesContext;
import org.glassfish.mojarra.mock.MockHttpServletRequest;
import org.glassfish.mojarra.mock.MockHttpServletResponse;
import org.glassfish.mojarra.mock.MockHttpSession;
import org.glassfish.mojarra.mock.MockServletContext;
import org.junit.jupiter.api.Test;

class SecretRendererTest {

    @Test
    void rendersPlaceholder() throws Exception {
        StringWriter writer = new StringWriter();
        FacesContext context = new MockFacesContext(
            new ExternalContextImpl(new MockServletContext(), new MockHttpServletRequest(new MockHttpSession()), new MockHttpServletResponse())
        );
        context.setResponseWriter(new TestResponseWriter(writer));

        HtmlInputSecret secret = new HtmlInputSecret() {

            @Override
            public String getClientId(FacesContext context) {
                return "secret";
            }

        };
        secret.getAttributes().put("placeholder", "Password");

        new SecretRenderer().encodeEnd(context, secret);

        assertTrue(writer.toString().contains("placeholder=\"Password\""), writer::toString);
    }

}
