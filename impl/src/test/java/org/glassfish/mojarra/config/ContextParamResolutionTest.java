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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.ResourceHandler;

import org.glassfish.mojarra.context.FacesContextParam;
import org.glassfish.mojarra.mock.MockServletContext;
import org.junit.jupiter.api.Test;

/**
 * A specification context parameter is resolved once, when this configuration is read, and every reader sees that same
 * value. This covers what the resolution does beyond handing the declared value back.
 */
class ContextParamResolutionTest {

    private static final String CSP_POLICY = "script-src 'self'";

    @Test
    void aParameterWhichWasNotDeclaredResolvesToItsDefault() {
        assertResolvesToItsDefault(FacesContextParam.CSP_POLICY, null);
        assertFalse(configure(null, null).isSet(FacesContextParam.CSP_POLICY));
    }

    /**
     * A parameter which used to be a Mojarra one answers to the unqualified name it had then, under the prefix it
     * actually carried and under the one the 5.0 rename produces for it.
     */
    @Test
    void aPromotedParameterIsHonouredUnderEitherOldSpelling() {
        for (String legacyName : new String[] { "com.sun.faces.cspPolicy", "org.glassfish.mojarra.cspPolicy" }) {
            WebConfiguration webConfiguration = configure(legacyName, CSP_POLICY);

            assertEquals(CSP_POLICY, webConfiguration.getString(FacesContextParam.CSP_POLICY), legacyName);
            assertTrue(webConfiguration.isSet(FacesContextParam.CSP_POLICY), legacyName);
        }
    }

    @Test
    void theSpecificationNameWinsOverAnOldSpelling() {
        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter("com.sun.faces.cspPolicy", "script-src 'none'");
        servletContext.addInitParameter(ResourceHandler.CSP_POLICY_PARAM_NAME, CSP_POLICY);

        assertEquals(CSP_POLICY, WebConfiguration.getInstance(servletContext).getValue(FacesContextParam.CSP_POLICY));
    }

    /**
     * A value which cannot be converted to the declared type behaves as though the parameter was never declared, rather
     * than as whatever a lenient parse happens to make of it, which for a boolean would silently be <code>false</code>
     * and would therefore turn a typo into the opposite of a default which is <code>true</code>.
     */
    @Test
    void anUnusableValueFallsBackToTheDefault() {
        assertResolvesToItsDefault(FacesContextParam.FACELETS_BUFFER_SIZE, "plenty");
        assertResolvesToItsDefault(FacesContextParam.SEPARATOR_CHAR, "::");
        assertResolvesToItsDefault(FacesContextParam.ENABLE_CSP_NONCE, "treu");
        assertResolvesToItsDefault(FacesContextParam.STATE_SAVING_METHOD, "somewhere");
    }

    /**
     * And surrounding whitespace, which a container is not obliged to strip from a context parameter, does not turn a
     * usable value into an unusable one.
     */
    @Test
    void surroundingWhitespaceIsTolerated() {
        assertEquals(2048, (int) configure("jakarta.faces.FACELETS_BUFFER_SIZE", "  2048  ").getValue(FacesContextParam.FACELETS_BUFFER_SIZE));
    }

    @Test
    void aListValuedParameterIsSplitOnItsOwnSeparator() {
        WebConfiguration webConfiguration = configure(ResourceHandler.RESOURCE_EXCLUDES_PARAM_NAME, " .class   .jsp ");

        assertArrayEquals(new String[] { ".class", ".jsp" }, webConfiguration.getStringArray(FacesContextParam.RESOURCE_EXCLUDES));
    }

    /**
     * A parameter read through an accessor for another type says so, rather than handing the value over to a cast
     * which fails on whichever path first reads it. The accessor is what names the type at the call site, and no
     * compiler checks that against the declaration.
     */
    @Test
    void readingAParameterAsTheWrongTypeIsRejected() {
        WebConfiguration webConfiguration = configure(null, null);

        assertThrows(IllegalStateException.class, () -> webConfiguration.getString(FacesContextParam.FACELETS_BUFFER_SIZE));
        assertThrows(IllegalStateException.class, () -> webConfiguration.getInt(FacesContextParam.CSP_POLICY));
        assertThrows(IllegalStateException.class, () -> webConfiguration.isEnabled(FacesContextParam.CSP_POLICY));
        assertThrows(IllegalStateException.class, () -> webConfiguration.getStringArray(FacesContextParam.CSP_POLICY));
        assertThrows(IllegalStateException.class, () -> webConfiguration.getEnum(ProjectStage.class, FacesContextParam.CSP_POLICY));

        assertEquals(ResourceHandler.DEFAULT_CSP_POLICY, webConfiguration.getString(FacesContextParam.CSP_POLICY));
    }

    private static void assertResolvesToItsDefault(FacesContextParam param, String value) {
        WebConfiguration webConfiguration = configure(value == null ? null : param.getName(), value);
        Object expected = param.getDefaultValue(webConfiguration.getProjectStage());

        assertEquals(expected, webConfiguration.getValue(param), param.getName());
    }

    private static WebConfiguration configure(String name, String value) {
        MockServletContext servletContext = new MockServletContext();

        if (name != null) {
            servletContext.addInitParameter(name, value);
        }

        return WebConfiguration.getInstance(servletContext);
    }
}
