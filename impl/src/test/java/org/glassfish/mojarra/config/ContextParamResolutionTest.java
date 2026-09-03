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

import static java.util.logging.Level.WARNING;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.ResourceHandler;
import jakarta.servlet.ServletContext;

import org.glassfish.mojarra.mock.MockServletContext;
import org.junit.jupiter.api.Test;

/**
 * A specification context parameter is resolved once, when this configuration is read, and every reader sees that same value. This covers what the resolution
 * does beyond handing the declared value back.
 */
class ContextParamResolutionTest extends ConfigurationLoggingTestBase {

    private static final String CSP_POLICY = "script-src 'self'";
    private static final String INVALID_VALUE = "faces.config.webconfig.boolconfig.invalidvalue";

    @Test
    void aParameterWhichWasNotDeclaredResolvesToItsDefault() {
        assertResolvesToItsDefault(FacesContextParam.CSP_POLICY, null);
        assertFalse(FacesContextParam.CSP_POLICY.isSet(configure(null, null)));
    }

    /**
     * A parameter which used to be a Mojarra one answers to the unqualified name it had then, under the prefix it actually carried and under the one the 5.0
     * rename produces for it.
     */
    @Test
    void aPromotedParameterIsHonouredUnderEitherOldSpelling() {
        for (String legacyName : new String[] { "com.sun.faces.cspPolicy", "org.glassfish.mojarra.cspPolicy" }) {
            ServletContext servletContext = configure(legacyName, CSP_POLICY);

            assertEquals(CSP_POLICY, FacesContextParam.CSP_POLICY.getString(servletContext), legacyName);
            assertTrue(FacesContextParam.CSP_POLICY.isSet(servletContext), legacyName);
        }
    }

    @Test
    void theSpecificationNameWinsOverAnOldSpelling() {
        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter("com.sun.faces.cspPolicy", "script-src 'none'");
        servletContext.addInitParameter(ResourceHandler.CSP_POLICY_PARAM_NAME, CSP_POLICY);

        assertEquals(CSP_POLICY, FacesContextParam.CSP_POLICY.getString(servletContext));
    }

    /**
     * A value which cannot be converted to the declared type behaves as though the parameter was never declared, rather than as whatever a lenient parse
     * happens to make of it, which for a boolean would silently be <code>false</code> and would therefore turn a typo into the opposite of a default which is
     * <code>true</code>.
     */
    @Test
    void anUnusableValueFallsBackToTheDefault() {
        assertResolvesToItsDefault(FacesContextParam.FACELETS_BUFFER_SIZE, "plenty");
        assertResolvesToItsDefault(FacesContextParam.SEPARATOR_CHAR, "::");
        assertResolvesToItsDefault(FacesContextParam.ENABLE_CSP_NONCE, "treu");
        assertResolvesToItsDefault(FacesContextParam.STATE_SAVING_METHOD, "somewhere");
    }

    /**
     * And surrounding whitespace, which a container is not obliged to strip from a context parameter, does not turn a usable value into an unusable one.
     */
    @Test
    void surroundingWhitespaceIsTolerated() {
        assertEquals(2048, FacesContextParam.FACELETS_BUFFER_SIZE.getInt(configure("jakarta.faces.FACELETS_BUFFER_SIZE", "  2048  ")));
    }

    @Test
    void aListValuedParameterIsSplitOnItsOwnSeparator() {
        ServletContext servletContext = configure(ResourceHandler.RESOURCE_EXCLUDES_PARAM_NAME, " .class   .jsp ");

        assertArrayEquals(new String[] { ".class", ".jsp" }, FacesContextParam.RESOURCE_EXCLUDES.getStringArray(servletContext));
    }

    /**
     * The separator trims entries and drops empty ones, so a value listed twice would otherwise reach every reader of the parameter and have the work behind it
     * done a second time. Each one is dropped and named, and the entries which survive keep the order and the first position they were declared at.
     */
    @Test
    void aValueListedMoreThanOnceIsDroppedAndReported() {
        ServletContext servletContext = configure(ResourceHandler.RESOURCE_EXCLUDES_PARAM_NAME, ".class .jsp .class .jspx .jsp");

        assertArrayEquals(
            new String[] { ".class", ".jsp", ".jspx" },
            FacesContextParam.RESOURCE_EXCLUDES.getStringArray(servletContext)
        );
        assertEquals(List.of(".class, .jsp"), duplicatedValues());
    }

    @Test
    void aValueListedOnceIsKeptAndNotReported() {
        ServletContext servletContext = configure(ResourceHandler.RESOURCE_EXCLUDES_PARAM_NAME, ".class .jsp");

        assertArrayEquals(new String[] { ".class", ".jsp" }, FacesContextParam.RESOURCE_EXCLUDES.getStringArray(servletContext));
        assertEquals(List.of(), duplicatedValues());
    }

    /**
     * A parameter whose type is not a list cannot hold a repeat, so it is never a candidate.
     */
    @Test
    void aSingleValuedParameterIsNotReported() {
        ServletContext servletContext = configure(FacesContextParam.CSP_POLICY.getName(), "script-src 'self' 'self'");

        FacesContextParam.CSP_POLICY.getString(servletContext);

        assertEquals(List.of(), duplicatedValues());
    }

    /**
     * A parameter read through an accessor for another type says so, rather than handing the value over to a cast which fails on whichever path first reads it.
     * The accessor is what names the type at the call site, and no compiler checks that against the declaration.
     */
    @Test
    void readingAParameterAsTheWrongTypeIsRejected() {
        ServletContext servletContext = configure(null, null);

        assertThrows(ClassCastException.class, () -> FacesContextParam.FACELETS_BUFFER_SIZE.getString(servletContext));
        assertThrows(ClassCastException.class, () -> FacesContextParam.CSP_POLICY.getInt(servletContext));
        assertThrows(ClassCastException.class, () -> FacesContextParam.CSP_POLICY.isEnabled(servletContext));
        assertThrows(ClassCastException.class, () -> FacesContextParam.CSP_POLICY.getStringArray(servletContext));
        assertThrows(ClassCastException.class, () -> FacesContextParam.CSP_POLICY.<ProjectStage>getEnum(servletContext));

        assertEquals(ResourceHandler.DEFAULT_CSP_POLICY, FacesContextParam.CSP_POLICY.getString(servletContext));
    }

    /**
     * The level at which every other parameter is reported derives from this one, so it has to be resolved before that level is known, and can therefore not be
     * reported itself while it is being resolved. Declaring it at all is what takes that path, at either of the two values which say so.
     */
    @Test
    void theParameterDecidingTheLoggingLevelResolvesBeforeThatLevelIsKnown() {
        for (String value : new String[] { "true", "false" }) {
            ServletContext servletContext = configure(MojarraContextParam.DISPLAY_CONFIGURATION.getName(), value);

            assertEquals(Boolean.valueOf(value), MojarraContextParam.DISPLAY_CONFIGURATION.isEnabled(servletContext), value);
        }
    }

    /**
     * A duration in milliseconds outgrows an <code>int</code> after about 24 days, which is well inside what a resource cache lifetime is plausibly set to.
     */
    @Test
    void aLongValuedParameterAcceptsMoreThanAnIntHolds() {
        long oneYearInMillis = 31536000000L;
        ServletContext servletContext = configure(MojarraContextParam.DEFAULT_RESOURCE_MAX_AGE.getName(), String.valueOf(oneYearInMillis));

        assertEquals(oneYearInMillis, MojarraContextParam.DEFAULT_RESOURCE_MAX_AGE.getLong(servletContext));
    }

    /**
     * The provider parameters resolve to an empty name rather than to none, which is what lets their readers tell an undeclared one apart by asking whether it
     * is empty. Were the default absent instead, an undeclared parameter would name a provider of its own and the system property and service entries behind it
     * would never be reached.
     */
    @Test
    void anUndeclaredProviderParameterResolvesToAnEmptyName() {
        ServletContext servletContext = configure(null, null);

        assertEquals("", MojarraContextParam.INJECTION_PROVIDER.getString(servletContext));
        assertEquals("", MojarraContextParam.SERIALIZATION_PROVIDER.getString(servletContext));
    }

    /**
     * A reported value is what would be written in web.xml, not what the reader's locale makes of it. A log message is a format pattern, so a default which is
     * a number reaches it as one, and an application told to fall back to '8,192' would be told to configure something which does not parse.
     */
    @Test
    void aReportedDefaultIsWrittenAsItWouldBeDeclared() {
        ServletContext servletContext = configure(MojarraContextParam.CLIENT_STATE_WRITE_BUFFER_SIZE.getName(), "true");

        assertEquals(8192, MojarraContextParam.CLIENT_STATE_WRITE_BUFFER_SIZE.getInt(servletContext));
        assertEquals(List.of("8192"), fallbackDefaultsOf(MojarraContextParam.CLIENT_STATE_WRITE_BUFFER_SIZE));
    }

    private List<String> fallbackDefaultsOf(ContextParam param) {
        return records.stream()
            .filter(
                record -> record.getLevel() == WARNING && INVALID_VALUE.equals(record.getMessage())
                    && param.getName().equals(record.getParameters()[2])
            )
            .map(record -> String.valueOf(record.getParameters()[4]))
            .toList();
    }

    private static void assertResolvesToItsDefault(FacesContextParam param, String value) {
        ServletContext servletContext = configure(value == null ? null : param.getName(), value);
        Object expected = param.getDefaultValue(FacesContextParam.PROJECT_STAGE.getEnum(servletContext));

        assertEquals(expected, WebConfiguration.getInstance(servletContext).getValue(param), param.getName());
    }

    private List<String> duplicatedValues() {
        return loggedArguments("faces.config.webconfig.param.duplicate_values", 2);
    }

    private static ServletContext configure(String name, String value) {
        MockServletContext servletContext = new MockServletContext();

        if (name != null) {
            servletContext.addInitParameter(name, value);
        }

        return servletContext;
    }

}
