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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.mojarra.mock.MockServletContext;
import org.junit.jupiter.api.Test;

/**
 * Covers the deprecation support of {@link WebConfiguration}, both for a parameter which has a replacement and for one
 * which has none.
 */
class DeprecatedContextParamTest extends ConfigurationLoggingTestBase {

    private static final String NO_REPLACEMENT = "faces.config.webconfig.param.deprecated.no_replacement";
    private static final String REPLACED = "faces.config.webconfig.param.deprecated";
    private static final String CONFIG_INFO = "faces.config.webconfig.configinfo";

    /**
     * The warning is not gated on the project stage, because it announces a change in the runtime rather than a mistake
     * in the application.
     */
    /**
     * A replacement is named rather than pointed at, because an enum constant may not refer to one declared after it
     * and the constants are in alphabetical order, so this is what a compiler would otherwise have caught.
     */
    @Test
    void everyReplacementNamesADeclaredParameter() {
        List<String> dangling = new ArrayList<>();

        for (MojarraContextParam param : MojarraContextParam.values()) {
            if (param.getAlternateName() != null && MojarraContextParam.of(param.getAlternateName()) == null) {
                dangling.add(param.getName() + " names " + param.getAlternateName());
            }
        }

        assertTrue(dangling.isEmpty(), () -> "Replacements which are not declared parameters: " + dangling);
    }

    @Test
    void aDeprecatedParameterWarnsWhenSet() {
        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter(MojarraContextParam.ALLOW_TEXT_CHILDREN.getName(), "true");

        assertTrue(MojarraContextParam.ALLOW_TEXT_CHILDREN.isEnabled(servletContext), "the value is still honored");
        assertEquals(List.of(MojarraContextParam.ALLOW_TEXT_CHILDREN.getName()), warnedParameterNames());
    }

    /**
     * Warning about a deprecation which the application did not opt into would be noise.
     */
    @Test
    void aDeprecatedParameterIsSilentWhenUnset() {
        WebConfiguration.getInstance(new MockServletContext());

        assertEquals(List.of(), warnedParameterNames());
    }

    /**
     * And a parameter which is merely set is not a deprecation.
     */
    @Test
    void aSupportedParameterIsSilentWhenSet() {
        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter(MojarraContextParam.COMPRESS_VIEW_STATE.getName(), "false");

        WebConfiguration.getInstance(servletContext);

        assertEquals(List.of(), warnedParameterNames());
    }

    /**
     * A renamed parameter hands its value to the parameter which replaces it, so that an application which was not
     * migrated yet keeps the setting it configured rather than silently falling back to the default.
     */
    @Test
    void aRenamedParameterHandsItsValueToItsReplacement() {
        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter(MojarraContextParam.NUMBER_OF_LOGICAL_VIEWS.getName(), "3");

        assertEquals(3, MojarraContextParam.NUMBER_OF_STATEFUL_PAGES_PER_SESSION.getInt(servletContext));
        assertEquals(List.of(MojarraContextParam.NUMBER_OF_LOGICAL_VIEWS.getName()), replacedParameterNames());
    }

    /**
     * And it is reported under the name it moved to, and only under that one, because that is the one governing the
     * behaviour from here on. Reporting it under the name it was declared with would leave the log naming a parameter
     * which decides nothing, and the replacement appearing nowhere at all.
     */
    @Test
    void aCarriedOverValueIsReportedUnderItsNewName() {
        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter(MojarraContextParam.DISPLAY_CONFIGURATION.getName(), "true");
        servletContext.addInitParameter(MojarraContextParam.NUMBER_OF_LOGICAL_VIEWS.getName(), "3");

        WebConfiguration.getInstance(servletContext);

        assertEquals(List.of("3"), reportedValuesOf(MojarraContextParam.NUMBER_OF_STATEFUL_PAGES_PER_SESSION), "the name it moved to");
        assertEquals(List.of(), reportedValuesOf(MojarraContextParam.NUMBER_OF_LOGICAL_VIEWS), "the name it was declared with");
    }

    /**
     * And the new name wins when both are declared, rather than the order the constants happen to be declared in.
     */
    @Test
    void theReplacementWinsWhenBothAreSet() {
        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter(MojarraContextParam.NUMBER_OF_LOGICAL_VIEWS.getName(), "3");
        servletContext.addInitParameter(MojarraContextParam.NUMBER_OF_STATEFUL_PAGES_PER_SESSION.getName(), "7");

        assertEquals(7, MojarraContextParam.NUMBER_OF_STATEFUL_PAGES_PER_SESSION.getInt(servletContext));
    }

    private List<String> replacedParameterNames() {
        return warnedParameterNames(REPLACED);
    }

    private List<String> warnedParameterNames() {
        return warnedParameterNames(NO_REPLACEMENT);
    }

    private List<String> reportedValuesOf(ContextParam param) {
        return records.stream()
                .filter(record -> CONFIG_INFO.equals(record.getMessage()) && param.getName().equals(record.getParameters()[1]))
                .map(record -> String.valueOf(record.getParameters()[2]))
                .toList();
    }

    private List<String> warnedParameterNames(String messageKey) {
        return loggedArguments(messageKey, 1);
    }
}
